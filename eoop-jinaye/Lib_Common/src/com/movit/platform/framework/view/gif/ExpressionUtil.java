package com.movit.platform.framework.view.gif;

import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;

import com.movit.platform.common.constants.CommConstants;

public class ExpressionUtil {
	public static String zhengze = "\\[(\\S+?)\\]"; // 正则表达式，用来判断消息内是否有表情
    
    public static SpannableString getExpressionString(Context context, String str, Hashtable<Integer, GifDrawalbe> cache, Vector<GifDrawalbe> drawables){
    	SpannableString spannableString = new SpannableString(str);
    	Log.v("SpannableString",str);
        Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);		//通过传入的正则表达式来生成一个pattern
        try {
            dealExpression(context, spannableString, sinaPatten, 0, cache, drawables);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return spannableString;
    }
    
	public static void dealExpression(Context context, SpannableString spannableString, Pattern patten, int start, Hashtable<Integer, GifDrawalbe> cache, Vector<GifDrawalbe> drawables) throws Exception {
		Matcher matcher = patten.matcher(spannableString);
		while (matcher.find()) {
			String key = matcher.group();
			if (matcher.start() < start) {
				continue;
			}

			int id = CommConstants.mFaceGifMap.get(key);
			if (id != 0) {
				GifDrawalbe mSmile = null;
				if (cache.containsKey(id)) {
					mSmile = cache.get(id);
				} else {
					mSmile = new GifDrawalbe(context, id);
					cache.put(id, mSmile);
				}
				ImageSpan span = new ImageSpan(mSmile, ImageSpan.ALIGN_BASELINE);
				int mstart = matcher.start();
				int end = mstart + key.length();
				spannableString.setSpan(span, mstart, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				if (!drawables.contains(mSmile))
					drawables.add(mSmile);
			}else {
				id = CommConstants.mFaceMap.get(key);
				Log.v("key",key);
				Bitmap bitmap = BitmapFactory.decodeResource(
						context.getResources(), id);
				ImageSpan localImageSpan = new ImageSpan(context,
						bitmap, ImageSpan.ALIGN_BASELINE);
				int mstart = matcher.start();
				int end = mstart + key.length();
				spannableString.setSpan(localImageSpan, mstart, end,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}
}