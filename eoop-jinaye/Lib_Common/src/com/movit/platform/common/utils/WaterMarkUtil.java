package com.movit.platform.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.view.View;

/**
 * @ClassName: WaterMarkUtil
 * @Description:
 * @Author: chao
 * @Data 2017-08-14 15:56
 */
public class WaterMarkUtil {

  //设置背景
  public static void setWaterMarkTextBg(Context context,View view, int bgColor, String text) {
    view.setBackgroundDrawable(drawTextToBitmap(context, view, bgColor, text));
  }

  /**
   * 生成水印文字图片
   */
  public static BitmapDrawable drawTextToBitmap(Context context, View view, int bgColor, String text) {
    try {
      float density = context.getResources().getDisplayMetrics().density;
      Bitmap bitmap = Bitmap.createBitmap((int) (100 * density), (int) (60 * density), Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(bitmap);
      canvas.drawColor(bgColor);
      Paint paint = new Paint();
      paint.setColor(Color.GRAY);
      paint.setAlpha((int) (30 * density));
      paint.setAntiAlias(true);
      paint.setTextAlign(Paint.Align.LEFT);
      paint.setTextSize(14 * density);
      Path path = new Path();
      path.moveTo(0, 50 * density);
      path.lineTo(100 * density, 0);
      canvas.drawTextOnPath(text, path, 0, 10 * density, paint);
      canvas.save(Canvas.ALL_SAVE_FLAG);
      canvas.restore();
      ACacheUtil.get(context).put(text, bitmap);
      BitmapDrawable drawable = new BitmapDrawable(bitmap);
      drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
      drawable.setDither(true);
      if (Build.VERSION.SDK_INT < VERSION_CODES.O){
        bitmap.recycle();
      }
      return drawable;
    } catch (Exception e) {

    }
    return null;

  }
}
