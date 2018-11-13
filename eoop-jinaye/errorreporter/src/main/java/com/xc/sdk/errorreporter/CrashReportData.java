package com.xc.sdk.errorreporter;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Created by chao on 2017/12/25.
 */
public class CrashReportData {

  private final String info;

  private CrashReportData(String crashInfo) {
    this.info = crashInfo;
  }

  public static CrashReportData produce(Thread thread, Throwable ex, Context context) {

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream print = new PrintStream(out);
    out.toString();
    print.append("crahtime:" + getDateToString(System.currentTimeMillis())).append("\n");
    print.append(SysInfo.getSysInfo(context)).append("\n");
    print.append(thread.getName()).append("(threadID=" + thread.getId() + ")").append("\n");
    print.append(ex.getMessage()).append("\n");
    ex.printStackTrace(print);
    return new CrashReportData(out.toString());
  }

  public void writeToFile(File file) {
    PrintWriter printer = null;
    try {
      // append to the end of crash file
      BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file, true));
      printer = new PrintWriter(out);
      printer.println(info);
      printer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (printer != null) {
        printer.close();
      }
      Log.w(AppCR.LOG_TAG, "write exception info to file over.");
    }
  }

  /**
   * 时间戳转换成字符窜
   */
  public static String getDateToString(long time) {
    Date d = new Date(time);
    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return sf.format(d);
  }

  @Override
  public String toString() {
    return info;
  }
}
