package com.xc.sdk.errorreporter;

import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * Created by chao on 2017/12/25.
 */
public class SysInfo {

  public static String getSysInfo(Context context) {

    StringBuilder info = new StringBuilder();
    info.append("osVersion=Android ").append(Build.VERSION.RELEASE).append("\n");
    info.append("model=").append(Build.MODEL).append("\n");
    info.append("brand=").append(Build.BRAND).append("\n");

    Log.i(AppCR.LOG_TAG, "sys info collect over.");
    return info.toString();
  }
}
