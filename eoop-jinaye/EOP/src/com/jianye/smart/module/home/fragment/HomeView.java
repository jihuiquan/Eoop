package com.jianye.smart.module.home.fragment;

import com.movit.platform.common.module.user.entities.UserInfo;

/**
 * @ClassName: HomeView
 * @Description:
 * @Author: chao
 * @Data 2017-08-01 17:11
 */
interface HomeView {

  void getTableData(HomeBean homeData);
  void getUserData(UserInfo userInfo);
}
