package com.jianye.smart.base;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.activity.IMBaseActivity;
import com.movit.platform.im.broadcast.HomeKeyEventReceiver;
import com.movit.platform.im.broadcast.ScreenReceiver;
import com.jianye.smart.R;
import com.jianye.smart.application.EOPApplication;
import com.jianye.smart.broadcast.OffLineReceiver;
import com.jianye.smart.module.gesture.GestureVerifyActivity;
import com.tencent.android.tpush.XGPushManager;

/**
 * Actity 工具支持类
 *
 * @author Potter.Tao
 */
public class BaseActivity extends IMBaseActivity {

    protected SharedPreUtils spUtil;
    protected Context context = null;
    protected CommonHelper tools;
    protected DialogUtils progressDialogUtil;

    private HomeKeyEventReceiver mHomeKeyEventReceiver;
    private ScreenReceiver screenReceiver;
    private OffLineReceiver offLineReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!CommConstants.IS_RUNNING) {
            EOPApplication.restartApp(getApplicationContext());
            return;
        }
        super.onCreate(savedInstanceState);
        context = this;
        tools = new CommonHelper(context);
        spUtil = new SharedPreUtils(context);
        progressDialogUtil = DialogUtils.getInstants();

        EOPApplication.addActivity(this);

        mHomeKeyEventReceiver = new HomeKeyEventReceiver();
        //注册广播
        registerReceiver(mHomeKeyEventReceiver, new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        screenReceiver = new ScreenReceiver();
        //注册广播
        registerReceiver(screenReceiver, new IntentFilter(
                Intent.ACTION_SCREEN_OFF));
        IntentFilter filter = new IntentFilter();
        filter.addAction(CommConstants.SIMPLE_LOGIN_ACTION);
        offLineReceiver = new OffLineReceiver();
        this.registerReceiver(offLineReceiver, filter);
    }

    @Override
    public void onBackPressed() {
        EOPApplication.popActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!"default".equals(spUtil.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            if (layout != null)
                layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }

        if (!CommConstants.IS_RUNNING) {
            EOPApplication.restartApp(getApplicationContext());
            return;
        }

        try {
            if (CommConstants.isLogin) {
                if (!CommConstants.isServiceRunning) {

                    // 判断跳转手势密码
                    SharedPreUtils spUtil = new SharedPreUtils(
                            context);
                    String code = spUtil.getString("GestureCode");
                    long time = spUtil.getLong("currentTime");
                    if (StringUtils.notEmpty(code)
                            && (System.currentTimeMillis() - time > 1000 * 5)
                            && !CommConstants.isGestureOK) {
                        startActivity(new Intent(context,
                                GestureVerifyActivity.class).putExtra("type",
                                GestureVerifyActivity.GestureTypeVerify));
                    }
                }
            }
            XGPushManager.onActivityStarted(this);// 某些手机进去之后会跳到登陆界面
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);// 必须要调用这句
    }

    @Override
    protected void onPause() {
        super.onPause();
        XGPushManager.onActivityStoped(this);
    }

    @Override
    public void onDestroy() {
        progressDialogUtil.dismiss();
        unregisterReceiver(mHomeKeyEventReceiver);
        unregisterReceiver(screenReceiver);
        unregisterReceiver(offLineReceiver);
        super.onDestroy();
    }

    public Context getContext() {
        return context;
    }

}
