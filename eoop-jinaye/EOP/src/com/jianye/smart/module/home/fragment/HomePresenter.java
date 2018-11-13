package com.jianye.smart.module.home.fragment;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.utils.SharedPreUtils;
import okhttp3.Call;
import org.json.JSONException;

/**
 * @ClassName: HomePresenter
 * @Description:
 * @Author: chao
 * @Data 2017-08-01 17:25
 */

class HomePresenter {

  public static final String HOME_DATA = "home_data";
  private SharedPreUtils sharedPreUtils;
  private HomeView view;
  private HomeModel model;

  public HomePresenter(HomeView view, HomeModel model) {
    this.view = view;
    this.model = model;
    this.sharedPreUtils = new SharedPreUtils(((Fragment) view).getActivity());
  }

  public void init() {

    view.getUserData(model.getUserData());
    model.getHomeData(new StringCallback() {
      @Override
      public void onError(Call call, Exception e) {
        String homeData = sharedPreUtils.getString(HOME_DATA);
        if (!TextUtils.isEmpty(homeData)) {
          view.getTableData(JSONObject.parseObject(homeData, HomeBean.class));
        }
      }

      @Override
      public void onResponse(String response) throws JSONException {
        JSONObject object = JSON.parseObject(response);
        if (object.getBoolean("ok") && object.getJSONObject("objValue") != null) {
          sharedPreUtils.setString(HOME_DATA, object.getString("objValue"));
          HomeBean bean = JSONObject.parseObject(object.getString("objValue"), HomeBean.class);
          view.getTableData(bean);
        }
      }
    });
  }
}
