package com.jianye.smart.application;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.androidquery.callback.BitmapAjaxCallback;
import com.baidu.mapapi.SDKInitializer;
import com.facebook.stetho.Stetho;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.manager.GroupManager;
import com.movit.platform.im.manager.MessageManager;
import com.jianye.smart.activity.SplashActivity;
import com.jianye.smart.service.LocationService;

import com.xc.sdk.errorreporter.AppCR;
import java.util.Stack;

public class EOPApplication extends BaseApplication {

    private static Stack<Activity> activityStack;
    private static EOPApplication mApplication;

    public LocationService locationService;

    public synchronized static EOPApplication getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        Stetho.initializeWithDefaults(this);
        setUIController(new EOPUIController());
        setManagerFactory(new ManagerFactory(this));
        //百度地图
        SDKInitializer.initialize(this);
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationService = new LocationService(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
    }

    public void clean() {
        CommConstants.loginXmppTime = 0;
        IMConstants.sysMsgList.clear();
        IMConstants.failedMsgList.clear();
        CommConstants.isLogin = false;
//        CommConstants.isExit = false;
        CommConstants.isServiceRunning = false;
        GroupManager.getInstance(this).clean();
        MessageManager.getInstance(this).clean();
        if (CommConstants.allUserInfos != null) {
            CommConstants.allUserInfos.clear();
        }
        if (CommConstants.allOrgunits != null) {
            CommConstants.allOrgunits.clear();
        }
    }

    // 添加Activity到容器中
    public static void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    public static void restartApp(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    // 遍历所有Activity并finish
    public static void exit() {
        try {
            while (true) {
                if (activityStack != null) {
                    if (activityStack.isEmpty()) {
                        break;
                    }
                    Activity activity = currentActivity();
                    if (activity == null) {
                        break;
                    }
                    popActivity(activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void popActivity(Activity activity) {
        if (activityStack != null) {
            if (activity != null) {
                activity.finish();
                activityStack.remove(activity);
            }
        } else {
            if (activity != null) {
                activity.finish();
            }
        }
    }

    public static Activity currentActivity() {
        Activity activity = null;
        try {
            if (activityStack != null) {
                activity = activityStack.lastElement();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return activity;
    }

    /**
     * 封装Toast提示框信息，显示在中间
     *
     * @param context
     * @param words
     */
    public static void showToast(Context context, String words) {
        Toast toast = Toast.makeText(context, words, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * 获取IMEI号，IESI号，手机型号
     */
    public void getPhoneInfo() {
        TelephonyManager mTm = (TelephonyManager) this
                .getSystemService(TELEPHONY_SERVICE);
        String imei = mTm.getDeviceId();
        String imsi = mTm.getSubscriberId();
        String mtype = android.os.Build.MODEL; // 手机型号
        String mtyb = android.os.Build.BRAND;// 手机品牌
        Log.i("text", "手机IMEI号：" + imei + "手机IESI号：" + imsi + "手机型号：" + mtype
                + "手机品牌：" + mtyb);
        CommConstants.PHONEBRAND = mtyb + " " + mtype;
        CommConstants.PHONEVERSION = android.os.Build.VERSION.RELEASE;
        // 手机IMEI号：358022057677637手机IESI号：null手机型号：SM-N9005手机品牌：samsung
    }

    @Override
    public void onLowMemory() {
        // clear all memory cached images when system is in low memory
        // note that you can configure the max image cache count, see
        // CONFIGURATION
        super.onLowMemory();
        BitmapAjaxCallback.clearCache();
    }

}
