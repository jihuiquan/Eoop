package com.movit.platform.sc.module.imagesbucket.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.LogUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.sc.R;
import com.movit.platform.sc.module.imagesbucket.adapter.ImageBucketAdapter;
import com.movit.platform.sc.module.imagesbucket.entities.ImageBucket;
import com.movit.platform.sc.module.imagesbucket.helper.AlbumHelper;
import com.movit.platform.sc.module.zone.activity.ZonePublishActivity;

import java.io.File;
import java.util.Date;
import java.util.List;

public class PicBucketActivity extends Activity {
    ImageView topLeft;
    ImageView topRight;
    TextView topTitle;
    List<ImageBucket> dataList;
    GridView gridView;
    ImageBucketAdapter adapter;// 自定义的适配器
    AlbumHelper helper;
    public static final String EXTRA_IMAGE_LIST = "imagelist";
    public static final String EXTRA_IMAGE_LIST_POSTION = "imagelist_postion";

    public Uri imageUri;// The Uri to store the big
    String currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
        setContentView(R.layout.sc_activity_pic_bucket);
        helper = AlbumHelper.getHelper();
        helper.init(getApplicationContext());

        if (savedInstanceState != null) {
            dataList = helper.getImagesBucketList(true);
        } else {
            initData();
        }
        initView();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        dataList = helper.getImagesBucketList(true);
    }

    /**
     * 初始化view视图
     */
    private void initView() {
        topLeft = (ImageView) findViewById(R.id.common_top_left);
        topRight = (ImageView) findViewById(R.id.common_top_right);
        topTitle = (TextView) findViewById(R.id.tv_common_top_title);
        topTitle.setText("图片");
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        topRight.setVisibility(View.GONE);
        gridView = (GridView) findViewById(R.id.image_bucket_gridview);

        SharedPreUtils spUtil = new SharedPreUtils(this);
        if (!"default".equals(spUtil.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }

        adapter = new ImageBucketAdapter(PicBucketActivity.this, dataList);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == 0) {// 拍照
                    System.gc();
                    // 跳转相机拍照
                    if (ZonePublishActivity.selectImagesList.size() >= PicGridPickedActivity.CHOOSE_PICS_COUNT) {
                        Toast.makeText(PicBucketActivity.this, "您最多可上传9张图片",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    String sdStatus = Environment.getExternalStorageState();
                    if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(PicBucketActivity.this, "找不到sd卡", Toast.LENGTH_LONG)
                                .show();
                        return;
                    }

                    currentTime = DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
                    imageUri = Uri.parse(CommConstants.IMAGE_FILE_LOCATION);

                    if (ContextCompat.checkSelfPermission(PicBucketActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        //申请 CAMERA 权限
                        ActivityCompat.requestPermissions(PicBucketActivity.this, new String[]{Manifest.permission.CAMERA},
                                CommConstants.CAMERA_REQUEST_CODE);
                    }else{
                        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        intent2.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                        startActivityForResult(intent2, 2);
                    }

                } else {
                    Intent intent = new Intent(PicBucketActivity.this,
                            PicGridPickedActivity.class);
                    // intent.putExtra(PicBucketActivity.EXTRA_IMAGE_LIST,
                    // (Serializable) dataList.get(position - 1).imageList);
                    intent.putExtra(PicBucketActivity.EXTRA_IMAGE_LIST_POSTION,
                            position - 1);
                    startActivityForResult(intent, 1);
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == CommConstants.CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                intent2.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                startActivityForResult(intent2, 2);
            } else {
                // Permission Denied
                Toast.makeText(this,"访问相机权限未获得您授权，无法使用拍照功能。",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == 1) {
                    setResult(1);
                    finish();
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    if (imageUri != null) {
                        imageUri = null;
                        LogUtils.v("currentTime", currentTime);
                        boolean copy = FileUtils.copyFile(CommConstants.SD_CARD
                                + "/temp.jpg", CommConstants.SD_CARD_IMPICTURES
                                + currentTime + ".jpg");
                        new File(CommConstants.SD_CARD + "/temp.jpg").delete();
                        if (copy) {
                            ZonePublishActivity.selectImagesList
                                    .remove(CommConstants.SD_CARD_IMPICTURES
                                            + currentTime + ".jpg");
                            ZonePublishActivity.selectImagesList
                                    .add(CommConstants.SD_CARD_IMPICTURES + currentTime
                                            + ".jpg");
                            PicUtils.scanImages(PicBucketActivity.this,
                                    CommConstants.SD_CARD_IMPICTURES + currentTime
                                            + ".jpg");
                            setResult(1);
                            finish();
                        }
                    }
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        BitmapAjaxCallback.clearCache();
        System.gc();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentTime", currentTime);
        LogUtils.v("PicBucket", "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (TextUtils.isEmpty(currentTime)) {
            currentTime = savedInstanceState.getString("currentTime");
        }
        LogUtils.v("PicBucket", "onRestoreInstanceState");
    }

}
