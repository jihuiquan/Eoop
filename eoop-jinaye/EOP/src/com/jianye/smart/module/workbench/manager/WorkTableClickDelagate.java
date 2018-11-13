package com.jianye.smart.module.workbench.manager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.okhttp.OkHttpUtils;
import com.movit.platform.common.okhttp.callback.Callback;
import com.movit.platform.common.okhttp.utils.AesUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.utils.ActivityUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.movit.platform.framework.utils.MD5Utils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.innerea.activity.MapGPSActivity;
import com.jianye.smart.R;
import com.jianye.smart.application.EOPApplication;
import com.jianye.smart.module.futureland.MagazineListActivity;
import com.jianye.smart.module.qrcode.MyCodeActivity;
import com.jianye.smart.module.workbench.activity.SuggestionActivity;
import com.jianye.smart.module.workbench.activity.WatingActivity;
import com.jianye.smart.module.workbench.activity.WebViewActivity;
import com.jianye.smart.module.workbench.attendance.activity.AttendanceListActivity;
import com.jianye.smart.module.workbench.bdo.activity.BDOCloudActivity;
import com.jianye.smart.module.workbench.bdo.activity.BDODocumentActivity;
import com.jianye.smart.module.workbench.constants.Constants;
import com.jianye.smart.module.workbench.meeting.activity.MeetingActivity;
import com.jianye.smart.module.workbench.model.WorkTable;
import com.jianye.smart.utils.DESUtils;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/1/28.
 */
public class WorkTableClickDelagate {

    public static  String JIANYE_MYERP_SHENPI = "";
    private Context context;
    private SharedPreUtils spUtil;
    private DialogUtils progressDialogUtil;

    public WorkTableClickDelagate(Context context) {
        this.context = context;
        this.spUtil = new SharedPreUtils(context);
        progressDialogUtil = DialogUtils.getInstants();
    }

