package com.jianye.smart.module.home.fragment;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.alibaba.fastjson.JSONArray;
import com.androidquery.AQuery;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.activity.UserDetailActivity;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.contacts.activity.ContactsActivity;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.innerea.activity.AlarmSettingActivity;
import com.jianye.smart.R;
import com.jianye.smart.application.EOPApplication;
import com.jianye.smart.base.BaseFragment;
import com.jianye.smart.module.qrcode.MyCodeActivity;
import com.jianye.smart.module.qrcode.TwoDimensionalCodeActivity;
import com.jianye.smart.module.workbench.activity.WokTableDragListActivity;
import com.jianye.smart.module.workbench.adapter.DragAdapter;
import com.jianye.smart.module.workbench.constants.Constants;
import com.jianye.smart.module.workbench.manager.WorkTableClickDelagate;
import com.jianye.smart.module.workbench.manager.WorkTableManage;
import com.jianye.smart.module.workbench.model.WorkTable;
import com.jianye.smart.utils.Json2ObjUtils;
import com.jianye.smart.view.DragGridViewPage;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import org.json.JSONObject;

public class HomeFragemnt extends BaseFragment {
    private ImageView back, topRight;
    private TextView title;

    private ImageView avatar, contact;
    private TextView name, job;

    public static List<WorkTable> myWorkTables;
    public static List<WorkTable> otherWorkTables;
    public ArrayList<WorkTable> allWorkTables;

    private DragGridViewPage dragGridViewPage;
    private DragAdapter dragAdapter;
    private Context context;
    private SharedPreUtils spUtil;
    private PopupWindow popupWindow;
    private AQuery aQuery;
    private Timer timer;

    private WorkTableManage workTableManage;

    public final static int MODULE_ERROR = 13;
    public final static int PERSIMMIONRESULT = 2;

    boolean permissionTop = false;
    boolean permissionBusinessReport = false;
    boolean permissionDailyReport = true;


    private Map<String, Integer> unReadNums = new HashMap<String, Integer>();

