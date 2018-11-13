package com.jianye.smart.module.qrcode;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.jianye.smart.R;
import com.jianye.smart.base.BaseActivity;

public class TwoDimensionalCodeActivity extends BaseActivity {

	WebView web;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_two_dimensional_code);
		ImageView back = (ImageView) findViewById(R.id.common_top_img_left);
		TextView title = (TextView) findViewById(R.id.tv_common_top_title);
		web = (WebView) findViewById(R.id.download_code_web);
		title.setText("智慧建业");
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		WebSettings settings = web.getSettings();
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		settings.setJavaScriptEnabled(true);
		settings.setSaveFormData(false);
		settings.setSupportZoom(false);
		settings.setDomStorageEnabled(true);
		web.setWebViewClient(new WebViewClient());
		web.loadUrl("https://gzt.jianye.com.cn:20799/app/download/echat.html");
	}

}