    private Handler tableHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:
                    String[] arr = (String[]) msg.obj;
                    String urlString;
                    if (arr[1].contains("?")) {
                        urlString = arr[1] + "&ticket=" + arr[0];
                    } else {
                        urlString = arr[1] + "?ticket=" + arr[0];
                    }
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra("URL", urlString);
                    context.startActivity(intent);
                    break;
            }
        }
    };

    public void onClickWorkTable(List<WorkTable> myWorkTables, int position) {
        WorkTable table = myWorkTables.get(position);
        if ("com.tencent.mobileqq".equals(table.getAndroid_access_url())){
            table.setAndroid_access_url("com.kdweibo.client");
        }
        onClickWorkTable(table);
    }

    public void onClickWorkTable(final WorkTable table) {
        if (Constants.STATUS_UNAVAILABLE.equals(table.getStatus())) {
            EOPApplication.showToast(context, "建设中...");
            return;
        }
        if (Constants.TYPE_INTERNAL_HTML5.equals(table.getType())) {
            try {
                if ("1".equals(table.getIsToken())){
                    JSONObject object = new JSONObject();
                    JSONObject req = new JSONObject();
                    object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                    req.put("secretMsg", AesUtils.getInstance().encrypt(object.toString()));
                    HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                        req.toString(), new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e) {
                            }

                            @Override
                            public void onResponse(String response) throws JSONException {
                                JSONObject object = new JSONObject(response);
                                if (object.optBoolean("ok")) {
                                    table.setToken(object.optString("value"));
                                    getWorkTableUrl(table);
                                }
                            }
                        });
                }else {
                    getWorkTableUrl(table);
                }
            }catch (Exception e){
            }
        } else if (Constants.TYPE_NATIVE_APP.equals(table.getType())) {
            if ("suggest".equals(table.getAndroid_access_url())) {
                String title = table.getName();
                context.startActivity(new Intent(context,
                        SuggestionActivity.class).putExtra(
                        "title", title));
            } else if ("meeting".equals(table
                    .getAndroid_access_url())) {
                Intent intent = new Intent(context,
                        MeetingActivity.class);
                context.startActivity(intent);
            } else if ("workStream".equals(table
                    .getAndroid_access_url())) {// 流程
                context.startActivity(new Intent(context,
                        WatingActivity.class));
            } else if ("ea".equals(table
                    .getAndroid_access_url())) {
                context.startActivity(new Intent(context,
                        AttendanceListActivity.class));
            } else if ("BDOCloud".equals(table
                    .getAndroid_access_url())) {
                context.startActivity(new Intent(context,
                        BDOCloudActivity.class));
            } else if ("mail".equals(table
                    .getAndroid_access_url())) {
                ActivityUtils.sendMail(context, "");
            } else if ("sign".equals(table
                    .getAndroid_access_url())) {
                context.startActivity(new Intent(context,
                        MyCodeActivity.class).putExtra(
                        "type", "sign"));
            } else if ("myDocument".equals(table
                    .getAndroid_access_url())) {
                context.startActivity(new Intent(context,
                        BDODocumentActivity.class));
            } else if ("futureland_magazine".equals(table
                    .getAndroid_access_url())) {
                context.startActivity(new Intent(context,
                        MagazineListActivity.class));
            } else if ("innerea".equals(table
                    .getAndroid_access_url())) {
                context.startActivity(new Intent(context,
                        MapGPSActivity.class));
            }
        } else if (Constants.TYPE_THIRDPARTY_APP.equals(table.getType())) {
            if ("2".equals(table.getIsToken())) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("tokenType", "2");
                    object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                    com.alibaba.fastjson.JSONObject secretMsg = new com.alibaba.fastjson.JSONObject();
                    secretMsg.put("secretMsg", AesUtils.getInstance().encrypt(object.toString()));
                    HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            secretMsg.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok")) {
                                        boolean flag = ActivityUtils.openThirdApplicationWithPackageName(
                                                context, table.getAndroid_access_url(), object.optString("value"));
                                        if (!flag) {
                                            if (StringUtils.notEmpty(table.getRemarks())) {
                                                Intent intent = new Intent(context,
                                                        WebViewActivity.class);
                                                intent.putExtra("URL", table.getRemarks());
                                                intent.putExtra("title", table.getName());
                                                if ("KK".equals(table.getName())
                                                        || "新橙社".equals(table.getName())
                                                        || "新城掌院".equals(table.getName())
                                                        || "新城经纪人".equals(table.getName())
                                                        || "竞优流程审批".equals(table.getName())
                                                        || "明源流程审批".equals(table.getName())) {
                                                    intent.putExtra("WebViewFuncion", true);
                                                }
                                                context.startActivity(intent);
                                            } else {
                                                Toast.makeText(context, "当前应用未安装！", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    }
                                }
                            });
                }catch (Exception e){
                }
            }else {
                boolean flag = ActivityUtils.openThirdApplicationWithPackageName(context, table.getAndroid_access_url());
                if (!flag) {
                    if (StringUtils.notEmpty(table.getRemarks())) {
                        Intent intent = new Intent(context,
                                WebViewActivity.class);
                        intent.putExtra("URL", table.getRemarks());
                        intent.putExtra("title", table.getName());
                        if ("KK".equals(table.getName())
                                || "新橙社".equals(table.getName())
                                || "新城掌院".equals(table.getName())
                                || "新城经纪人".equals(table.getName())
                                || "竞优流程审批".equals(table.getName())
                                || "明源流程审批".equals(table.getName())) {
                            intent.putExtra("WebViewFuncion", true);
                        }
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "当前应用未安装！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (Constants.TYPE_WEB_HTML5.equals(table.getType())) {
            if ("timeSheet".equals(table
                    .getAndroid_access_url())) {
                try {
                    final String urlString = CommConstants.TIMESHEET_URL;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("title", table.getName());
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("TITLE", "TimeSheet");
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("title", table.getName());
                        intent.putExtra("URL", urlString);
                        intent.putExtra("TITLE", "TimeSheet");
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("duty".equals(table.getAndroid_access_url())) {// 考勤
                try {
                    final String urlString = Constants.URL_OFFICE_ATTENDANCE;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("title", table.getName());
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("goChat", true);
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("title", table.getName());
                        intent.putExtra("URL", urlString);
                        intent.putExtra("goChat", true);
                        context.startActivity(intent);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("hr".equals(table
                    .getAndroid_access_url())) {
                try {
                    final String urlString = Constants.URL_OFFICE_HR;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getIsToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("title", table.getName());
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("goChat", true);
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("title", table.getName());
                        intent.putExtra("URL", urlString);
                        intent.putExtra("goChat", true);
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("task".equals(table.getAndroid_access_url())) {
                try {
                    final String urlString = Constants.URL_OFFICE_TASK;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("title", table.getName());
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("title", table.getName());
                        intent.putExtra("URL", urlString);
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("excel".equals(table
                    .getAndroid_access_url())) {// 报表
                EOPApplication
                        .showToast(context, "建设中...");
            } else if ("notice".equals(table.getAndroid_access_url())) {
                try {
                    final String urlString = Constants.URL_OFFICE_NEWS;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("title", table.getName());
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("TITLE", "新闻公告");
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("title", table.getName());
                        intent.putExtra("URL", urlString);
                        intent.putExtra("TITLE", "新闻公告");
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("shop".equals(table.getAndroid_access_url())) {
                try {
                    final String urlString = Constants.URL_OFFICE_SHOP;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("title", table.getName());
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("title", table.getName());
                        intent.putExtra("URL", urlString);
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (table.getName().startsWith("BPM")
                    || table.getName().startsWith("EIP")
                    || table.getName().equals("建业BPM")) {
                try {
                    // cas认证
                    boolean isCas;
                    isCas = context
                            .getPackageManager()
                            .getApplicationInfo(
                                    context
                                            .getPackageName(),
                                    PackageManager.GET_META_DATA).metaData
                            .getBoolean("CHANNEL_CAS");
                    String ticket = spUtil.getString("ticket");
                    if (isCas) {casTickets(ticket, table.getAndroid_access_url());
                    } else {
                        final String urlString = table.getAndroid_access_url()
                                + "?UserID=" + spUtil.getString(CommConstants.EMPADNAME);
                        if ("1".equals(table.getIsToken())){
                            JSONObject object = new JSONObject();
                            object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                            HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                                object.toString(), new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e) {
                                    }

                                    @Override
                                    public void onResponse(String response) throws JSONException {
                                        JSONObject object = new JSONObject(response);
                                        if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                            JSONObject bean = object.optJSONObject("objValue");
                                            Intent intent = new Intent(context, WebViewActivity.class);
                                            intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                            intent.putExtra("BPM", true);
                                            context.startActivity(intent);
                                        }
                                    }
                                });
                        }else {
                            Intent intent = new Intent(context, WebViewActivity.class);
                            intent.putExtra("URL", urlString);
                            intent.putExtra("BPM", true);
                            context.startActivity(intent);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if ("futureland_manage".equals(table.getAndroid_access_url())) {// 任务管理
                //测试环境
//                String urlString = "http://218.4.117.11:6060/eoop/wwwPhone/transitionPage_manage.html?username=" +
//                        spUtil.getString(CommConstants.EMPADNAME);

                //生产环境
//                String urlString = "http://task.900950.com:9082/wwwPhone/transitionPage_manage.html?username=" +
//                        spUtil.getString(CommConstants.EMPADNAME);
                try {
                    String urlStringTemp;

                    String remark = table.getRemarks();
                    if (!TextUtils.isEmpty(remark)) {
                        urlStringTemp = remark + "/wwwPhone/task/task.html?username=" +
                            spUtil.getString(CommConstants.EMPADNAME);
                    } else {
                        urlStringTemp = "http://task.900950.com:9082/wwwPhone/task/task.html?username=" +
                            spUtil.getString(CommConstants.EMPADNAME);
                    }
                    final String urlString = urlStringTemp;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("title", table.getName());
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("FutureLand", true);
                                        intent.putExtra("WebViewFuncion", true);
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("title", table.getName());
                        intent.putExtra("URL", urlString);
                        intent.putExtra("FutureLand", true);
                        intent.putExtra("WebViewFuncion", true);
                        context.startActivity(intent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else if ("futureland_approval".equals(table.getAndroid_access_url())) {// EKP流程审批
                try {
                    String urlStringTemp;
                    String remark = table.getRemarks();
                    if (!TextUtils.isEmpty(remark)) {
                        urlStringTemp = remark + "/eoop/wwwPhone/splc/main.html?username=" +
                                spUtil.getString(CommConstants.EMPADNAME);
                    } else {
                        urlStringTemp = "http://" + CommConstants.URL_API + CommConstants.HOST_PORT + "/eoop/wwwPhone/splc/main.html?username=" +
                                spUtil.getString(CommConstants.EMPADNAME);
                    }
                    final String urlString = urlStringTemp;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("title", table.getName());
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("FutureLand", true);
                                        intent.putExtra("WebViewFuncion", true);
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("title", table.getName());
                        intent.putExtra("URL", urlString);
                        intent.putExtra("FutureLand", true);
                        intent.putExtra("WebViewFuncion", true);
                        context.startActivity(intent);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            } else if ("futureland_diary".equals(table.getAndroid_access_url())) {// 工作日志
                //生产环境
//                String urlString = "http://222.185.192.11:8078/h5/myDiary.html?user_id=" +
//                        spUtil.getString(CommConstants.EMPADNAME);

                //UAT环境
//                String urlString = "http://211.147.69.208:8012/h5/myDiary.html?user_id=" +
//                        spUtil.getString(CommConstants.EMPADNAME);

                try {
                    String workLogUrl = context.getPackageManager().getApplicationInfo(
                            context.getPackageName(), PackageManager.GET_META_DATA).metaData
                            .getString("WORKLOG");
                    String urlStringTemp;
                    String remark = table.getRemarks();
                    if (!TextUtils.isEmpty(remark)) {
                        urlStringTemp = remark + "/h5/myDiary.html?user_id=" +
                                spUtil.getString(CommConstants.EMPADNAME);
                    } else {
                        urlStringTemp = workLogUrl + "/h5/myDiary.html?user_id=" +
                                spUtil.getString(CommConstants.EMPADNAME);
                    }
                    final String urlString = urlStringTemp;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("title", table.getName());
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("FutureLand", true);
                                        intent.putExtra("WebViewFuncion", true);
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("title", table.getName());
                        intent.putExtra("URL", urlString);
                        intent.putExtra("FutureLand", true);
                        intent.putExtra("WebViewFuncion", true);
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (WorkTableManage.BUSINESS_REPORT.equals(table.getAndroid_access_url())) {// 商业报表
                try {
                    String urlStringTemp;
                    String remark = table.getRemarks();
                    if (!TextUtils.isEmpty(remark)) {
                        urlStringTemp = remark + "/eoop/wwwPhone/businessReport/selectPage.html?username=" +
                                spUtil.getString(CommConstants.EMPADNAME);
                    } else {
                        urlStringTemp = "http://" + CommConstants.URL_API + CommConstants.HOST_PORT + "/eoop/wwwPhone/businessReport/selectPage.html?username=" +
                                spUtil.getString(CommConstants.EMPADNAME);
                    }
                    final String urlString = urlStringTemp;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("title", table.getName());
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("FutureLand", true);
                                        intent.putExtra("WebViewFuncion", true);
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("title", table.getName());
                        intent.putExtra("URL", urlString);
                        intent.putExtra("FutureLand", true);
                        intent.putExtra("WebViewFuncion", true);
                        context.startActivity(intent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else if ("futureland_topReport".equals(table.getAndroid_access_url())) {// Top报表
                try {
                    String urlStringTemp;
                    String remark = table.getRemarks();
                    if (!TextUtils.isEmpty(remark)) {
                        urlStringTemp = remark + "/eoop/www/frontPage.html?username=" +
                            spUtil.getString(CommConstants.EMPADNAME);
                    } else {
                        urlStringTemp = "http://" + CommConstants.URL_API + CommConstants.HOST_PORT + "/eoop/www/frontPage.html?username=" +
                            spUtil.getString(CommConstants.EMPADNAME);
                    }
                    final String urlString = urlStringTemp;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("title", table.getName());
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("FutureLand", true);
                                        intent.putExtra("WebViewFuncion", true);
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("title", table.getName());
                        intent.putExtra("URL", urlString);
                        intent.putExtra("FutureLand", true);
                        intent.putExtra("WebViewFuncion", true);
                        context.startActivity(intent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else if ("futureland_news".equals(table.getAndroid_access_url())) {// 信息中心
                try {
                    String urlStringTemp;
                    String remark = table.getRemarks();
                    if (!TextUtils.isEmpty(remark)) {
                        urlStringTemp = remark + "/eoop/wwwPhone/news/informationCenter.html?username=" +
                            spUtil.getString(CommConstants.EMPADNAME);
                    } else {
                        urlStringTemp = "http://" + CommConstants.URL_API + CommConstants.HOST_PORT + "/eoop/wwwPhone/news/informationCenter.html?username=" +
                            spUtil.getString(CommConstants.EMPADNAME);
                    }
                    final String urlString = urlStringTemp;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("title", table.getName());
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("FutureLand", true);
                                        intent.putExtra("WebViewFuncion", true);
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("title", table.getName());
                        intent.putExtra("URL", urlString);
                        intent.putExtra("FutureLand", true);
                        intent.putExtra("WebViewFuncion", true);
                        context.startActivity(intent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else if ("futureland_dailyReport".equals(table.getAndroid_access_url())) {// 地产报表
                try {
                    String urlStringTemp;
                    String remark = table.getRemarks();
                    if (!TextUtils.isEmpty(remark)) {
                        urlStringTemp = remark + "/wwwPhone/sms/msgList.html?username=" +
                            spUtil.getString(CommConstants.EMPADNAME);
                    } else {
                        urlStringTemp = "http://task.900950.com:9082/wwwPhone/sms/msgList.html?username=" +
                            spUtil.getString(CommConstants.EMPADNAME);
                    }
                    final String urlString = urlStringTemp;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("title", table.getName());
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("FutureLand", true);
                                        intent.putExtra("WebViewFuncion", true);
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("title", table.getName());
                        intent.putExtra("URL", urlString);
                        intent.putExtra("FutureLand", true);
                        intent.putExtra("WebViewFuncion", true);
                        context.startActivity(intent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else if ("schedule-task".equals(table.getAndroid_access_url())) {// 计划任务
                try {
                    String userId = spUtil.getString(CommConstants.USERID);
                    String userName = spUtil.getString(CommConstants.EMPADNAME);
                    String key = MD5Utils.encode(userName + "JHXT");
                    String urlStringTemp;
                    String remark = table.getRemarks();
                    if (!TextUtils.isEmpty(remark)) {
                        urlStringTemp = remark + "/mobile/main.html?UserId=" + userName + "&Key=" + key;
                    } else {
                        urlStringTemp = String.format(CommConstants.URL_SCHEDULE_TASK, userName, key);
                    }
                    final String urlString = urlStringTemp;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("FutureLand", true);
                                        intent.putExtra("WebViewFuncion", true);
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("URL", urlString);
                        intent.putExtra("FutureLand", true);
                        intent.putExtra("WebViewFuncion", true);
                        context.startActivity(intent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else if ("mingyuan".equals(table.getAndroid_access_url())) {// 新明源流程审批
                try {
                    String arg = DESUtils.encryptDES(spUtil.getString(CommConstants.EMPADNAME));
                    String urlStringTemp;
                    String remark = table.getRemarks();
                    if (!TextUtils.isEmpty(remark)) {
                        urlStringTemp = remark + "/workflow-micro/my56e800314fe86/lists/process-list/index?__from=landrayjc&kindType=5&Userticket="
                                + arg;
                    } else {
                        urlStringTemp = "http://www.fdccloud.com/workflow-micro/my56e800314fe86/lists/process-list/index?__from=landrayjc&kindType=5&Userticket="
                                + arg;
                    }
                    final String urlString = urlStringTemp;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("title", table.getName());
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("URL", urlString);
                        intent.putExtra("title", table.getName());
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            } else if ("procurement".equals(table.getAndroid_access_url())) {// 采购招投标
                try {
                    String arg = DESUtils.encryptDES(spUtil.getString(CommConstants.EMPADNAME));
                    String urlStringTemp;
                    String remark = table.getRemarks();
                    if (!TextUtils.isEmpty(remark)) {
                        urlStringTemp = remark + "/feature-list.html?userGUID=" +
                            spUtil.getString(CommConstants.EMPADNAME);
                    } else {
                        urlStringTemp = "http://61.132.109.12:9081/feature-list.html?userGUID=" +
                            spUtil.getString(CommConstants.EMPADNAME);
                    }
                    final String urlString = urlStringTemp;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("title", table.getName());
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("URL", urlString);
                        intent.putExtra("title", table.getName());
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            } else if ("futureland_fanruan".equals(table.getAndroid_access_url())) {// 帆软
                frLogin(table, spUtil.getString(CommConstants.EMPADNAME), spUtil.getString(CommConstants.PASSWORD), false, null);
            } else if ("partner".equals(table.getAndroid_access_url())) {//新城合伙人
                try {
                    String arg = DESUtils.encryptDES(spUtil.getString(CommConstants.EMPADNAME));
                    String urlStringTemp;
                    String remark = table.getRemarks();
                    if (!TextUtils.isEmpty(remark)) {
                        urlStringTemp = remark + "/H5/main.html?UserId=" +
                            spUtil.getString(CommConstants.EMPADNAME);
                    } else {
                        urlStringTemp = "http://hhr.xincheng.com:8061/H5/main.html?UserId" +
                            spUtil.getString(CommConstants.EMPADNAME);
                    }
                    final String urlString = urlStringTemp;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("FutureLand", true);
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("URL", urlString);
                        intent.putExtra("FutureLand", true);
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            } else if (WorkTableManage.TASK_MANAGE.equals(table.getAndroid_access_url())) {//任务督办
                try {
                    String arg = DESUtils.encryptDES(spUtil.getString(CommConstants.EMPADNAME));
                    String urlStringTemp;
                    String remark = table.getRemarks();
                    if (!TextUtils.isEmpty(remark)) {
                        urlStringTemp = remark + "/RwdbH5/index?username=" +
                            spUtil.getString(CommConstants.EMPADNAME);
                    } else {
                        urlStringTemp = "http://211.147.69.208:8012/RWDB/RwdbH5/index?username=" +
                            spUtil.getString(CommConstants.EMPADNAME);
                    }
                    final String urlString = urlStringTemp;
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("title", table.getName());
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("URL", urlString);
                        intent.putExtra("title", table.getName());
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            } else {
                try {
                    final String urlString = table.getAndroid_access_url();
                    if ("1".equals(table.getIsToken())){
                        JSONObject object = new JSONObject();
                        object.put("userName", CommConstants.loginConfig.getmUserInfo().getEmpAdname());
                        HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/rest/token/getToken",
                            object.toString(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
                                    JSONObject object = new JSONObject(response);
                                    if (object.optBoolean("ok") && object.optJSONObject("objValue") != null) {
                                        JSONObject bean = object.optJSONObject("objValue");
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("URL", urlString + "Token=" + bean.optString("Token"));
                                        intent.putExtra("title", table.getName());
                                        context.startActivity(intent);
                                    }
                                }
                            });
                    }else {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("URL", urlString);
                        intent.putExtra("title", table.getName());
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        } else if ("cas_html".equals(table.getType())) {
            try {
                // cas认证
                boolean isCas = false;
                isCas = context.getPackageManager()
                        .getApplicationInfo(
                                context.getPackageName(),
                                PackageManager.GET_META_DATA).metaData
                        .getBoolean("CHANNEL_CAS");
                String ticket = spUtil.getString("ticket");
                if (isCas) {
                    casTickets(ticket,
                            table.getAndroid_access_url());
                } else {
                    String urlString = table
                            .getAndroid_access_url()
                            + "?UserID="
                            + spUtil.getString(CommConstants.EMPADNAME);
                    Intent intent = new Intent(context,
                            WebViewActivity.class);
                    intent.putExtra("URL", urlString);
                    context.startActivity(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void casTickets(final String value, final String url) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("service", url);
                String result = HttpClientUtils.post(value, params);
                tableHandle.obtainMessage(2, new String[]{result, url})
                        .sendToTarget();
            }
        }).start();
    }

    private boolean isLoading = false;

    private void frLogin(final WorkTable table, final String fr_username, final String fr_password, final boolean isInternal, final String internalUrl) {
        if (isLoading) {
            return;
        }
        isLoading = true;
        progressDialogUtil.showLoadingDialog(context, "请稍候...", false);
        final String remark = table.getRemarks();
        String url;
        if (!TextUtils.isEmpty(remark)) {
            url ="http://61.136.122.245:8075/WebReport/ReportServer?op=fs_load&cmd=sso&fr_username=%1$s&fr_password=%2$s";
        } else {
            url = "http://10.0.2.17:8080/BI/ReportServer?op=fs_load&cmd=sso&fr_username=%1$s&fr_password=%2$s";
        }
        String strUrl = "";
        try {
            strUrl = String.format(url, URLEncoder.encode(fr_username, "UTF-8"), fr_password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        OkHttpUtils.get()
                .url(strUrl)
                .build()
                .execute(new Callback() {
                    @Override
                    public Object parseNetworkResponse(Response response) throws Exception {
                        List<String> cookies = response.headers("Set-Cookie");
                        String cookie = cookies.toString().replace("[", "").replace("]", "");
                        isLoading = false;
                        progressDialogUtil.dismiss();
                        Intent intent = new Intent(context,
                                WebViewActivity.class);
                        String url;
                        if (isInternal && !TextUtils.isEmpty(internalUrl)) {
                            url = internalUrl;
                        } else {
                            if (!TextUtils.isEmpty(remark)) {
                                url = remark;
                            } else {
                                url = "http://10.0.2.17:8080/BI/ReportServer?op=fs";
                            }
                        }
                        intent.putExtra("URL", url);
                        intent.putExtra("cookie", cookie);
                        intent.putExtra("title", table.getName());
                        context.startActivity(intent);
                        return null;
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        isLoading = false;
                        progressDialogUtil.dismiss();
                    }

                    @Override
                    public void onResponse(Object response) throws org.json.JSONException {

                    }
                });
    }

    private void getWorkTableUrl(final WorkTable workTable) {
        final String moduleId = workTable.getId();
        if (isLoading) {
            return;
        }
        isLoading = true;
        progressDialogUtil.showLoadingDialog(context, context.getString(R.string.waiting), false);

        String url = "";
        String token = MFSPHelper.getString(CommConstants.TOKEN);
        JSONObject object = new JSONObject();
        JSONObject req = new JSONObject();
        try {
             url = CommConstants.URL_WORK_EMAIL;
            if ("jianye_qyyx".equals(workTable.getAndroid_access_url())) {
                object.put("userName", spUtil.getString(CommConstants.EMPADNAME));
                object.put("password", spUtil.getString(CommConstants.PASSWORD));
            }else {
                url = CommConstants.URL_WORK_TABLE;
                object.put("token", token);
                object.put("moduleId", moduleId);
                object.put("loginName", spUtil.getString(CommConstants.EMPADNAME));
            }
            req.put("secretMsg", AesUtils.getInstance().encrypt(object.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpManager.postJson(url, req.toString(), new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
                isLoading = false;
                progressDialogUtil.dismiss();
            }

            @Override
            public void onResponse(String response) throws JSONException {

                isLoading = false;
                progressDialogUtil.dismiss();
                if (StringUtils.notEmpty(response)) {
                    Log.d("WorkTableClickDelagate", "onResponse: " + response);
                    JSONObject jsonObject = new JSONObject(response);
                    boolean ok = jsonObject.getBoolean("ok");
                    if (ok) {
                        JSONObject objValue = jsonObject.getJSONObject("objValue");
                        String targetUrl = objValue.getString("targetUrl");
                        if ("futureland_fanruan".equals(workTable.getAndroid_access_url())) {
                            frLogin(workTable, spUtil.getString(CommConstants.EMPADNAME), spUtil.getString(CommConstants.PASSWORD), true, targetUrl);
                        }else {
                            Intent intent = new Intent(context, WebViewActivity.class);
                            if ("jianye_dbsx".equals(workTable.getAndroid_access_url())){
                                intent.putExtra("jianye_dbsx", "jianye_dbsx");
                            }
                            if ("1".equals(workTable.getIsToken())) {
                                targetUrl = targetUrl + "&token=" + workTable.getToken();
                            }
                            if ("schedule-task".equals(workTable.getAndroid_access_url())) {
                                String userName = spUtil.getString(CommConstants.EMPADNAME);
                                String key = MD5Utils.encode(userName + "JHXT");
                                //targetUrl = targetUrl + "?UserId=" + userName + "&Key=" + key;
                                targetUrl = targetUrl + "&Key=" + key;
                                intent.putExtra("URL", targetUrl);
                            } else if ("jianye_myerp_jihua".equals(workTable.getAndroid_access_url())
                                    || "jianye_myerp_shenpi".equals(workTable.getAndroid_access_url())) {
                                String arg;
                                try {
                                    arg = DESUtils.encryptDES(spUtil.getString(CommConstants.EMPADNAME));
                                } catch (Exception e) {
                                    arg = "";
                                }
                                targetUrl = targetUrl + "?__from=mentor&kindType=5&Userticket=" + arg;
                                intent.putExtra("URL", targetUrl);
                            } else {
                                intent.putExtra("URL", targetUrl);
                            }
                            //不隐藏title
                            boolean hideTitle = false;
                            if (workTable.getAndroid_access_url().equals("futureland_manage") ||
                                    workTable.getAndroid_access_url().equals(WorkTableManage.BUSINESS_REPORT) ||
                                    workTable.getAndroid_access_url().equals(WorkTableManage.TASK_MANAGE) ||
                                    "mingyuan".equals(workTable.getAndroid_access_url()) ||
                                    "futureland_diary".equals(workTable.getAndroid_access_url())) {
                                hideTitle = false;
                            }
                            intent.putExtra("FutureLand", hideTitle);
                            intent.putExtra("WebViewFuncion", true);
                            if (objValue.has("headers")) {
                                JSONObject headers = objValue.getJSONObject("headers");
                                intent.putExtra("headers", headers.toString());
                            }

                            if (objValue.has("cookies")) {
                                JSONObject cookies = objValue.getJSONObject("cookies");
                                intent.putExtra("cookies", cookies.toString());
                            }
                            intent.putExtra("title", workTable.getName());
                            context.startActivity(intent);
                        }
                    }
                }

            }
        });
    }
}
