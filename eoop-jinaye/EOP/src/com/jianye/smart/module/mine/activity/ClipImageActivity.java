package com.jianye.smart.module.mine.activity;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.sc.view.clipview.ClipImageView;
import com.jianye.smart.R;
import com.jianye.smart.base.BaseActivity;
import com.movit.platform.framework.utils.PicUtils;

public class ClipImageActivity extends BaseActivity {
	private ClipImageView imageView;
	private TextView topRight;
	private TextView topTitle;
	private ImageView topLeft;

	AQuery aQuery;
	String path;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clipimage);
		aQuery = new AQuery(this);

		path = getIntent().getStringExtra("takePicturePath");
		imageView = (ClipImageView) findViewById(R.id.src_pic);
		topRight = (TextView) findViewById(R.id.common_top_img_right);
		topTitle = (TextView) findViewById(R.id.tv_common_top_title);
		topLeft = (ImageView) findViewById(R.id.common_top_img_left);
		// 设置需要裁剪的图片
		BitmapAjaxCallback callback = new BitmapAjaxCallback();
		callback.rotate(true);
		Log.v("takePicturePath", path);
		aQuery.id(imageView).image(new File(path), true, 1000, callback);
		
		topTitle.setText("");
		topLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});
		topRight.setText("完成");
		topRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 此处获取剪裁后的bitmap
				Bitmap bitmap = imageView.clip();
				String tempPath  = PicUtils.compressImageAndSave(path, bitmap, 300);

				Intent intent = new Intent();
				intent.putExtra("takePicturePath", tempPath);
				setResult(3, intent);
				finish();
			}
		});
	}

}
