package com.jianye.smart.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.jianye.smart.R;
import com.jianye.smart.module.qrcode.InputCodeActivity;
import com.jianye.smart.activity.LoginActivity;
import com.jianye.smart.base.CompanyInfoable;

public class TourDialogView extends LinearLayout {

	private Context context;

	private Dialog dialog;
	ViewPager viewPager;
	private ArrayList<View> pageViews;
	private WelcomeViewPagerAdapter viewPagerAdapter;
	private ImageView[] imageViews;
	private View view1, view2, view3, view4, view5;// 各个页卡
	ImageView imageView1_1, imageView1_2, imageView1_3, imageView1_4,
			imageView2_1, imageView2_2, imageView2_3, imageView2_4,
			imageView3_1, imageView3_2, imageView3_3, imageView3_4,
			imageView4_1, imageView4_2, imageView4_3, imageView5_1,
			imageView5_2, imageView5_3, people_1, people_2, people_3, people_4,
			people_5;

	private SharedPreUtils spUtil;
	Handler handler = new Handler() {

		@Override
		public void dispatchMessage(Message msg) {
			// TODO Auto-generated method stub
			super.dispatchMessage(msg);
			if (msg.what == 1) {
				if (dialog != null) {
					dialog.dismiss();
					dialog = null;
					((Activity) context).finish();
				}
				spUtil.setBoolean(CommConstants.IS_SHOW_TOUR, false);
			} else if (msg.what == 5) {
				context.startActivity(new Intent(context, LoginActivity.class)
						.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
				handler.sendEmptyMessage(1);
			}
		}

	};

	public TourDialogView(Context context) {
		super(context);
		this.context = context;
		spUtil = new SharedPreUtils(context);
	}

	public void showDialog() {
		try {
			spUtil.setInteger(
					CommConstants.ORIGINAL_VERSION,
					context.getPackageManager().getPackageInfo(
							context.getPackageName(),
							PackageManager.GET_META_DATA).versionCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		View dialogView = inflate(context, R.layout.dialog_welcome_phone, null);
		imageViews = new ImageView[5];
		imageViews[0] = (ImageView) dialogView
				.findViewById(R.id.welcome_indicator_01);
		imageViews[1] = (ImageView) dialogView
				.findViewById(R.id.welcome_indicator_02);
		imageViews[2] = (ImageView) dialogView
				.findViewById(R.id.welcome_indicator_03);
		imageViews[3] = (ImageView) dialogView
				.findViewById(R.id.welcome_indicator_04);
		imageViews[4] = (ImageView) dialogView
				.findViewById(R.id.welcome_indicator_05);

		viewPager = (ViewPager) dialogView.findViewById(R.id.guidePages);
		pageViews = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(context);

		view1 = inflater.inflate(R.layout.dialog_welcome1_phone, null);
		view2 = inflater.inflate(R.layout.dialog_welcome2_phone, null);
		view3 = inflater.inflate(R.layout.dialog_welcome3_phone, null);
		view4 = inflater.inflate(R.layout.dialog_welcome4_phone, null);
		view5 = inflater.inflate(R.layout.dialog_welcome5_phone, null);
		pageViews.add(view1);
		pageViews.add(view2);
		pageViews.add(view3);
		pageViews.add(view4);
		pageViews.add(view5);

		viewPagerAdapter = new WelcomeViewPagerAdapter(pageViews);

		viewPager.setAdapter(viewPagerAdapter);
		viewPager.setCurrentItem(0);
		initImageView();
		initAnimationForEachView(0);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(final int arg0) {
				// TODO Auto-generated method stub
				for (int i = 0; i < imageViews.length; i++) {
					if (arg0 == i) {
						imageViews[i]
								.setImageResource(R.drawable.im_welcome_page_indicator_focused);
					} else {
						imageViews[i]
								.setImageResource(R.drawable.im_welcome_page_indicator_unfocused);
					}
				}

				initAnimationForEachView(arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});

		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
		dialog = new Dialog(this.context, R.style.tourDialogTheme);
		dialog.setContentView(dialogView, new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		dialog.setCancelable(false);
		dialog.show();
	}

	private void initImageView() {
		// TODO Auto-generated method stub
		imageView1_1 = (ImageView) view1.findViewById(R.id.welcome1_imageView1);
		imageView1_2 = (ImageView) view1.findViewById(R.id.welcome1_imageView2);
		imageView1_3 = (ImageView) view1.findViewById(R.id.welcome1_imageView3);
		imageView1_4 = (ImageView) view1.findViewById(R.id.welcome1_imageView4);
		imageView2_1 = (ImageView) view2.findViewById(R.id.welcome2_imageView1);
		imageView2_2 = (ImageView) view2.findViewById(R.id.welcome2_imageView2);
		imageView2_3 = (ImageView) view2.findViewById(R.id.welcome2_imageView3);
		imageView2_4 = (ImageView) view2.findViewById(R.id.welcome2_imageView4);
		imageView3_1 = (ImageView) view3.findViewById(R.id.welcome3_imageView1);
		imageView3_2 = (ImageView) view3.findViewById(R.id.welcome3_imageView2);
		imageView3_3 = (ImageView) view3.findViewById(R.id.welcome3_imageView3);
		imageView3_4 = (ImageView) view3.findViewById(R.id.welcome3_imageView4);
		imageView4_1 = (ImageView) view4.findViewById(R.id.welcome4_imageView1);
		imageView4_2 = (ImageView) view4.findViewById(R.id.welcome4_imageView2);
		imageView4_3 = (ImageView) view4.findViewById(R.id.welcome4_imageView3);
		imageView5_1 = (ImageView) view5.findViewById(R.id.welcome5_imageView1);
		imageView5_2 = (ImageView) view5.findViewById(R.id.welcome5_imageView2);
		imageView5_3 = (ImageView) view5.findViewById(R.id.welcome5_imageView3);
		people_1 = (ImageView) view1.findViewById(R.id.welcome1_people1);
		people_2 = (ImageView) view2.findViewById(R.id.welcome2_people2);
		people_3 = (ImageView) view3.findViewById(R.id.welcome3_people3);
		people_4 = (ImageView) view4.findViewById(R.id.welcome4_people4);
		people_5 = (ImageView) view5.findViewById(R.id.welcome5_people5);

	}

	private void initAnimationForEachView(int i) {
		Animation animationSet = AnimationUtils.loadAnimation(context,
				R.anim.scaletor_set);
		TranslateAnimation animation1 = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, -0.8f,
				Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT,
				0f, Animation.RELATIVE_TO_PARENT, 0f);
		animation1.setDuration(2000);
		TranslateAnimation animation2 = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, -0.7f,
				Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT,
				0f, Animation.RELATIVE_TO_PARENT, 0f);
		animation2.setDuration(2000);
		switch (i) {
		case 0:
			imageView1_1.startAnimation(animationSet);
			imageView1_2.startAnimation(animationSet);
			imageView1_3.startAnimation(animationSet);
			imageView1_4.startAnimation(animationSet);
			people_1.startAnimation(animation1);
			break;
		case 1:
			imageView2_1.startAnimation(animationSet);
			imageView2_2.startAnimation(animationSet);
			imageView2_3.startAnimation(animationSet);
			imageView2_4.startAnimation(animationSet);
			people_2.startAnimation(animation2);
			break;
		case 2:
			imageView3_1.startAnimation(animationSet);
			imageView3_2.startAnimation(animationSet);
			imageView3_3.startAnimation(animationSet);
			imageView3_4.startAnimation(animationSet);
			people_3.startAnimation(animation1);
			break;
		case 3:
			imageView4_1.startAnimation(animationSet);
			imageView4_2.startAnimation(animationSet);
			imageView4_3.startAnimation(animationSet);
			people_4.startAnimation(animation2);
			break;
		case 4:
			imageView5_1.startAnimation(animationSet);
			imageView5_2.startAnimation(animationSet);
			imageView5_3.startAnimation(animationSet);
			people_5.startAnimation(animation1);
			break;
		default:
			break;
		}

	}

	class WelcomeViewPagerAdapter extends PagerAdapter {

		private List<View> mListViews;

		public WelcomeViewPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mListViews.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mListViews.get(position), 0);
			View view = mListViews.get(position);
			if (position == (getCount() - 1)) {
				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String ip = spUtil.getString("ip");
						if (StringUtils.empty(ip)) {
							context.startActivity(new Intent(context,
									InputCodeActivity.class)
									.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
							handler.sendEmptyMessage(1);
						} else {
							new Thread(
									new CompanyInfoable(context, handler))
									.start();
						}
					}
				});
			}
			return mListViews.get(position);
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}
}
