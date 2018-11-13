package com.jianye.smart.module.workbench.attendance.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.androidquery.callback.BitmapAjaxCallback;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.utils.UploadUtils;
import com.movit.platform.common.utils.UploadUtils.OnUploadProcessListener;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.view.viewpager.ImageViewPagerActivity;
import com.jianye.smart.R;
import com.jianye.smart.application.EOPApplication;
import com.jianye.smart.base.BaseActivity;
import com.jianye.smart.module.workbench.attendance.adapter.AttendancePicGridAdapter;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import okhttp3.Call;
import okhttp3.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AttendanceCreateActivity extends BaseActivity implements
        OnUploadProcessListener {
    GridView gridView;
    TextView title;
    ImageView topLeft;
    ImageView topRight;
    EditText reason;
    TextView address;
    TextView time;
    Button create;

    AttendancePicGridAdapter gridAdapter;
    ArrayList<String> picPaths = new ArrayList<>();

    Uri imageUri;// The Uri to store the big
    String currentTime;

    private LocationClient mLocationClient;
    public MyLocationListener mMyLocationListener;

    public static final int COMPRESS_RESULT = 5;
    public static final int COMPRESS_FAILED = 6;
    boolean isDestory = false;
    public ArrayList<String> uploadImages = new ArrayList<>();
    int uploadCount = 0;// 当前上传的数量
    int count = 0; // 检测上传响应的计数
    boolean isAllCompressed = false;
    boolean hasUploadFailed = false;
    List<String> picUnames = new ArrayList<>();
    ArrayList<String> deleteList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_create);

        InitLocation();
        iniView();
        initData();

        setAdapter();
    }

    private void iniView() {
        gridView = (GridView) findViewById(R.id.attendance_gridview);
        title = (TextView) findViewById(R.id.tv_common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_left);
        topRight = (ImageView) findViewById(R.id.common_top_right);
        title.setText("创建");
        topRight.setVisibility(View.GONE);

        reason = (EditText) findViewById(R.id.attendance_reason_txt);
        address = (TextView) findViewById(R.id.attendance_location_txt);
        time = (TextView) findViewById(R.id.attendance_time_txt);
        create = (Button) findViewById(R.id.attendance_create);

        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initData() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String url = CommConstants.url_attendance + "getTime";
                    String result = HttpClientUtils.post(url, "{}", Charset.forName("UTF-8"));
                    Log.v("json", "--" + result);
                    JSONObject object = new JSONObject(result);
                    boolean ok = object.getBoolean("ok");
                    if (ok) {
                        String timeString = object.getString("objValue");
                        handler.obtainMessage(4, timeString).sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        create.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String cause = reason.getText().toString().trim();
                if (StringUtils.empty(cause)) {
                    EOPApplication.showToast(context, "请填写事由！");
                    return;
                }
                if (cause.length() > 50) {
                    EOPApplication.showToast(context, "最多输入50个字！");
                    return;
                }
                if (picPaths.isEmpty()) {
                    EOPApplication.showToast(context, "请至少上传1张照片！");
                    return;
                }
                progressDialogUtil.showLoadingDialog(context, "正在上传考勤信息...",
                        false);
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (isAllCompressed) {
                            Log.v("upload-size", uploadImages.size() + "");
                            uploadCount = uploadImages.size();
                            if (uploadCount == 0) {
                                toSave();
                                return;
                            }
                            ArrayList<String> tempList = new ArrayList<>();
                            tempList.addAll(uploadImages);

                            for (int i = 0; i < tempList.size(); i++) {
                                String upImage = tempList.get(i);
                                String oImage = upImage.replace("_temp", "");
                                MessageBean bean = new MessageBean();
                                bean.setContent(oImage);
                                toUploadFile(upImage, bean);
                            }
                            tempList = null;
                        } else {
                            handler.postDelayed(this, 500);
                        }
                    }
                });
            }
        });
    }

    public void toSave() {
        String picture = "";
        for (String uname : picUnames) {
            picture += uname + ",";
        }
        JSONObject object = new JSONObject();
        try {
            object.put("userId", spUtil.getString(CommConstants.USERID));
            object.put("cause", reason.getText().toString().trim());
            object.put("position", address.getText().toString());
            object.put("picture", picture);
        } catch (Exception e) {
            e.printStackTrace();
            handler.sendEmptyMessage(99);
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpUtils.postStringWithToken()
                .url(CommConstants.url_attendance + "save")
                .content(object.toString())
                .mediaType(JSON)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        handler.sendEmptyMessage(99);
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        System.out.println("rrrrrrrrrrrrrrrrr="+response);
                    }
                });












