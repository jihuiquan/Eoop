package com.jianye.smart.module.workbench.bdo.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.view.CusListView;
import com.movit.platform.framework.view.widget.SelectPicPopup;
import com.jianye.smart.R;
import com.jianye.smart.base.BaseActivity;
import com.jianye.smart.module.workbench.bdo.adapter.BDOCloudAdapter;
import com.jianye.smart.module.workbench.bdo.model.BDOCloud;
import com.movit.platform.framework.utils.PicUtils;

public class BDOCloudActivity extends BaseActivity {
	TextView title;
	ImageView topLeft,topRight;
	CusListView listView;

	BDOCloudAdapter adapter;
	private List<BDOCloud> mData = new ArrayList<BDOCloud>();

	SelectPicPopup popWindow;
	Uri imageUri;// The Uri to store the big
	String currentTime;

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
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
		iniView();
		iniData();
//		Intent intent = getIntent();  
//        //获得Intent的Action  
//        String action = intent.getAction();  
//        //获得Intent的MIME type  
//        String type = intent.getValue();
//        if(Intent.ACTION_SEND.equals(action) && type != null){  
//            //我们这里处理所有的文本类型  
//            if(type.startsWith("text/")){  
//                //处理获取到的文本，这里我们用TextView显示  
//            }  
//            //图片的MIME type有 image/png , image/jepg, image/gif 等，  
//            else if(type.startsWith("image/")){  
//                //处理获取到图片，我们用ImageView显示  
//            }  
//        }  
//        else if(Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null){  
//             if (type.startsWith("image/")) {  
//                    //处理多张图片，我们用一个GridView来显示  
//                }  
//        }  
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void iniView() {
		listView = (CusListView) findViewById(R.id.group_listview);
		title = (TextView) findViewById(R.id.tv_common_top_title);
		topLeft = (ImageView) findViewById(R.id.common_top_img_left);
		topRight = (ImageView) findViewById(R.id.common_top_img_right);
		topRight.setImageResource(R.drawable.bdo_cloud_takephoto);
		title.setText("云盘");
		topRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) context
						.getSystemService(context.INPUT_METHOD_SERVICE);
				if (imm.isActive()) {
					imm.hideSoftInputFromWindow(topRight.getWindowToken(), 0);
				}
				// 实例化SelectPicPopupWindow
				popWindow = new SelectPicPopup(BDOCloudActivity.this,
						itemsOnClick);
				// 显示窗口
				popWindow.showAtLocation(topRight, Gravity.BOTTOM
						| Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置

			}
		});
		topLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	private void iniData() {
		progressDialogUtil.showLoadingDialog(context, "正在加载...", false);
		String zip = "";
		String png = "";
		String doc = "";
		zip = CommConstants.URL_BDODOWNLOAD + "wget-1.13.4.zip";
		png = CommConstants.URL_BDODOWNLOAD + "立信PNG文件.png";
		doc = CommConstants.URL_BDODOWNLOAD + "立信Word文件.doc";

		// 1,2,6,7 文档 4，5图片,3文件夹
		BDOCloud cloud1 = new BDOCloud(doc, "excel", "", "", "1");
		BDOCloud cloud2 = new BDOCloud(zip, "pdf", "", "", "2");
		BDOCloud cloud3 = new BDOCloud("", "文件夹", "", "", "3");
		BDOCloud cloud4 = new BDOCloud("", "others", "", "", "0");
		BDOCloud cloud5 = new BDOCloud(png, "png", "", "", "5");
		BDOCloud cloud6 = new BDOCloud("", "ppt", "", "", "6");
		BDOCloud cloud7 = new BDOCloud(zip, "word", "", "", "7");
		BDOCloud cloud8 = new BDOCloud(png, "jpg", "", "", "4");
		BDOCloud cloud9 = new BDOCloud(doc, "excel", "", "", "1");
		BDOCloud cloud10 = new BDOCloud("", "文件夹", "", "", "3");
		BDOCloud cloud11 = new BDOCloud(doc, "word", "", "", "7");
		BDOCloud cloud12 = new BDOCloud("", "others", "", "", "0");
		mData.add(cloud1);
		mData.add(cloud2);
		mData.add(cloud3);
		mData.add(cloud4);
		mData.add(cloud5);
		mData.add(cloud6);
		mData.add(cloud7);
		mData.add(cloud8);
		mData.add(cloud9);
		mData.add(cloud10);
		mData.add(cloud11);
		mData.add(cloud12);
		setArapter();
	}

	private void setArapter() {
		progressDialogUtil.dismiss();
		adapter = new BDOCloudAdapter(context, mData, listView, handler);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				BDOCloud cloud = mData.get(position);
				String type = cloud.getType();
				if (type.equals("1") || type.equals("2") || type.equals("6")
						|| type.equals("7")) {
					// http://tusdk.com/sdk/TuSDK-for-Android-demo-1.8.5.zip

					// new FileUtils().openFile(context, file);
				} else if (type.equals("3")) {// folder

				} else if (type.equals("4") || type.equals("5")) {// jpg , png

				} else {// others

				}
			}
		});
	}

	private OnClickListener itemsOnClick = new OnClickListener() {

		public void onClick(View v) {
			popWindow.dismiss();
			switch (v.getId()) {
			case R.id.btn_take_photo:
				// 跳转相机拍照
				currentTime = DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
				imageUri = Uri.parse(CommConstants.IMAGE_FILE_LOCATION);
				if (imageUri == null) {
					return;
				}
				String sdStatus = Environment.getExternalStorageState();
				if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
					Toast.makeText(context, "找不到sd卡", 2000).show();
					return;
				}

                if (ContextCompat.checkSelfPermission(BDOCloudActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请 CAMERA 权限
                    ActivityCompat.requestPermissions(BDOCloudActivity.this, new String[]{Manifest.permission.CAMERA},
                            CommConstants.CAMERA_REQUEST_CODE);
                }else{
                    Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent2, 2);
                }

				break;
			case R.id.btn_pick_photo:
				currentTime = DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				// intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				startActivityForResult(intent, 1);
				break;
			default:
				break;
			}
		}
	};

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CommConstants.CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent2, 2);
            } else {
                // Permission Denied
                Toast.makeText(this,"访问相机权限未获得您授权，无法使用拍照功能。",Toast.LENGTH_LONG).show();
            }
        }
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			// 如果是直接从相册获取
			case 1:
				// 从相册中直接获取文件的真是路径，然后上传
				final String picPath = PicUtils.getPicturePath(data,
						BDOCloudActivity.this);
				Log.v("picPath", "===" + picPath);
				try {
					PicUtils.getSmallImageFromFileAndRotaing(picPath);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				break;
			// 如果是调用相机拍照时
			case 2:
				if (imageUri != null) {
					boolean copy = FileUtils.copyFile(CommConstants.SD_CARD
							+ "/temp.jpg", CommConstants.SD_CARD_IMPICTURES
							+ currentTime + ".jpg");
					new File(CommConstants.SD_CARD + "/temp.jpg").delete();
					if (copy) {
						String pathString = CommConstants.SD_CARD_IMPICTURES
								+ currentTime + ".jpg";
						Log.v("takePicturePath", "===" + pathString);

						PicUtils.scanImages(BDOCloudActivity.this, pathString);
						try {
							PicUtils.getSmallImageFromFileAndRotaing(pathString);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
				break;
			default:
				break;
			}
		} else {
			if (StringUtils.notEmpty(currentTime)) {
				String path = CommConstants.SD_CARD_IMPICTURES + currentTime
						+ ".jpg";
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				}
				String newPathString = PicUtils.getTempPicPath(path);
				File f = new File(newPathString);
				if (f.exists()) {
					f.delete();
				}
			}
		}
	}

}
