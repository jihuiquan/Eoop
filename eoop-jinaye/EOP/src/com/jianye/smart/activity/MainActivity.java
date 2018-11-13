package com.jianye.smart.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.manager.CommManager;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.contacts.fragment.ContactsFragmentV2;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFHelper;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.helper.ServiceHelper;
import com.movit.platform.im.manager.XmppManager;
import com.movit.platform.im.module.record.fragment.ChatRecordsFragment;
import com.movit.platform.innerea.activity.MapGPSActivity;
import com.movit.platform.innerea.entities.MapPoint;
import com.movit.platform.sc.module.zone.constant.ZoneConstants;
import com.movit.platform.sc.module.zone.fragment.ZoneFragment;
import com.movit.platform.sc.timer.TimerHelper;
import com.jianye.smart.R;
import com.jianye.smart.application.EOPApplication;
import com.jianye.smart.base.BaseActivity;
import com.jianye.smart.broadcast.XGPushReceiver;
import com.jianye.smart.module.home.fragment.HomeFragemnt;
import com.jianye.smart.module.home.fragment.HomeFragmentV2;
import com.jianye.smart.module.mine.fragment.MyFragemnt;
import com.jianye.smart.module.workbench.adapter.FragmentTabAdapter;
import com.jianye.smart.module.workbench.adapter.FragmentTabAdapter.OnRgsExtraCheckedChangedListener;
import com.jianye.smart.module.workbench.manager.WorkTableClickDelagate;
import com.jianye.smart.module.workbench.manager.WorkTableManage;
import com.jianye.smart.module.workbench.model.WorkTable;
import com.jianye.smart.task.CheckAndUpdateAPKTask;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.common.Constants;
import com.tencent.android.tpush.service.XGPushServiceV3;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseActivity implements TimerHelper.CallBack, ZoneFragment.IZoneFragment {

    public static TextView zonePoint;
    public static ImageView smallPoint;
    public static RadioGroup rgs;
    public List<Fragment> fragments = new ArrayList<Fragment>();

    FragmentTabAdapter tabAdapter;
    public static Activity activity;
    private TextView chatPoint;
    public ImageView mainGuid;

    ChatRecordsFragment chatRecordsFragment;
    Timer timer;

    TimerHelper timerHelper;
    private IntentFilter filter;
    private CheckAndUpdateAPKTask task;

    @Override
    public void setRedPoint(int point) {
//        if (fragments.get(2) == null) return;
//        if (((HomeFragmentV2)fragments.get(2)).numChat == null) return;
        if (0 == point) {
//            ((HomeFragmentV2)fragments.get(2)).numChat.setVisibility(View.GONE);
            chatPoint.setVisibility(View.GONE);
        } else {
//            ((HomeFragmentV2)fragments.get(2)).numChat.setText(point + "");
//            ((HomeFragmentV2)fragments.get(2)).numChat.setVisibility(View.VISIBLE);
            chatPoint.setText(point + "");
            chatPoint.setVisibility(View.VISIBLE);
        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String value = (String) msg.obj;
                    Intent intent = new Intent(CommConstants.SIMPLE_LOGIN_ACTION);
                    intent.putExtra("body", value);
                    intent.putExtra("type", "2");//  2正常使用
                    intent.setPackage(context.getPackageName());
                    sendBroadcast(intent);
                    break;
                case ZoneConstants.ZONE_NEW_SAY_COUNT_RESULT:
                    try {
                        String result = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(result);
                        int code = jsonObject.getInt("code");
                        if (code == 0) {
                            if (jsonObject.has("val")) {
                                String val = jsonObject.getString("val");
                                if ("0".equals(val)) {
                                    smallPoint.setVisibility(View.GONE);
                                } else {
                                    smallPoint.setVisibility(View.VISIBLE);
                                }
                            } else {
                                smallPoint.setVisibility(View.GONE);
                            }
                        } else {
                            smallPoint.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (smallPoint != null) {
                            smallPoint.setVisibility(View.GONE);
                        }
                    }
                    break;
                case 99:
                    if (smallPoint != null) {
                        smallPoint.setVisibility(View.VISIBLE);
                    }
                    break;
                case 4:
                    setGPSAlarm();
                    break;
                default:
                    break;
            }
        }
    };

    private void setGPSAlarm() {
        String upTime = MFSPHelper.getString("upTime");
        String downTime = MFSPHelper.getString("downTime");
        String alarmUpTime = MFSPHelper.getString("alarmUpTime");
        String alarmDownTime = MFSPHelper.getString("alarmDownTime");
        if ("".equals(alarmUpTime) || "".equals(alarmDownTime)) {

            if ("".equals(alarmUpTime) && StringUtils.notEmpty(upTime)) {
                Calendar c = DateUtils.str2Calendar(upTime, "HH:mm");
                if (c != null) {
                    c.add(Calendar.MINUTE, -5);
                    String timeString = DateUtils.date2Str(c, "HH:mm");
                    alarmUpTime = timeString;
                }
            }
            if ("".equals(alarmDownTime) && StringUtils.notEmpty(downTime)) {
                Calendar c2 = DateUtils.str2Calendar(downTime, "HH:mm");
                String timeString2 = DateUtils.date2Str(c2, "HH:mm");
                alarmDownTime = timeString2;
            }
            MFSPHelper.setString("alarmUpTime", alarmUpTime);
            MFSPHelper.setString("alarmDownTime", alarmDownTime);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eop_activity_main);
        CommConstants.isExit = false;
        bindViews();

        chatRecordsFragment = new ChatRecordsFragment();
        fragments.clear();
        fragments.add(chatRecordsFragment);
//        fragments.add(new HomeFragemnt());
//        fragments.add(new KKWebViewFragemnt());
        fragments.add(new ContactsFragmentV2());
        fragments.add(new HomeFragmentV2());
        fragments.add(new ZoneFragment(this));
        fragments.add(new MyFragemnt());
        tabAdapter = new FragmentTabAdapter(MainActivity.this, fragments, R.id.main_frame, rgs);

        tabAdapter
                .setOnRgsExtraCheckedChangedListener(new OnRgsExtraCheckedChangedListener() {
                    @Override
                    public void OnRgsExtraCheckedChanged(RadioGroup radioGroup,
                                                         int checkedId, int index) {
                        int zoneIndex = 3;
                        if (index == zoneIndex) {
                            getWindow()
                                    .setSoftInputMode(
                                            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                            boolean isFirst = spUtil.getBoolean(
                                    CommConstants.IS_FIRSTSTART, true);
                            if (isFirst) {
                                mainGuid.setVisibility(View.VISIBLE);
                            }
                            ((ZoneFragment) fragments.get(zoneIndex)).notifyList();

                            if (smallPoint.getVisibility() == View.VISIBLE) {
                                ((ZoneFragment) fragments.get(zoneIndex))
                                        .refreshData(true);
                                smallPoint.setVisibility(View.GONE);
                            }
                        } else {
                            getWindow()
                                    .setSoftInputMode(
                                            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                        }
                    }
                });

        mainGuid.setVisibility(View.GONE);
        mainGuid.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mainGuid.setVisibility(View.GONE);
                spUtil.setBoolean(CommConstants.IS_FIRSTSTART, false);
            }
        });

        timer = new Timer();
        timerHelper = new TimerHelper(this, timer, this);

        UserDao dao = UserDao.getInstance(this);
        String officeId = dao.getUserInfoById(spUtil.getString(CommConstants.USERID)).getOrgId();
        dao.closeDb();

        timerHelper.startZoneRedPointTimer(officeId);

//        filter = new IntentFilter();
//        filter.addAction(CommConstants.NEW_MESSAGE_ACTION);
//        filter.addAction(CommConstants.SET_REDPOINT_ACTION);
//        registerReceiver(messageReceiver, filter);
        if (spUtil.getBoolean(SharedPreUtils.autoLoginThisTime, false)) {
            spUtil.setBoolean(SharedPreUtils.autoLoginThisTime, false);
            task = CheckAndUpdateAPKTask.getInstance(this);
            task.checkVersion();
        }
        //TODO anna 登陆时去获取,保证service中join in group时已经获取到group
//        if (IMConstants.mDatas.isEmpty() || IMConstants.groupsMap.isEmpty()) {m,
//            GroupManager.getInstance(context).getGroupList();
//        }
        checkLicense();
        getAllPoints();

        registerXGPush();
        //单点登录？？
//        OkHttpUtils.get().url("http://218.29.116.171:8080/WebReport/ReportServer?op=fs_load&cmd=sso&fr_username="
//            + CommConstants.loginConfig.getmUserInfo().getEmpCname() +"&fr_password=" + CommConstants.loginConfig.getPassword()).build().execute(null);
        super.registReceiver();
    }

    private void bindViews() {
        mainGuid = (ImageView) findViewById(R.id.zone_main_guid);
        chatPoint = (TextView) findViewById(R.id.main_chat_num);
        zonePoint = (TextView) findViewById(R.id.main_dian_zone);
        smallPoint = (ImageView) findViewById(R.id.main_dian_zone_small);
        rgs = (RadioGroup) findViewById(R.id.main_radiogroup);
//        findViewById(R.id.radio_kk_bt).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//              //跳转KK5.0及5.0以下
//              if (AppUtil.isAppInstalled(getContext(), "com.landray.kkplus")) {
//                startActivity(getPackageManager().getLaunchIntentForPackage("com.landray.kkplus"));
//              } else if (AppUtil.isAppInstalled(getContext(), "cn.com.landray.lma")) {
//                startActivity(getPackageManager().getLaunchIntentForPackage("cn.com.landray.lma"));
//              }else {
//                Uri uri = Uri.parse("http://app.qq.com/#id=detail&appid=100971194");
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(intent);
//              }
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (AppUtil.isAppInstalled(getContext(), "cn.com.landray.lma") || AppUtil.isAppInstalled(getContext(), "com.landray.kkplus")){
//            if (fragments.get(0) instanceof KKWebViewFragemnt && rgs.getCheckedRadioButtonId() == R.id.radio_msg){
//                rgs.check(R.id.radio_msg);
//            }
//            findViewById(R.id.radio_kk_bt).setVisibility(View.VISIBLE);
//            findViewById(R.id.radio_kk).setVisibility(View.GONE);
//        }else {
//            findViewById(R.id.radio_kk_bt).setVisibility(View.GONE);
//            findViewById(R.id.radio_kk).setVisibility(View.VISIBLE);
//        }
    }

    private void registerXGPush() {
        String deviceId = MFHelper.getDeviceId(context);
        //信鸽服务器注册
        XGPushManager.registerPush(getApplicationContext(), deviceId, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                //token在设备卸载重装的时候有可能会变
                Log.w(Constants.LogTag, "注册成功，设备token为：" + data);
            }
            @Override
            public void onFail(Object data, int errCode, String msg) {
                Log.w(Constants.LogTag, "注册失败，错误码：" + errCode + ",错误信息：" + msg);
            }
        });
        //EOP服务器注册
        CommManager.postDeviceType(deviceId, context);
    }

    //获取打卡相关信息
    public void getAllPoints() {
        HttpManager.postJson(CommConstants.URL_INTERNALEA, "", new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) throws JSONException {
                if (TextUtils.isEmpty(response)) {
                    return;
                }
                JsonObject jsonObject = new JsonParser().parse(response)
                        .getAsJsonObject();
                int code = jsonObject.get("ResponseCode").getAsInt();
                if (code == 1) {
                    com.google.gson.JsonArray array = jsonObject.get("list").getAsJsonArray();
                    for (int i = 0; i < array.size(); i++) {
                        MapPoint mapPoint = JSON.parseObject(array.get(i).toString(), MapPoint.class);
                        if (!TextUtils.isEmpty(mapPoint.getLatitude()) && !TextUtils.isEmpty(mapPoint.getLongitude())) {
                            if (MapGPSActivity.points == null) {
                                MapGPSActivity.points = new ArrayList<>();
                            }
                            MapGPSActivity.points.add(mapPoint);
                        }
                    }
                    String gpsWaring = jsonObject.get("ResponseMessage")
                            .getAsString();
                    String upTime = jsonObject.get("F1").getAsString();
                    String downTime = jsonObject.get("F2").getAsString();
                    String round = jsonObject.get("F3").getAsString();
                    MFSPHelper.setString("gpsWaring", gpsWaring);
                    MFSPHelper.setString("upTime", upTime);
                    MFSPHelper.setString("downTime", downTime);
                    // baseHandler.sendEmptyMessage(3);
                    if (MapGPSActivity.points != null) {
                        for (int i = 0; i < MapGPSActivity.points.size(); i++) {
                            MapGPSActivity.points.get(i).setRoundRange(
                                    round);
                            MapGPSActivity.points.get(i).setUpTime(upTime);
                            MapGPSActivity.points.get(i).setDownTime(
                                    downTime);
                        }
                    }
                    handler.sendEmptyMessage(4);
                }
            }
        });
    }


    // 检查license是否过期
    private void checkLicense() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String result = HttpClientUtils.post(CommConstants.URL_STUDIO
                            + "checkLicense", "{}", Charset.forName("UTF-8"));
                    JSONObject jsonObject = new JSONObject(result);
                    boolean ok = jsonObject.getBoolean("ok");
                    if (!ok) {
                        String value = jsonObject.getString("objValue");
                        handler.obtainMessage(1, value).sendToTarget();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (XGPushReceiver.MAIN_ACTIVITY.equals(action)) {

            } else if (XGPushReceiver.DIARY_REPORT_ACTIVITY.equals(action)) {
                for (WorkTable table : HomeFragemnt.myWorkTables) {
                    if (WorkTableManage.DIARY_REPORT.equals(table.getAndroid_access_url())) {
                        WorkTableClickDelagate clickDelagate = new WorkTableClickDelagate(context);
                        clickDelagate.onClickWorkTable(table);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String urlString = "http://" + CommConstants.URL_API + "/eoop-api/r/unread/updateEstateReportCount";
                                    JSONObject object = new JSONObject();
                                    object.put("userName",spUtil.getString(CommConstants.USERNAME));
                                    String responseStr = HttpClientUtils
                                            .post(urlString, object.toString(), Charset.forName("UTF-8"));
                                    System.out.println("responseStr2=" + responseStr);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        break;
                    }
                }
            }
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        XGPushManager.onActivityStoped(this);
    }

    @Override
    public void onDestroy() {
        unRegistReceiver();
        timerHelper.stopZoneRedPointTimer(timer);
        if (task != null) {
            task.cancel();
        }
        super.onDestroy();
    }

    boolean backFlag = false;

    @Override
    public void onBackPressed() {
        if (!backFlag) {
            Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
        } else {
            CommConstants.isExit = true;
            //TODO modify by anna
//            tools.stopService();
            new ServiceHelper(context).stopService();

            EOPApplication.getInstance().clean();
            XmppManager.getInstance().disconnect();
            Log.d("chatRecords", "onBackPressed: "+XmppManager.getInstance().isConnected());
            EOPApplication.exit();// activity finish并且断开xmpp连接
            // android.os.Process.killProcess(android.os.Process.myPid());
        }
        backFlag = true;
        backHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                backFlag = false;
            }
        }, 1500);
    }

    Handler backHandler = new Handler();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void refreshZoneRedPoint(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            int code = jsonObject.getInt("code");
            if (code == 0) {
                if (jsonObject.has("val")) {
                    String val = jsonObject.getString("val");
                    if ("0".equals(val)) {
                        zonePoint.setVisibility(View.GONE);
                    } else {
                        zonePoint.setText(val);
                        zonePoint.setVisibility(View.VISIBLE);
                    }
                    ((ZoneFragment) fragments.get(2))
                            .refreshDian(val);
                } else {
                    zonePoint.setVisibility(View.GONE);
                }
            } else {
                zonePoint.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (zonePoint != null) {
                zonePoint.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void showZoneRedPoint(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            int code = jsonObject.getInt("code");
            if (code == 0) {
                if (jsonObject.has("val")) {
                    String val = jsonObject.getString("val");
                    if ("0".equals(val)) {
                        smallPoint.setVisibility(View.GONE);
                    } else {
                        smallPoint.setVisibility(View.VISIBLE);
                    }
                } else {
                    smallPoint.setVisibility(View.GONE);
                }
            } else {
                smallPoint.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (smallPoint != null) {
                smallPoint.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setBottomTabStatus(boolean isShow) {
        if (isShow) {
            rgs.setVisibility(View.VISIBLE);
        } else {
            rgs.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        chatRecordsFragment.onActivityResult(requestCode,resultCode,data);
    }
}
