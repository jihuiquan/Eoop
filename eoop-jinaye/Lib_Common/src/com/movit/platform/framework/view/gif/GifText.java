package com.movit.platform.framework.view.gif;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;

public class GifText extends TextView {
	private Vector<AnimationDrawable> drawables;
	private Hashtable<Integer, Integer> delays;
	private Hashtable<Integer, AnimationDrawable> cache;
	ArrayList<Bitmap> mapList;

	public static boolean mRunning = true;

	public GifText(Context context) {
		super(context);
		drawables = new Vector<AnimationDrawable>();
		cache = new Hashtable<Integer, AnimationDrawable>();
		delays = new Hashtable<Integer, Integer>();
		mapList = new ArrayList<Bitmap>();
	}

	public GifText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		drawables = new Vector<AnimationDrawable>();
		cache = new Hashtable<Integer, AnimationDrawable>();
		delays = new Hashtable<Integer, Integer>();
		mapList = new ArrayList<Bitmap>();
	}

	public GifText(Context context, AttributeSet attrs) {
		super(context, attrs);
		drawables = new Vector<AnimationDrawable>();
		cache = new Hashtable<Integer, AnimationDrawable>();
		delays = new Hashtable<Integer, Integer>();
		mapList = new ArrayList<Bitmap>();
	}

	private static final Pattern EMOTION_URL = Pattern.compile("\\[(\\S+?)\\]");// 正则表达式，用来判断消息内是否有表情

	public void setSpannableText(Context context, String message,
			boolean small, int textSize) {
		String hackTxt;
		if (message.startsWith("[") && message.endsWith("]")) {
			hackTxt = message + " ";
		} else {
			hackTxt = message;
		}

		SpannableString value = SpannableString.valueOf(hackTxt);
		setText(value);
		Matcher localMatcher = EMOTION_URL.matcher(value);
		while (localMatcher.find()) {
			String str2 = localMatcher.group(0);
			int k = localMatcher.start();
			int m = localMatcher.end();
			if (m - k < 8) {
				if (CommConstants.mFaceGifMap.containsKey(str2)) {
					// gif
					int face = CommConstants.mFaceGifMap.get(str2);
					AnimationDrawable mFace;
					mFace = new AnimationDrawable();
					GifHelper helper = new GifHelper();
					helper.read(context.getResources().openRawResource(face));
					Bitmap bitmap = helper.getImage();
					BitmapDrawable bd = new BitmapDrawable(null, bitmap);
					mFace.addFrame(bd, helper.getDelay(0));
					for (int i = 1; i < helper.getFrameCount(); i++) {
						mFace.addFrame(
								new BitmapDrawable(null, helper.nextBitmap()),
								helper.getDelay(i));
					}
					mFace.setBounds(0, 0, helper.getImage().getWidth(), helper
							.getImage().getHeight());
					bd.setBounds(0, 0, bd.getIntrinsicWidth(),
							bd.getIntrinsicHeight());

					mFace.setOneShot(false);
					int delay = helper.nextDelay();
					drawables.add(mFace);
					cache.put(face, mFace);
					delays.put(face, delay);
					ImageSpan span = new ImageSpan(mFace,
							ImageSpan.ALIGN_BASELINE);
					value.setSpan(span, k, m,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					
					new Thread(new TextRunnable(mFace, delay)).start();
				} else {
					// 静态图
					int face = CommConstants.mFaceMap.get(str2);

					Bitmap bitmap = BitmapFactory.decodeResource(
							context.getResources(), face);
					if (bitmap != null) {
						if (small) {
							int rawHeigh = bitmap.getHeight();
							int rawWidth = bitmap.getHeight();
							int newHeight = textSize;
							int newWidth = textSize;
							// 计算缩放因子
							float heightScale = ((float) newHeight) / rawHeigh;
							float widthScale = ((float) newWidth) / rawWidth;
							// 新建立矩阵
							Matrix matrix = new Matrix();
							matrix.postScale(heightScale, widthScale);
							// 设置图片的旋转角度
							// matrix.postRotate(-30);
							// 设置图片的倾斜
							// matrix.postSkew(0.1f, 0.1f);
							// 将图片大小压缩
							// 压缩后图片的宽和高以及kB大小均会变化
							bitmap = Bitmap.createBitmap(bitmap, 0, 0,
									rawWidth, rawHeigh, matrix, true);
						}
						ImageSpan localImageSpan = new ImageSpan(context,
								bitmap, ImageSpan.ALIGN_BASELINE);
						value.setSpan(localImageSpan, k, m,
								Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}
			}
		}
		setText(value);

	}

	public class TextRunnable implements Runnable {
		AnimationDrawable mFace;
		int mFrame = 0;
		int delay;

		public TextRunnable() {
			super();
		}

		public TextRunnable(AnimationDrawable mFace, int delay) {
			super();
			this.mFace = mFace;
			this.delay = delay;
		}

		@Override
		public void run() {

			Log.v("run", mFace.getNumberOfFrames() + " " + delay);
			while (mRunning) {
				if (hasWindowFocus()) {

					mFace.selectDrawable(mFrame++);
					if (mFrame == mFace.getNumberOfFrames()) {
						mFrame = 0;
					}
					mFace.run();
					postInvalidate();

					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					return;
				}
			}
		}
	}

	public void destroy() {
		mRunning = false;
		drawables.clear();
		drawables = null;
		cache = null;
	}

}