    Handler handler = new Handler() {//主要处理未读消息

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    break;
                case Constants.GET_UNREAD_TASK_MANAGE:
                    unReadNums.put(WorkTableManage.TASK_MANAGE, (Integer) msg.obj);
                    Log.d("test", "dragAdapter=" + dragAdapter);
                    if (null != dragAdapter) {

                        dragAdapter.setUnreadNums(unReadNums);

                        dragAdapter.reSetData();
                        dragAdapter.notifyDataSetChanged();

                    }
                    break;
                case Constants.GET_UNREAD_DIARY:
                    unReadNums.put(WorkTableManage.FUTURELAND_DIARY, (Integer) msg.obj);
                    Log.d("test", "dragAdapter=" + dragAdapter);
                    if (null != dragAdapter) {

                        dragAdapter.setUnreadNums(unReadNums);

                        dragAdapter.reSetData();
                        dragAdapter.notifyDataSetChanged();

                    }
                    break;
                case Constants.GET_TASK_UNREAD_NUM_RESULT:

                    unReadNums.put(WorkTableManage.FUTURELAND_MANAGE, (Integer) msg.obj);

                    Log.d("test", "dragAdapter=" + dragAdapter);
                    if (null != dragAdapter) {

                        dragAdapter.setUnreadNums(unReadNums);

                        dragAdapter.reSetData();
                        dragAdapter.notifyDataSetChanged();

                    }
                    break;
                case Constants.GET_UNREAD_MING_YUAN_RESULT:// 明源流程

                    unReadNums.put(WorkTableManage.MOBILEWORKFLOW, (Integer) msg.obj);
                    unReadNums.put(WorkTableManage.MINGYUAN, (Integer) msg.obj);

                    Log.d("test", "dragAdapter=" + dragAdapter);
                    if (null != dragAdapter) {

                        dragAdapter.setUnreadNums(unReadNums);

                        dragAdapter.reSetData();
                        dragAdapter.notifyDataSetChanged();

                    }
                    break;
                case Constants.GET_UNREAD_EKP_RESULT://EKP流程审批

                    unReadNums.put(WorkTableManage.FUTURELAND_APPROVAL, (Integer) msg.obj);

                    Log.d("test", "dragAdapter=" + dragAdapter);
                    if (null != dragAdapter) {

                        dragAdapter.setUnreadNums(unReadNums);

                        dragAdapter.reSetData();
                        dragAdapter.notifyDataSetChanged();

                    }
                    break;
                case Constants.GET_UNREAD_JING_YOU_RESULT://竟优流程审批

                    unReadNums.put(WorkTableManage.LIVE, (Integer) msg.obj);

                    Log.d("test", "dragAdapter=" + dragAdapter);
                    if (null != dragAdapter) {

                        dragAdapter.setUnreadNums(unReadNums);

                        dragAdapter.reSetData();
                        dragAdapter.notifyDataSetChanged();

                    }
                    break;
                case Constants.GET_UNREAD_BID_OPENING_RESULT://移动开标待办数

                    unReadNums.put(WorkTableManage.PROCUREMENT, (Integer) msg.obj);

                    Log.d("test", "dragAdapter=" + dragAdapter);
                    if (null != dragAdapter) {

                        dragAdapter.setUnreadNums(unReadNums);

                        dragAdapter.reSetData();
                        dragAdapter.notifyDataSetChanged();

                    }
                    break;
                case Constants.GET_UNREAD_SCHEDULE_TASK://运营管理未读

                    unReadNums.put(WorkTableManage.SCHEDULE_TASK, (Integer) msg.obj);

                    Log.d("test", "dragAdapter=" + dragAdapter);
                    if (null != dragAdapter) {
                        dragAdapter.setUnreadNums(unReadNums);
                        dragAdapter.reSetData();
                        dragAdapter.notifyDataSetChanged();
                    }
                    break;
                case Constants.GET_UNREAD_DIARY_REPORT:
                    unReadNums.put(WorkTableManage.DIARY_REPORT, (Integer) msg.obj);
                    if (null != dragAdapter) {
                        dragAdapter.setUnreadNums(unReadNums);
                        dragAdapter.reSetData();
                        dragAdapter.notifyDataSetChanged();
                    }
                    break;
                case PERSIMMIONRESULT:
                    saveAndShowWorkTable((String) msg.obj);
                    break;
                case Constants.PERSONALMODULES_RESULT:
                    checkPermissions((String) msg.obj);
                    break;
                case MODULE_ERROR:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(getActivity(), "获取信息失败！");
                    setAdapter();
                    break;
                case 33:// model del
                    WorkTable table = (WorkTable) msg.obj;
                    // 保存本地
                    WorkTable more = new WorkTable();
                    more.setId("more");
                    myWorkTables.remove(more);
                    String myjson = JSONArray.toJSONString(myWorkTables);
                    spUtil.setString("myWorkTables", myjson);
                    myWorkTables.add(more);

                    otherWorkTables.add(table);
                    String otherjson = JSONArray.toJSONString(otherWorkTables);
                    spUtil.setString("otherWorkTables", otherjson);
                    break;
                case 34:
                    final WorkTable moreTable = new WorkTable();
                    moreTable.setId("more");
                    myWorkTables.remove(moreTable);
                    final String myjsons = JSONArray.toJSONString(myWorkTables);
                    spUtil.setString("myWorkTables", myjsons);
                    myWorkTables.add(moreTable);
                    break;
                case Constants.GET_UNREAD_TASK_ALL:
                    String message= (String) msg.obj;
                    final String[] strings = message.split("---");
                    unReadNums.put(strings[0], Integer.valueOf(strings[1]));

                    Log.d("test", "dragAdapter=" + dragAdapter);
                    if (null != dragAdapter) {
                        dragAdapter.setUnreadNums(unReadNums);
                        dragAdapter.reSetData();
                        dragAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }

    };

