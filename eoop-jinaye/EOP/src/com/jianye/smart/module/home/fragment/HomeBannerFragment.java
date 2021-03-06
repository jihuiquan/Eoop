package com.jianye.smart.module.home.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.jianye.smart.R;
import com.jianye.smart.activity.MainActivity;
import com.jianye.smart.module.qrcode.MyCodeActivity;
import com.jianye.smart.module.workbench.activity.WebViewActivity;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.okhttp.utils.AesUtils;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.Callback;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.movit.platform.framework.utils.MD5Utils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.im.manager.MessageManager;
import com.movit.platform.im.module.single.activity.ChatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Response;

@SuppressLint("ValidFragment")
public class HomeBannerFragment extends Fragment {

    private static final int CLICK_ON_WEBVIEW = 1;
    private static final int CLICK_ON_URL = 2;

    private String url;
    private WebView webview;
    private ProgressBar prg;

    public HomeBannerFragment(String url) {
        this.url = url;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.eop_fragment_home_banner_web, null, false);
        webview = (WebView) view.findViewById(R.id.eop_fragment_home_item_web);
        prg = (ProgressBar) view.findViewById(R.id.eop_fragment_home_item_prg);
        WebSettings settings = webview.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setSaveFormData(false);
        settings.setSupportZoom(false);
        settings.setDomStorageEnabled(true);
        webview.setWebViewClient(new WebViewClient());
        final String cookieUrl = "http://61.136.122.245:8075/WebReport/ReportServer?op=fs_load&cmd=sso&fr_username=" + CommConstants.loginConfig.getmUserInfo().getEmpAdname()
                + "&fr_password=" + CommConstants.loginConfig.getPassword() + "&fr_remember=true";
        JSONObject object = new JSONObject();
        try {
            object.put("userName", MFSPHelper.getString(CommConstants.USERNAME).toUpperCase());
            object.put("divisionUrl", url);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        com.alibaba.fastjson.JSONObject secretMsg = new com.alibaba.fastjson.JSONObject();
        secretMsg.put("secretMsg", AesUtils.getInstance().encrypt(object.toString()));

        HttpManager.postJson("https://gzt.jianye.com.cn:20799/eoop-api/r/token/getDivisionTokenUrl",
                secretMsg.toString(), new StringCallback() {


                    @Override
                    public void onError(okhttp3.Call call, Exception e) {
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        Log.d(TAG, "onResponse() called with: response = [" + response + "]");
                        JSONObject object = new JSONObject(response);
                        if (object.optBoolean("ok")) {
                            final String value = object.getString("value");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    syncCookie(value, new SharedPreUtils(getActivity()).getString(cookieUrl));
                                    webview.loadUrl(value);
                                }
                            });

                        }
                    }
                });

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                prg.setProgress(newProgress);
                if (newProgress >= 95) {
                    webview.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (!TextUtils.isEmpty(consoleMessage.message()) && !TextUtils.isEmpty(new SharedPreUtils(getActivity()).getString(cookieUrl))) {
                    JSONObject msg = null;
                    try {
                        msg = new JSONObject(consoleMessage.message());
                        String url = msg.optString("url");
                        String title = msg.optString("title");
                        Intent intent = new Intent(getActivity(), WebViewActivity.class);
                        intent.putExtra("URL", url);
                        intent.putExtra("cookie", new SharedPreUtils(getActivity()).getString(cookieUrl));
                        intent.putExtra("title", title);
                        startActivity(intent);
                    } catch (JSONException e) {
                    }
                }
                return super.onConsoleMessage(consoleMessage);
            }
        });
        return view;
    }

    private static final String TAG = "HomeBannerFragment";

    /**
     * 将cookie同步到WebView
     *
     * @param url    WebView要加载的url
     * @param cookie 要同步的cookie
     * @return true 同步cookie成功，false同步cookie失败
     */
    public boolean syncCookie(String url, String cookie) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(getActivity());
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(url, cookie);//如果没有特殊需求，这里只需要将session id以"key=value"形式作为cookie即可
        String newCookie = cookieManager.getCookie(url);
        return TextUtils.isEmpty(newCookie);
    }
}
