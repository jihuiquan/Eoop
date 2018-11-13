package com.movit.platform.innerea.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.xc.sdk.utils.CheckUtil;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.okhttp.utils.AesUtils;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFHelper;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.utils.ActivityUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.innerea.R;
import com.movit.platform.innerea.db.DBManager;
import com.movit.platform.innerea.entities.MapPoint;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.MediaType;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by scorpiokara on 15/8/31.
 */
public class MapGPSActivity extends Activity {
    public static List<MapPoint> points;
    Context context;
    MapView mMapView = null;
    BaiduMap mBaiduMap;

    // 定位相关Ø
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private LocationMode mCurrentMode;
    // BitmapDescriptor mCurrentMarker;
    boolean isFirstLoc = true;// 是否首次定位

    BitmapDescriptor point = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_gps_point);

    Button homeBtn;
    Button calenderBtn;
    Button settingBtn;
    DialogUtils progressDialogUtil;
    Button gpsSubmit;
    TextView tips;
    TextView waring;
    TextView info;
    Button locationBtn;

    MapPoint inGpsPoint;

    String userId;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    break;
                case 2:
                    progressDialogUtil.dismiss();
                    Map<String, String> map = (Map<String, String>) msg.obj;
                    showDialog(map.get("time"), true, map.get("title"));
                    DBManager manager = new DBManager(context,
                            MFSPHelper.getString(CommConstants.EMPADNAME));
                    manager.insertGPSLog(map.get("time"));
                    manager.closeDb();
                    gpsSubmit.setEnabled(true);
                    break;
                case 3:
                    progressDialogUtil.dismiss();
                    showDialog(MapGPSActivity.this.getString(R.string.do_gps_failed), false, "");
                    gpsSubmit.setEnabled(true);
                    break;
                case 4:
                    initLocalData();
                    break;
                case 5:
                    progressDialogUtil.dismiss();
                    String s = (String) msg.obj;
                    showDialog(s, false, "");
                    gpsSubmit.setEnabled(true);
                    break;
                case 6:
                    ArrayList<String> logList = (ArrayList<String>) msg.obj;
                    DBManager dbManager = new DBManager(MapGPSActivity.this,
                            MFSPHelper.getString(CommConstants.EMPADNAME));
                    dbManager.deleteAll();
                    for (int i = 0; i < logList.size(); i++) {
                        dbManager.insertGPSLog(logList.get(i));
                    }
                    dbManager.closeDb();
                    progressDialogUtil.dismiss();
                    Intent intent = new Intent(context, CalendarActivity.class);
                    intent.putExtra("success", true);
                    startActivity(intent);
                    break;
                case 7:
                    progressDialogUtil.dismiss();
                    Intent intent2 = new Intent(context, CalendarActivity.class);
                    intent2.putExtra("success", false);
                    startActivity(intent2);
                    break;
                default:
                    break;
            }
        }

    };
    /** 打卡后的运营图片接口**/
    private String attendance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.fragment_gps);
        initLocalView();
        initLocalData();
    }

    public void initLocalView() {
        // 获取地图控件引用
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.showScaleControl(false);
        homeBtn = (Button) findViewById(R.id.btn_home);
        calenderBtn = (Button) findViewById(R.id.btn_gps_calender);
        settingBtn = (Button) findViewById(R.id.btn_gps_setting);
        gpsSubmit = (Button) findViewById(R.id.gps_submit);
        tips = (TextView) findViewById(R.id.gps_tips);
        waring = (TextView) findViewById(R.id.gps_waring);
        locationBtn = (Button) findViewById(R.id.btn_location);
        info = (TextView) findViewById(R.id.gps_info);
        progressDialogUtil = new DialogUtils();
    }

    public void initLocalData() {
        userId = MFSPHelper.getString(CommConstants.USERID);
        String str = MFSPHelper.getString("gpsWaring");
        if (StringUtils.notEmpty(str)) {
            String[] arr = str.split("\\|\\|");
            waring.setText(arr[0]);
            if (arr.length > 1) {
                info.setText(arr[1]);
            }
        }
        setCannotGps();
        checkPermission();
        if (!ActivityUtils.isGPSOpen(context)) {
            showGPSDialog();
            setCannotGps();
        }
        homeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        calenderBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialogUtil.showDownLoadingDialog(MapGPSActivity.this,
                        getResources().getString(R.string.dialog_syn_ing),
                        false);
                getGpsLogFromSever();
            }
        });
        settingBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, AlarmSettingActivity.class));
            }
        });
        gpsSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ActivityUtils.isGPSOpen(context)) {
                    if (checkPointCanGps()) {
                        progressDialogUtil.showDownLoadingDialog(
                                context,
                                getResources().getString(
                                        R.string.dialog_do_gps_ing), false);
                        gpsSubmit.setEnabled(false);
                        doGps();
                    }
                } else {
                    showGPSDialog();
                    setCannotGps();
                    if (mLocClient != null && mLocClient.isStarted()) {
                        mLocClient.stop();
                    }
                }

            }
        });
        locationBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mLocClient != null && mLocClient.isStarted()) {
                    mLocClient.stop();
                    setCannotGps();
                    isFirstLoc = true;
                }
                if (mLocClient != null) {
                    mLocClient.start();
                }
            }
        });
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        } else {
            initMapAndLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initMapAndLocation();
            } else {
                Toast.makeText(this, R.string.not_get_permission, Toast.LENGTH_LONG).show();
                gpsSubmit.setEnabled(false);
                tips.setEnabled(false);
                waring.setText(getResources().getString(R.string.not_get_permission));
            }
        }
    }

    public void setCanGps() {
        gpsSubmit.setEnabled(true);
        tips.setEnabled(true);
        tips.setText(getResources().getString(R.string.gps_in_location));
    }

    public void setCannotGps() {
        gpsSubmit.setEnabled(false);
        tips.setEnabled(false);
        tips.setText(getResources().getString(R.string.gps_out_location));
    }

    public void initMapAndLocation() {
        mCurrentMode = LocationMode.NORMAL;
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, true, null));
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);
        mBaiduMap.setMapStatus(msu);
        // 定位初始化
        mLocClient = new LocationClient(context);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(3000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    /**
     * 添加点、线、多边形、圆、文字
     */
    public void addCustomElements() {
        if (MapGPSActivity.points != null) {
            for (int i = 0; i < MapGPSActivity.points.size(); i++) {
                MapPoint mapPoint = MapGPSActivity.points.get(i);
                if (!TextUtils.isEmpty(mapPoint.getLatitude()) && !TextUtils.isEmpty(mapPoint.getLongitude())) {
                    try {
                        // 添加圆
                        LatLng sourceLatLng = new LatLng(Double.parseDouble(mapPoint
                                .getLatitude()), Double.parseDouble(mapPoint
                                .getLongitude()));
                        CoordinateConverter converter = new CoordinateConverter();
                        converter.from(CoordinateConverter.CoordType.COMMON);
                        converter.coord(sourceLatLng);
                        LatLng llCircle = converter.convert();
                        OverlayOptions ooA = new MarkerOptions().position(llCircle)
                                .icon(point).zIndex(0).draggable(false);
                        mBaiduMap.addOverlay(ooA);
                        OverlayOptions ooB = new CircleOptions().center(llCircle).radius(Integer.parseInt(mapPoint.getRoundRange())).
                                fillColor(0x10C0D0FF).stroke(new Stroke(1, 0xAA00FF00));
                        mBaiduMap.addOverlay(ooB);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
            checkPointCanGps();
        }
    }

    private boolean checkPointCanGps() {
        if (MapGPSActivity.points != null) {
            LatLng pt = new LatLng(mBaiduMap.getLocationData().latitude,
                    mBaiduMap.getLocationData().longitude);
            boolean flag = false;
            for (int i = 0; i < MapGPSActivity.points.size(); i++) {
                MapPoint mapPoint = MapGPSActivity.points.get(i);
                // 判断点pt是否在，以pCenter为中心点，radius为半径的圆内。
                LatLng sourceLatLng = new LatLng(Double.parseDouble(mapPoint
                        .getLatitude()), Double.parseDouble(mapPoint
                        .getLongitude()));
                CoordinateConverter converter = new CoordinateConverter();
                converter.from(CoordinateConverter.CoordType.COMMON);
                converter.coord(sourceLatLng);
                LatLng pCenter = converter.convert();
                boolean bool = SpatialRelationUtil
                        .isCircleContainsPoint(pCenter,
                                Integer.parseInt(mapPoint.getRoundRange()), pt);
                if (bool) {
                    flag = true;
                    inGpsPoint = mapPoint;
                    int k = (int) DistanceUtil.getDistance(pCenter, pt);
                    break;
                }
            }

            if (flag) {
                setCanGps();
                return true;
            } else {
                setCannotGps();
                return false;
            }
        } else {
            setCannotGps();
            return false;
        }
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
//            LatLng sourceLatLng = new LatLng(location.getLatitude(), location
//                    .getLongitude());
//            CoordinateConverter converter = new CoordinateConverter();
//            converter.from(CoordinateConverter.CoordType.COMMON);
//            converter.coord(sourceLatLng);
//            LatLng pCenter = converter.convert();
            MyLocationData locData = new MyLocationData.Builder().accuracy(0)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            Log.v("onReceiveLocation",
                    location.getLatitude() + ":" + location.getLongitude());
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll,
                        17.0f);
                mBaiduMap.animateMapStatus(u);
            }
//			checkPointCanGps();
            addCustomElements();
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    private void doGps() {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject object = new JSONObject();
        try {
            object.put("UserID", userId);
            object.put("Device", MFHelper.getDeviceId(context));
            object.put("DeviceType", "android");
            object.put("PlaceId", inGpsPoint.getId());

        } catch (Exception e) {
            e.printStackTrace();
            handler.sendEmptyMessage(3);
        }
        /** 打卡后的运营图片接口**/

        OkHttpUtils.postString()
            .url("http://gzt.jianye.com.cn:80/eoop-api/r/sys/appmgtrest/queryAttendancePath")
            .content("{}")
            .mediaType(JSON)
            .build()
//            .url(CommConstants.URL_ATTENDANCE).build()
            .execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e) {
                }

                @Override
                public void onResponse(String response) throws JSONException {
                    JSONObject object = new JSONObject(response);
                    if (object.optBoolean("ok")) {
                        attendance = object.optString("objValue");
                        if (!TextUtils.isEmpty(attendance)) {
                            Picasso.with(MapGPSActivity.this).load(object.optString("objValue"))
                                .fetch();
                        }
                    }
                }
            });

        JSONObject req = new JSONObject();
        try {
            req.put("secretMsg", AesUtils.getInstance().encrypt(object.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpUtils.postStringWithToken()
                .url("http://gzt.jianye.com.cn:80/eoop-api/r/sys/punchcard")
//                .url(CommConstants.URL_PUNCHCARD)
                .content(req.toString())
                .mediaType(JSON)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        handler.sendEmptyMessage(3);
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        JsonObject jsonObject = new JsonParser().parse(response.replace("#", "\\"))
                                .getAsJsonObject();
                        boolean ok = jsonObject.get("ok").getAsBoolean();
                        if (ok) {
                            Map<String, String> data = new HashMap<>();
                            String time = jsonObject.get("objValue").getAsString();
                            String title = jsonObject.get("value").getAsString();
                            data.put("time", time);
                            data.put("title", title);
                            handler.obtainMessage(2, data).sendToTarget();
                        } else {
                            String message = jsonObject.get("value").getAsString();
                            Message message1 = handler.obtainMessage();
                            message1.what = 5;
                            message1.obj = message;
                            handler.sendMessage(message1);
                        }
                    }
                });
    }

    Dialog dialog;

    public void showDialog(String time, boolean success, String title) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        View dialogView = null;
        LinearLayout layout = null;
        if (success) {
            dialogView = LayoutInflater.from(context).inflate(
                    R.layout.dialog_gps_success, null);
            layout = (LinearLayout) dialogView
                    .findViewById(R.id.dialog_gps_success_view);
            TextView timeView = (TextView) dialogView
                    .findViewById(R.id.gps_success_time);
            TextView go = (TextView) dialogView
                    .findViewById(R.id.gps_success_go);
            timeView.setText(time);
            if (CheckUtil.isNullorEmpty(title)){
              go.setVisibility(View.GONE);
            }else {
              go.setVisibility(View.VISIBLE);
              go.setText(title);
            }
        } else {
            dialogView = LayoutInflater.from(context).inflate(
                    R.layout.dialog_gps_failed, null);
            layout = (LinearLayout) dialogView
                    .findViewById(R.id.dialog_gps_failed_view);
            TextView txt_message = (TextView) dialogView
                    .findViewById(R.id.txt_message);
            TextView go = (TextView) dialogView
                    .findViewById(R.id.txt_message_go);
            go.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(time)) {
                txt_message.setText(time);
            }
        }
        layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog = new Dialog(context, R.style.ImageloadingDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        dialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!TextUtils.isEmpty(attendance)) {
                    Intent intent = new Intent(MapGPSActivity.this, ActionActivity.class);
                    intent.putExtra("data", attendance);
                    startActivity(intent);
                    dialog.dismiss();
                }
            }
        });

    }

    public void showGPSDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        View dialogView = null;
        dialogView = LayoutInflater.from(context).inflate(
                R.layout.dialog_show_gps, null);
        TextView gpsView = (TextView) dialogView
                .findViewById(R.id.push_gps_btn);
        TextView okView = (TextView) dialogView.findViewById(R.id.push_ok_btn);

        gpsView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        okView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog = new Dialog(context, R.style.ImageloadingDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        if (mLocClient != null) {
            mLocClient.start();
        }
        if (!ActivityUtils.isGPSOpen(context)) {
            showGPSDialog();
            setCannotGps();
        }
    }

    @Override
    public void onPause() {
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        if (mLocClient != null && mLocClient.isStarted()) {
            mLocClient.stop();
            setCannotGps();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // 退出时销毁定位
        if (mLocClient != null) {
            mLocClient.stop();
        }
        // 关闭定位图层
        if (mBaiduMap != null) {
            mBaiduMap.setMyLocationEnabled(false);
        }
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mMapView = null;
        // 回收 bitmap 资源
        point.recycle();
        super.onDestroy();
    }

    /**
     * 获取当前人 三个月的打卡记录
     */
    private void getGpsLogFromSever() {
        JSONObject obj = new JSONObject();
        JSONObject rq = new JSONObject();
        try {
            obj.put("userId", MFSPHelper.getString(CommConstants.USERID));
            rq.put("secretMsg", AesUtils.getInstance().encrypt(obj.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpManager.postJsonWithToken(CommConstants.URL_GET_PUNSH_RECORD, rq.toString(), new StringCallback() {

                    @Override
                    public void onError(Call call, Exception e) {
                        handler.sendEmptyMessage(7);
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        JsonObject jsonObject = new JsonParser().parse(response)
                                .getAsJsonObject();
                        int code = jsonObject.get("ResponseCode").getAsInt();
                        if (code == 1) {
                            JsonArray array = jsonObject.get("List")
                                    .getAsJsonArray();
                            ArrayList<String> logList = new ArrayList<>();
                            for (int i = 0; i < array.size(); i++) {
                                JsonObject object = array.get(i).getAsJsonObject();
                                String logTime = object.get("punshTime")
                                        .getAsString();
                                logList.add(logTime);
                            }
                            handler.obtainMessage(6, logList).sendToTarget();
                        } else {
                            handler.sendEmptyMessage(7);
                        }
                    }
                });
    }

}
