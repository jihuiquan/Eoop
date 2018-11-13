package com.jianye.smart.task;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.okhttp.OkHttpUtils;
import com.movit.platform.common.okhttp.callback.FileCallBack;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.view.CusDialog;
import java.io.File;
import okhttp3.Call;
import okhttp3.Request;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/11/9.
 */
public class CheckAndUpdateAPKTask {

    AsyncTask<Void, Void, String> checkTask;
    int serverVersionName = 0;
    private String forceUpdate;
    private String newChanges;
    private String appDownloadUrl;
    private CusDialog dialogUtil;
    private Activity activity;
    private SharedPreUtils spUtil;
    private static CheckAndUpdateAPKTask task;

  public static CheckAndUpdateAPKTask getInstance(Activity context) {
    if (task == null) {
      task = new CheckAndUpdateAPKTask(context);
    }
    return task;
  }

    private CheckAndUpdateAPKTask(Activity activity) {
        this.activity = activity;
        spUtil = new SharedPreUtils(activity);
    }

    public void checkVersion() {
        if (checkTask != null) {
            checkTask.cancel(true);
            checkTask = null;
        }
        checkTask = new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                CommConstants.DEVICE_ID = Settings.Secure.getString(activity.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                String result = HttpClientUtils.get(CommConstants.DOWNLOAD_URL);
                Log.v("版本信息：", "版本信息：" + result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("version")) {
                        serverVersionName = jsonObject.getInt("version");
                    }
                    if (jsonObject.has("forceUpdate")) {
                        forceUpdate = jsonObject.getString("forceUpdate");
                    }
                    if (jsonObject.has("newChanges")) {
                        newChanges = jsonObject.getString("newChanges");
                    }
                    if (jsonObject.has("url")) {
                        appDownloadUrl = jsonObject.getString("url");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                int versionName = 0;
                try {
                    versionName = activity.getApplication().getPackageManager().getPackageInfo(
                            activity.getPackageName(),
                            PackageManager.GET_META_DATA).versionCode;
                    int ignoreVersion = spUtil
                            .getInteger(CommConstants.IGNORE_CHECK_VERSION_CODE);
                    if (ignoreVersion > 0) {
                        if ((ignoreVersion < serverVersionName)) {
                            showUpgradeDialog(appDownloadUrl);
                        }
                    } else {
                        if (versionName < serverVersionName) {
                            showUpgradeDialog(appDownloadUrl);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        };
        checkTask.execute(null, null, null);
    }

    public void cancel() {
        if (checkTask != null) {
            checkTask.cancel(true);
            checkTask = null;
        }
    }


    private void showUpgradeDialog(final String url) {
        if (dialogUtil != null) {
            if (dialogUtil.isShowing()) {
                return;
            }
        }
        dialogUtil = CusDialog.getInstance();
        dialogUtil.showVersionDialog(activity);
        dialogUtil.setUpdateDialog(newChanges.toString(), forceUpdate);
        dialogUtil.setConfirmClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    spUtil.setInteger(CommConstants.IGNORE_CHECK_VERSION_CODE, 0);
                    if (dialogUtil != null) {
                        dialogUtil.dismiss();
                    }
                    downloadAPP(activity, url);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        dialogUtil.setCancleClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    spUtil.setInteger(CommConstants.IGNORE_CHECK_VERSION_CODE, 0);
                    if (dialogUtil != null) {
                        dialogUtil.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void downloadAPP(final Context context, String appDownloadUrl) {

        final SharedPreUtils spUtil = new SharedPreUtils(context);
        final DialogUtils progressDownLoading = DialogUtils.getInstants();
        progressDownLoading.showDownLoadingDialog(context, "正在下载...", false);
        progressDownLoading.getLoadingDialog().setCancelable(false);
        final String destFileDir = CommConstants.SD_DOWNLOAD;
        final String destFileName = appDownloadUrl.substring(appDownloadUrl.lastIndexOf("/") + 1);
        OkHttpUtils.get()
                .url(appDownloadUrl)
                .build()
                .execute(new FileCallBack(destFileDir, destFileName) {

                    @Override
                    public void onBefore(Request request) {
                    }

                    @Override
                    public void onAfter() {
//                        spUtil.setInteger(CommConstants.IGNORE_CHECK_VERSION_CODE, 0);
//                        progressDownLoading.dismiss();
//                        installAPK(context,destFileDir+destFileName);
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onSuccess() {
                        spUtil.setInteger(CommConstants.IGNORE_CHECK_VERSION_CODE, 0);
                        progressDownLoading.dismiss();
                        installAPK(context, destFileDir + destFileName);
                    }

                    @Override
                    public void onResponse(File response) throws JSONException {

                    }

                    @Override
                    public void inProgress(float progress, long total) {
                        progressDownLoading.setDownLoadProcess(progress * 100);
                    }
                });
    }

    private static void installAPK(final Context context, String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

}
