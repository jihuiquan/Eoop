package com.jianye.smart.module.home.fragment;

import com.alibaba.fastjson.JSONObject;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.common.okhttp.utils.AesUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.manager.HttpManager;

/**
 * @ClassName: HomeModel
 * @Description:
 * @Author: chao
 * @Data 2017-08-01 17:30
 */
class HomeModel {

  public void getHomeData(StringCallback callback) {
    JSONObject secretMsg = new JSONObject();
    JSONObject userId = new JSONObject();
    userId.put("userId", getUserData().getId());
    secretMsg.put("secretMsg", AesUtils.getInstance().encrypt(userId.toString()));
//    OkHttpUtils.post()
//        .url("http://gzt.jianye.com.cn:80/eoop-api/r/sys/appmgtrest/queryPersonalModules").params(secretMsg.toJSONString())
//        .build().execute(callback);
//    OkHttpUtils.postString()
//        .url("http://gzt.jianye.com.cn:80/eoop-api/r/sys/appmgtrest/queryPersonalModules").mediaType()
//        .content(secretMsg.toJSONString()).build().execute(callback);
    HttpManager.postJson("http://gzt.jianye.com.cn:80/eoop-api/r/sys/appmgtrest/queryPersonalModules",
        secretMsg.toJSONString(), callback);
  }

  public UserInfo getUserData() {
    return CommConstants.loginConfig.getmUserInfo();
  }
}
