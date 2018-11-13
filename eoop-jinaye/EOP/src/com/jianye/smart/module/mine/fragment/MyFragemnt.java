package com.jianye.smart.module.mine.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.util.AQUtility;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.common.utils.UploadUtils;
import com.movit.platform.common.utils.UploadUtils.OnUploadProcessListener;
import com.movit.platform.framework.helper.MFHelper;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.movit.platform.framework.utils.LogUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.view.CusDialog;
import com.movit.platform.framework.view.widget.SelectPicPopup;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.helper.ServiceHelper;
import com.movit.platform.im.manager.XmppManager;
import com.movit.platform.sc.module.zone.activity.ZoneOwnActivity;
import com.movit.platform.sc.module.zone.constant.ZoneConstants;
import com.movit.platform.sc.module.zone.manager.ZoneManager;
import com.jianye.smart.R;
import com.jianye.smart.activity.LoginActivity;
import com.jianye.smart.application.EOPApplication;
import com.jianye.smart.base.BaseFragment;
import com.jianye.smart.module.gesture.GestureActivity;
import com.jianye.smart.module.mine.activity.AttentionListActivity;
import com.jianye.smart.module.mine.activity.ClipImageActivity;
import com.jianye.smart.module.mine.activity.RePasswordActivity;
import com.jianye.smart.utils.Obj2JsonUtils;
import com.tencent.android.tpush.XGPushManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyFragemnt extends BaseFragment implements OnUploadProcessListener {
    ImageView back;
    ImageView topRight;
    TextView title;

    ImageView avatar;
    ImageView gender;
    TextView name;
    TextView subname;
    TextView empid;
    TextView objname;
    TextView post;
    TextView jobTitle;
    TextView userCity;
    TextView phone;
    TextView officePhone;
    TextView mail;
    TextView rePassword;
    LinearLayout repasswordLayout;
    LinearLayout gestureLayout;
    LinearLayout clearCacheLayout;

    LinearLayout clearMDMLayout;

    String cityStr;
    // 自定义的弹出框类
    SelectPicPopup popWindow;
    // 用来标识请求照相功能的activity

    Uri imageUri;// The Uri to store the big
    String currentTime;
    String takePicturePath = "";

    SharedPreUtils spUtil;
    private Button userLogout;

    LinearLayout zoneCountLayout;
    LinearLayout attentionCountLayout;
    LinearLayout beAttentionCountLayout;
    TextView zoneCount;
    TextView attentionCount;
    TextView beAttentionCount;
    TextView txt_device_id;
    AQuery aQuery;

    ServiceHelper serviceHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spUtil = new SharedPreUtils(getActivity());
        aQuery = new AQuery(getActivity());
        serviceHelper = new ServiceHelper(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.eop_fragment_mine, null, false);
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    protected void initViews() {
        back = (ImageView) findViewById(R.id.common_top_left);
        title = (TextView) findViewById(R.id.tv_common_top_title);
        topRight = (ImageView) findViewById(R.id.common_top_right);
        userLogout = (Button) findViewById(R.id.user_logout);
        topRight.setVisibility(View.GONE);
        back.setVisibility(View.GONE);
        title.setTextColor(getResources().getColor(R.color.white));
        title.setText("个人资料");
        findViewById(R.id.common_top_layout).setBackgroundColor(getResources().getColor(R.color.color_3fb0ff));
        if (!"default".equals(spUtil.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }

        avatar = (ImageView) findViewById(R.id.user_avatar);
        name = (TextView) findViewById(R.id.user_name);
        subname = (TextView) findViewById(R.id.user_subname);
        gender = (ImageView) findViewById(R.id.user_gender);
        empid = (TextView) findViewById(R.id.user_empid);
        objname = (TextView) findViewById(R.id.user_objname);
        post = (TextView) findViewById(R.id.user_post);
        jobTitle = (TextView) findViewById(R.id.user_jobtitle);
        phone = (TextView) findViewById(R.id.user_phone);
        officePhone = (TextView) findViewById(R.id.user_office_phone);
        mail = (TextView) findViewById(R.id.user_mail);
        rePassword = (TextView) findViewById(R.id.user_repassword);
        repasswordLayout = (LinearLayout) findViewById(R.id.user_repassword_Layout);
        gestureLayout = (LinearLayout) findViewById(R.id.user_gesture_Layout);
        userCity = (TextView) findViewById(R.id.user_city);
        clearCacheLayout = (LinearLayout) findViewById(R.id.user_clear_cache_Layout);
        zoneCountLayout = (LinearLayout) findViewById(R.id.user_zone_count_ll);
        zoneCount = (TextView) findViewById(R.id.user_zone_count);
        attentionCountLayout = (LinearLayout) findViewById(R.id.user_attention_count_ll);
        attentionCount = (TextView) findViewById(R.id.user_attention_count);
        beAttentionCountLayout = (LinearLayout) findViewById(R.id.user_be_attention_count_ll);
        beAttentionCount = (TextView) findViewById(R.id.user_be_attention_count);
        txt_device_id = (TextView) findViewById(R.id.txt_device_all_id);
        txt_device_id.setText(MFHelper.getDeviceId(this.getActivity()));
        clearMDMLayout = (LinearLayout) findViewById(R.id.user_clear_mdm_layout);
        avatar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(getActivity().INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(avatar.getWindowToken(), 0);
                }
                // 实例化SelectPicPopupWindow
                popWindow = new SelectPicPopup(getActivity(),
                        itemsOnClick);
                // 显示窗口
                popWindow.showAtLocation(
                        getActivity().findViewById(R.id.user_avatar),
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置

            }
        });
        userLogout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                spUtil.setString("GestureCode", "");

                serviceHelper.stopService();

                XmppManager.getInstance().disconnect();
                IMConstants.contactListDatas.clear();
                LoginInfo loginConfig = CommConstants.loginConfig;
                new CommonHelper(getActivity()).saveLoginConfig(loginConfig);
                spUtil.setBoolean(CommConstants.IS_AUTOLOGIN, false);
                spUtil.setBoolean(CommConstants.IS_REMEMBER, false);
                spUtil.setString(CommConstants.PASSWORD, "");
                EOPApplication.exit();
                clearDeviceType();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                getActivity().startActivity(
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            }
        });

        boolean isVisibility = false;
        boolean isGesture = false;
        try {
            isVisibility = getActivity().getPackageManager()
                    .getApplicationInfo(getActivity().getPackageName(),
                            PackageManager.GET_META_DATA).metaData
                    .getBoolean("CHANNEL_PASSWORD");
            isGesture = getActivity().getPackageManager().getApplicationInfo(
                    getActivity().getPackageName(),
                    PackageManager.GET_META_DATA).metaData
                    .getBoolean("CHANNEL_GESTURE");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (isVisibility) {
            repasswordLayout.setVisibility(View.VISIBLE);
        } else {
            repasswordLayout.setVisibility(View.GONE);
        }
        rePassword.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),
                        RePasswordActivity.class));
            }
        });

        if (isGesture) {
            gestureLayout.setVisibility(View.VISIBLE);
        } else {
            gestureLayout.setVisibility(View.GONE);
        }
        gestureLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), GestureActivity.class));
            }
        });
        clearCacheLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                progressDialogUtil.showLoadingDialog(getActivity(), "正在清除...",
                        false);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        AQUtility.cleanCache(
                                AQUtility.getCacheDir(getActivity()), 3000000,
                                2000000);
                        AQUtility.cleanCache(
                                new File(CommConstants.SD_DATA, "audio"), 3000000,
                                2000000);
                        mHandler.sendEmptyMessage(11);
                    }
                }).start();
            }
        });

        clearMDMLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final CusDialog dialogUtil = CusDialog.getInstance();
                dialogUtil.showCustomDialog(getActivity());
                dialogUtil
                        .setWebDialog("解除绑定后，此设备将被默认加入黑名单，您将不能用此设备登录APP，确认解除绑定吗？");
                dialogUtil.setConfirmClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialogUtil.dismiss();
                        progressDialogUtil.showLoadingDialog(getActivity(),
                                "请稍候...", false);
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("token",
                                            spUtil.getString(CommConstants.TOKEN));
                                    params.put("deviceType", "2");

                                    TelephonyManager tm = (TelephonyManager) getActivity()
                                            .getSystemService(
                                                    Context.TELEPHONY_SERVICE);
                                    String deviceId = tm.getDeviceId();
                                    params.put("device", deviceId);

                                    String json = Obj2JsonUtils.map2json(params);
                                    String responseStr = HttpClientUtils
                                            .post(CommConstants.URL_MDM, json,
                                                    Charset.forName("UTF-8"));
                                    JSONObject object = new JSONObject(
                                            responseStr);
                                    boolean ok = object.getBoolean("ok");
                                    if (ok) {
                                        mHandler.sendEmptyMessage(98);
                                    } else {
                                        mHandler.sendEmptyMessage(99);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mHandler.sendEmptyMessage(99);
                                }
                            }
                        }).start();
                    }
                });
                dialogUtil.setCancleClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialogUtil.dismiss();
                    }
                });

            }
        });
    }

    private void clearDeviceType() {
        XGPushManager.unregisterPush(getActivity());
        new Thread() {
            public void run() {
                String deviceType = "2";
                JSONObject object = new JSONObject();
                try {
                    object.put("userId", spUtil.getString(CommConstants.USERID));
                    object.put("deviceType", deviceType);
                    object.put("device", "");
                    object.put("mobilemodel", CommConstants.PHONEBRAND);
                    object.put("mobileversion", CommConstants.PHONEVERSION);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HttpClientUtils.post(CommConstants.URL
                                + "updateDevice", object.toString(),
                        Charset.forName("UTF-8"));
            }

        }.start();
    }

    @Override
    protected void initDatas() {
    }

    @Override
    protected void resumeDatas() {
        String uname = spUtil.getString(CommConstants.AVATAR);
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        UserDao dao = UserDao.getInstance(getActivity());
        UserInfo userInfo2 = dao.getUserInfoById(userInfo.getId());
        dao.closeDb();

        if (userInfo != null && userInfo2 != null) {
            String avatarName = userInfo.getAvatar();
            int picId = R.drawable.avatar_male;
            if ("男".equals(userInfo.getGender())) {
                gender.setImageResource(R.drawable.user_man);
                picId = R.drawable.avatar_male;
            } else if ("女".equals(userInfo.getGender())) {
                gender.setImageResource(R.drawable.user_woman);
                picId = R.drawable.avatar_female;
            }
            String avatarUrl = "";
            if (StringUtils.notEmpty(avatarName)) {
                avatarUrl = avatarName;
            }
            if (StringUtils.notEmpty(uname)) {
                avatarUrl = uname;
            }

            if (StringUtils.notEmpty(avatarUrl)) {
                BitmapAjaxCallback callback = new BitmapAjaxCallback();
                callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                        .round(10).fallback(picId)
                        .url(CommConstants.URL_DOWN + avatarUrl).memCache(true)
                        .fileCache(true).targetWidth(128);
                aQuery.id(avatar).image(callback);

            } else {
                Bitmap bitmap = PicUtils.getRoundedCornerBitmap(getActivity(),
                        picId, 10);
                avatar.setImageBitmap(bitmap);
            }

            if (StringUtils.notEmpty(userInfo.getMphone())) {
                phone.setText(userInfo.getMphone());
            }
            if (StringUtils.notEmpty(userInfo.getPhone())) {
                officePhone.setText(userInfo.getPhone());
            }

//            phone.setOnClickListener(new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    startActivityForResult(
//                            new Intent(getActivity(), RenameGroupActivity.class)
//                                    .putExtra("type", "mphone").putExtra(
//                                    "text", phone.getText().toString()),
//                            10);
//
//                }
//            });
//            officePhone.setOnClickListener(new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    startActivityForResult(
//                            new Intent(getActivity(), RenameGroupActivity.class)
//                                    .putExtra("type", "phone").putExtra("text",
//                                    officePhone.getText().toString()),
//                            10);
//                }
//            });
            if (StringUtils.notEmpty(userInfo2.getMail())) {
                mail.setText(userInfo2.getMail());
            }
            String[] nameStrings = userInfo2.getEmpCname().split("\\.");
            if (nameStrings != null && nameStrings.length > 0) {
                name.setText(nameStrings[0]);
            }
            if (nameStrings != null && nameStrings.length > 1) {
                empid.setText(nameStrings[1]);
            }

            subname.setText(userInfo2.getEmpAdname());

            dao = UserDao.getInstance(getActivity());
            OrganizationTree org = dao.getOrganizationByOrgId(userInfo2
                    .getOrgId());
            dao.closeDb();
            if (StringUtils.notEmpty(userInfo2.getDeptName())) {
                objname.setText(userInfo2.getDeptName());
            }

            if (StringUtils.notEmpty(userInfo2.getJobName())) {
                post.setText(userInfo2.getJobName());
            }

            if (StringUtils.notEmpty(userInfo2.getCity())) {
                userCity.setText(userInfo2.getCity());
            }
            jobTitle.setText(userInfo2.getEmpId());

            zoneCountLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(),
                            ZoneOwnActivity.class).putExtra("userId",
                            spUtil.getString(CommConstants.USERID)));
                }
            });
            attentionCountLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(),
                            AttentionListActivity.class).putExtra("type",
                            "attention"));
                }
            });
            beAttentionCountLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(),
                            AttentionListActivity.class).putExtra("type",
                            "beAttention"));
                }
            });
        }

        try {
            ArrayList<String> temp = new ArrayList<String>();
            ArrayList<String> idStrings = userInfo.getAttentionPO();
            temp.addAll(idStrings);
            dao = UserDao.getInstance(getActivity());
            for (int i = 0; i < temp.size(); i++) {
                UserInfo user = dao.getUserInfoById(temp.get(i));
                if (user == null) {
                    idStrings.remove(temp.get(i));
                }
            }
            temp.clear();
            ArrayList<String> toIdStrings = userInfo.getToBeAttentionPO();

            temp.addAll(toIdStrings);
            for (int i = 0; i < temp.size(); i++) {
                UserInfo user = dao.getUserInfoById(temp.get(i));
                if (user == null) {
                    toIdStrings.remove(temp.get(i));
                }
            }
            dao.closeDb();
            attentionCount.setText(idStrings.size() + "");
            beAttentionCount.setText(toIdStrings.size() + "");
            ZoneManager zoneManager = new ZoneManager(getActivity());
            zoneManager.mysaycount(mHandler);

            if (!CommConstants.GET_ATTENTION_FINISH) {
                DialogUtils.getInstants().showLoadingDialog(getActivity(), "请稍候...", true);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (CommConstants.GET_ATTENTION_FINISH) {
                            DialogUtils.getInstants().dismiss();
                            resumeDatas();
                        } else {
                            mHandler.postDelayed(this
                                    , 1500);
                        }
                    }
                }, 1500);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private OnClickListener itemsOnClick = new OnClickListener() {

        public void onClick(View v) {
            popWindow.dismiss();
            switch (v.getId()) {
                case R.id.btn_take_photo:
                    // 跳转相机拍照
                    currentTime = DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
                    imageUri = Uri.parse(CommConstants.IMAGE_FILE_LOCATION);
                    if (imageUri == null) {
                        return;
                    }

                    String sdStatus = Environment.getExternalStorageState();
                    if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(getActivity(), "找不到sd卡", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (ContextCompat.checkSelfPermission(MyFragemnt.this.getActivity(), Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        //申请 CAMERA 权限
                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                CommConstants.CAMERA_REQUEST_CODE);
                    } else {
                        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(intent2, 2);
                    }

                    break;
                case R.id.btn_pick_photo:
                    currentTime = DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, 1);
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("test", "onRequestPermissionsResult");

        if (requestCode == CommConstants.CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent2, 2);
            } else {
                // Permission Denied
                Toast.makeText(MyFragemnt.this.getActivity(), "访问相机权限未获得您授权，无法使用拍照功能。", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (data != null) {
                if (resultCode == 1) {
                    phone.setText(data.getStringExtra("mphone"));
                } else if (resultCode == 2) {
                    officePhone.setText(data.getStringExtra("phone"));
                }

            }
        } else if (requestCode == 3) {
            // 取得裁剪后的图片
            if (data != null) {
                takePicturePath = data.getStringExtra("takePicturePath");
                if (StringUtils.notEmpty(takePicturePath)) {
                    progressDialogUtil.showLoadingDialog(getActivity(),
                            "正在上传...", false);

                    // 上传图片
                    toUploadFile(takePicturePath);
                }
            }
        } else {
            if (resultCode == getActivity().RESULT_OK) {
                switch (requestCode) {
                    // 如果是直接从相册获取
                    case 1:
                        // 从相册中直接获取文件的真是路径，然后上传
                        String picPath = PicUtils
                                .getPicturePath(data, getActivity());
                        Log.v("picPath", "===" + picPath);
                        startActivityForResult(new Intent(getActivity(),
                                ClipImageActivity.class).putExtra(
                                "takePicturePath", picPath), 3);
                        break;
                    // 如果是调用相机拍照时
                    case 2:
                        if (StringUtils.empty(currentTime)) {
                            return;
                        }
                        boolean copy = FileUtils.copyFile(CommConstants.SD_CARD
                                + "/temp.jpg", CommConstants.SD_CARD_IMPICTURES
                                + currentTime + ".jpg");
                        new File(CommConstants.SD_CARD + "/temp.jpg").delete();
                        if (copy) {
                            String path = CommConstants.SD_CARD_IMPICTURES + currentTime
                                    + ".jpg";
                            Log.v("body", path);

                            startActivityForResult(new Intent(getActivity(),
                                    ClipImageActivity.class).putExtra(
                                    "takePicturePath", path), 3);
                            PicUtils.scanImages(getActivity(), path);
                        }
                        break;
                    default:
                        break;
                }
            } else {
                if (StringUtils.notEmpty(currentTime)) {
                    String path = CommConstants.SD_CARD_IMPICTURES + currentTime
                            + ".jpg";
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                    String newPathString = PicUtils.getTempPicPath(path);
                    File f = new File(newPathString);
                    if (f.exists()) {
                        f.delete();
                    }
                }
            }
        }

    }

    private void toUploadFile(String filePath) {
        String fileKey = "file";
        UploadUtils uploadUtils = UploadUtils.getInstance();
        uploadUtils.setOnUploadProcessListener(this);
        uploadUtils.uploadFile(filePath, fileKey, CommConstants.URL_UPLOAD, null,
                null);
    }

    @Override
    public void onUploadDone(int responseCode, String message,
                             MessageBean messageDataObj) {
        android.os.Message msg = android.os.Message.obtain();
        msg.what = responseCode;
        msg.obj = message;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onUploadProcess(int fileSize, int uploadSize) {
        // TODO Auto-generated method stub
    }

    @Override
    public void initUpload(int fileSize) {
        // TODO Auto-generated method stub
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UploadUtils.UPLOAD_SUCCESS_CODE:
                    // 上传成功，发送消息
                    String message = (String) msg.obj;
                    System.out.println("====" + message);
                    try {
                        JSONArray array = new JSONArray(message);
                        String uname = array.getJSONObject(0).getString("uName");

                        // 在去上传头像名称
                        toUploadAvatar(uname);
                    } catch (Exception e) {
                        e.printStackTrace();
                        progressDialogUtil.dismiss();
                        EOPApplication.showToast(getActivity(), "头像上传失败！");
                    } finally {
                        // 删除拍照旋转的图片；
                        File f = new File(takePicturePath);
                        if (f.exists()) {
                            f.delete();
                        }
                    }
                    break;
                case 4:
                    progressDialogUtil.dismiss();
                    String uname = (String) msg.obj;
                    spUtil.setString(CommConstants.AVATAR, uname);

                    LogUtils.d("test", "my,uname=" + uname);

                    BitmapAjaxCallback callback = new BitmapAjaxCallback();
                    callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                            .round(10).url(CommConstants.URL_DOWN + uname)
                            .memCache(true).fileCache(true).targetWidth(128);
                    aQuery.id(avatar).image(callback);
                    break;
                case 5:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(getActivity(), "头像上传失败！");
                    break;
                case ZoneConstants.ZONE_MY_SAY_COUNT_RESULT:
                    // {"item":23,"code":0,"msg":"OK"}
                    try {
                        String result = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(result);
                        int code = jsonObject.getInt("code");
                        if (code == 0) {
                            if (jsonObject.has("item")) {
                                int count = jsonObject.getInt("item");
                                zoneCount.setText(count + "");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 11:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(getActivity(), "清除成功！");
                    break;
                case 98:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(getActivity(), "解绑成功！");
                    spUtil.setString("GestureCode", "");
                    serviceHelper.stopService();
                    XmppManager.getInstance().disconnect();
                    LoginInfo loginConfig = CommConstants.loginConfig;
                    new CommonHelper(getActivity()).saveLoginConfig(loginConfig);
                    spUtil.setBoolean(CommConstants.IS_AUTOLOGIN, false);
                    spUtil.setBoolean(CommConstants.IS_REMEMBER, false);
                    spUtil.setString(CommConstants.PASSWORD, "");
                    EOPApplication.exit();
                    clearDeviceType();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    getActivity().startActivity(
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    break;
                case 99:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(getActivity(), "解绑失败！");
                    break;
                default:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(getActivity(), "头像上传失败！");
                    // 删除拍照旋转的图片；
                    File f = new File(takePicturePath);
                    if (f.exists()) {
                        f.delete();
                    }
                    break;
            }
        }
    };

    private void toUploadAvatar(final String uname) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                JSONObject object = new JSONObject();
                try {
                    object.put("userId",spUtil.getString(CommConstants.USERID));
                    object.put("avatar",uname);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String result = HttpClientUtils.post(
                        CommConstants.URL_STUDIO + "setUserAvatar", object.toString(),
                        Charset.forName("UTF-8"));

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    boolean ok = jsonObject.getBoolean("ok");
                    if (ok) {
                        mHandler.obtainMessage(4, uname).sendToTarget();
                    } else {
                        mHandler.sendEmptyMessage(5);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(5);
                }
            }
        }).start();

    }

}
