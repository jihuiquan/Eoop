package com.xc.sdk.errorreporter;

import android.content.Context;
import android.icu.util.Calendar;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.IOException;

/**
 * Created by chao on 2017/12/25.
 */
public class ReporterExecutor {

  public static final String TAG = ReporterExecutor.class.getSimpleName();
  private Context mContext;
  private boolean mEnabled = false;
  private final Thread.UncaughtExceptionHandler mDefaultExceptionHandler;
  private File mCrashInfoFile;

  public ReporterExecutor(Context context,
      Thread.UncaughtExceptionHandler defaultedExceptionHandler) {

    mContext = context;
    mDefaultExceptionHandler = defaultedExceptionHandler;

    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      File path = Environment.getExternalStorageDirectory();
      File dir = new File(path, "CrashReport");
      if (!dir.exists()) {
        dir.mkdirs();
      }

      mCrashInfoFile = new File(dir, getCrashFileName());
      if (!mCrashInfoFile.exists()) {
        try {
          mCrashInfoFile.createNewFile();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public boolean isEnabled() {
    return mEnabled;
  }

  public void setEnabled(boolean enabled) {
    mEnabled = enabled;
  }

  public void execute(Thread thread, Throwable ex) {

    if (!mEnabled) {
      endApplication(thread, ex);
      return;
    }

    // log crash info to file
    Log.w(AppCR.LOG_TAG, "getSysInfo.");
    CrashReportData data = CrashReportData.produce(thread, ex, mContext);
    data.writeToFile(mCrashInfoFile);
    endApplication(thread, ex);

  }

  private void endApplication(Thread thread, Throwable ex) {

    if (mDefaultExceptionHandler != null) {
      Log.w(AppCR.LOG_TAG, "execute default uncaughtException handler.");
      mDefaultExceptionHandler.uncaughtException(thread, ex);
    } else {
      Log.w(AppCR.LOG_TAG, "kill process and exit.");
      android.os.Process.killProcess(android.os.Process.myPid());
      System.exit(10);
    }
  }

  private String getCrashFileName() {
    StringBuilder ret = new StringBuilder();
    Calendar calendar = Calendar.getInstance();

    ret.append("crash_");
    ret.append(calendar.get(Calendar.YEAR));
    int month = calendar.get(Calendar.MONTH) + 1;
    int date = calendar.get(Calendar.DATE);
    if (month < 10) {
      ret.append("0");
    }
    ret.append(month);
    if (date < 10) {
      ret.append("0");
    }
    ret.append(date);
    ret.append(".txt");
    return ret.toString();
  }
}
