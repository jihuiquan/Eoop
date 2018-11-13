package com.jianye.smart.module.workbench.activity;

import static com.jianye.smart.module.workbench.manager.WorkTableClickDelagate.JIANYE_MYERP_SHENPI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jianye.smart.R;
import com.jianye.smart.application.EOPApplication;
import com.jianye.smart.base.BaseActivity;
import com.jianye.smart.module.qrcode.MyCodeActivity;
import com.jianye.smart.utils.DESUtils;
import com.jianye.smart.utils.Obj2JsonUtils;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.DownloadFiles;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.view.CusDialog;
import com.movit.platform.framework.view.widget.SelectPicPopup;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.manager.GroupManager;
import com.movit.platform.im.module.group.activity.GroupChatActivity;
import com.movit.platform.im.module.group.entities.Group;
import com.movit.platform.im.module.single.activity.ChatActivity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class WebViewActivity extends BaseActivity implements OnCheckedChangeListener {
    private WebView mWebView;
    private WebView mWebView0;
    CusDialog dialogUtil;
    TextView topTitle, topClose;
    ImageView topLeft;
    ImageView topRight;

    private ArrayList<UserInfo> atAllUserInfos = new ArrayList<UserInfo>();
    private ArrayList<OrganizationTree> atOrgunitionLists = new ArrayList<OrganizationTree>();

    ValueCallback<Uri> mUploadMessage;
    public final static int FILECHOOSER_RESULTCODE = 22;
    public final static int CAMERA_RESULTCODE = 23;
    public final static int PHOTO_RESULTCODE = 24;
    String mCameraFilePath;
    String moduleId;
    private RelativeLayout toplayout;

    private String uploadAcctachUrl;
    private FrameLayout todoRdf;
    private RadioGroup todoRdg;
    private RadioButton todoRd;
    private RadioButton mingyuanRd;


    private ProgressDialog mProgressDialogBeforeShowPercent;
    private final int ATTACHMENT_DOWNLOAD_END = 0;
    private final int ADJUST_LAYOUT_FOR_ATTACHMENT = 1;
    public static final int ERROR_DOWNLOAD_ATTACHMENT = 2;
    private final int SHOW_MY_PROGRESS = 3;
    private final int CLOSE_MPROGRESS_DIALOG_BEFORE_SHOW_PERCENT = 4;
    private final int SHOW_DIALOG_BEFORE_DOWNLOAD = 5;
    private final int SHOW_DIALOG_BEFORE_APKLOAD = 6;

    private static boolean ATTACHMENT_FLAG = false;
    private long exitTime = 0;
    private String fileLink;
    private String qqmailType;
    private String qqFileName;
    private String goutongType;
    private String gtFileName;
    private String h5Cookie;
    private ProgressBar pb = null;

    private ProgressBar progressBar;
    // 自定义的弹出框类
    SelectPicPopup popWindow;
    // 用来标识请求照相功能的activity

    String currentTime;
    Uri imageUri;// The Uri to store the big
    String takePicturePath = "";


    //only for android 5.0
    private ValueCallback<Uri[]> mFilePathCallback;
    //private ValueCallback<Uri> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE_FOR_INPUT_FILE = 26;
    private String mCameraPhotoPath;

    int progress = 0;
    private Handler myHandler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (progress < 100) {
                progress = progress + 5;
            }
            progressBar.setProgress(progress);
            myHandler.postDelayed(this, 50);// 50是延时时长
        }
    };

    private Handler mHandler = new Handler() {

        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case ATTACHMENT_DOWNLOAD_END:
                    // pb.setVisibility(View.GONE);
                    // Toast.makeText(ShimActivity.this, "download complete!",
                    // FileUtils.TOAST_SHOW_TIME).show();
                    mProgressDialogBeforeShowPercent.dismiss();
                    choseThirdPartySoftwareToOpenAttachment();
                    break;
                case ADJUST_LAYOUT_FOR_ATTACHMENT:
                    AdjustLayoutForAttachment();
                    break;
                case ERROR_DOWNLOAD_ATTACHMENT:
                    Toast toast = Toast.makeText(WebViewActivity.this, "网络连接异常。",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    break;
                case SHOW_MY_PROGRESS:
                    // mProgressDialogBeforeShowPercent.dismiss();
                    // pb.setVisibility(View.VISIBLE);
                    // pb.setMax(fileSize);
                    // pb.setProgress(1);
                    break;
                // 条件不满足时dismiss 该进度条.
                case CLOSE_MPROGRESS_DIALOG_BEFORE_SHOW_PERCENT:
                    mProgressDialogBeforeShowPercent.dismiss();
                    break;
                case SHOW_DIALOG_BEFORE_DOWNLOAD:
                    mProgressDialogBeforeShowPercent = ProgressDialog.show(
                            WebViewActivity.this, "", "正在加载附件...", true, true);
                    break;
                case SHOW_DIALOG_BEFORE_APKLOAD:
                    mProgressDialogBeforeShowPercent = ProgressDialog.show(
                            WebViewActivity.this, "", "正在下载安装程序...", true, true);
                    break;
                default:
                    pb.setProgress(msg.what);
                    break;
            }
        }

    };
    private boolean mIsPageLoading;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        pb = new ProgressBar(this);
        pb.setVisibility(View.GONE);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        todoRdf = (FrameLayout) findViewById(R.id.web_actions_tabs_fl);
        todoRdg = (RadioGroup) findViewById(R.id.web_actions_tabs);
        todoRd = (RadioButton) findViewById(R.id.web_actions_tabs_todo);
        mingyuanRd = (RadioButton) findViewById(R.id.web_actions_tabs_mingyuan);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView0 = (WebView) findViewById(R.id.webview0);
        toplayout = (RelativeLayout) findViewById(R.id.common_top_layout);
        topTitle = (TextView) findViewById(R.id.tv_common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_img_left);
        topRight = (ImageView) findViewById(R.id.common_top_img_right);
        topClose = (TextView) findViewById(R.id.common_top_close);
        topRight.setImageResource(R.drawable.ico_chat);
        todoRdg.setOnCheckedChangeListener(this);
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    if ("TimeSheet".equals(topTitle.getText())
                            || "我的考勤".equals(topTitle.getText())
                            || "任务日程".equals(topTitle.getText())
                            || "吾悦广场".equals(topTitle.getText())) {
                        onBackPressed();
                    } else if (mWebView.getUrl().contains("eoop/newsDetail")) {
                        onBackPressed();
                    } else {
                        if (mWebView.canGoBack()) {
                            mWebView.goBack();
                        } else {
                            onBackPressed();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onBackPressed();
                }
            }
        });

        topRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                UserDao dao = UserDao.getInstance(context);
                UserInfo userInfo = dao.getUserInfoByADName("karolina.tian");
                dao.closeDb();
                if (userInfo != null) {
                    startActivity(new Intent(WebViewActivity.this,
                            ChatActivity.class).putExtra("userInfo", userInfo));
                }
            }
        });

        topClose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (StringUtils.notEmpty(moduleId)) {
                    addUserActionLog(moduleId);
                }
                onBackPressed();
            }
        });
        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");
        moduleId = intent.getStringExtra("moduleId");
        boolean sao_sao = intent.getBooleanExtra("sao-sao", false);
        boolean goChat = intent.getBooleanExtra("goChat", false);
        boolean localHtml = intent.getBooleanExtra("local_html", false);
        final boolean bpm = intent.getBooleanExtra("BPM", false);
        boolean futureland = intent.getBooleanExtra("FutureLand", false);
        boolean WebViewFuncion = intent.getBooleanExtra("WebViewFuncion", false);
        final String webTitle = intent.getStringExtra("title");
        if (futureland) {
            toplayout.setVisibility(View.GONE);
        }
        if (goChat) {
            topRight.setVisibility(View.VISIBLE);
        } else {
            topRight.setVisibility(View.GONE);
        }
        try {
            boolean gohr = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getBoolean("CHANNEL_GOHR", false);
            if (gohr) {
                if (spUtil.getString(CommConstants.EMPADNAME).equalsIgnoreCase(
                        "karolina.tian")) {
                    topRight.setVisibility(View.GONE);
                }
            } else {
                topRight.setVisibility(View.GONE);
            }
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }

        if (sao_sao) {
            topTitle.setText("扫描结果");
        } else {
            topTitle.setText("正在加载...");
        }

        if (bpm) {
            topTitle.setText("建业BPM");
        }

        initWebSetting(mWebView);
        initWebSetting(mWebView0);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        Log.d("maomao", "densityDpi = " + mDensity);
        if (mDensity == 240) {
            mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        } else if (mDensity == 160) {
            mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        } else if (mDensity == 120) {
            mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
        } else if (mDensity == DisplayMetrics.DENSITY_XHIGH) {
            mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        } else if (mDensity == DisplayMetrics.DENSITY_TV) {
            mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        } else {
            mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        }
        // mWebView.getSettings().setRenderPriority(RenderPriority.HIGH);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (WebViewFuncion) {
            mWebView.addJavascriptInterface(new WebViewFuncion(), "webview");
        } else {
            mWebView.addJavascriptInterface(new WebViewFuncion(), "eoopWebView");
        }

        mWebView.clearCache(true);

        String cookie = getIntent().getStringExtra("cookie");
        syncCookie(url, cookie);

        if (localHtml) {
            mWebView.loadUrl(url);
        } else {
            if (url.startsWith("http:")) {
                mWebView.loadUrl(url);
            } else {
//				mWebView.loadData(url, "text/html", "UTF-8");
                mWebView.loadDataWithBaseURL("about:blank", url, "text/html",
                        "utf-8", null);
            }
        }

        initWBClient(mWebView);
        initWBClient(mWebView0);

        WebChromeClient wvcc = new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                try {
                    if (bpm) {
                        topTitle.setText("建业BPM");
                    } else if (!TextUtils.isEmpty(webTitle)) {
                        topTitle.setText(webTitle);
                    } else {
                        topTitle.setText(title);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            // For Android > 4.1.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType) {
                if (mUploadMessage != null)
                    return;
                mUploadMessage = uploadMsg;
                startActivityForResult(createDefaultOpenableIntent(acceptType),
                        FILECHOOSER_RESULTCODE);
            }

            // android 5.0
            @SuppressLint("NewApi")
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
                if (mFilePathCallback != null) {
                    // mFilePathCallback.onReceiveValue(null);
                    mFilePathCallback = null;
                }
                mFilePathCallback = filePathCallback;

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        //设置MediaStore.EXTRA_OUTPUT路径,相机拍照写入的全路径
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (Exception ex) {
                        // Error occurred while creating the File
                        Log.e("WebViewSetting", "Unable to create Image File", ex);
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        System.out.println(mCameraPhotoPath);
                    } else {
                        takePictureIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*");
                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                    System.out.println(takePictureIntent);
                } else {
                    intentArray = new Intent[0];
                }
                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                try {
                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE_FOR_INPUT_FILE);
                    return true;
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context.getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
            }

        };
        mWebView.setWebChromeClient(wvcc);

        mWebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                if (url.contains("m.exmail.qmail.com/cgi-bin/download?")
                    && !StringUtils.empty(mimetype)) {
                    qqmailType = mimetype;
                    qqFileName =  contentDisposition.replaceAll("\"", "").split("=")[1];
                    openAttachmentWebView(url,"", "");
                }else if (url.contains("zh.jianye.com.cn/sys/attachment/sys_att_main/sysAttMain.do?method=download&")){
                    goutongType = mimetype;
                    gtFileName =  contentDisposition.replaceAll("\"", "").split("=")[1];
                    openAttachmentWebView(url, "", "");
                }else {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });

        String cookies = intent.getStringExtra("cookies");
        if (!TextUtils.isEmpty(cookies)) {
            CookieSyncManager.createInstance(WebViewActivity.this);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            try {
                URL aURL = new URL(url);
                String domain = aURL.getHost();
                String path = aURL.getPath();
                HashMap<String, String> map = new Gson().fromJson(cookies, new TypeToken<HashMap<String,
                        String>>() {
                }.getType());
                for (String key : map.keySet()) {
                    String cookieValue = "";
                    String value = map.get(key);
                    cookieValue += key + "=" + value + ";";
                    cookieValue += "domain=" + domain + ";" + "path=" + path;
                    cookieManager.setCookie(url, cookieValue);
                }
                CookieSyncManager.getInstance().sync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if ("jianye_dbsx".equals(getIntent().getStringExtra("jianye_dbsx"))
            && !TextUtils.isEmpty(JIANYE_MYERP_SHENPI)) {
            todoRdf.setVisibility(View.GONE);
            loadingMingyuan();
        }

        String headers = intent.getStringExtra("headers");
        if (!TextUtils.isEmpty(headers)) {
            HashMap<String, String> map = new Gson().fromJson(headers, new TypeToken<HashMap<String,
                    String>>() {
            }.getType());
            mWebView.loadUrl(url, map);
        } else {
            mWebView.loadUrl(url);
        }
    }

    private void initWBClient(final WebView webView) {
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(mIsPageLoading) {
                    return false;
                }
                if(url != null && url.startsWith("http")) {
                    view.loadUrl(url);
                    return true;
                } else {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        view.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                // WebViewDialog.this.dismiss();
                try {
                    view.stopLoading();
                    view.clearView();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Message msg = handler.obtainMessage();// 发送通知，加入线程
                // msg.what = 1;// 通知加载自定义404页面
                // handler.sendMessage(msg);// 通知发送！
            }

            @Override
            public void onReceivedSslError(WebView view,
                                           SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                handler.cancel();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mIsPageLoading= true;
                progressBar.setVisibility(View.VISIBLE);
                progress = 0;
                myHandler.postDelayed(runnable, 50);// 打开定时器，执行操作
//                if (webView.getUrl().contains("www.fdccloud.com/plan-micro/my5773575115df8")
//                    && getIntent().getStringExtra("URL").contains("jianye.com.cn/sys/notify/mobile")) {
//                    webView.setVisibility(View.GONE);
//                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mIsPageLoading= false;
                myHandler.removeCallbacks(runnable);// 关闭定时器处理
                progressBar.setProgress(100);
                progressBar.setVisibility(View.GONE);
                CookieSyncManager.createInstance(WebViewActivity.this);
                CookieManager cookieManager = CookieManager.getInstance();
                h5Cookie = cookieManager.getCookie(url);//从H5获取cookie
                CookieSyncManager.getInstance().sync();
//                Log.e("onPageFinished", url);
//                if (getIntent().getStringExtra("URL").contains("jianye.com.cn/sys/notify/mobile")) {
//                    if (webView.getUrl().contains("www.fdccloud.com/plan-micro/my5773575115df8")) {
//                        todoRdg.check(R.id.web_actions_tabs_todo);
//                        webView.setVisibility(View.GONE);
//                    } else {
//                        webView.setVisibility(View.VISIBLE);
//                        progressDialogUtil.dismiss();
//                    }
//                }
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view,
                                                              String url) {
                return super.shouldInterceptRequest(view, url);
            }

        });
    }

    private void initWebSetting(WebView webView) {
        webView.stopLoading();
        webView.clearHistory();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        webView.getSettings().setJavaScriptEnabled(true);
        /*** 打开本地缓存提供JS调用 **/
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getApplicationContext().getCacheDir()
                .getAbsolutePath();
        webView.getSettings().setAppCachePath(appCachePath);
        webView.getSettings().setAllowFileAccess(true);// 设置允许访问文件数据
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setUserAgentString("jianye-echat");

        // User settings
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setUseWideViewPort(true);//关键点
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setBuiltInZoomControls(true); // 设置显示缩放按钮
        webView.getSettings().setSupportZoom(true); // 支持缩放
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setBlockNetworkImage(false);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    @SuppressLint("SdCardPath")
    private File createImageFile() {
        //mCameraPhotoPath="/mnt/sdcard/tmp.png";
        File file = new File(Environment.getExternalStorageDirectory() + "/", "tmp.png");
        mCameraPhotoPath = file.getAbsolutePath();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 将cookie同步到WebView
     *
     * @param url    WebView要加载的url
     * @param cookie 要同步的cookie
     * @return true 同步cookie成功，false同步cookie失败
     */
    public boolean syncCookie(String url, String cookie) {
        if (TextUtils.isEmpty(cookie)) return false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(WebViewActivity.this);
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(url, cookie);//如果没有特殊需求，这里只需要将session id以"key=value"形式作为cookie即可
        String newCookie = cookieManager.getCookie(url);
        return TextUtils.isEmpty(newCookie);
    }

    @Override
    protected void onActivityResult(int arg0, int resultCode, Intent data) {
        super.onActivityResult(arg0, resultCode, data);
        switch (arg0) {
            case 1:
                Log.v("onActivityResult", "1");
                String ids = "";
                String members = "";
                String adNames = "";
                Map<String, String> map = new HashMap<String, String>();
                if (data != null) {
                    atOrgunitionLists = (ArrayList<OrganizationTree>) data
                            .getSerializableExtra("atOrgunitionLists");
                    atAllUserInfos = (ArrayList<UserInfo>) data
                            .getSerializableExtra("atUserInfos");
                    if (!atAllUserInfos.isEmpty()) {
                        for (int i = 0; i < atAllUserInfos.size(); i++) {
                            ids += atAllUserInfos.get(i).getId() + ",";
                            members += atAllUserInfos.get(i).getEmpCname() + ",";
                            adNames += atAllUserInfos.get(i).getEmpAdname() + ",";
                        }
                        map.put("ids", ids.substring(0, ids.length() - 1));
                        map.put("members",
                                members.substring(0, members.length() - 1));
                        map.put("adnames",
                                adNames.substring(0, adNames.length() - 1));

                        final String json = Obj2JsonUtils.map2json(map);
                        Log.v("json", json);

                        mWebView.post(new Runnable() {
                            @Override
                            public void run() {
                                mWebView.loadUrl("javascript:eoopWeb.confirmMember(" + json
                                        + ")");
                            }
                        });

                    }
                }

                break;
            case 2:
                Log.v("onActivityResult", "2");
                mWebView.reload();
                break;
            case FILECHOOSER_RESULTCODE:
                if (null == mUploadMessage)
                    return;
                Uri result = data == null || resultCode != RESULT_OK ? null : data
                        .getData();
                if (result == null && data == null
                        && resultCode == Activity.RESULT_OK) {
                    if (mCameraFilePath != null) {
                        File cameraFile = new File(mCameraFilePath);
                        if (cameraFile.exists()) {
                            result = Uri.fromFile(cameraFile);
                            // Broadcast to the media scanner that we have a new photo
                            // so it will be added into the gallery for the user.
                            sendBroadcast(new Intent(
                                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));

                        }
                    }
                }
                if (null != mUploadMessage) {
                    mUploadMessage.onReceiveValue(result);
                    mUploadMessage = null;
                }
                result = null;
                break;

            // 如果是直接从相册获取
            case PHOTO_RESULTCODE:
                // 从相册中直接获取文件的真是路径，然后上传
                final String picPath = PicUtils.getPicturePath(data,
                        WebViewActivity.this);
                Log.v("picPath", "===" + picPath);
                try {
                    takePicturePath = PicUtils
                            .getSmallImageFromFileAndRotaing(picPath);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                toUploadFile(takePicturePath);
                break;
            // 如果是调用相机拍照时
            case CAMERA_RESULTCODE:
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
                        toUploadFile(takePicturePath);
                    }
                }
                break;
            case FILECHOOSER_RESULTCODE_FOR_INPUT_FILE:

                if (mFilePathCallback != null) {
                    // 5.0的回调
                    Uri[] results = null;
                    if (resultCode == Activity.RESULT_OK) {
                        if (data == null) {
                            if (mCameraPhotoPath != null) {
                                results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                            }
                        } else {
                            String dataString = data.getDataString();
                            if (dataString != null) {
                                results = new Uri[]{Uri.parse(dataString)};
                            }
                        }
                    }
                    mFilePathCallback.onReceiveValue(results);
                    mFilePathCallback = null;
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.web_actions_tabs_todo:
                mWebView.loadUrl(getIntent().getStringExtra("URL"));
                break;
            case R.id.web_actions_tabs_mingyuan:
                loadingMingyuan();
                break;
            default:
                break;
        }
    }

    private void loadingMingyuan() {
        String arg = null;
        try {
            arg = DESUtils.encryptDES(spUtil.getString(CommConstants.EMPADNAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String targetUrl = "https://www.fdccloud.com/plan-micro/my5773575115df8/task/home/index";
        targetUrl = targetUrl + "?__from=mentor&kindType=5&Userticket=" + arg;
        mWebView0.loadUrl(targetUrl);
    }

    class WebViewFuncion {

        public WebViewFuncion() {
            super();
        }

        @JavascriptInterface
        public void showNativeActionSheet() {
            // 实例化SelectPicPopupWindow
            popWindow = new SelectPicPopup(WebViewActivity.this,
                    itemsOnClick);
            // 显示窗口
            popWindow.showAtLocation(toplayout,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
        }

        @JavascriptInterface
        public void openDetail(String url) {
            if (url.startsWith("http:")) {

            } else {
                URL url2;
                try {
                    url2 = new URL(mWebView.getUrl());
                    System.out.println(url2.getHost());
                    System.out.println(url2.getPort());
                    url = "http://" + url2.getHost() + ":" + url2.getPort()
                            + url;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            final String newUrl = url;
            (new Handler()).postDelayed(new Runnable() {

                @Override
                public void run() {
                    System.out.println(newUrl);
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra("URL", newUrl);
                    startActivity(intent);
                }
            }, 100);
        }

        @JavascriptInterface
        public void openDeviceBrowser(String url) {

            final Uri uri = Uri.parse(url);
            final Intent it = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(it);
        }

        @JavascriptInterface
        public void openTaskCalendarURL(String url) {
            if (url.startsWith("http:")) {

            } else {
                URL url2;
                try {
                    url2 = new URL(mWebView.getUrl());
                    System.out.println(url2.getHost());
                    System.out.println(url2.getPort());
                    url = "http://" + url2.getHost() + ":" + url2.getPort()
                            + url;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            final String newUrl = url;
            (new Handler()).postDelayed(new Runnable() {

                @Override
                public void run() {
                    System.out.println(newUrl);
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra("URL", newUrl);
                    startActivityForResult(intent, 2);
                }
            }, 100);
        }

        @JavascriptInterface
        public void closeBroswer() {
            onBackPressed();
        }

        @JavascriptInterface
        public void chooseMember(String ids) {
            Log.v("ids", "==" + ids);
            if (StringUtils.notEmpty(ids)) {
                atAllUserInfos.clear();
                atOrgunitionLists.clear();

                String[] arrId = ids.split(",");
                UserDao dao = UserDao.getInstance(context);
                for (int i = 0; i < arrId.length; i++) {
                    UserInfo userInfo = dao.getUserInfoById(arrId[i]);
                    atAllUserInfos.add(userInfo);
                }

                dao.closeDb();

                UserInfo userInfo = tools.getLoginConfig().getmUserInfo();
                if (atAllUserInfos.contains(userInfo)) {
                    atAllUserInfos.remove(userInfo);
                }
            }
            Intent intent = new Intent();
            intent.putExtra("TITLE", "选择联系人");
            intent.putExtra("ACTION", "WebView");
            intent.putExtra("atUserInfos", atAllUserInfos);

            ((BaseApplication) WebViewActivity.this.getApplication()).getUIController().onIMOrgClickListener(WebViewActivity.this, intent, 1);
        }

        @JavascriptInterface
        public void finishSaveTask(String message) {
            Log.v("message", "==" + message);
            if (StringUtils.notEmpty(message)) {
                EOPApplication.showToast(context, message);
            }
            onBackPressed();
        }

        @JavascriptInterface
        public void turnToGroup(String roomId, String displayName) {
            // 获取群组信息 ， 跳转聊天
            progressDialogUtil.showLoadingDialog(context, "正在获取群组信息...", false);
            handler.obtainMessage(3, new String[]{roomId, displayName})
                    .sendToTarget();
        }

        @JavascriptInterface
        public void showAlert(String msg) {
            Log.v("alert", msg);
            handler.obtainMessage(4, msg).sendToTarget();
        }

        @JavascriptInterface
        public void showTaskCalendarConfirm(String title) {
            Log.v("alert", title);
            dialogUtil = CusDialog.getInstance();
            dialogUtil.showCustomDialog(context);
            dialogUtil.setWebDialog(title);
            dialogUtil.setCancleClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialogUtil.dismiss();
                }
            });
            dialogUtil.setConfirmClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

//					mWebView.loadUrl("javascript:eoopWeb.confirmSure()");

                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadUrl("javascript:eoopWeb.confirmSure()");
                        }
                    });

                    dialogUtil.dismiss();
                }
            });
        }

        @JavascriptInterface
        public void showTaskCalendarAlert(String title) {
            Log.v("alert", title);
            dialogUtil = CusDialog.getInstance();
            dialogUtil.showCustomDialog(context);
            dialogUtil.setSimpleDialog(title);
            dialogUtil.setConfirmClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadUrl("javascript:eoopWeb.alertOK()");
                        }
                    });

                    dialogUtil.dismiss();
                }
            });
        }

        @JavascriptInterface
        public void showTaskCalendarToast(String message) {
            EOPApplication.showToast(context, message);
        }

        @JavascriptInterface
        public void startLoading() {
            handler.sendEmptyMessage(5);
        }

        @JavascriptInterface
        public void stopLoading() {
            handler.sendEmptyMessage(6);
        }

        @JavascriptInterface
        public void meetingScan() {
            startActivity(new Intent(context, MyCodeActivity.class)
                    .putExtra("type", "sign"));
        }

        @JavascriptInterface
        public void openURL(String url) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            startActivity(intent);
        }

        @JavascriptInterface
        public boolean hidenKeyboard() {
            ((InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getCurrentFocus()
                            .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            return true;
        }

        @JavascriptInterface
        public void openAttachmentWebView(String fileLink, String user, String password) {
            WebViewActivity.this.openAttachmentWebView(fileLink, user, password);
        }

        //for计划系统
        public void openAttachmentWebView(String fileLink, String user,
                                          String password, String fileType) {
            Message message = mHandler.obtainMessage();
            if (fileLink.indexOf(".apk") != -1) {
                message.what = SHOW_DIALOG_BEFORE_APKLOAD;
            } else {
                message.what = SHOW_DIALOG_BEFORE_DOWNLOAD;
            }
            mHandler.sendMessage(message);

            // TODO Anna
//        FileUtils.mFileSuffix = fileType;
            WebViewActivity.this.fileLink = fileLink;

            if (FileUtils.getInstance().existSoftwareForTheFile(WebViewActivity.this,
                    WebViewActivity.this.fileLink)) {
                if (!Environment.getExternalStorageState().equals(
                        android.os.Environment.MEDIA_MOUNTED)) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = CLOSE_MPROGRESS_DIALOG_BEFORE_SHOW_PERCENT;
                    mHandler.sendMessage(msg);
                    Toast.makeText(WebViewActivity.this, "您好，没有找到SD卡,附件无法下载，请确认SD卡是否已插好。",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (DownloadFiles.isNetAvailable(WebViewActivity.this)) {
                    WebViewActivity.this.toDownloadAttachment(user, password);
                } else {
                    Message msg = mHandler.obtainMessage();
                    msg.what = CLOSE_MPROGRESS_DIALOG_BEFORE_SHOW_PERCENT;
                    mHandler.sendMessage(msg);
                    Toast.makeText(WebViewActivity.this, "当前网络不可用，请检查。",
                            Toast.LENGTH_LONG).show();
                }

            } else {
                Message msg = mHandler.obtainMessage();
                msg.what = CLOSE_MPROGRESS_DIALOG_BEFORE_SHOW_PERCENT;
                mHandler.sendMessage(msg);
                Toast.makeText(WebViewActivity.this,
                        "您好，请先下载该附件相关的办公软件，WPS， Polaris Office 等等。",
                        Toast.LENGTH_LONG).show();
            }

        }

        public void openAttachmentWebView1(String url) {
            ATTACHMENT_FLAG = true;
            // mVebView.loadUrl("file:///android_asset/www/empty.html");
            mWebView.loadUrl(url);
            // Message msg = new Message();
            // msg.what = ADJUST_LAYOUT_FOR_ATTACHMENT;
            // mHandler.sendMessage(msg);
            System.out.println("点哈军工ioajiogdohgioaphgiojewjiojdio231231");
        }

    }

    private void openAttachmentWebView(String fileLink, String user, String password) {
        Log.d("test", "openAttachmentWebView");

        Message message = mHandler.obtainMessage();
        if (fileLink.indexOf(".apk") != -1) {
            message.what = SHOW_DIALOG_BEFORE_APKLOAD;
        } else {
            message.what = SHOW_DIALOG_BEFORE_DOWNLOAD;
        }
        mHandler.sendMessage(message);

        WebViewActivity.this.fileLink = fileLink;

        if (FileUtils.getInstance().existSoftwareForTheFile(WebViewActivity.this,
                WebViewActivity.this.fileLink)) {
            if (!Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                Message msg = mHandler.obtainMessage();
                msg.what = CLOSE_MPROGRESS_DIALOG_BEFORE_SHOW_PERCENT;
                mHandler.sendMessage(msg);
                Toast.makeText(WebViewActivity.this, "您好，没有找到SD卡,附件无法下载，请确认SD卡是否已插好。",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (DownloadFiles.isNetAvailable(WebViewActivity.this)) {
                // 在下载文件的方法里判断： 下载之前先判断附件是否存在，文件是否在server 端已经更新了？
                // File.lastModified() 好象不行，一会再看看。
                WebViewActivity.this.toDownloadAttachment(user, password);

                // 附件下载到手机以后，列出支持该附件的第三方软件，供用户选择使用。
                // this.choseThirdPartySoftwareToOpenAttachment();

                // Toast.makeText(this, "True, 该手机系统支持  类型的附件。",
                // FileUtils.TOAST_SHOW_TIME).show();
            } else {
                Message msg = mHandler.obtainMessage();
                msg.what = CLOSE_MPROGRESS_DIALOG_BEFORE_SHOW_PERCENT;
                mHandler.sendMessage(msg);
                Toast.makeText(WebViewActivity.this, "当前网络不可用，请检查。",
                        Toast.LENGTH_LONG).show();
            }

        } else {
            Message msg = mHandler.obtainMessage();
            msg.what = CLOSE_MPROGRESS_DIALOG_BEFORE_SHOW_PERCENT;
            mHandler.sendMessage(msg);
            Toast.makeText(WebViewActivity.this,
                    "您好，请先下载该附件相关的办公软件，WPS， Polaris Office 等等。",
                    Toast.LENGTH_LONG).show();
        }
    }

    public class UpdataBarListernerImpl implements FileUtils.UpdataBarListerner {

        @Override
        public void onUpdate(int value, int status) {
            Message message = mHandler.obtainMessage();
            message.what = value;
            mHandler.sendMessage(message);
        }

        @Override
        public void onError(int value, int status) {
            Message message = mHandler.obtainMessage();
            message.what = value;
            mHandler.sendMessage(message);
        }
    }

    private void toDownloadAttachment(final String username,
                                      final String password) {
        Message message = mHandler.obtainMessage();
        message.what = SHOW_MY_PROGRESS;
        mHandler.sendMessage(message);

        Thread thread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                String fileName = FileUtils.getInstance().geFileName(fileLink);
                if (fileLink.contains("m.exmail.qmail.com/cgi-bin/download?")) {
                    fileName = qqFileName;
                } else if (fileLink.contains("zh.jianye.com.cn/sys/attachment/sys_att_main/sysAttMain.do?method=download")) {
                    fileName = gtFileName;
                }
                DownloadFiles.downFile(WebViewActivity.this.fileLink, h5Cookie,
                        CommConstants.SD_DOWNLOAD, fileName, username,
                        password, new UpdataBarListernerImpl());
                //TODO 由于历史原因，暂时下载时间写死，以便正常下载
                getWindow().getDecorView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Message message = mHandler.obtainMessage();
                        message.what = ATTACHMENT_DOWNLOAD_END;
                        mHandler.sendMessage(message);
                    }
                }, 5000);
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    protected void AdjustLayoutForAttachment() {
        LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams) mWebView.getLayoutParams();
        float density = getResources().getDisplayMetrics().density;
        // layout.y = (int) (40 * density + 0.5f);
        layout.topMargin = (int) (44 * density + 0.5f);
        mWebView.setLayoutParams(layout);

    }

    protected void choseThirdPartySoftwareToOpenAttachment() {
        File file;
        if (fileLink.contains("m.exmail.qmail.com/cgi-bin/download?")) {
            file = new File(CommConstants.SD_DOWNLOAD + qqFileName);
        } else if (fileLink.contains("zh.jianye.com.cn/sys/attachment/sys_att_main/sysAttMain.do?method=download")) {
            file = new File(CommConstants.SD_DOWNLOAD + gtFileName);
        } else {
            file = FileUtils.getInstance().getFileFromSDByFileLink(CommConstants.SD_DOWNLOAD, this.fileLink);
        }
        // Use for debug
        // boolean exists = file.exists();
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 设置intent的Action属性
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        // 不能用直接使用wps打开的附件刚自行选择软件
        try {
            // 获取文件file的MIME类型
            String type = null;
            if (fileLink.contains("m.exmail.qmail.com/cgi-bin/download?")){
                type = qqmailType;
            } else if (fileLink.contains("zh.jianye.com.cn/sys/attachment/sys_att_main/sysAttMain.do?method=download")) {
                type = goutongType;
            } else {
                type = FileUtils.getInstance().getMIMEType(this.fileLink);
            }
            // 设置intent的data和Type属性。
            intent.setDataAndType(uri, type);
            // 跳转
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(WebViewActivity.this, "您好，请先下载该附件相关的办公软件。",
                    Toast.LENGTH_LONG).show();
        }

    }

    public void getAllUsersInOrg(UserDao dao, OrganizationTree org,
                                 List<UserInfo> allUsers) {
        String orgId = org.getId();
        List<UserInfo> userInfos = dao.getAllUserInfosByOrgId(orgId);
        allUsers.addAll(userInfos);
        List<OrganizationTree> orgs = dao.getAllOrganizationsByParentId(orgId);
        for (int i = 0; i < orgs.size(); i++) {
            getAllUsersInOrg(dao, orgs.get(i), allUsers);
        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    progressDialogUtil.dismiss();
                    try {
                        Group group = (Group) msg.obj;
                        Intent intent = new Intent(WebViewActivity.this,
                                GroupChatActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("room", group.getGroupName());
                        bundle.putString("subject", group.getDisplayName());
                        bundle.putInt(CommConstants.KEY_GROUP_TYPE, CommConstants.CHAT_TYPE_GROUP_PERSON);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(context, "群组信息获取失败！");
                    break;
                case 3:
                    try {
                        String[] group = (String[]) msg.obj;
                        if (IMConstants.groupsMap.containsKey(group[0])) {
                            progressDialogUtil.dismiss();
                            // 已存在
                            Intent intent = new Intent(WebViewActivity.this,
                                    GroupChatActivity.class);

                            Bundle bundle = new Bundle();
                            bundle.putString("room", group[0]);
                            bundle.putString("subject", IMConstants.groupsMap.get(group[0]).getDisplayName());
                            bundle.putInt(CommConstants.KEY_GROUP_TYPE, CommConstants.CHAT_TYPE_GROUP_PERSON);
                            intent.putExtras(bundle);

                            startActivity(intent);
                        } else {
                            GroupManager manager = GroupManager.getInstance(context);
                            manager.getGroupInfo(group[0], group[1], handler);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        progressDialogUtil.dismiss();
                    }
                    break;
                case 4:
                    final String messageString = (String) msg.obj;
                    dialogUtil = CusDialog.getInstance();
                    dialogUtil.showCustomDialog(context);
                    dialogUtil.setSimpleDialog(messageString);
                    dialogUtil.setConfirmClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (messageString.contains("注册成功")) {
                                dialogUtil.dismiss();
                                onBackPressed();
                            } else {
                                dialogUtil.dismiss();
                            }
                        }
                    });
                    break;
                case 5:
                    progressDialogUtil.showLoadingDialog(context, "请稍候...", false);
                    break;
                case 6:
                    progressDialogUtil.dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    private Intent createDefaultOpenableIntent(String acceptType) {
        // Create and return a chooser with the default OPENABLE
        // actions including the camera, camcorder and sound
        // recorder where available.
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        if (StringUtils.empty(acceptType)) {
            i.setType("*/*");
        } else {
            i.setType(acceptType);
        }

        Intent chooser = createChooserIntent(createCameraIntent());
        chooser.putExtra(Intent.EXTRA_INTENT, i);
        return chooser;
    }

    private Intent createChooserIntent(Intent... intents) {
        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
        chooser.putExtra(Intent.EXTRA_TITLE, "File Chooser");
        return chooser;
    }

    private Intent createCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File externalDataDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File cameraDataDir = new File(externalDataDir.getAbsolutePath()
                + File.separator + "browser-photos");
        cameraDataDir.mkdirs();
        mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator
                + System.currentTimeMillis() + ".jpg";
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(new File(mCameraFilePath)));
        return cameraIntent;
    }

    @JavascriptInterface
    public Intent createCamcorderIntent() {
        return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    }

    @JavascriptInterface
    public Intent createSoundRecorderIntent() {
        return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
    }

    private void addUserActionLog(final String moduleId) {

        // AddUserLog
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
                    String url = "http://" + CommConstants.URL_EOP_API + "r/sys/appmgtrest/savemodulelog";
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("userId", userInfo.getId());
                    params.put("moduleId", moduleId);
                    params.put("action", "2");// 1表示进入,2退出
                    String result = HttpClientUtils.post(url, params);
                    Log.v("result", result);
                    // { "ok" : true}
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
                        Toast.makeText(WebViewActivity.this, "找不到sd卡", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent2, CAMERA_RESULTCODE);
                    break;
                case R.id.btn_pick_photo:
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, PHOTO_RESULTCODE);

                    break;
                default:
                    break;
            }

        }

    };

    AsyncTask<Void, Void, String> uploadFileTask = null;

    private void toUploadFile(final String filePath) {
        if (uploadFileTask != null) {
            uploadFileTask.cancel(true);
            uploadFileTask = null;
        }
        try {
            URL url2 = new URL(mWebView.getUrl());
            if (url2.getPort() > 0) {
                uploadAcctachUrl = url2.getProtocol() + "://" + url2.getHost() + ":" + url2.getPort();
            } else {
                uploadAcctachUrl = url2.getProtocol() + "://" + url2.getHost();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        uploadFileTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialogUtil.showLoadingDialog(WebViewActivity.this,
                        "请稍候...", false);
            }

            @Override
            protected String doInBackground(Void... params) {
                String result = null;
                try {
                    Bitmap bm = BitmapFactory.decodeFile(filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                    byte[] byteArrayImage = baos.toByteArray();

                    String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

                    Map<String, String> map = new HashMap<>();
                    map.put("strJson", encodedImage);
                    result = HttpClientUtils.post(uploadAcctachUrl + "/WebService/PlanMobileService.asmx/SaveAttach", map);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                try {
                    InputStream inputStream = new ByteArrayInputStream(
                            result.getBytes());
                    // 创建解析
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser saxParser = spf.newSAXParser();
                    XMLSimpleContentHandler handler = new XMLSimpleContentHandler();
                    saxParser.parse(inputStream, handler);
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                progressDialogUtil.dismiss();

            }
        };
        uploadFileTask.execute(null, null, null);
    }

    public class XMLSimpleContentHandler extends DefaultHandler {
        // localName表示元素的本地名称（不带前缀）；qName表示元素的限定名（带前缀）；atts 表示元素的属性集�
        StringBuffer body;

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (localName.equals("string")) {
                body = new StringBuffer();
            }

        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            super.characters(ch, start, length);
            body.append(ch, start, length); // 将读取的字符数组追加到builder中
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
            if (localName.equals("string")) {
                Log.v("XMLSimpleContentHandler", "body=" + body.toString());
                Log.v("XMLSimpleContentHandler", "takePicturePath=" + takePicturePath);
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:reportDetail.setImageUrl('" + takePicturePath + "','" + body.toString()
                                + "')");
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(WebViewActivity.this);
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        cookieManager.removeSessionCookie();//移除
        cookieManager.flush();
    }
}
