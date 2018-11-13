package com.movit.platform.innerea.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.innerea.R;
import com.squareup.picasso.Picasso;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @ClassName: ActionActivity
 * @Description: 打卡活动
 * @Author: chao
 * @Data 2017-08-08 09:21
 */
public class ActionActivity extends Activity implements OnClickListener {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_action);
    ImageView imageView = (ImageView) findViewById(R.id.action_img);
    loadImage(imageView);
    imageView.setOnClickListener(this);
  }

  /**
   * 加载图片
   */
  private void loadImage(ImageView imageView) {
    if (getIntent().getStringExtra("data") != null) {
      Picasso.with(this).load(getIntent().getStringExtra("data")).into(imageView);
    } else {
      OkHttpUtils.postString()
          .url("http://gzt.jianye.com.cn:80/eoop-api/r/sys/appmgtrest/queryAttendancePath")
          .content("{}")
          .build()
//          .url(CommConstants.URL_ATTENDANCE).build()
          .execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) throws JSONException {
              JSONObject object = new JSONObject(response);
              if (object.optBoolean("ok")) {
                Picasso.with(ActionActivity.this).load(object.optString("objValue")).fetch();
              }
            }
          });
    }
  }

  @Override
  public void onClick(View v) {
    finish();
  }
}
