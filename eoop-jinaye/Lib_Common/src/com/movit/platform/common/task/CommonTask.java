package com.movit.platform.common.task;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.movit.platform.common.R;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.common.manager.CommManager;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.common.utils.Json2ObjUtils;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.movit.platform.framework.utils.Obj2Json;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.ZipUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 登录异步任务.
 *
 * @author shimiso
 */
public class CommonTask extends AsyncTask<Map<String, String>, Void, Integer> {
    private Context context;
    private LoginInfo loginConfig;
    private String failReason;
    private CommonHelper tools;

    private boolean isOrgFinish = false;
    private boolean isGroupFinish = false;
    private DialogUtils progressDialogUtil;

    private DialogUtils progressDownLoading;

    private

    SharedPreUtils spUtil;

    public final static int SUCCESSS = 1;
    public final static int FAIL = 2;

    public String userName, password;

    public CommonTask(Context context, String userName, String password) {

        CommonHelper commonHelper = new CommonHelper(context);
        LoginInfo loginConfig = commonHelper.getLoginConfig();
        loginConfig.setPassword(password);
        loginConfig.setUsername(userName);

        this.userName = userName;
        this.password = password;

        this.loginConfig = loginConfig;
        this.context = context;
        tools = new CommonHelper(context);
        spUtil = new SharedPreUtils(context);
        progressDialogUtil = DialogUtils.getInstants();
        progressDownLoading = DialogUtils.getInstants();
    }

