package com.jianye.smart.module.workbench.bdo.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.oauth.BaiduOAuth;
import com.baidu.oauth.BaiduOAuth.BaiduOAuthResponse;
import com.baidu.pcs.BaiduPCSActionInfo;
import com.baidu.pcs.BaiduPCSClient;
import com.baidu.pcs.BaiduPCSStatusListener;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.view.CusScrollView;
import com.movit.platform.framework.view.CusListView;
import com.movit.platform.framework.view.viewpager.ImageViewPagerActivity;
import com.movit.platform.sc.module.zone.activity.ZonePublishActivity;
import com.jianye.smart.R;
import com.jianye.smart.module.workbench.activity.WebViewForDocActivity;
import com.jianye.smart.application.EOPApplication;
import com.jianye.smart.module.workbench.bdo.adapter.BDODocumentAdapter;

public class BDODocumentActivity extends Activity {
	TextView title;
	ImageView topLeft;
	ImageView topRight;
	CusScrollView cusScrollView;
	CusListView listView;

	BDODocumentAdapter adapter;
	private List<File> mData = new ArrayList<File>();

	Uri imageUri;// The Uri to store the big
	String currentTime;
	String takePicturePath = "";
	DialogUtils progressDialogUtil;
	private PopupWindow popupWindow;

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			progressDialogUtil.dismiss();
			switch (msg.what) {

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comm_activity_group);
		progressDialogUtil = DialogUtils.getInstants();
		iniView();
		Intent intent = getIntent();
		if (intent != null) {
			try {
				// 获得Intent的Action
				String action = intent.getAction();
				if (action.equals(Intent.ACTION_VIEW)) {
					// 获得Intent的MIME type
					String oldPath = getRealPathFromURI(intent.getData());
					FileUtils.copyFile(oldPath, CommConstants.SD_DOCUMENT
							+ (new File(oldPath)).getName());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		iniData();
	}

	private String getRealPathFromURI(Uri data) {
		// TODO Auto-generated method stub
		if ("content".equalsIgnoreCase(data.getScheme())) {
			Cursor cursor = null;
			final String column = "_data";
			final String[] projection = { column };
			try {
				cursor = getContentResolver().query(data, projection, null,
						null, null);
				if (cursor != null && cursor.moveToFirst()) {
					final int column_index = cursor
							.getColumnIndexOrThrow(column);
					return cursor.getString(column_index);
				}
			} finally {
				if (cursor != null)
					cursor.close();
			}
		}
		// File
		else if ("file".equalsIgnoreCase(data.getScheme())) {
			return data.getPath();
		}
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private void iniView() {
		listView = (CusListView) findViewById(R.id.group_listview);
		title = (TextView) findViewById(R.id.tv_common_top_title);
		topLeft = (ImageView) findViewById(R.id.common_top_left);
		topRight = (ImageView) findViewById(R.id.common_top_right);
		topRight.setImageResource(R.drawable.bdo_cloud_takephoto);
		title.setText("我的文档");
		topRight.setVisibility(View.GONE);
		topLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	private void iniData() {
		progressDialogUtil.showLoadingDialog(BDODocumentActivity.this,
				"正在加载...", false);
		File file = new File(CommConstants.SD_DOCUMENT);
		File[] listFiles = file.listFiles();
		for (int i = 0; i < listFiles.length; i++) {
			File subFile = listFiles[i];
			if (subFile.isFile()) {
				mData.add(listFiles[i]);
			}
		}

		setArapter();
	}

	private void setArapter() {
		progressDialogUtil.dismiss();
		adapter = new BDODocumentAdapter(BDODocumentActivity.this, mData,
				listView, handler);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (null != popupWindow) {
					popupWindow.dismiss();
					popupWindow = null;
					return;
				} else {
					initPopuptWindow(position);
				}

				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				} else {
					popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
				}
			}
		});
	}

