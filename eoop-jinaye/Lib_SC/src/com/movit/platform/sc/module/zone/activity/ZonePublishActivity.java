package com.movit.platform.sc.module.zone.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.api.IZoneManager;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.common.utils.UploadUtils;
import com.movit.platform.common.utils.UploadUtils.OnUploadProcessListener;
import com.movit.platform.framework.faceview.FaceViewPage;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.viewpager.ImageViewPagerActivity;
import com.movit.platform.sc.R;
import com.movit.platform.sc.module.imagesbucket.activity.PicBucketActivity;
import com.movit.platform.sc.module.zone.adapter.ZonePublishPicGridViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class ZonePublishActivity extends Activity implements
        OnTouchListener, OnClickListener, OnUploadProcessListener {
    EditText editText;
    TextView topLeft;
    TextView topRight;
    TextView topTitle;
    ImageView cutLine0;
    TextView atTextView;
    ImageView cutLine;
    HorizontalScrollView scrollView;
    GridView gridView;

    Button categoryBtn1;
    Button categoryBtn2;

    CheckBox chechFace;
    CheckBox checkPic;
    CheckBox checkAt;
    LinearLayout mRootView;

    private WindowManager.LayoutParams mWindowNanagerParams;
    private InputMethodManager mInputMethodManager;
    FaceViewPage faceViewPage;
    private boolean mIsFaceShow = false;// 是否显示表情
    private boolean mIsInputShow = false;// 是否显示键盘
    private boolean mIsHasMedios = false;// 是否显示多媒体
    private boolean mIsHasAts = false;// 是否显示at的人

    private ArrayList<UserInfo> atAllUserInfos = new ArrayList<UserInfo>();
    private ArrayList<OrganizationTree> atOrgunitionLists = new ArrayList<OrganizationTree>();
    private ArrayList<UserInfo> atUserInfos = new ArrayList<UserInfo>();

    private String type = "0";// work 1,life 0
    private String isSecret = "0";// 隐私 1是 0否

    boolean isAllCompressed = false;
    boolean hasUploadFailed = false;
    int uploadCount = 0;// 当前上传的数量
    int count = 0; // 检测上传响应的计数
    public static ArrayList<String> selectImagesList = new ArrayList<String>();
    public ArrayList<String> uploadImages = new ArrayList<String>();
    JSONArray uploadImagesJsonArray = new JSONArray();

//    private String[] selectedImagesArray = new String[9];
//    private String[] uploadImagesArray = new String[9];


    public static final int ZONE_SAY_RESULT = 4;
    public static final int COMPRESS_RESULT = 5;
    public static final int COMPRESS_FAILED = 6;

    ArrayList<String> deleteList = new ArrayList<String>();

    AQuery aQuery;
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ZONE_SAY_RESULT:
                    DialogUtils.getInstants().dismiss();
                    String result = (String) msg.obj;
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        int code = jsonObject.getInt("code");
                        if (0 == code) {
                            ToastUtils.showToast(ZonePublishActivity.this,
                                    "发表成功！");
                            setResult(1);
                            finish();
                        } else {
                            ToastUtils.showToast(ZonePublishActivity.this,
                                    "发表失败！");
                            topRight.setEnabled(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.showToast(ZonePublishActivity.this, "发表失败！");
                        topRight.setEnabled(true);
                    }
                    break;
                case UploadUtils.UPLOAD_SUCCESS_CODE:
                    Bundle bundle = msg.getData();
                    String message = bundle.getString("message");
                    String path = bundle.getString("uploadPath");
                    // 上传成功，发送消息
                    try {
                        System.out.println("====" + message);
                        JSONArray array = new JSONArray(message);
                        String uname = array.getJSONObject(0).getString("uName");

                        JSONObject remoteJson = new JSONObject();

                        String sizejson = PicUtils.getPicSizeJson(path);
                        remoteJson.put("name", uname);
                        remoteJson.put("size", sizejson);
                        uploadImagesJsonArray.put(selectImagesList.indexOf(path),
                                remoteJson);

                        // 删除拍照旋转的图片；
                        String newPathString = PicUtils.getTempPicPath(path);
                        File f = new File(newPathString);
                        if (f.exists()) {
                            f.delete();
                            Log.v("onUpload-success", "del file:" + newPathString);
                        }
                        // 删除已上传了的
                        uploadImages.remove(newPathString);
                        // 判断是否全部上传完
                        if (!uploadImages.isEmpty()) {
                            return;
                        }
                        toSay();
                    } catch (Exception e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(UploadUtils.UPLOAD_SERVER_ERROR_CODE);
                    }
                    break;
                case UploadUtils.UPLOAD_FILE_NOT_EXISTS_CODE
                        | UploadUtils.UPLOAD_SERVER_ERROR_CODE:
                    if (!hasUploadFailed) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                hasUploadFailed = true;
                                while (true) {
                                    if (count >= uploadCount) {// 上传动作做完后
                                        handler.sendEmptyMessage(999);
                                        break;
                                    }
                                }
                            }
                        }).start();
                    }
                    break;
                case 999:
                    DialogUtils.getInstants().dismiss();
                    ToastUtils.showToast(ZonePublishActivity.this,
                            uploadImages.size() + "张图片上传失败！");
                    topRight.setEnabled(true);
                    hasUploadFailed = false;
                    count = 0;
                    break;
                case COMPRESS_RESULT:
                    // 压缩后的temp图片路径
                    Bundle data = msg.getData();
                    String filePath = data.getString("filePath");
                    Log.v("COMPRESS_RESULT", filePath);
                    if (isDestory) {
                        File f = new File(filePath);
                        if (f.exists()) {
                            f.delete();
                            Log.v("COMPRESS_RESULT", "del file:" + filePath);
                        }
                    }
                    MessageBean bean = (MessageBean) data
                            .getSerializable("MessageBean");
                    // 判断这次的压缩图是否是已选择中的
                    if (selectImagesList.contains(bean.getContent())) {
                        if (!uploadImages.contains(filePath)) {
                            uploadImages.add(filePath);
                            if (uploadImages.size() == selectImagesList.size()) {
                                isAllCompressed = true;
                            }
                        }
                    }
                    break;
                case COMPRESS_FAILED:
                    String str = (String) msg.obj;
                    ToastUtils.showToast(ZonePublishActivity.this,
                            "您选择的第" + selectImagesList.indexOf(str) + "图片有误!");
                    break;
                default:
                    break;
            }
        }

    };

    protected IZoneManager zoneManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
        setContentView(R.layout.sc_activity_zone_publish);
        aQuery = new AQuery(this);
        selectImagesList.clear();
        initView();// 初始化view
        initData();// 初始化数据
        initFace();
        gridviewInit();
    }

    private void initView() {
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mWindowNanagerParams = getWindow().getAttributes();
        editText = (EditText) findViewById(R.id.zone_publish_edit);
        topLeft = (TextView) findViewById(R.id.style1_top_left);
        topRight = (TextView) findViewById(R.id.style1_top_right);
        topTitle = (TextView) findViewById(R.id.tv_common_top_title);
        cutLine = (ImageView) findViewById(R.id.zone_publish_cut_line);
        scrollView = (HorizontalScrollView) findViewById(R.id.zone_publish_scrollview);
        cutLine0 = (ImageView) findViewById(R.id.zone_publish_cut_line0);
        atTextView = (TextView) findViewById(R.id.zone_publish_at);
        gridView = (GridView) findViewById(R.id.zone_publish_pic_gridview);
        categoryBtn1 = (Button) findViewById(R.id.zone_publish_category_btn1);
        categoryBtn2 = (Button) findViewById(R.id.zone_publish_category_btn2);
        chechFace = (CheckBox) findViewById(R.id.zone_publish_bottom_radio_face);
        checkPic = (CheckBox) findViewById(R.id.zone_publish_bottom_radio_pic);
        checkAt = (CheckBox) findViewById(R.id.zone_publish_bottom_radio_at);
        mRootView = (LinearLayout) findViewById(R.id.zone_publish_bottom_ll);

        SharedPreUtils spUtil = new SharedPreUtils(this);
        if (!"default".equals(spUtil.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }

        chechFace.setOnClickListener(this);
        checkPic.setOnClickListener(this);
        checkAt.setOnClickListener(this);

        editText.setOnTouchListener(this);
        categoryBtn1.setOnClickListener(this);
        categoryBtn2.setOnClickListener(this);
        topRight.setOnClickListener(this);
        mIsInputShow = true;
        scrollView.setVisibility(View.GONE);
        cutLine.setVisibility(View.GONE);
        cutLine0.setVisibility(View.GONE);
        atTextView.setVisibility(View.GONE);

        if (mIsHasMedios) {
            onClick(checkPic);
        }

        topLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        topTitle.setText("发表日志");
        topRight.setText("发送");
        topLeft.setText("取消");
        topRight.setEnabled(false);
        editText.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mIsInputShow || mIsFaceShow) {
                        mIsInputShow = false;
                        faceViewPage.getmFaceRoot().setVisibility(View.GONE);
                        mIsFaceShow = false;
                        chechFace.setChecked(false);
                        return true;
                    }
                }
                return false;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    topRight.setEnabled(true);
                } else {
                    if (!mIsHasMedios) {
                        topRight.setEnabled(false);
                    }
                }
            }
        });
    }

    private void initData() {
        zoneManager = ((BaseApplication) this.getApplication()).getManagerFactory().getZoneManager();
    }

    private void initFace() {
        faceViewPage = new FaceViewPage(editText, mRootView);
        faceViewPage.initFacePage();
    }


    private void gridviewInit() {
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        ZonePublishPicGridViewAdapter adapter = new ZonePublishPicGridViewAdapter(
                this, selectImagesList);
        adapter.setSelectedPosition(0);
        int size = 0;
        if (selectImagesList.size() < 9) {
            size = selectImagesList.size() + 1;
        } else {
            size = selectImagesList.size();
        }
        for (int i = 0; i < selectImagesList.size(); i++) {
            System.out.println(selectImagesList.get(i));
        }
        LayoutParams params = gridView.getLayoutParams();
        float dp = getResources().getDimension(R.dimen.dp_64);
        final int width = size * (int) (dp + 40);
        params.width = width;
        gridView.setLayoutParams(params);
        gridView.setColumnWidth((int) (dp + 40));
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setNumColumns(size);

        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == selectImagesList.size()) {
                    startActivityForResult(new Intent(ZonePublishActivity.this,
                            PicBucketActivity.class).putStringArrayListExtra(
                            "selectImagesList", selectImagesList), 1);
                } else {
                    Intent intent = new Intent(ZonePublishActivity.this,
                            ImageViewPagerActivity.class);
                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    intent.putExtra("locationX", location[0]);
                    intent.putExtra("locationY", location[1]);
                    intent.putExtra("width", view.getWidth());
                    intent.putExtra("height", view.getHeight());
                    intent.putStringArrayListExtra("selectedImgs",
                            selectImagesList);
                    intent.putExtra("postion", position);
                    startActivityForResult(intent.putExtra("FromBucket", true)
                            .putExtra("CanDelete", true), ImageViewPagerActivity.IMAGEVIEWPAGE_DELETE);
                    overridePendingTransition(0, 0);
                }
            }
        });

        scrollView.getViewTreeObserver().addOnPreDrawListener(// 绘制完毕
                new OnPreDrawListener() {
                    public boolean onPreDraw() {
                        scrollView.scrollTo(width, 0);
                        scrollView.getViewTreeObserver()
                                .removeOnPreDrawListener(this);
                        return false;
                    }
                });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if (id == R.id.zone_publish_edit) {
            if (!mIsInputShow) {
                Log.v("action", "onTouch");
                mIsInputShow = true;
                mInputMethodManager.showSoftInput(editText, 0);

                if (!mIsHasMedios) {
                    scrollView.setVisibility(View.GONE);
                    cutLine.setVisibility(View.GONE);
                }
                if (!mIsHasAts) {
                    atTextView.setVisibility(View.GONE);
                    cutLine0.setVisibility(View.GONE);
                }
                faceViewPage.getmFaceRoot().setVisibility(View.GONE);
                mIsFaceShow = false;
                chechFace.setChecked(false);
                checkPic.setChecked(false);
                checkAt.setChecked(false);
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.zone_publish_category_btn1) {
            if ("公开".equals(categoryBtn1.getText())) {
                isSecret = "1";
                categoryBtn1.setText("私密");
                categoryBtn1.setTextColor(getResources().getColor(
                        R.color.zone_publish_simi_red_color));
                categoryBtn1.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.zone_ico_leaf, 0, 0, 0);
            } else if ("私密".equals(categoryBtn1.getText())) {
                isSecret = "0";
                categoryBtn1.setText("公开");
                categoryBtn1.setTextColor(getResources().getColor(
                        R.color.user_detail_content_blue_color));
                categoryBtn1.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.zone_ico_earth, 0, 0, 0);
            }
        } else if (id == R.id.zone_publish_category_btn2) {
            if ("生活".equals(categoryBtn2.getText())) {
                type = "1";
                categoryBtn2.setText("工作");
                categoryBtn2.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.zone_ico_work, 0, 0, 0);
            } else if ("工作".equals(categoryBtn2.getText())) {
                type = "0";
                categoryBtn2.setText("生活");
                categoryBtn2.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.zone_ico_life, 0, 0, 0);
            }
        } else if (id == R.id.zone_publish_bottom_radio_face) {

            Log.d("test", "mIsFaceShow=" + mIsFaceShow);
            if (!mIsFaceShow) {
                handler.postDelayed(new Runnable() {
                    // 解决此时界面会变形，有闪烁的现象
                    @Override
                    public void run() {
                        faceViewPage.getmFaceRoot().setVisibility(View.VISIBLE);
                        mIsFaceShow = true;
                        chechFace.setChecked(true);
                    }
                }, 80);
                mInputMethodManager.hideSoftInputFromWindow(
                        editText.getWindowToken(), 0);
                mIsInputShow = false;

                checkAt.setChecked(false);
                checkPic.setChecked(false);
            }
        } else if (id == R.id.zone_publish_bottom_radio_pic) {
            if (mIsFaceShow) {
                // 隐藏表情
                mIsFaceShow = false;
                faceViewPage.getmFaceRoot().setVisibility(View.GONE);
                chechFace.setChecked(false);
            }
            mInputMethodManager.hideSoftInputFromWindow(
                    editText.getWindowToken(), 0);
            mIsInputShow = false;
            checkAt.setChecked(false);
            if (!mIsHasMedios) {
                scrollView.setVisibility(View.VISIBLE);
                cutLine.setVisibility(View.VISIBLE);
            }
            startActivityForResult(new Intent(ZonePublishActivity.this,
                    PicBucketActivity.class).putStringArrayListExtra(
                    "selectImagesList", selectImagesList), 1);
            checkPic.setChecked(true);
        } else if (id == R.id.zone_publish_bottom_radio_at) {
            if (mIsFaceShow) {
                // 隐藏表情
                mIsFaceShow = false;
                faceViewPage.getmFaceRoot().setVisibility(View.GONE);
                chechFace.setChecked(false);
            }
            mInputMethodManager.hideSoftInputFromWindow(
                    editText.getWindowToken(), 0);
            mIsInputShow = false;
            checkPic.setChecked(false);
            Intent intent = new Intent();
            intent.putExtra("TITLE", "选择联系人");
            intent.putExtra("ACTION", "@");
            intent.putExtra("atUserInfos", atAllUserInfos);
            intent.putExtra("atOrgunitionLists", atOrgunitionLists);
            ((BaseApplication) ZonePublishActivity.this.getApplication()).getUIController().onIMOrgClickListener(ZonePublishActivity.this, intent, 2);

            checkAt.setChecked(true);
        } else if (id == R.id.style1_top_right) {
            topRight.setEnabled(false);
            String content = editText.getText().toString().trim();
            if (mIsHasMedios) {
                DialogUtils.getInstants().showLoadingDialog(ZonePublishActivity.this,
                        "正在发表...", false);

                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (isAllCompressed) {
                            uploadCount = uploadImages.size();
                            if (uploadCount == 0) {
                                toSay();
                                return;
                            }
                            for (int i = 0; i < selectImagesList.size(); i++) {
                                String oImage = selectImagesList.get(i);
                                MessageBean bean = new MessageBean();
                                bean.setContent(oImage);
                                String upImage = PicUtils.getTempPicPath(oImage);
                                toUploadFile(upImage, bean);
                            }

//                            ArrayList<String> tempList = new ArrayList<String>();
//                            tempList.addAll(uploadImages);
//
//                            for (int i = 0; i < tempList.size(); i++) {
//                                String upImage = tempList.postWithoutEncrypt(i);
//                                String oImage = upImage.replace("_temp", "");
//                                MessageBean bean = new MessageBean();
//                                bean.setContent(oImage);
//                                toUploadFile(upImage, bean);
//                            }
//                            tempList = null;
                        } else {
                            handler.postDelayed(this, 500);
                        }
                    }
                });

            } else {
                if (!"".equals(content)) {
                    DialogUtils.getInstants().showLoadingDialog(
                            ZonePublishActivity.this, "正在发表...", false);

                    if (mIsHasAts) {
                        String sAtGroup = "";
                        String sAtPerson = "";
                        String sMessageList = "";
                        for (int i = 0; i < atOrgunitionLists.size(); i++) {
                            sAtGroup += atOrgunitionLists.get(i).getId() + ",";
                        }
                        for (int i = 0; i < atUserInfos.size(); i++) {
                            sAtPerson += atUserInfos.get(i).getId() + ",";
                        }
                        for (int i = 0; i < atAllUserInfos.size(); i++) {
                            sMessageList += atAllUserInfos.get(i).getId() + ",";
                        }
                        zoneManager.say(content, type, isSecret, "", sAtGroup,
                                sAtPerson, sMessageList, handler);
                    } else {
                        zoneManager.say(content, type, isSecret, "", "", "",
                                "", handler);
                    }
                }
            }
        } else {
        }
    }

    public void toSay() {
        try {
            JSONObject imageJsonObject = new JSONObject();
            imageJsonObject.put("image", uploadImagesJsonArray);

            String content = editText.getText().toString().trim();

            if (mIsHasAts) {
                String sAtGroup = "";
                String sAtPerson = "";
                String sMessageList = "";
                for (int i = 0; i < atOrgunitionLists.size(); i++) {
                    sAtGroup += atOrgunitionLists.get(i).getId() + ",";
                }
                for (int i = 0; i < atUserInfos.size(); i++) {
                    sAtPerson += atUserInfos.get(i).getId() + ",";
                }
                for (int i = 0; i < atAllUserInfos.size(); i++) {
                    sMessageList += atAllUserInfos.get(i).getId() + ",";
                }
                zoneManager.say(content, type, isSecret,
                        imageJsonObject.toString(), sAtGroup, sAtPerson,
                        sMessageList, handler);
            } else {
                zoneManager.say(content, type, isSecret,
                        imageJsonObject.toString(), "", "", "", handler);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent data) {
        switch (arg0) {
            case 1:// pic
                if (arg1 == 1) {
                    if (!selectImagesList.isEmpty()) {
                        mIsHasMedios = true;
                        topRight.setEnabled(true);
                    } else {
                        mIsHasMedios = false;
                    }

                    // 先去进行压缩处理
                    if (!handlerThread1.isAlive()) {
                        handlerThread1.start();
                        handlerThread2.start();
                        handlerThread3.start();
                    }
                    if (handlerpost1 == null) {
                        handlerpost1 = new MyHandler(handlerThread1.getLooper());
                        handlerpost2 = new MyHandler(handlerThread2.getLooper());
                        handlerpost3 = new MyHandler(handlerThread3.getLooper());
                    }
                    ArrayList<String> tempList = new ArrayList<String>();
//                    tempList.addAll(uploadImages);
//                    for (int i = 0; i < tempList.size(); i++) {
//                        if (!selectImagesList.contains(tempList.postWithoutEncrypt(i).replace(
//                                "_temp", ""))) {
//                            uploadImages.remove(tempList.postWithoutEncrypt(i));
//                            deleteList.add(tempList.postWithoutEncrypt(i).replace("_temp", ""));
//                        }
//                    }

                    ArrayList<String> newList = new ArrayList<String>();

                    for (int i = 0; i < selectImagesList.size(); i++) {
                        String filePath = selectImagesList.get(i);
                        String uploadPath = PicUtils.getTempPicPath(filePath);
                        if (uploadImages.contains(uploadPath)) {
                            //保存原先已经压缩过的图片路径
                            tempList.add(uploadPath);
                        } else {
                            newList.add(filePath);
                        }
                    }
                    uploadImages.removeAll(tempList);
                    for (String path : uploadImages) {
                        File f = new File(path);
                        if (f.exists()) {
                            f.delete();
                            Log.v("onActivityResult", "del-file " + path);
                        }
                    }
                    uploadImages = tempList;

                    isAllCompressed = false;

                    if (uploadImages.size() == selectImagesList.size()) {
                        isAllCompressed = true;
                    }

                    for (String newPaht : newList) {
                        handlerpost1.obtainMessage(1, newPaht)
                                .sendToTarget();
                    }

//                    for (int i = 0; i < selectImagesList.size(); i++) {
//                        String tempPath = PicUtils.getTempPicPath(selectImagesList
//                                .postWithoutEncrypt(i));
//                        if (!uploadImages.contains(tempPath)) {
//                            handlerpost1.obtainMessage(1, selectImagesList.postWithoutEncrypt(i))
//                                    .sendToTarget();
//                        }
//                    }
                    gridviewInit();
                }

                break;
            case 2:// @
                if (data != null) {
                    atAllUserInfos = (ArrayList<UserInfo>) data
                            .getSerializableExtra("atUserInfos");
                    atOrgunitionLists = (ArrayList<OrganizationTree>) data
                            .getSerializableExtra("atOrgunitionLists");
                    if (atAllUserInfos.isEmpty() && atOrgunitionLists.isEmpty()) {
                        mIsHasAts = false;
                        cutLine0.setVisibility(View.GONE);
                        atTextView.setVisibility(View.GONE);
                        return;
                    } else {
                        mIsHasAts = true;
                        cutLine0.setVisibility(View.VISIBLE);
                        atTextView.setVisibility(View.VISIBLE);
                    }

                    String atStrings = "";
                    for (int i = 0; i < atOrgunitionLists.size(); i++) {
                        atStrings += "@" + atOrgunitionLists.get(i).getObjname()
                                + " ";
                    }
                    UserDao dao = UserDao.getInstance(ZonePublishActivity.this);
                    for (int i = 0; i < atAllUserInfos.size(); i++) {
                        boolean flag = true;
                        for (int j = 0; j < atOrgunitionLists.size(); j++) {
                            OrganizationTree org = dao
                                    .getOrganizationByOrgId(atAllUserInfos.get(i)
                                            .getOrgId());
                            if (org != null) {
                                if (org.getObjname().equals(
                                        atOrgunitionLists.get(j).getObjname())) {
                                    flag = false;
                                    break;
                                }
                            }

                        }
                        if (flag) {
                            atUserInfos.add(atAllUserInfos.get(i));
                            atStrings += "@" + atAllUserInfos.get(i).getEmpCname()
                                    + " ";
                        }
                    }
                    dao.closeDb();

                    SpannableString spannableString = new SpannableString(atStrings);
                    spannableString.setSpan(new ForegroundColorSpan(getResources()
                                    .getColor(R.color.user_detail_content_blue_color)), 0,
                            atStrings.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    atTextView.setText(spannableString);

                }
                break;
            case ImageViewPagerActivity.IMAGEVIEWPAGE_DELETE:
                if (arg1 == ImageViewPagerActivity.IMAGEVIEWPAGE_DELETE) {
                    Log.v("result", "IMAGEVIEWPAGE_DELETE");
                    deleteList = data
                            .getStringArrayListExtra("IMAGEVIEWPAGE_DELETE");

                    for (int i = 0; i < deleteList.size(); i++) {
                        if (selectImagesList.contains(deleteList.get(i))) {
                            selectImagesList.remove(deleteList.get(i));
                        }
                    }
//                    ArrayList<String> tempList = new ArrayList<String>();
//                    tempList.addAll(uploadImages);
//                    for (int i = 0; i < tempList.size(); i++) {
//                        if (!selectImagesList.contains(tempList.postWithoutEncrypt(i).replace(
//                                "_temp", ""))) {
//                            uploadImages.remove(tempList.postWithoutEncrypt(i));
//                        }
//                    }
//                    tempList = null;

                    for (int i = 0; i < deleteList.size(); i++) {
                        String tempPath = PicUtils.getTempPicPath(deleteList.get(i));
                        if (uploadImages.contains(tempPath)) {
                            uploadImages.remove(tempPath);
                        }
                        File f = new File(tempPath);
                        if (f.exists()) {
                            f.delete();
                        }
                    }
                    gridviewInit();
                }
            default:
                break;
        }
        super.onActivityResult(arg0, arg1, data);
    }

    class CompressImageRunnale implements Runnable {
        String filePath;

        public CompressImageRunnale(String filePath) {
            super();
            this.filePath = filePath;
        }

        @Override
        public void run() {
            String path = "";
            try {
                path = PicUtils.getSmallImageFromFileAndRotaing(filePath);
                Message message = new Message();
                MessageBean bean = new MessageBean();
                bean.setContent(filePath);
                Bundle data = new Bundle();
                data.putString("filePath", path);
                data.putSerializable("MessageBean", bean);
                message.what = COMPRESS_RESULT;
                message.setData(data);
                handler.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
                handler.obtainMessage(COMPRESS_FAILED, filePath).sendToTarget();
            }
        }
    }

    private void toUploadFile(String filePath, MessageBean bean) {
        String fileKey = "file";
        UploadUtils uploadUtils = UploadUtils.getInstance();
        uploadUtils.setOnUploadProcessListener(this);
        uploadUtils.uploadFile(filePath, fileKey, CommConstants.URL_UPLOAD, null,
                bean);
    }

    @Override
    public void initUpload(int fileSize) {

    }

    @Override
    public void onUploadProcess(int fileSize, int uploadSize) {
        // Log.v("onUploadProcess", fileSize + "---" + uploadSize);
    }

    @Override
    public void onUploadDone(int responseCode, String message,
                             MessageBean messageDataObj) {

        count++;

        Message msg = Message.obtain();
        msg.what = responseCode;
        Bundle data = new Bundle();
        data.putString("message", message);
        data.putString("uploadPath", messageDataObj.getContent());
        msg.setData(data);
        handler.sendMessage(msg);
    }

    boolean isDestory = false;

    @Override
    public void onDestroy() {
        if (handlerpost1 != null) {
            handlerpost1.getLooper().quit();
        }
        if (handlerpost2 != null) {
            handlerpost2.getLooper().quit();
        }
        if (handlerpost3 != null) {
            handlerpost3.getLooper().quit();
        }
        if (!uploadImages.isEmpty()) {
            for (int i = 0; i < uploadImages.size(); i++) {
                // 删除拍照旋转的图片；
                File f = new File(uploadImages.get(i));
                if (f.exists()) {
                    f.delete();
                }
            }
        }
        isDestory = true;
        BitmapAjaxCallback.clearCache();
        System.gc();
        super.onDestroy();
    }

    public HandlerThread handlerThread1 = new HandlerThread("handler_thread1");
    public HandlerThread handlerThread2 = new HandlerThread("handler_thread2");
    public HandlerThread handlerThread3 = new HandlerThread("handler_thread3");

    public MyHandler handlerpost1;
    public MyHandler handlerpost2;
    public MyHandler handlerpost3;

    class MyHandler extends Handler {

        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            String filePath = (String) msg.obj;
            switch (msg.what) {
                case 1:
                    handlerpost1.post(new CompressImageRunnale(filePath));
                    break;
                case 2:
                    handlerpost2.post(new CompressImageRunnale(filePath));
                    break;
                case 3:
                    handlerpost3.post(new CompressImageRunnale(filePath));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mIsFaceShow", mIsFaceShow);
        outState.putBoolean("mIsInputShow", mIsInputShow);
        outState.putBoolean("mIsHasMedios", mIsHasMedios);
        outState.putBoolean("mIsHasAts", mIsHasAts);

        outState.putSerializable("atAllUserInfos", atAllUserInfos);
        outState.putSerializable("atOrgunitionLists", atOrgunitionLists);
        outState.putSerializable("atUserInfos", atUserInfos);
        outState.putString("type", type);
        outState.putString("isSecret", isSecret);

        outState.putStringArrayList("uploadImages", uploadImages);
        outState.putStringArrayList("selectImagesList", selectImagesList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectImagesList = savedInstanceState
                .getStringArrayList("selectImagesList");
        uploadImages = savedInstanceState.getStringArrayList("uploadImages");
        mIsFaceShow = savedInstanceState.getBoolean("mIsFaceShow");
        mIsInputShow = savedInstanceState.getBoolean("mIsInputShow");
        mIsHasMedios = savedInstanceState.getBoolean("mIsHasMedios");
        mIsHasAts = savedInstanceState.getBoolean("mIsHasAts");

        atAllUserInfos = (ArrayList<UserInfo>) savedInstanceState
                .getSerializable("atAllUserInfos");
        atOrgunitionLists = (ArrayList<OrganizationTree>) savedInstanceState
                .getSerializable("atOrgunitionLists");
        atUserInfos = (ArrayList<UserInfo>) savedInstanceState
                .getSerializable("atUserInfos");

        type = savedInstanceState.getString("type");
        isSecret = savedInstanceState.getString("isSecret");
    }

}
