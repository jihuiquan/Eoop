package cn.com.xc.sdk.utils;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * @ClassName: AppUtil
 * @Description:
 * @Author: chao
 * @Data 2017-08-11 14:35
 */
public class AppUtil {

  /**
   * 判断某个APK是否已经安装
   */
  public static boolean isAppInstalled(Context context, String packageName) {
    PackageManager pm = context.getPackageManager();
    boolean installed = false;
    try {
      pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
      installed = true;
    } catch (PackageManager.NameNotFoundException e) {
      installed = false;
    }
    return installed;
  }
}
