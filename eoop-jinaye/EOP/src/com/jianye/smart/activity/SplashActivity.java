package com.jianye.smart.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.view.pageIndicator.MyPageIndicator;
import com.jianye.smart.R;
import com.jianye.smart.application.EOPApplication;
import com.jianye.smart.base.CompanyInfoable;
import com.jianye.smart.module.qrcode.InputCodeActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends Activity {
    public TextView text;
    public boolean isShowTour = false;
    SharedPreUtils spUtil;

    List<Integer> imageIds1 = new ArrayList<Integer>();
    List<Integer> imageIds2 = new ArrayList<Integer>();
    List<View> mListViews = new ArrayList<View>();
    ViewPager viewPager;
    MyPageIndicator mIndicator;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    mHandler.postDelayed(gotoLoginAct, 2000);
                    break;
                case 2:
                    String skinType = (String) msg.obj;
                    spUtil.setString(BaseApplication.SKINTYPE, skinType);
                    mHandler.postDelayed(gotoLoginAct, 500);
                    break;
                case 5:
                    startActivity(new Intent(SplashActivity.this,
                            LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    spUtil.setBoolean(CommConstants.IS_SHOW_TOUR, false);
                    try {
                        spUtil.setInteger(
                                CommConstants.ORIGINAL_VERSION,
                                getPackageManager().getPackageInfo(
                                        getPackageName(),
                                        PackageManager.GET_META_DATA).versionCode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    SplashActivity.this.finish();
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        CommConstants.IS_RUNNING = true;
        new FileUtils();// 创建目录
        spUtil = new SharedPreUtils(this);
        isShowTour = spUtil.getBoolean(CommConstants.IS_SHOW_TOUR, true);
        if (!isShowTour) {
            try {
                int nowVersion = getPackageManager().getPackageInfo(
                        getPackageName(), PackageManager.GET_META_DATA).versionCode;
                int originalVersion = spUtil
                        .getInteger(CommConstants.ORIGINAL_VERSION);
                if (nowVersion > originalVersion) {
                    isShowTour = true;
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        setContentView(R.layout.activity_splash);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE},
                    CommConstants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        } else {
            doNext();
        }
    }

    private void doNext() {
        EOPApplication.getInstance().getPhoneInfo();
        imageIds1.add(R.drawable.frame_1_1);
        imageIds1.add(R.drawable.frame_2_1);
        imageIds1.add(R.drawable.frame_3_1);
        imageIds1.add(R.drawable.frame_5_1);
        imageIds1.add(R.drawable.frame_6_1);

        imageIds2.add(R.drawable.frame_1_2);
        imageIds2.add(R.drawable.frame_2_2);
        imageIds2.add(R.drawable.frame_3_2);
        imageIds2.add(R.drawable.frame_5_2);
        imageIds2.add(R.drawable.frame_6_2);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        mIndicator = (MyPageIndicator) findViewById(R.id.indicator);

        for (int i = 0; i < imageIds1.size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(
                    R.layout.dialog_welcome_viewpager_item, null);
            mListViews.add(view);
        }
        if ("".equals(spUtil.getString(BaseApplication.SKINTYPE))) {
            spUtil.setString(BaseApplication.SKINTYPE, "default");
        }
        loadSkin();
    }

    Runnable gotoLoginAct = new Runnable() {

        @Override
        public void run() {
            if (isShowTour) {
                // TourDialogView tourDialogView = new TourDialogView(
                // SplashActivity.this);
                // tourDialogView.showDialog();
                FrameViewsAdapter topNewsAdapter = new FrameViewsAdapter(mListViews);
                viewPager.setAdapter(topNewsAdapter);
                mIndicator.setViewPager(viewPager);
            } else {
                String ip = spUtil.getString("ip");
                if (StringUtils.empty(ip)) {
                    startActivity(new Intent(SplashActivity.this,
                            InputCodeActivity.class));
                    finish();
                } else {
                    new Thread(new CompanyInfoable(SplashActivity.this,
                            mHandler)).start();
                }

            }
        }
    };



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CommConstants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                doNext();
            } else {
                // Permission Denied
                Toast.makeText(this, "APP未获得您的授权，无法登录。", Toast.LENGTH_LONG).show();
            }
        }

    }

    class FrameViewsAdapter extends PagerAdapter {
        private List<View> mListViews;

        public FrameViewsAdapter(List<View> mListViews) {
            super();
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));// 删除页卡
        }

        @Override
        public int getCount() {
            return mListViews.size();// 返回页卡的数量
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;// 官方提示这样写
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position), 0);// 添加页卡
            View view = (View) mListViews.get(position);
            ImageView imageView1 = (ImageView) view
                    .findViewById(R.id.view_pager_img1);
            ImageView imageView2 = (ImageView) view
                    .findViewById(R.id.view_pager_img2);

            Button button = (Button) view.findViewById(R.id.view_pager_btn);
            imageView1.setImageResource(imageIds1.get(position));
            imageView2.setImageResource(imageIds2.get(position));

            if (position == mListViews.size() - 1) {
                button.setVisibility(View.VISIBLE);
                button.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                String ip = spUtil.getString("ip");
                                if (StringUtils.empty(ip)) {
                                    startActivity(new Intent(
                                            SplashActivity.this,
                                            InputCodeActivity.class));
                                    SplashActivity.this.finish();
                                } else {
                                    new Thread(new CompanyInfoable(
                                            SplashActivity.this, mHandler))
                                            .start();
                                }
                            }
                        }, 50);

                    }
                });
            }
            return mListViews.get(position);
        }
    }


    public void loadSkin() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                File skinDir = null;