    private void saveAndShowWorkTable(String json) {

        progressDialogUtil.dismiss();
        try {
            allWorkTables = Json2ObjUtils.getAllmodules(json);
            for (int i = 0; i < allWorkTables.size(); i++) {
                if ("Top报表".endsWith(allWorkTables.get(i).getName()) && !permissionTop) {
                    allWorkTables.get(i).setStatus(Constants.STATUS_OFFLINE);
                }
                if ("商业报表".endsWith(allWorkTables.get(i).getName()) && !permissionBusinessReport) {
                    allWorkTables.get(i).setStatus(Constants.STATUS_OFFLINE);
                }
                if ("地产报表".endsWith(allWorkTables.get(i).getName()) && !permissionDailyReport) {
                    allWorkTables.get(i).setStatus(Constants.STATUS_OFFLINE);
                }
            }

//            if (myWorkTables == null || otherWorkTables == null) {
            if (myWorkTables == null) {
                myWorkTables = new ArrayList<>();
            } else {
                myWorkTables.clear();
            }
            if (otherWorkTables == null) {
                otherWorkTables = new ArrayList<>();
            } else {
                otherWorkTables.clear();
            }

            for (int i = 0; i < allWorkTables.size(); i++) {
                WorkTable workTable = allWorkTables.get(i);
                if ("innerea".equals(workTable.getAndroid_access_url())) {
                    if (Constants.STATUS_AVAILABLE.equals(workTable.getStatus())) {
                        if (MFSPHelper.getBoolean(AlarmSettingActivity.FIRSTALARM, false)) {
                            AlarmSettingActivity.setAlarm(getActivity(), MFSPHelper.getString("alarmUpTime"),
                                    AlarmSettingActivity.FIRSTALARM_ID);
                        } else {
                            AlarmSettingActivity.cancelAlarm(getActivity(), AlarmSettingActivity.FIRSTALARM_ID);
                        }
                        if (MFSPHelper.getBoolean(AlarmSettingActivity.LASTALARM, false)) {
                            AlarmSettingActivity.setAlarm(getActivity(), MFSPHelper.getString("alarmDownTime"),
                                    AlarmSettingActivity.LASTALARM_ID);
                        } else {
                            System.out.println(MFSPHelper.getString("alarmDownTime"));
                            AlarmSettingActivity.cancelAlarm(getActivity(), AlarmSettingActivity.LASTALARM_ID);
                        }
                    } else {
                        AlarmSettingActivity.cancelAlarm(getActivity(), AlarmSettingActivity.FIRSTALARM_ID);
                        AlarmSettingActivity.cancelAlarm(getActivity(), AlarmSettingActivity.LASTALARM_ID);
                    }
                }

                if (Constants.STATUS_OFFLINE.equals(allWorkTables.get(i)
                        .getStatus())) {
                    continue;
                }
                if (Constants.STATUS_AVAILABLE
                        .equals(allWorkTables.get(i).getStatus())) {
                    myWorkTables.add(allWorkTables.get(i));
                } else {
                    otherWorkTables.add(allWorkTables.get(i));
                }
            }
            // 保存本地
            String myjson = JSONArray.toJSONString(myWorkTables);
            spUtil.setString("myWorkTables", myjson);
            String otherjson = JSONArray
                    .toJSONString(otherWorkTables);
            spUtil.setString("otherWorkTables", otherjson);
            if (null != dragAdapter) {
                WorkTable table = new WorkTable();
                table.setId("more");
                myWorkTables.add(table);
                dragAdapter.setUnreadNums(unReadNums);

                dragAdapter.reSetData();
                dragAdapter.notifyDataSetChanged();

            } else {
                setAdapter();
            }

            workTableManage.getAllUnreadNumber(handler, myWorkTables);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        spUtil = new SharedPreUtils(context);
        aQuery = new AQuery(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        workTableManage = new WorkTableManage(getActivity());
        progressDialogUtil.showLoadingDialog(getActivity(), "正在加载...", false);
        workTableManage.getPersonalModules(handler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.eop_fragment_home, null, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void initDialog() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.pop_home_guid, null);
        final Dialog customDialog = new Dialog(context, com.movit.platform.common.R.style.ImageloadingDialogStyle);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(view);
        customDialog.setCancelable(false);
        customDialog.setCanceledOnTouchOutside(false);
        Window win = customDialog.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        win.setAttributes(lp);
        customDialog.show();

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                spUtil.setBoolean("isHomeGuid", false);
                customDialog.dismiss();
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void initViews() {
        back = (ImageView) findViewById(R.id.common_top_img_left);
        title = (TextView) findViewById(R.id.common_top_title);
        topRight = (ImageView) findViewById(R.id.common_top_img_right);
        avatar = (ImageView) findViewById(R.id.home_avatar);
        name = (TextView) findViewById(R.id.home_name);
        job = (TextView) findViewById(R.id.home_job);
        contact = (ImageView) findViewById(R.id.home_contact);

        LinearLayout homeTopLayout = (LinearLayout) findViewById(R.id.home_top_layout);
        if (!"default".equals(spUtil.getString(BaseApplication.SKINTYPE))) {
            String skinType = spUtil.getString(BaseApplication.SKINTYPE);
            File dir = context.getDir("theme", Context.MODE_PRIVATE);
            File skinDir = new File(dir, skinType);
            BitmapDrawable home_top_bg = new BitmapDrawable(getResources(),
                    skinDir + "/home_top_bg.png");
            Bitmap bitmap = BitmapFactory.decodeFile(skinDir
                    + "/icon_contact.png");
            homeTopLayout.setBackground(home_top_bg);
            contact.setImageBitmap(bitmap);
        }

        dragGridViewPage = (DragGridViewPage) findViewById(R.id.dragGridView);
        if (StringUtils.notEmpty(CommConstants.productName)) {
            title.setText(CommConstants.productName);
        } else {
            title.setText("新城控股");
        }

        avatar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // 跳转自己
                String userid = new SharedPreUtils(HomeFragemnt.this.getActivity()).getString(CommConstants.USERID);
                UserDao dao = UserDao.getInstance(HomeFragemnt.this.getActivity());
                UserInfo me = dao.getUserInfoById(userid);
                dao.closeDb();

                Intent intent = new Intent();
                intent.putExtra("userInfo", me);
                intent.setClass(HomeFragemnt.this.getActivity(), UserDetailActivity.class);
                HomeFragemnt.this.getActivity().startActivityForResult(intent, 0);
            }
        });
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.putExtra("TITLE", "发起邮件");
                intent.putExtra("ACTION", "EMAIL");

                ((BaseApplication) HomeFragemnt.this.getActivity().getApplication()).getUIController().onIMOrgClickListener(HomeFragemnt.this.getActivity(), intent, 0);
            }
        });
        topRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getPopupWindow();
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    popupWindow.showAsDropDown(v);
                }
            }
        });
        contact.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),
                        ContactsActivity.class));
            }
        });
        //不需要首页的通讯录引导
