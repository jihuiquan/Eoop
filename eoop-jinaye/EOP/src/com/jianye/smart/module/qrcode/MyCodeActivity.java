package com.jianye.smart.module.qrcode;

import java.nio.charset.Charset;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.mining.app.zxing.view.ViewfinderView;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.jianye.smart.R;
import com.jianye.smart.application.EOPApplication;
import com.jianye.smart.module.workbench.activity.WebViewActivity;

public class MyCodeActivity extends MipcaActivity {
	private ImageView back;
	private ImageView topRight;
	private TextView title;
	String type;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);

		surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		back = (ImageView) findViewById(R.id.common_top_left);
		title = (TextView) findViewById(R.id.tv_common_top_title);
		topRight = (ImageView) findViewById(R.id.common_top_right);
		title.setText("扫一扫");
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d("test","back");
				MyCodeActivity.this.finish();

//				onBackPressed();
			}
		});
		topRight.setVisibility(View.GONE);

		type = getIntent().getStringExtra("type");
	}

	@Override
	public void handleDecode(Result result, Bitmap barcode) {

		Log.d("test","handleDecode");

		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		final String resultString = result.getText();

		Log.d("test","resultString="+resultString+",type="+type);

		if (resultString.equals("")) {
			Toast.makeText(MyCodeActivity.this, "扫描出错啦！",
					Toast.LENGTH_SHORT).show();
		} else {
			if ("sign".equals(type)) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							JSONObject object = new JSONObject();
							object.put("userName",spUtil.getString(CommConstants.EMPADNAME));
							String result = HttpClientUtils.post(resultString,
									object.toString(), Charset.forName("UTF-8"));
							mHandler.obtainMessage(1, result).sendToTarget();
						} catch (Exception e) {
							e.printStackTrace();
							mHandler.sendEmptyMessage(2);
						}
					}
				}).start();
			} else {
				Intent resultIntent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("result", resultString);
				bundle.putParcelable("bitmap", barcode);
				resultIntent.putExtras(bundle);
//				this.setResult(RESULT_OK, resultIntent);
				startActivity(new Intent(this, WebViewActivity.class).putExtra(
						"URL", resultString).putExtra("sao-sao", true));
			}
		}
		MyCodeActivity.this.finish();
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				try {
					String json = (String) msg.obj;
					JSONObject object = new JSONObject(json);
					int code = object.getInt("code");
					String message = object.getString("msg");
					if (code == 0) {
						MyCodeActivity.this.finish();
					}
					EOPApplication.showToast(context, message);
				} catch (JSONException e) {
					e.printStackTrace();
					EOPApplication.showToast(context, "签到失败");
				}
				break;
			case 2:
				EOPApplication.showToast(context, "签到失败");
				break;
			default:
				break;
			}
		}

	};
}