	private void initPopuptWindow(int postion) {
		View contactView = LayoutInflater.from(BDODocumentActivity.this)
				.inflate(R.layout.bdo_bottom_menu, null);
		TextView del = (TextView) contactView.findViewById(R.id.bdo_bottom_del);
		TextView open = (TextView) contactView
				.findViewById(R.id.bdo_bottom_open);
		TextView openWithOther = (TextView) contactView
				.findViewById(R.id.bdo_bottom_openWithOther);
		TextView cloud = (TextView) contactView
				.findViewById(R.id.bdo_bottom_cloud);
		TextView more = (TextView) contactView
				.findViewById(R.id.bdo_bottom_more);
		del.setOnClickListener(new BottomMenuClick(postion));
		open.setOnClickListener(new BottomMenuClick(postion));
		openWithOther.setOnClickListener(new BottomMenuClick(postion));
		cloud.setOnClickListener(new BottomMenuClick(postion));
		more.setOnClickListener(new BottomMenuClick(postion));

		popupWindow = new PopupWindow(contactView,
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		popupWindow.setFocusable(false);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setAnimationStyle(R.style.popwindown_animstyle);
	}

	class BottomMenuClick implements OnClickListener {
		int postion;

		public BottomMenuClick(int postion) {
			super();
			this.postion = postion;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			File file = mData.get(postion);

			switch (v.getId()) {
			case R.id.bdo_bottom_del:
				file.delete();
				mData.remove(postion);
				popupWindow.dismiss();
				adapter.notifyDataSetChanged();
				break;
			case R.id.bdo_bottom_open:
				String suffix = new FileUtils().getFileSuffix(file);
				if (".jpeg".equals(suffix) || ".jpg".equals(suffix)
						|| ".png".equals(suffix)) {
					ZonePublishActivity.selectImagesList.clear();
					ZonePublishActivity.selectImagesList.add(file
							.getAbsolutePath());
					startActivity(new Intent(BDODocumentActivity.this,
							ImageViewPagerActivity.class)
							.putStringArrayListExtra("selectedImgs",
									ZonePublishActivity.selectImagesList)
							.putExtra("FromBucket", true));
					overridePendingTransition(0, 0);
				} else if (".txt".equals(suffix)) {
					String url = "file://" + file.getAbsolutePath();
					Intent intent = new Intent(BDODocumentActivity.this,
							WebViewForDocActivity.class);
					intent.putExtra("URL", url);
					intent.putExtra("TITLE", file.getName());
					startActivity(intent);
				} else {
					EOPApplication.showToast(BDODocumentActivity.this,
							"当前文件格式不支持，请用其他应用打开！");
				}
				break;
			case R.id.bdo_bottom_openWithOther:
				new FileUtils().openFile(BDODocumentActivity.this, file);
				break;
			case R.id.bdo_bottom_cloud:
				test_login();
				break;
			case R.id.bdo_bottom_more:

				break;
			default:
				break;
			}
		}
	}

	private final static String mbApiKey = "KH0tsEgVMiEFEGzU1NlTQwbq";
	// the default root folder
	/*
	 * mbRootPath should be your app_path, please instead of
	 * "/apps/pcstest_oauth"
	 */
	private final static String mbRootPath = "/apps/movit_im";
	private String mbOauth = null;

	private void test_login() {
		BaiduOAuth oauthClient = new BaiduOAuth();
		oauthClient.startOAuth(this, mbApiKey, new String[] { "basic",
				"netdisk" }, new BaiduOAuth.OAuthListener() {
			@Override
			public void onException(String msg) {
				Toast.makeText(getApplicationContext(), "Login failed " + msg,
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onComplete(BaiduOAuthResponse response) {
				if (null != response) {
					mbOauth = response.getAccessToken();
					Toast.makeText(
							getApplicationContext(),
							"Token: " + mbOauth + "    User name:"
									+ response.getUserName(),
							Toast.LENGTH_SHORT).show();
					test_upload();
				}
			}

			@Override
			public void onCancel() {
				Toast.makeText(getApplicationContext(), "Login cancelled",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void test_upload() {
		if (null != mbOauth) {
			Thread workThread = new Thread(new Runnable() {
				public void run() {
					String tmpFile = "/mnt/sdcard/123456.jpg";

					BaiduPCSClient api = new BaiduPCSClient();
					api.setAccessToken(mbOauth);
					final BaiduPCSActionInfo.PCSFileInfoResponse response = api
							.uploadFile(tmpFile, mbRootPath + "/123456.jpg",
									new BaiduPCSStatusListener() {
										@Override
										public void onProgress(long bytes,
												long total) {
											// TODO Auto-generated method stub
											final long bs = bytes;
											final long tl = total;
											handler.post(new Runnable() {
												public void run() {
													Toast.makeText(
															getApplicationContext(),
															"total: "
																	+ tl
																	+ "    sent:"
																	+ bs,
															Toast.LENGTH_SHORT)
															.show();
												}
											});
										}

										@Override
										public long progressInterval() {
											return 1000;
										}
									});
					handler.post(new Runnable() {
						public void run() {
							Toast.makeText(
									getApplicationContext(),
									response.status.errorCode + "  "
											+ response.status.message + "  "
											+ response.commonFileInfo.blockList,
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			});
			workThread.start();
		}
	}
}
