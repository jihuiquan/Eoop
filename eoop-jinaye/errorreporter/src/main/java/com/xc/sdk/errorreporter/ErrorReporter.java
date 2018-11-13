package com.xc.sdk.errorreporter;

import android.app.Application;
import android.util.Log;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Created by chao on 2017/12/25.
 */
public class ErrorReporter implements UncaughtExceptionHandler {

  private final Application mContext;
  private final ReporterExecutor mReporterExecutor;

  ErrorReporter(Application context, boolean enabled) {
    mContext = context;
    final Thread.UncaughtExceptionHandler defaultExceptionHandler = Thread
        .getDefaultUncaughtExceptionHandler();
    mReporterExecutor = new ReporterExecutor(context, defaultExceptionHandler);
    mReporterExecutor.setEnabled(enabled);
    Thread.setDefaultUncaughtExceptionHandler(this);
  }

  @Override
  public void uncaughtException(final Thread thread, final Throwable ex) {
    Log.i(AppCR.LOG_TAG, "catch uncaughtException");
    mReporterExecutor.execute(thread, ex);
  }

  public void setEnabled(boolean enabled) {
    Log.i(AppCR.LOG_TAG, "AppCR is" + (enabled ? "enabled" : "disabled") + " for "
        + mContext.getPackageName());
  }
}