//                try {
//                    String result = HttpClientUtils.post("http://eoopapppub.movitechcn.com:8080/eoopapp/skin/android/"
//                        + CommConstants.HOST_TYPE + "/SkinVersion.json","{}", Charset.forName("UTF-8"));
//                    JSONObject object = new JSONObject(result);
//                    String skinType = object.getString("skinType");
//                    String url = object.getString("url");
//                    String fileName = object.getString("skinFileName");
//                    BaseApplication.TOP_COLOR = object.getString("topColor");
//                    if ("default".equals(skinType)) {
//                        spUtil.setString(BaseApplication.SKINTYPE, "default");
//                        mHandler.sendEmptyMessage(1);
//                    } else {
//                        File dir = getDir("theme", Context.MODE_PRIVATE);
//                        skinDir = new File(dir, skinType);
//                        if (!skinDir.exists()) {
//                            //下载皮肤资源包
//                            File zipFile = null;
//                            FileUtils fileUtils = new FileUtils();
//                            int k = fileUtils.downfile(mHandler, url,
//                                    CommConstants.SD_DOWNLOAD, fileName);
//                            if (k == 0 || k == 1) {// 或者已存在
//                                // 解压缩
//                                zipFile = new File(CommConstants.SD_DOWNLOAD,
//                                        fileName);
//                                ZipUtils.upZipFilePro(zipFile, skinDir.getAbsolutePath());
//                                LogUtils.v("loadSkin", "解压成功");
//                            } else if (k == -1) {// 失败
//                                LogUtils.v("loadSkin", "下载失败");
//                                mHandler.sendEmptyMessage(1);
//                            }
//                        }
//                        mHandler.obtainMessage(2, skinType).sendToTarget();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    mHandler.sendEmptyMessage(1);
//                } catch (ZipException e) {
//                    e.printStackTrace();
//                    if (skinDir != null) {
//                        deleteFile(skinDir);
//                    }
//                    mHandler.sendEmptyMessage(1);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    if (skinDir != null) {
//                        deleteFile(skinDir);
//                    }
//                    mHandler.sendEmptyMessage(1);
//                }
//            }
//        }).start();
        spUtil.setString(BaseApplication.SKINTYPE, "default");
        mHandler.sendEmptyMessage(1);
    }

    /**
     * 2
     * 删除文件夹所有内容
     * 3
     * <p>
     * 4
     */
    public void deleteFile(File file) {
        if (file.exists()) { // 判断文件是否存在
            if (file.isFile()) { // 判断是否是文件
                file.delete(); // delete()方法 你应该知道 是删除的意思;
            } else if (file.isDirectory()) { // 否则如果它是一个目录
                File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
                for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
                    this.deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
                }
            }
            file.delete();
        }
    }

}