//        boolean isguid = spUtil.getBoolean("isHomeGuid", true);
//        if (isguid) {
//            initDialog();
//        }
    }

    @Override
    protected void initDatas() {
        UserInfo userInfo = CommConstants.loginConfig
                .getmUserInfo();

        if (null != userInfo && null != userInfo.getEmpAdname()) {

            //anna:之前的逻辑
            if (userInfo.getEmpAdname().contains(".")) {
                name.setText(userInfo.getEmpCname()
                        + "   "
                        + userInfo.getEmpAdname().substring(0,
                        userInfo.getEmpAdname().indexOf(".")));
            } else {
                //anna：新增的逻辑
                name.setText(userInfo.getEmpCname()
                        + "   "
                        + userInfo.getEmpAdname());
            }

            UserDao dao = UserDao.getInstance(getActivity());
            OrganizationTree org = dao
                    .getOrganizationByOrgId(userInfo.getOrgId());
            dao.closeDb();
            if (org != null) {
                job.setText(org.getObjname());
            }
        }

        String json = spUtil.getString("myWorkTables");
        String otherjson = spUtil.getString("otherWorkTables");
        try {
            myWorkTables = com.alibaba.fastjson.JSONArray.parseArray(json,
                    WorkTable.class);
            otherWorkTables = com.alibaba.fastjson.JSONArray.parseArray(
                    otherjson, WorkTable.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void resumeDatas() {
        UserInfo userInfo = CommConstants.loginConfig
                .getmUserInfo();
        if (userInfo != null) {
            String uname = spUtil.getString(CommConstants.AVATAR);

            String avatarName = userInfo.getAvatar();
            int picId = R.drawable.avatar_male;
            if ("男".equals(userInfo.getGender())) {
                picId = R.drawable.avatar_male;
            } else if ("女".equals(userInfo.getGender())) {
                picId = R.drawable.avatar_female;
            }
            String avatarUrl = "";
            if (StringUtils.notEmpty(avatarName)) {
                avatarUrl = avatarName;
            }
            if (StringUtils.notEmpty(uname)) {
                avatarUrl = uname;
            }

            // 这边的图片不做缓存处理 这边的是圆的
            if (StringUtils.notEmpty(avatarUrl)) {
                aQuery.id(avatar).image(CommConstants.URL_DOWN + avatarUrl, false,
                        true, 128, picId);
            } else {
                Bitmap bitmap = BitmapFactory.decodeResource(
                        context.getResources(), picId);
                avatar.setImageBitmap(bitmap);
            }
        }
    }

    public void setAdapter() {
        if (myWorkTables == null) {
            myWorkTables = new ArrayList<>();
        }
        WorkTable table = new WorkTable();
        table.setId("more");
        myWorkTables.add(table);
        dragAdapter = new DragAdapter(context, myWorkTables, dragGridViewPage,
                handler, "del");

        dragAdapter.setUnreadNums(unReadNums);
        dragGridViewPage.setAdapter(dragAdapter, true);
        dragGridViewPage.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == -1) {
                    return;
                }
                if (dragAdapter.isShowDel()) {
                    dragAdapter.clearDrag();
                } else {
                    if (position == dragGridViewPage.getAvalableChildCount() - 1) {// more
                        startActivityForResult(new Intent(getActivity(),
                                WokTableDragListActivity.class), 1);
                    } else {
                        if (position > dragGridViewPage.getCustomerPostion()) {
                            position = position - 1;
                        }
                        WorkTableClickDelagate clickDelagate = new WorkTableClickDelagate(context);
                        clickDelagate.onClickWorkTable(myWorkTables, position);
                    }

                }
            }
        });
        dragGridViewPage.setOnRefreshListener(new DragGridViewPage.OnRefreshListener() {
            @Override
            public void onRefresh() {
                progressDialogUtil.showLoadingDialog(getActivity(), "正在加载...", false);
                workTableManage.getPersonalModules(handler);
            }
        });
    }

    private void checkPermissions(final String worktables) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("begin");
                try {
                    //Top 报表
                    JSONObject object = new JSONObject();
                    object.put("user_id", spUtil.getString(CommConstants.EMPADNAME));
                    String json = HttpClientUtils
                            .post("http://gzt.jianye.com.cn:80/eoop/rest/getReportByUserId", "{}", Charset.forName("UTF-8"));
                    Log.v("checkPermissions", "--1--" + json);
                    org.json.JSONArray array = new org.json.JSONArray(json);
                    if (array.length() == 0) {
                        permissionTop = false;
                    } else {
                        permissionTop = true;
                    }
                    //商业报表
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("dateTime", new Date().getTime() + "");
                    String json2 = HttpClientUtils
                            .post("http://gzt.jianye.com.cn:80/eoop/rest/getReportRight/"
                                    + spUtil.getString(CommConstants.EMPADNAME), map);
                    Log.v("checkPermissions", "--2--" + json2);
                    JSONObject object2 = new JSONObject(json2);
                    if (object2.has("ok")) {
                        if (object2.getBoolean("ok")) {
                            permissionBusinessReport = true;
                        } else {
                            permissionBusinessReport = false;
                        }
                    }
                    //不再判断地产报表的权限，始终为true
//                    Map<String, String> map2 = new HashMap<String, String>();
//                    map2.put("userName", spUtil.getString(CommConstants.EMPADNAME));
//                    String json3 = HttpClientUtils
//                            .post("http://gzt.jianye.com.cn:80/task/xcsms/num", map2);
//                    Log.v("checkPermissions", "--3--" + json3);
//                    System.out.println("object3=" + json3);
//                    JSONObject object3 = new JSONObject(json3);
//                    double totalCount = object3.getDouble("totalCount");
//                    int tc = (int) totalCount;
//                    if (tc > 0) {
//                        System.out.println("PerTrue");
//                        permissionDailyReport = true;
//                    } else {
//                        System.out.println("PerFAIL");
//                        permissionDailyReport = false;
//                    }
                    handler.obtainMessage(PERSIMMIONRESULT, worktables).sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.obtainMessage(PERSIMMIONRESULT, worktables).sendToTarget();
                }
            }
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == 1) {
                dragAdapter.reSetData();
                dragAdapter.notifyDataSetChanged();
            }
        }
    }

    private void getPopupWindow() {
        if (null != popupWindow) {
            return;
        } else {
            initPopuptWindow();
        }
    }

    private void initPopuptWindow() {
        View contactView = LayoutInflater.from(getActivity()).inflate(
                R.layout.contact_pop_window, null);
        LinearLayout group_add = (LinearLayout) contactView
                .findViewById(R.id.pop_linearlayout_1);
        ImageView imageView1 = (ImageView) contactView
                .findViewById(R.id.pop_imageview_1);
        TextView textView1 = (TextView) contactView
                .findViewById(R.id.pop_textview_1);
        LinearLayout email_add = (LinearLayout) contactView
                .findViewById(R.id.pop_linearlayout_2);
        ImageView imageView2 = (ImageView) contactView
                .findViewById(R.id.pop_imageview_2);
        TextView textView2 = (TextView) contactView
                .findViewById(R.id.pop_textview_2);
        imageView1.setImageResource(R.drawable.scan_ico_scanning);
        textView1.setText("扫一扫");
        imageView2.setImageResource(R.drawable.scan_ico_download);
        textView2.setText("下载我");
        group_add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                Intent intent = new Intent();
                intent.setClass(HomeFragemnt.this.getActivity(), MyCodeActivity.class);
                startActivity(intent);
            }
        });
        email_add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                startActivity(new Intent(getActivity(),
                        TwoDimensionalCodeActivity.class));
            }
        });
        popupWindow = new PopupWindow(contactView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (myWorkTables != null) {
            myWorkTables.clear();
            myWorkTables = null;
        }

    }

}
