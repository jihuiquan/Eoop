package com.xc.sdk.errorreporter;

import android.app.Application;

/**
 * Created by chao on 2017/12/25.
 */
public class AppCR {

  public static final String LOG_TAG = AppCR.class.getSimpleName();
  private static ErrorReporter mErrorReporter;

  public static void init(Application application) {
    init(application, true);
  }

  public static void init(Application application, boolean enabled) {
    mErrorReporter = new ErrorReporter(application, enabled);
  }
}
