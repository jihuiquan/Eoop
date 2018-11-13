package com.jianye.smart.module.gesture;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.movit.platform.framework.utils.StringUtils;
import com.jianye.smart.R;
import com.jianye.smart.base.BaseActivity;
import com.jianye.smart.view.widget.SwitchButton;

public class GestureActivity extends BaseActivity {

	private TextView title;
	private ImageView topLeft,topRight;

	private SwitchButton switchButton;
	private LinearLayout gestureModefy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gesture);
		initView();
	}

	private void initView() {
		title = (TextView) findViewById(R.id.tv_common_top_title);
		topLeft = (ImageView) findViewById(R.id.common_top_left);
		topRight = (ImageView) findViewById(R.id.common_top_right);
		switchButton = (SwitchButton) findViewById(R.id.gesture_switch);
		gestureModefy = (LinearLayout) findViewById(R.id.user_gesture_modify_Layout);
		title.setText("手势");
		topLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		topRight.setVisibility(View.GONE);

		String gestureCode = spUtil.getString("GestureCode");
		if (StringUtils.notEmpty(gestureCode)) {
			switchButton.setChecked(true);
			gestureModefy.setVisibility(View.VISIBLE);
		} else {
			switchButton.setChecked(false);
			gestureModefy.setVisibility(View.GONE);
		}

		switchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				String code = spUtil.getString("GestureCode");
				if (isChecked) {
					// 打开
					if (StringUtils.empty(code)) {
						startActivity(new Intent(context,
								GestureEditActivity.class));
					}
				} else {
					// 关闭
					if (StringUtils.notEmpty(code)) {
						startActivity(new Intent(context,
								GestureVerifyActivity.class).putExtra("type",
										GestureVerifyActivity.GestureTypeClose));
					}
				}
			}
		});
		gestureModefy.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 修改
				startActivity(new Intent(context, GestureVerifyActivity.class)
						.putExtra("type", GestureVerifyActivity.GestureTypeModify));
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		final String gestureCode = spUtil.getString("GestureCode");
		if (StringUtils.notEmpty(gestureCode)) {
			switchButton.setChecked(true);
			gestureModefy.setVisibility(View.VISIBLE);
		} else {
			switchButton.setChecked(false);
			gestureModefy.setVisibility(View.GONE);
		}
	}

}