//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                try {
//                    String picture = "";
//                    for (String uname : picUnames) {
//                        picture += uname + ",";
//                    }
//                    String sssString = "userId="
//                            + spUtil.getString(CommConstants.USERID)
//                            + "&cause="
//                            + URLEncoder.encode(reason.getText().toString()
//                            .trim(), "UTF-8") + "&position="
//                            + address.getText() + "&picture=" + picture;
//                    String responseStr = HttpClientUtils.postZone(
//                            CommConstants.url_attendance + "save", sssString,
//                            Charset.forName("UTF-8"));
//                    System.out.println("rrrrrrrrrrrrrrrr=" + responseStr);
//                    Log.v("json", "--" + responseStr);
//                    JSONObject object = new JSONObject(responseStr);
//                    boolean ok = object.getBoolean("ok");
//                    if (ok) {
//                        handler.sendEmptyMessage(7);
//                    } else {
//                        handler.sendEmptyMessage(99);
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    handler.sendEmptyMessage(99);
//                }
//
//            }
//        }).start();
    }

    private void setAdapter() {
        gridAdapter = new AttendancePicGridAdapter(context, picPaths);
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (picPaths.size() > position
                        && picPaths.get(position) != null) {
                    Intent intent = new Intent(AttendanceCreateActivity.this,
                            ImageViewPagerActivity.class);
                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    intent.putExtra("locationX", location[0]);
                    intent.putExtra("locationY", location[1]);
                    intent.putExtra("width", view.getWidth());
                    intent.putExtra("height", view.getHeight());
                    intent.putStringArrayListExtra("selectedImgs", picPaths);
                    intent.putExtra("postion", position);
                    startActivityForResult(intent.putExtra("FromBucket", true)
                                    .putExtra("CanDelete", true),
                            ImageViewPagerActivity.IMAGEVIEWPAGE_DELETE);
                    overridePendingTransition(0, 0);
                } else {
                    // 跳转相机拍照
                    currentTime = DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
                    imageUri = Uri.parse(CommConstants.IMAGE_FILE_LOCATION);
                    if (imageUri == null) {
                        return;
                    }
                    String sdStatus = Environment.getExternalStorageState();
                    if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(AttendanceCreateActivity.this, "找不到sd卡",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (ContextCompat.checkSelfPermission(AttendanceCreateActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        //申请 CAMERA 权限
                        ActivityCompat.requestPermissions(AttendanceCreateActivity.this, new String[]{Manifest.permission.CAMERA},
                                CommConstants.CAMERA_REQUEST_CODE);
                    } else {
                        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(intent2, 2);
                    }

                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CommConstants.CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent2, 2);
            } else {
                // Permission Denied
                Toast.makeText(this, "访问相机权限未获得您授权，无法使用拍照功能。", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:

                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    if (imageUri != null) {
                        imageUri = null;
                        Log.v("currentTime", currentTime);
                        boolean copy = FileUtils.copyFile(CommConstants.SD_CARD
                                + "/temp.jpg", CommConstants.SD_CARD_IMPICTURES
                                + currentTime + ".jpg");
                        new File(CommConstants.SD_CARD + "/temp.jpg").delete();
                        if (copy) {
                            picPaths.add(CommConstants.SD_CARD_IMPICTURES + currentTime
                                    + ".jpg");
                            PicUtils.scanImages(AttendanceCreateActivity.this,
                                    CommConstants.SD_CARD_IMPICTURES + currentTime
                                            + ".jpg");

                            isAllCompressed = false;

                            if (uploadImages.size() == picPaths.size()) {
                                isAllCompressed = true;
                            }

                            for (int i = 0; i < picPaths.size(); i++) {
                                // 已经有压缩过的不需要再次压缩
                                String tempPath = PicUtils.getTempPicPath(picPaths
                                        .get(i));
                                if (!uploadImages.contains(tempPath)) {
                                    handler.post(new CompressImageRunnale(picPaths
                                            .get(i)));
                                }
                            }

                            gridAdapter.notifyDataSetChanged();
                        }
                    }
                }
                break;
            case ImageViewPagerActivity.IMAGEVIEWPAGE_DELETE:
                if (resultCode == ImageViewPagerActivity.IMAGEVIEWPAGE_DELETE) {
                    Log.v("result", "IMAGEVIEWPAGE_DELETE");
                    deleteList = data
                            .getStringArrayListExtra("IMAGEVIEWPAGE_DELETE");

                    for (int i = 0; i < deleteList.size(); i++) {
                        if (picPaths.contains(deleteList.get(i))) {
                            picPaths.remove(deleteList.get(i));
                        }
                    }
                    ArrayList<String> tempList = new ArrayList<String>();
                    tempList.addAll(uploadImages);
                    for (int i = 0; i < tempList.size(); i++) {
                        if (!picPaths
                                .contains(tempList.get(i).replace("_temp", ""))) {
                            uploadImages.remove(tempList.get(i));
                        }
                    }
                    tempList = null;

                    for (int i = 0; i < deleteList.size(); i++) {
                        String newPath = PicUtils.getTempPicPath(deleteList.get(i));
                        File f = new File(newPath);
                        if (f.exists()) {
                            f.delete();
                            Log.v("onActivityResult", "del file:" + newPath);
                        }
                    }
                    gridAdapter.notifyDataSetChanged();
                }
            default:
                break;
        }
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

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 4:
                    String sysTime = (String) msg.obj;
                    time.setText(sysTime);
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
                    if (picPaths.contains(bean.getContent())) {
                        if (!uploadImages.contains(filePath)) {
                            uploadImages.add(filePath);
                            if (uploadImages.size() == picPaths.size()) {
                                isAllCompressed = true;
                            }
                        }
                    }
                    break;
                case COMPRESS_FAILED:
                    String str = (String) msg.obj;
                    EOPApplication.showToast(context,
                            "您拍照的第" + picPaths.indexOf(str) + "图片有误!");
                    break;

                case UploadUtils.UPLOAD_SUCCESS_CODE:
                    Bundle bundle = msg.getData();
                    String message = bundle.getString("message");
                    String path = bundle.getString("uploadPath");
                    // 上传成功，发送消息
                    try {
                        System.out.println("====" + message);
                        JSONObject jsonObject = new JSONObject(message);
                        JSONObject response = jsonObject.getJSONObject("response");
                        JSONArray array = response.getJSONArray("message");
                        String uname = array.getJSONObject(0).getString("uname");

                        picUnames.add(uname);

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
                        toSave();

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
                case 7:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(context, "上传成功！");
                    setResult(1);
                    finish();
                    break;
                case 99:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(context, "上传失败！");
                    break;
                case 999:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(context, uploadImages.size()
                            + "张图片上传失败！");
                    topRight.setEnabled(true);
                    hasUploadFailed = false;
                    count = 0;
                    break;
                default:
                    break;
            }
        }
    };

    private void InitLocation() {

        mLocationClient = new LocationClient(context);
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
        String tempcoor = "gcj02";
        // tempcoor="bd09ll";
        // tempcoor="bd09";
        option.setOpenGps(true);
        option.setCoorType(tempcoor);// 返回的定位结果是百度经纬度，默认值gcj02
        int span = 3000;
        option.setScanSpan(span);// 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);// 是否要反地理编码
        mLocationClient.setLocOption(option);
    }

    /**
     * 实现实位回调监听
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // Receive Location
            String addr = location.getAddrStr();
            float direction = location.getDirection();
            String street = location.getStreet() + " "
                    + location.getStreetNumber();
            address.setText(addr);
        }

    }

    @Override
    protected void onStart() {
        mLocationClient.start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mLocationClient.stop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (!uploadImages.isEmpty()) {
            for (int i = 0; i < uploadImages.size(); i++) {
                // 删除拍照旋转的图片；
                File f = new File(uploadImages.get(i));
                if (f.exists()) {
                    f.delete();
                    Log.v("onDestroy", "del file:" + uploadImages.get(i));
                }
            }
        }
        isDestory = true;
        BitmapAjaxCallback.clearCache();
        System.gc();
        mLocationClient.unRegisterLocationListener(mMyLocationListener);
        mLocationClient = null;
        super.onDestroy();
    }

}
