package com.jianye.smart.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.movit.platform.common.broadcast.DownloadOrgReceiver;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.common.task.CommonTask;
import com.movit.platform.framework.utils.ActivityUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.CusRelativeLayout;
import com.movit.platform.framework.view.CusRelativeLayout.OnSizeChangedListener;
import com.movit.platform.im.helper.XmppHelper;
import com.movit.platform.im.manager.XmppManager;
import com.jianye.smart.R;
import com.jianye.smart.application.EOPApplication;
import com.jianye.smart.module.gesture.GestureVerifyActivity;
import com.jianye.smart.module.qrcode.InputCodeActivity;
import com.jianye.smart.module.workbench.activity.WebViewActivity;
import com.jianye.smart.task.CheckAndUpdateAPKTask;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {

    private Button mLoginBtn;
    private EditText mAccountEt, mPasswordEt;
    private CheckBox remenberPwdBtn, autoLoginBtn;
    private LoginInfo loginConfig;
    private TextView regist, version;

    private CusRelativeLayout relativeLayout;
    private ScrollView scrollView;
    private SharedPreUtils spUtil = null;
    private Handler h = new Handler();

    private RelativeLayout loginBottom;
    private ImageView goInputCode, logo;
    private TextView company;
    private AQuery aQuery;

    private CommonTask commonTask;
    private TelephonyManager mTelephonyManager;
    private DownloadOrgReceiver receiver;
    private CheckAndUpdateAPKTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eop_activity_login);
        EOPApplication.getInstance().clean();
        aQuery = new AQuery(this);
        loginConfig = new CommonHelper(this).getLoginConfig();
        spUtil = new SharedPreUtils(this);
        CommConstants.isExit = false;
        iniView();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //注册广播
        IntentFilter filter = new IntentFilter(CommConstants.ACTION_ORGUNITION_DONE);
        receiver = new DownloadOrgReceiver(this, new DownloadOrgReceiver.DownloadOrgCallBack() {
            // 初始化结束后的操作
            @Override
            public void afterInitCommon() {

                //防止手动杀进程后未断开连接
                XmppManager.getInstance().disconnect();
                //login xmpp
                XmppHelper XmppHelper = new XmppHelper(LoginActivity.this);
                XmppHelper.loginXMPP();

                Intent i = new Intent();
                i.setClass(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(i);

                finish();
            }
        });
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // // 校验SD卡
        ActivityUtils.checkMemoryCard(this);
        // 检测网络和版本
        boolean hasNetWork = ActivityUtils.hasNetWorkConection(this);
        if (!hasNetWork) {
            ActivityUtils.openWirelessSet(this);
        }
        // 判断跳转手势密码
        String code = spUtil.getString("GestureCode");
        if (StringUtils.notEmpty(code)) {
            if (!CommConstants.isGestureOK) {
                startActivity(new Intent(LoginActivity.this,
                        GestureVerifyActivity.class).putExtra("type", "verify"));
            } else {
                mPasswordEt.setText(loginConfig.getPassword());
                remenberPwdBtn.setChecked(true);
                autoLoginBtn.setChecked(true);
                login();
            }
        } else {
            boolean isAutoLogin = spUtil.getBoolean(CommConstants.IS_AUTOLOGIN,
                    false);
            boolean isRemember = spUtil.getBoolean(CommConstants.IS_REMEMBER, false);
            if (isRemember) {
                mPasswordEt.setText(loginConfig.getPassword());
                remenberPwdBtn.setChecked(true);
            } else {
                remenberPwdBtn.setChecked(false);
            }
            if (isAutoLogin) {
                autoLoginBtn.setChecked(true);
                spUtil.setBoolean(SharedPreUtils.autoLoginThisTime,true);
                login();
            } else {
                autoLoginBtn.setChecked(false);
                task = CheckAndUpdateAPKTask.getInstance(this);
                task.checkVersion();
            }
        }
    }

    private void iniView() {
        mAccountEt = (EditText) findViewById(R.id.account_input);
        mPasswordEt = (EditText) findViewById(R.id.password);
        mLoginBtn = (Button) findViewById(R.id.login);
        mAccountEt.setText(loginConfig.getUsername());
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        relativeLayout = (CusRelativeLayout) findViewById(R.id.rl);
        version = (TextView) findViewById(R.id.version);
        remenberPwdBtn = (CheckBox) findViewById(R.id.remenber_pwd);
        autoLoginBtn = (CheckBox) findViewById(R.id.auto_login);
        regist = (TextView) findViewById(R.id.regist);
        goInputCode = (ImageView) findViewById(R.id.login_input_code);
        company = (TextView) findViewById(R.id.company);
        logo = (ImageView) findViewById(R.id.logo);
        loginBottom = (RelativeLayout) findViewById(R.id.login_bottom);

        try {

            boolean canScan = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getBoolean("CHANNEL_SCAN", false);
            boolean canRegist = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getBoolean("CHANNEL_REGIST", false);
            String versionname = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getString("CHANNEL_VERSION_NAME");

            if (canScan) {
                goInputCode.setVisibility(View.VISIBLE);
            } else {
                goInputCode.setVisibility(View.GONE);
            }
            if (canRegist) {
                regist.setVisibility(View.VISIBLE);
            } else {
                regist.setVisibility(View.GONE);
            }

            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String versionName = info.versionName;

            version.setText(versionname + " v" + versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (StringUtils.notEmpty(CommConstants.companyName)) {
            company.setText(CommConstants.companyName);
        }
        if (StringUtils.notEmpty(CommConstants.companyLogo)) {
            aQuery.id(logo).image(
                    aQuery.getCachedImage(CommConstants.URL_DOWN
                            + CommConstants.companyLogo));
        }
        remenberPwdBtn
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        String code = spUtil.getString("GestureCode");
                        if (StringUtils.empty(code)) {
                            if (isChecked) {
                                spUtil.setBoolean(CommConstants.IS_REMEMBER, true);
                            } else {
                                spUtil.setBoolean(CommConstants.IS_REMEMBER, false);
                            }
                        }
                    }
                });
        autoLoginBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                String code = spUtil.getString("GestureCode");
                if (StringUtils.empty(code)) {
                    if (isChecked) {
                        remenberPwdBtn.setChecked(true);
                        spUtil.setBoolean(CommConstants.IS_AUTOLOGIN, true);
                    } else {
                        spUtil.setBoolean(CommConstants.IS_AUTOLOGIN, false);
                    }
                }

            }
        });

        mLoginBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE},
                            CommConstants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                } else {
                    login();
                }

            }
        });
        mAccountEt.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                changeScrollView();
                return false;
            }
        });
        mPasswordEt.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                changeScrollView();
                return false;
            }
        });

        regist.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,
                        WebViewActivity.class);
                intent.putExtra("URL", CommConstants.REGIST_URL);
                intent.putExtra("TITLE", "注册");
                startActivity(intent);
            }
        });

        goInputCode.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,
                        InputCodeActivity.class));
            }
        });
        relativeLayout.setOnSizeChangedListener(new OnSizeChangedListener() {

            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                if (oldh > h) {
                    loginBottom.setVisibility(View.GONE);
                } else if (h > oldh) {
                    LoginActivity.this.h.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            loginBottom.setVisibility(View.VISIBLE);
                        }
                    }, 100);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CommConstants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                login();
            } else {
                // Permission Denied
                Toast.makeText(this, "APP未获得您的授权，无法登录。", Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * 使ScrollView指向底部
     */
    private void changeScrollView() {
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, scrollView.getHeight());
            }
        }, 300);
    }

    protected void login() {
        String name = mAccountEt.getText().toString().trim();
        String password = mPasswordEt.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)) {

            if ("admin".equalsIgnoreCase(name)) {
                ToastUtils.showToast(this, "管理员请在后台登录");
            } else {
                loginEOPServer(name, password);
            }
        } else {
            ToastUtils.showToast(this, "用户名和密码不能为空");
        }
    }

    private void loginEOPServer(String name, String password) {
        commonTask = new CommonTask(LoginActivity.this, name, password);
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", name);
        params.put("password", password);
        params.put("deviceType", "2");
        try {
            String deviceId = mTelephonyManager.getDeviceId();
            params.put("device", deviceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        commonTask.execute(params);
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (commonTask != null) {
            commonTask.cancel(true);
            commonTask = null;
        }
        if (task != null) {
            task.cancel();
        }
        DialogUtils.getInstants().dismiss();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        CommConstants.isExit = true;
        super.onBackPressed();
        if (task != null) {
            task.cancel();
        }
    }

}
