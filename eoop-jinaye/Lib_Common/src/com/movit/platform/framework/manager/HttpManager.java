package com.movit.platform.framework.manager;

import android.util.Log;

import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.builder.PostFormBuilder;
import com.movit.platform.framework.core.okhttp.callback.BitmapCallback;
import com.movit.platform.framework.core.okhttp.callback.Callback;
import com.movit.platform.framework.core.okhttp.callback.FileCallBack;
import com.movit.platform.framework.core.okhttp.callback.ListCallback;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.core.okhttp.callback.StringCallback2;

import java.io.File;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;

/**
 * Created by Administrator on 2016/5/25.
 */
public class HttpManager {

    private static final String TAG ="HttpManager";

    //下载图片
    public static void downloadBitmap(String fileUrl, BitmapCallback bitmapCallBack) {

        Log.d(TAG,"fileUrl="+fileUrl);

        OkHttpUtils
                .getWithToken()
                .url(fileUrl)
                .build()
                .execute(bitmapCallBack);
    }

    //下载文件
    public static void downloadFile(String fileUrl, FileCallBack fileCallBack) {
        Log.d(TAG,"fileUrl="+fileUrl);
        OkHttpUtils
                .getWithToken()
                .url(fileUrl)
                .build()
                .execute(fileCallBack);
    }

    //get请求
    public static void getJsonWithToken(String url, StringCallback stringCallback) {

        Log.d(TAG,"url="+url);
        OkHttpUtils
                .getWithToken()
                .url(url)
                .build()
                .execute(stringCallback);
    }

    //get请求,不加密解密
    public static void getJsonWithToken(String url, StringCallback2 stringCallback) {

        Log.d(TAG,"url="+url);
        OkHttpUtils
                .getWithToken()
                .url(url)
                .build()
                .execute(stringCallback);
    }

    //get请求
    public static void getJson(String url, Callback<String> stringCallback) {
        Log.d(TAG,"url="+url);
        OkHttpUtils.get().url(url)
                .build()
                .execute(stringCallback);
    }

    //post请求
    public static void postJsonWithToken(String url, String content, StringCallback stringCallback) {
        Log.d(TAG,"url="+url);
        Log.d(TAG,"content="+content);
        OkHttpUtils
                .postStringWithToken()
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .url(url).content(content)
                .build()
                .execute(stringCallback);
    }

    //post请求
    public static void postJson(String url, String content, StringCallback stringCallback) {
        Log.d(TAG,"url="+url);
        Log.d(TAG,"content="+content);
        OkHttpUtils
                .postString()
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .url(url).content(content)
                .build()
                .execute(stringCallback);
    }

    //上传单个文件
    public static void uploadFile(String url, Map<String, String> params, File file, String fileKey, Callback<String> callback) {
        Log.d(TAG,"url="+url);
        OkHttpUtils.postWithToken()
                .addFile(fileKey, file.getName(), file)
                .url(url)
                .params(params)
                .build()
                .execute(callback);
    }

    //上传多个文件
    public static void multiFileUpload(String url, List<String> filePaths, String fileKey, StringCallback callback){
        PostFormBuilder builder = OkHttpUtils.postWithToken();
        for (int i = 0; i < filePaths.size(); i++) {
            String filePath = filePaths.get(i);
            File file = new File(filePath);
            builder.addFile(fileKey, file.getName(), file);
        }
        builder.url(url)
                .build()
                .execute(callback);
    }

    //上传文件（给webview使用的，没有测试过）
    public static void uploadFile(String url, Map<String, String> params, StringCallback callback) {
        Log.d(TAG,"url="+url);
        OkHttpUtils.postWithToken()
                .url(url)
                .params(params)
                .build()
                .execute(callback);
    }

}