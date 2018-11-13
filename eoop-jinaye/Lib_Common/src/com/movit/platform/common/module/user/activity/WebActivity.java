package com.movit.platform.common.module.user.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.movit.platform.common.R;

/**
 * @ClassName: WebActivity
 * @Description:
 * @Author: chao
 * @Data 2017-08-22 18:38
 */

public class WebActivity extends Activity implements OnClickListener {

  private WebView mWebView;
  private TextView topTitle, topClose;
  private ImageView topLeft;
  private ProgressBar progressBar;
  private static String WEB_URL = "url";
  private static String WEB_TITLE = "title";

  public static Intent newIntent(Context context, String title, String url) {
    Intent intent = new Intent(context, WebActivity.class);
    intent.putExtra(WEB_TITLE, title);
    intent.putExtra(WEB_URL, url);
    return intent;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_resume_web);

    progressBar = (ProgressBar) findViewById(R.id.progressBar);
    mWebView = (WebView) findViewById(R.id.webview);
    topTitle = (TextView) findViewById(R.id.tv_common_top_title);
    topLeft = (ImageView) findViewById(R.id.common_top_img_left);
    topClose = (TextView) findViewById(R.id.common_top_close);
    topTitle.setText(getIntent().getStringExtra(WEB_TITLE));
    topLeft.setOnClickListener(this);
    topClose.setOnClickListener(this);
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebView.setWebChromeClient(new WebChromeClient() {
      @Override
      public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        progressBar.setProgress(newProgress);
        if (newProgress == 100) {
          progressBar.setVisibility(View.INVISIBLE);
        }
      }
    });
    mWebView.setWebViewClient(new WebViewClient());
    mWebView.loadUrl(getIntent().getStringExtra(WEB_URL));
  }

  @Override
  public void onClick(View v) {
    finish();
  }
}
