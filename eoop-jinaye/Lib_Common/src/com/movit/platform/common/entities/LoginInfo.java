package com.movit.platform.common.entities;

import com.movit.platform.common.module.user.entities.UserInfo;

/**
 * 登录配置.
 *
 * @author Potter.Tao
 */
public class LoginInfo {

    private String username;// 用户名
    private String password;// 密码
    private UserInfo mUserInfo; // 当前用户的信息，可能需要使用static

    @Override
    public String toString() {
        return "LoginInfo [username=" + username + ", password=" + password
                + ", mUserInfo=" + mUserInfo
                + "]";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserInfo getmUserInfo() {
        return mUserInfo;
    }

    public void setmUserInfo(UserInfo mUserInfo) {
        this.mUserInfo = mUserInfo;
    }

}