    @Override
    protected void onPreExecute() {
        //默认未登录
        CommConstants.IS_LOGIN_EOP_SERVER = false;
        progressDialogUtil.showLoadingDialog(context, "登录中...", false);
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected Integer doInBackground(Map<String, String>... params) {

        JSONObject jsonResult = null;
        try {
            String json = Obj2Json.map2json(params[0]);
            String responseStr = HttpClientUtils.post(CommConstants.URL_STUDIO
                    + "loginVerify", json, Charset.forName("UTF-8"));
            jsonResult = new JSONObject(responseStr);
        } catch (JSONException e) {
            jsonResult = null;
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 处理返回的信息
        Integer integer = doResult(jsonResult);
        return integer;
    }

    protected Integer doResult(JSONObject result) {
        if (result != null) {
            try {
                boolean okValue = result.getBoolean("ok");
                if (!okValue) {
                    String value = result.getString("value");
                    failReason = value;
                    return CommConstants.LOGIN_FAILE_REASON;
                } else {
                    if (result.has("value")) {
                        String ticket = result.getString("value");
                        spUtil.setString("ticket", ticket);
                    }
                    UserInfo userInfo = Json2ObjUtils.getUserInfoFromJson(result
                            .getString("objValue"));

                    loginConfig.setmUserInfo(userInfo);
                    CommConstants.loginConfig = loginConfig;
                    tools.saveLoginConfig(loginConfig);// 保存用户配置信息
                    BaseApplication.Token = userInfo.getOpenFireToken();
                    return CommConstants.LOGIN_SECCESS;
                }
            } catch (Exception e) {
                CommConstants.IS_LOGIN_EOP_SERVER = false;
                e.printStackTrace();
                return CommConstants.SERVER_UNAVAILABLE;
            }
        } else {
            return CommConstants.SERVER_UNAVAILABLE;
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        switch (result) {
            case CommConstants.LOGIN_FAILE_REASON:
                progressDialogUtil.dismiss();
                Toast.makeText(context, failReason, Toast.LENGTH_SHORT).show();
                break;
            case CommConstants.LOGIN_SECCESS:
                // 账号登录成功
                // 直接去获取组织架构，群组信息登陆后获取
                handler.sendEmptyMessage(1);
                break;
            case CommConstants.LOGIN_ERROR_ACCOUNT_PASS:// 账户或者密码错�
                progressDialogUtil.dismiss();
                Toast.makeText(
                        context,
                        context.getResources().getString(
                                R.string.message_invalid_username_password),
                        Toast.LENGTH_SHORT).show();
                break;
            case CommConstants.SERVER_UNAVAILABLE:// 服务器连接失败
                progressDialogUtil.dismiss();
                Toast.makeText(
                        context,
                        context.getResources().getString(
                                R.string.message_server_unavailable),
                        Toast.LENGTH_SHORT).show();
                break;
            case CommConstants.LOGIN_ERROR:// 未知异常
                progressDialogUtil.dismiss();
                Toast.makeText(
                        context,
                        context.getResources().getString(
                                R.string.unrecoverable_error), Toast.LENGTH_SHORT)
                        .show();
                break;
        }
        super.onPostExecute(result);
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //获取关注与被关注的人的信息
                    CommManager.getAttentionData(context);
//                    getGroupData();
                    boolean orgFull;
                    try {
                        orgFull = context.getPackageManager().getApplicationInfo(
                                context.getPackageName(),
                                PackageManager.GET_META_DATA).metaData.getBoolean(
                                "CHANNEL_ORG_FULL", false);
                    } catch (Exception e) {
                        orgFull = false;
                    }
                    if (orgFull) {
                        downEoopDBForAll();
                    } else {
                        downEoopDB();
                    }
                    break;
                case 2:
                    onPostExecute(CommConstants.SERVER_UNAVAILABLE);
                    break;
                case 3:
                    progressDownLoading.dismiss();
                    progressDialogUtil.dismiss();
                    onPostExecute(CommConstants.LOGIN_FAILE_REASON);
                    break;
                case 4:
                    updateEoopDB(userName, password);
                    break;
                case DialogUtils.progressHandlerIndex:
                    int fileSize = msg.arg1;
                    int downSize = msg.arg2;
                    progressDownLoading.setDownLoadProcess(fileSize, downSize);
                    break;
                case 6:
                    downEoopDB();
                    break;
                default:
                    break;
            }
        }

    };

    private void downEoopDB() {

        SharedPreUtils spUtil = new SharedPreUtils(context);
        long lastSyncTime = spUtil.getLong("lastSyncTime");

        if (lastSyncTime == -1) {
            progressDownLoading.dismiss();
            progressDialogUtil.dismiss();
            progressDownLoading.showDownLoadingDialog(context, "正在下载资源文件...",
                    false);
            progressDownLoading.getLoadingDialog().setCancelable(true);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    File zipFile = null;
                    try {
                        SharedPreUtils sp = new SharedPreUtils(
                                context);
                        String result = HttpClientUtils.post(CommConstants.URL_STUDIO
                            + "org/getFullOrgList", "{}", Charset.forName("UTF-8"));
                        JSONObject jsonResult = new JSONObject(result);
                        long orgTime = jsonResult.getLong("lastSyncTime");
                        String downUrl = jsonResult
                                .getString("fullOrgFilePath");
                        FileUtils fileUtils = new FileUtils();
                        fileUtils.setDownLoadProcessListener(progressDownLoading);
                        int k = fileUtils.downfile( handler, downUrl,
                                CommConstants.SD_DOWNLOAD, "eoop.db.zip");

                        if (k == 0 || k == 1) {// 或者已存在
                            // 解压缩
                            zipFile = new File(CommConstants.SD_DOWNLOAD,
                                    "eoop.db.zip");
                            ZipUtils.upZipFile(zipFile, CommConstants.SD_DOWNLOAD);
//                            UserDao dao = UserDao.getInstance(context);
//                            dao.closeDb();
                            sp.setLong("lastSyncTime", orgTime);
                            handler.sendEmptyMessage(4);
                        } else if (k == -1) {// 失败
                            failReason = context.getResources().getString(R.string.fail_to_load_resource);
                            handler.sendEmptyMessage(3);
                        }
                    } catch (Exception e) {
                        CommConstants.IS_LOGIN_EOP_SERVER = false;
                        failReason = context.getResources().getString(R.string.fail_to_load_resource);
                        handler.sendEmptyMessage(3);
                    } finally {
                        // 删除
                        if (zipFile != null) {
                            zipFile.delete();
                        }
                        new File(CommConstants.SD_DOWNLOAD,
                                UserDao.DATABASE_FILENAME).delete();
                    }
                }
            }).start();
        } else {
            handler.sendEmptyMessage(4);
        }
    }

    private void updateEoopDB(final String userName, final String password) {
        progressDialogUtil.dismiss();
        progressDownLoading.dismiss();
        progressDialogUtil.showLoadingDialog(context, "请稍候...", false);
        progressDialogUtil.getLoadingDialog().setCancelable(true);

        new Thread(new Runnable() {

            @Override
            public void run() {
                UserDao dao = UserDao.getInstance(context);
                try {
                    CommConstants.allUserInfos = dao.getAllUserInfos();
                    CommConstants.allOrgunits = dao.getAllOrgunitions();

                    SharedPreUtils spUtil = new SharedPreUtils(
                            context);
                    long lastSyncTime = spUtil.getLong("lastSyncTime");
                    // 增量更新
                    String updateResult = HttpClientUtils.post(CommConstants.URL_STUDIO
                        + "org/getDeltaOrgList", "{\"lastUpdateTime\":\""
                        + DateUtils.date2Str(new Date(lastSyncTime), "yyyy-MM-dd_HH:mm:ss") + "\"}", Charset.forName("UTF-8"));

                    JSONObject jsonResult = new JSONObject(updateResult);
                    if (jsonResult.has("code")) {
                        String code = jsonResult.getString("code");
                        if ("org.outOfDeltaRange".equals(code)) {
                            spUtil.setLong("lastSyncTime", -1);
//                            String databaseFilename = dao.getDATABASE_PATH()
//                                    + "/" + dao.DATABASE_FILENAME;
//                            (new File(databaseFilename)).delete();
//                            dao.closeDb();
                            dao.deleteDb();
                            CommConstants.allUserInfos = null;
                            CommConstants.allOrgunits = null;
                            handler.sendEmptyMessage(6);
                            return;
                        }
                    }
                    long time = jsonResult.getLong("lastSyncTime");
                    List<OrganizationTree> orgList = new ArrayList<OrganizationTree>();
                    List<UserInfo> userList = new ArrayList<UserInfo>();

                    if (jsonResult.has("orgList")) {
                        JSONArray array = jsonResult.getJSONArray("orgList");
                        for (int i = 0; i < array.length(); i++) {
                            orgList.add(Json2ObjUtils.getOrgunFromJson(array
                                    .getJSONObject(i).toString()));
                        }
                    }
                    if (jsonResult.has("userList")) {
                        JSONArray userarray = jsonResult
                                .getJSONArray("userList");
                        for (int j = 0; j < userarray.length(); j++) {
                            userList.add(Json2ObjUtils
                                    .getUserInfoFromJson(userarray
                                            .getJSONObject(j).toString()));
                        }
                    }

                    for (int i = 0; i < orgList.size(); i++) {
                        dao.updateOrgByFlags(orgList.get(i));
                    }
                    for (int j = 0; j < userList.size(); j++) {
                        dao.updateUserByFlags(userList.get(j));
                    }
                    spUtil.setLong("lastSyncTime", time);

                    UserInfo userInfo = dao.getUserInfoById(loginConfig
                            .getmUserInfo().getId());
                    if (userInfo == null) {
                        dao.closeDb();
                        failReason = "查无此人，请联系管理员！";
                        handler.sendEmptyMessage(3);
                        return;
                    }
                    getGroupData();
                    isOrgFinish = true;
                    //TODO anna
                    if (isGroupFinish && isOrgFinish) {
//					if (isGroupFinish && isOrgFinish && isAttentionFinish) {
                        isOrgFinish = false;
                        //TODO anna
                        isGroupFinish = false;
//						isAttentionFinish = false;
//						getAttentionByIds();

                        Intent intent = new Intent(
                                CommConstants.ACTION_ORGUNITION_DONE);
                        intent.putExtra("getOrgunitList", SUCCESSS);
                        intent.putExtra("userName", userName);
                        intent.putExtra("password", password);
                        intent.setPackage(context.getPackageName());
                        context.sendBroadcast(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    CommConstants.IS_LOGIN_EOP_SERVER = false;
                    handler.sendEmptyMessage(2);
                } finally {
                    dao.closeDb();
                }
            }
        }).start();
    }

    /**
     * 走全量更新
     */
    private void downEoopDBForAll() {
        SharedPreUtils spUtil = new SharedPreUtils(context);
        long lastSyncTime = spUtil.getLong("lastSyncTime");

        if (lastSyncTime == -1) {
            progressDownLoading.dismiss();
            progressDialogUtil.dismiss();
            progressDownLoading.showDownLoadingDialog(context, "正在下载资源文件...",
                    false);
            progressDownLoading.getLoadingDialog().setCancelable(true);
            new Thread(new EoopDownThread()).start();
        } else {
            // 判断是否需要全量下载
            progressDownLoading.dismiss();
            new Thread(new IsEoopDownThread(lastSyncTime)).start();
        }
    }

    class IsEoopDownThread implements Runnable {

        private long lastSyncTime = -1;

        public IsEoopDownThread(long lastSyncTime) {
            this.lastSyncTime = lastSyncTime;
        }

        @Override
        public void run() {
            try {
                String result = HttpClientUtils.post(CommConstants.URL_STUDIO
                    + "org/getFullOrgList", "{}", Charset.forName("UTF-8"));
                JSONObject jsonResult = new JSONObject(result);
                long orgTime = jsonResult.getLong("lastSyncTime");
                if (!String.valueOf(orgTime).equalsIgnoreCase(String.valueOf(lastSyncTime))) {
                    spUtil.setLong("lastSyncTime", -1);
                    UserDao.getInstance(context).deleteDb();
//                    progressDownLoading.showDownLoadingDialog(context,
//                            "正在下载资源文件...", false);
//                    progressDownLoading.getLoadingDialog().setCancelable(true);
                    new Thread(new EoopDownThread()).start();
                } else {
                    UserDao dao = UserDao.getInstance(context);
                    CommConstants.allUserInfos = dao.getAllUserInfos();
                    CommConstants.allOrgunits = dao.getAllOrgunitions();
                    UserInfo userInfo = dao.getUserInfoById(loginConfig
                            .getmUserInfo().getId());
                    if (userInfo == null) {
                        dao.closeDb();
                        failReason = "查无此人，请联系管理员！";
                        handler.sendEmptyMessage(3);
                        return;
                    }
                    dao.closeDb();
                    isOrgFinish = true;
                    if (isOrgFinish) {
                        isOrgFinish = false;
                        Intent intent = new Intent(
                                CommConstants.ACTION_ORGUNITION_DONE);
                        intent.putExtra("getOrgunitList", SUCCESSS);
                        intent.putExtra("userName", userName);
                        intent.putExtra("password", password);
                        intent.setPackage(context.getPackageName());
                        context.sendBroadcast(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
//                Toast.makeText(context,"Json解析错误",Toast.LENGTH_SHORT).show();
            }
        }
    }

    class EoopDownThread implements Runnable {

        @Override
        public void run() {
            File zipFile = null;
            try {
                SharedPreUtils sp = new SharedPreUtils(context);
                String result = HttpClientUtils.post(CommConstants.URL_STUDIO
                    + "org/getFullOrgList", "{}", Charset.forName("UTF-8"));

                JSONObject jsonResult = new JSONObject(result);
                long orgTime = jsonResult.getLong("lastSyncTime");
                String downUrl = jsonResult.getString("fullOrgFilePath");

                FileUtils fileUtils = new FileUtils();
                fileUtils.setDownLoadProcessListener(progressDownLoading);
                int k = fileUtils.downfile(handler, downUrl,
                        CommConstants.SD_DOWNLOAD, "eoop.db.zip");
                if (k == 0 || k == 1) {// 或者已存在
                    // 解压缩
                    // 解压缩
                    zipFile = new File(CommConstants.SD_DOWNLOAD, "eoop.db.zip");
                    ZipUtils.upZipFile(zipFile, CommConstants.SD_DOWNLOAD);
                    UserDao dao = UserDao.getInstance(context);
                    sp.setLong("lastSyncTime", orgTime);

                    CommConstants.allUserInfos = dao.getAllUserInfos();
                    CommConstants.allOrgunits = dao.getAllOrgunitions();

                    UserInfo userInfo = dao.getUserInfoById(loginConfig
                            .getmUserInfo().getId());
                    if (userInfo == null) {
                        dao.closeDb();
                        failReason = "查无此人，请联系管理员！";
                        handler.sendEmptyMessage(3);
                        return;
                    }
                    dao.closeDb();
                    isOrgFinish = true;
                    if (isOrgFinish) {
                        isOrgFinish = false;
                        Intent intent = new Intent(
                                CommConstants.ACTION_ORGUNITION_DONE);
                        intent.putExtra("getOrgunitList", SUCCESSS);
                        intent.putExtra("userName", userName);
                        intent.putExtra("password", password);
                        intent.setPackage(context.getPackageName());
                        context.sendBroadcast(intent);
                        handler.sendEmptyMessage(66);
                    }

                } else if (k == -1) {// 失败
                    failReason = context.getResources().getString(R.string.fail_to_load_resource);
                    handler.sendEmptyMessage(3);
                }
            } catch (Exception e) {
                failReason = context.getResources().getString(R.string.fail_to_load_resource);
                CommConstants.IS_LOGIN_EOP_SERVER = false;
                handler.sendEmptyMessage(3);
            } finally {
                // 删除
                if (zipFile != null) {
                    zipFile.delete();
                }
                new File(CommConstants.SD_DOWNLOAD, UserDao.DATABASE_FILENAME)
                        .delete();
            }
        }

    }

    //TODO anna 登陆时去获取,保证service中join in group时已经获取到group
    private void getGroupData() {

//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
        ((BaseApplication) ((Activity) context).getApplication()).getManagerFactory().getGroupManager().getGroupList();
        isGroupFinish = true;
        if (isGroupFinish && isOrgFinish) {
            //TODO anna
//					if (isGroupFinish && isOrgFinish && isAttentionFinish) {
            isOrgFinish = false;
            isGroupFinish = false;
            //TODO anna
//						isAttentionFinish = false;
//						getAttentionByIds();
//						Intent intent = new Intent(
//								CommConstants.ACTION_ORGUNITION_DONE);
//						intent.putExtra("getOrgunitList", SUCCESSS);
//						intent.setPackage(context.getPackageName());
//						context.sendBroadcast(intent);
        }
//				} catch (Exception e) {
//					e.printStackTrace();
//					handler.sendEmptyMessage(2);
//					return;
//				}
    }
//		}).start();
//	}

    //TODO anna　无效代码
//	private void getAttentionByIds() {
//
//		ArrayList<String> temp = new ArrayList<String>();
//		ArrayList<String> idStrings = loginConfig.getmUserInfo()
//				.getAttentionPO();
//		temp.addAll(idStrings);
//		UserDao dao = new UserDao(context);
//		for (int i = 0; i < temp.size(); i++) {
//			UserInfo userInfo = dao.getUserInfoById(temp.postWithoutEncrypt(i));
//			if (userInfo == null) {
//				idStrings.remove(temp.postWithoutEncrypt(i));
//			}
//		}
//		temp.clear();
//		ArrayList<String> toIdStrings = loginConfig.getmUserInfo()
//				.getToBeAttentionPO();
//
//		temp.addAll(toIdStrings);
//		for (int i = 0; i < temp.size(); i++) {
//			UserInfo userInfo = dao.getUserInfoById(temp.postWithoutEncrypt(i));
//			if (userInfo == null) {
//				toIdStrings.remove(temp.postWithoutEncrypt(i));
//			}
//		}
//		dao.closeDb();
//	}
}
