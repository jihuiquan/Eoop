package com.jianye.smart.module.home.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import com.jianye.smart.R;

public class KKWebViewFragemnt extends Fragment {

  private WebView webView;

  @Override
  public View onCreateView(LayoutInflater inflater,
      @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.eop_fragment_kk, null, false);
    webView = (WebView) view.findViewById(R.id.eop_fragment_kk);
    webView.setDownloadListener(new DownloadListener() {
      @Override
      public void onDownloadStart(final String url, String userAgent, String contentDisposition,
          String mimetype, long contentLength) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
      }
    });
    webView.getSettings().setUseWideViewPort(true);
    webView.getSettings().setLoadWithOverviewMode(true);
    webView.getSettings().setJavaScriptEnabled(true);
    return view;
  }

  @Override
  public void onResume() {
    webView.loadUrl("http://gzt.jianye.com.cn:80/eoop/wwwPhone/bowserHref/kk.html");
    super.onResume();
  }
}
