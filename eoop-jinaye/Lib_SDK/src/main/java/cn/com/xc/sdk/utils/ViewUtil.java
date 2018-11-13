package cn.com.xc.sdk.utils;

import android.view.View;
import android.view.View.OnClickListener;

/**
 * @ClassName: ViewUtil
 * @Description:
 * @Author: chao
 * @Data 2017-08-03 20:20
 */

public class ViewUtil {

  /**
   * 设置点击事件
   */
  public static void setOnClickListener(OnClickListener listener, View... views) {
    if (listener != null) {
      for (View view : views) {
        view.setOnClickListener(listener);
      }
    }
  }

}
