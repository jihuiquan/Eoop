package com.jianye.smart.module.mine.activity;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.jianye.smart.R;
import com.jianye.smart.base.BaseActivity;
import com.jianye.smart.application.EOPApplication;
import com.jianye.smart.utils.Obj2JsonUtils;

public class RePasswordActivity extends BaseActivity {

	private TextView title;
	private TextView commenLeft;
	private TextView commenRight;
	private EditText oldPassword;
	private EditText newPassword;
	private EditText confirmPassword;
	private String newName;
	String type;
	String text;

	Pattern pattern = Pattern.compile("^[A-Za-z0-9_]{6,20}$");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_repassword);
		initView();
	}

	private void initView() {
		title = (TextView) findViewById(R.id.tv_common_top_title);
		commenLeft = (TextView) findViewById(R.id.style1_top_left);
		commenRight = (TextView) findViewById(R.id.style1_top_right);
		oldPassword = (EditText) findViewById(R.id.old_password);
		newPassword = (EditText) findViewById(R.id.new_password);
		confirmPassword = (EditText) findViewById(R.id.confirm_password);
		title.setText("修改密码");
		commenLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});
		commenRight.setText("保存");
		commenRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String old = oldPassword.getText().toString();
				String newStr = newPassword.getText().toString();
				String confirm = confirmPassword.getText().toString();
				String password = spUtil.getString(CommConstants.PASSWORD);
				if (old.equals(password)) {
					if (newStr.equals(confirm)) {
						Matcher matcher = pattern.matcher(newStr);
						if (matcher.matches()) {
							updateUserPassword(newStr);
						} else {
							EOPApplication.showToast(context,
									"密码只能包含数字，字母，下划线，位数在6-20位之间");
						}
					} else {
						EOPApplication.showToast(context, "新密码两次输入不一致！");
					}
				} else {
					EOPApplication.showToast(context, "原密码不正确！");
				}
			}
		});
	}

	public void updateUserPassword(final String newPassWord) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Map<String, String> paramsMap = new HashMap<String, String>();
					paramsMap.put("id", spUtil.getString(CommConstants.USERID));
					paramsMap.put("empAdpassword", newPassWord);
					String json = Obj2JsonUtils.map2json(paramsMap);
					String result = HttpClientUtils.post(CommConstants.URL_STUDIO
							+ "updateUserInfo", json, Charset.forName("UTF-8"));
					Log.v("json", result);
					// {"objValue":null,"ok":true,"value":null}
					JSONObject object = new JSONObject(result);
					boolean ok = object.getBoolean("ok");
					if (ok) {
						handler.sendEmptyMessage(1);
					} else {
						handler.sendEmptyMessage(2);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handler.sendEmptyMessage(2);
				}
			}
		}).start();

	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				progressDialogUtil.dismiss();
				EOPApplication.showToast(context, "密码修改成功！");
				spUtil.setString(CommConstants.PASSWORD, newPassword.getText()
						.toString());
				onBackPressed();
				break;
			case 2:
				progressDialogUtil.dismiss();
				EOPApplication.showToast(context, "密码修改失败！");
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
