package com.jianye.smart.module.workbench.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.androidquery.AQuery;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.utils.UploadUtils;
import com.movit.platform.common.utils.UploadUtils.OnUploadProcessListener;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.widget.SelectPicPopup;
import com.jianye.smart.R;
import com.jianye.smart.application.EOPApplication;
import com.jianye.smart.base.BaseActivity;
import com.jianye.smart.module.workbench.adapter.SuggestionsGridAdapter;
import com.jianye.smart.module.workbench.model.WorkTable;
import com.jianye.smart.service.LocationService;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class SuggestionActivity extends BaseActivity implements
        OnUploadProcessListener {
    private EditText userSuggestion;
    private GridView gridView;
    private Spinner spAdviceType;
    private EditText etAddress;
    SelectPicPopup popWindow;

    Uri imageUri;// The Uri to store the big
    String currentTime;

    public LocationService locationService;
    private String suggestionLocation;
    private List<WorkTable> typeList;

    SuggestionsGridAdapter suggestionsGridAdapter = null;
    List<Object> images;
    boolean disableListener = false;
    private final int MAX_COUNT = 8;
    Map imageMap = new HashMap<>();
    AQuery aQuery;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int position = 0;
            Object o = msg.obj;
            if (msg.obj != null) {
                position = (Integer) msg.obj;
            }
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    suggestionsGridAdapter = new SuggestionsGridAdapter(
                            SuggestionActivity.this, images, handler);
                    gridView.setAdapter(suggestionsGridAdapter);
                    break;
                case 2:
                    images.remove(position);
                    disableListener = false;
                    if (!images.contains(R.drawable.image_add)) {
                        images.add(R.drawable.image_add);
                    }
                    suggestionsGridAdapter = new SuggestionsGridAdapter(
                            SuggestionActivity.this, images, handler);
                    suggestionsGridAdapter.setDisableListener(disableListener);
                    suggestionsGridAdapter.setDeling(false);
                    gridView.setAdapter(suggestionsGridAdapter);
                    break;
                case 3:
                    int postion = (Integer) msg.obj;
                    if (!disableListener) {
                        if (postion == images.size() - 1) {
                            InputMethodManager imm = (InputMethodManager) SuggestionActivity.this
                                    .getSystemService(SuggestionActivity.this.INPUT_METHOD_SERVICE);
                            if (imm.isActive()) {
                                imm.hideSoftInputFromWindow(
                                        gridView.getWindowToken(), 0);
                            }
                            // 实例化SelectPicPopupWindow
                            popWindow = new SelectPicPopup(
                                    SuggestionActivity.this, itemsOnClick);
                            // 显示窗口
                            popWindow.showAtLocation(gridView, Gravity.BOTTOM
                                    | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                        } else {
                            suggestionsGridAdapter
                                    .setDeling(!suggestionsGridAdapter.isDeling());
                            suggestionsGridAdapter.notifyDataSetChanged();
                        }
                    } else {
                        suggestionsGridAdapter.setDeling(!suggestionsGridAdapter
                                .isDeling());
                        suggestionsGridAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_suggestion);
        aQuery = new AQuery(this);
        userSuggestion = (EditText) findViewById(R.id.user_suggestion);
        TextView title = (TextView) findViewById(R.id.tv_common_top_title);
        ImageView topLeft = (ImageView) findViewById(R.id.common_top_img_left);
        TextView topRight = (TextView) findViewById(R.id.common_top_img_right);
        gridView = (GridView) findViewById(R.id.chat_detail_gridview);
        topRight.setText("提交");

        spAdviceType = (Spinner) findViewById(R.id.sp_suggestion_type);
        etAddress = (EditText) findViewById(R.id.et_address);

        String titleString = getIntent().getStringExtra("title");
        title.setText(titleString);
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final String userId = CommConstants.loginConfig
                .getmUserInfo().getId();

        topRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String advice = userSuggestion.getText().toString();
                if (StringUtils.empty(advice)) {
                    EOPApplication.showToast(context, "请输入您的建议");
                    return;
                }
                String address = etAddress.getText().toString();
                if (TextUtils.isEmpty(address)) {
                    EOPApplication.showToast(context, "请输入地址");
                    return;
                }


                int position = spAdviceType.getSelectedItemPosition();
                String typeId = typeList.get(position).getId();
                String typeName = typeList.get(position).getName();

                postSuggestion(userId, advice, typeId, typeName, address);
            }
        });

        setAdapter();
        postAdviceType();//获取建议类型
    }

    private void setAdapter() {
        images = new ArrayList<>();
        images.add(R.drawable.image_add);
        suggestionsGridAdapter = new SuggestionsGridAdapter(
                SuggestionActivity.this, images, handler);
        gridView.setAdapter(suggestionsGridAdapter);
    }

    AsyncTask<Void, Void, String> suggestionTask = null;

    private void postSuggestion(final String userId, final String suggestion,
                                final String typeId, final String typeName, final String address) {
        if (suggestionTask != null) {
            suggestionTask.cancel(true);
            suggestionTask = null;
        }
        suggestionTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialogUtil.showLoadingDialog(SuggestionActivity.this,
                        "请稍候...", false);
            }

            @Override
            protected String doInBackground(Void... params) {
                String result = "";
                try {
                    String picurl = "";
                    for (Object obj : images) {
                        if (obj instanceof String) {
                            String localImage = (String) obj;
                            Log.v("localImage", "localImage :" + localImage);
                            String[] localPath = localImage.split("/");
                            String namePath = localPath[localPath.length - 1];
                            picurl += imageMap.get(namePath) + ";";
                        }

                    }
                    JSONObject map = new JSONObject();
                    map.put("userId", userId);
                    map.put("content", suggestion);
                    map.put("picture", picurl);
                    map.put("typeId", typeId);
                    map.put("typeName", typeName);
                    map.put("address", address);
                    map.put("addressCoordinate", suggestionLocation);
                    String url = CommConstants.url_suggestion;
                    result = HttpClientUtils.post(url, map.toString(), Charset.forName("UTF-8"));
                    Log.v("postSuggestion", "--" + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                progressDialogUtil.dismiss();
                if (!TextUtils.isEmpty(result)) {
                    //ToastUtils.showToast(getApplicationContext(),"提交成功!");
                    finish();
                } else {
                    ToastUtils.showToast(getApplicationContext(), "提交失败!");
                }
//				int theme = Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1 ? AlertDialog.THEME_HOLO_LIGHT:0;
//				AlertDialog.Builder builder = new AlertDialog.Builder(context,theme);
//				builder.setObjValue("如你的建议被采纳,我们会将受理结果发送至你的邮箱");
//				builder.setTitle("提交成功");
//				builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//						finish();
//					}
//				});
//				builder.create().show();

            }
        };
        suggestionTask.execute(null, null, null);
    }

    AsyncTask<Void, Void, String> getAdviceTypeTask = null;

    private void postAdviceType() {
        if (getAdviceTypeTask != null) {
            getAdviceTypeTask.cancel(true);
            getAdviceTypeTask = null;
        }
        getAdviceTypeTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialogUtil.showLoadingDialog(SuggestionActivity.this,
                        "请稍候...", false);
            }

            @Override
            protected String doInBackground(Void... params) {

                return HttpClientUtils.post(
                        CommConstants.url_advice_type, null);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                try {
                    com.alibaba.fastjson.JSONObject object = JSON.parseObject(result);
                    String objValue = object.getString("objValue");
                    typeList = JSON.parseArray(objValue, WorkTable.class);
                    ArrayList<String> txts = new ArrayList<>();
                    for (int i = 0; i < typeList.size(); i++) {
                        txts.add(typeList.get(i).getName());
                    }
                    ArrayAdapter adapter = new ArrayAdapter(SuggestionActivity.this,
                            R.layout.item_spinner_checked_text, txts);
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(R.layout.item_spinner_checked_text);
                    // Apply the adapter to the spinner
                    spAdviceType.setAdapter(adapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progressDialogUtil.dismiss();


            }
        };
        getAdviceTypeTask.execute(null, null, null);
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
                        Toast.makeText(SuggestionActivity.this, "找不到sd卡", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    if (ContextCompat.checkSelfPermission(SuggestionActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        //申请 CAMERA 权限
                        ActivityCompat.requestPermissions(SuggestionActivity.this, new String[]{Manifest.permission.CAMERA},
                                CommConstants.CAMERA_REQUEST_CODE);
                    } else {
                        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(intent2, 2);
                    }

                    break;
                case R.id.btn_pick_photo:
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

    String takePicturePath = "";

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == SuggestionActivity.this.RESULT_OK) {
            switch (requestCode) {
                // 如果是直接从相册获取
                case 1:
                    // 从相册中直接获取文件的真是路径，然后上传
                    final String picPath = PicUtils.getPicturePath(data,
                            SuggestionActivity.this);
                    Log.v("picPath", "===" + picPath);
                    try {
                        takePicturePath = PicUtils
                                .getSmallImageFromFileAndRotaing(picPath);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    notifyGridView(picPath);
                    toUploadFile(takePicturePath);
                    break;
                // 如果是调用相机拍照时
                case 2:
                    if (imageUri != null) {
                        boolean copy = FileUtils.copyFile(CommConstants.SD_CARD
                                + "/temp.jpg", CommConstants.SD_CARD_IMPICTURES
                                + currentTime + ".jpg");
                        new File(CommConstants.SD_CARD + "/temp.jpg").delete();
                        if (copy) {
                            String pathString = CommConstants.SD_CARD_IMPICTURES
                                    + currentTime + ".jpg";
                            Log.v("pathString", "===" + pathString);

                            PicUtils.scanImages(context, pathString);

                            try {
                                takePicturePath = PicUtils
                                        .getSmallImageFromFileAndRotaing(pathString);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            notifyGridView(pathString);
                            toUploadFile(takePicturePath);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void toUploadFile(String filePath) {
        String fileKey = "file";
        UploadUtils uploadUtils = UploadUtils.getInstance();
        uploadUtils.setOnUploadProcessListener(this);
        progressDialogUtil.showLoadingDialog(SuggestionActivity.this, "请稍候...",
                false);
        uploadUtils.uploadFile(filePath, fileKey, CommConstants.URL_UPLOAD, null,
                null);
    }

    // {response:{'state':200,'message':[{'oname':'pic_beauty_on_sofa.jpg','uname':'ce10d0e8-bc76-4286-a0f3-692bc0b609ca.jpg',
    // 'suffix':'.jpg','size':'66874','dir':'D:\\nginx\\content\\'} ]}}
    @Override
    public void onUploadDone(int responseCode, String message,
                             MessageBean messageDataObj) {
        progressDialogUtil.dismiss();
        if (responseCode == CommConstants.MSG_SEND_SUCCESS) {
            File file = new File(takePicturePath);
            if (file.exists()) {
                file.delete();
                Log.v("onSendSuggestion", "上传成功，删除本地文件" + message);
            }
        }
        try {
            JSONObject jsonObject = new JSONObject(message);
            JSONObject response = jsonObject.getJSONObject("response");
            JSONArray array = response.getJSONArray("message");
            String uname = array.getJSONObject(0).getString("uname");
            String oname = array.getJSONObject(0).getString("oname");
            imageMap.put(oname.replace("_temp", ""), uname);
        } catch (Exception e) {
            e.printStackTrace();
            //EOPApplication.showToast(context, "图片上传失败！");
            Log.v("onSendSuggestion", "上传成功，删除本地文件" + message);
        }
    }

    @Override
    public void onUploadProcess(int fileSize, int uploadSize) {
        Log.v("onUploadProcess", fileSize + "   " + uploadSize);
    }

    @Override
    public void initUpload(int fileSize) {
        // TODO Auto-generated method stub

    }

    private void notifyGridView(String takePicturePath) {
        if (images != null && images.size() == MAX_COUNT) {
            images.add(images.size() - 1, takePicturePath);
            int k = 0;
            for (int i = 0; i < images.size(); i++) {
                if (images.get(i) instanceof Integer) {
                    k = i;
                    disableListener = true;
                    suggestionsGridAdapter.setDisableListener(disableListener);
                }
            }
            images.remove(k);
        } else {
            images.add(images.size() - 1, takePicturePath);
        }
        handler.sendEmptyMessage(1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // -----------location config ------------
        locationService = ((EOPApplication) getApplication()).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //注册监听
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        locationService.start();// 定位SDK
        // start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
    }

    /***
     * Stop location service
     */
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (suggestionTask != null) {
            suggestionTask.cancel(true);
            suggestionTask = null;
        }
        if (getAdviceTypeTask != null) {
            getAdviceTypeTask.cancel(true);
            getAdviceTypeTask = null;
        }
    }

    /*****
     * 定位结果回调，重写onReceiveLocation方法，
     */
    private BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                StringBuffer sb = new StringBuffer(256);
                sb.append("time : ");
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
                sb.append(location.getTime());
                sb.append("\nerror code : ");
                sb.append(location.getLocType());
                sb.append("\nlatitude : ");
                sb.append(location.getLatitude());
                sb.append("\nlontitude : ");
                sb.append(location.getLongitude());
                sb.append("\nradius : ");
                sb.append(location.getRadius());
                sb.append("\nCountryCode : ");
                sb.append(location.getCountryCode());
                sb.append("\nCountry : ");
                sb.append(location.getCountry());
                sb.append("\ncitycode : ");
                sb.append(location.getCityCode());
                sb.append("\ncity : ");
                sb.append(location.getCity());
                sb.append("\nDistrict : ");
                sb.append(location.getDistrict());
                sb.append("\nStreet : ");
                sb.append(location.getStreet());
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\nDescribe: ");
                sb.append(location.getLocationDescribe());
                sb.append("\nDirection(not all devices have value): ");
                sb.append(location.getDirection());
                sb.append("\nPoi: ");
                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                    for (int i = 0; i < location.getPoiList().size(); i++) {
                        Poi poi = (Poi) location.getPoiList().get(i);
                        sb.append(poi.getName() + ";");
                    }
                }
                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(location.getSpeed());// 单位：km/h
                    sb.append("\nsatellite : ");
                    sb.append(location.getSatelliteNumber());
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 单位：米
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                    sb.append("\noperationers : ");
                    sb.append(location.getOperators());
                    sb.append("\ndescribe : ");
                    sb.append("网络定位成功");
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }

                String address = location.getAddrStr();
                if (!TextUtils.isEmpty(address)) {
                    etAddress.setText(address);
                    //etAddress.setEnabled(false);
                }
                suggestionLocation = location.getLongitude() + "," + location.getLatitude();

                locationService.stop();
            }
        }

    };


}
