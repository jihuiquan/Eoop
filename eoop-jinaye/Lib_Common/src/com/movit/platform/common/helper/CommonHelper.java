package com.movit.platform.common.helper;

import android.content.Context;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.SharedPreUtils;

public class CommonHelper {

    private SharedPreUtils spUtil;

    public CommonHelper(Context context) {
        super();
        this.spUtil = new SharedPreUtils(context);
    }

    public void saveLoginConfig(LoginInfo loginConfig) {
        if (loginConfig == null) {
            return;
        }
        spUtil.setString(CommConstants.USERNAME, loginConfig.getUsername());
        spUtil.setString(CommConstants.PASSWORD, loginConfig.getPassword());

        spUtil.setString(CommConstants.EMPADNAME, loginConfig.getmUserInfo()
                .getEmpAdname());
        spUtil.setString(CommConstants.EMPCNAME, loginConfig.getmUserInfo()
                .getEmpCname());
        spUtil.setString(CommConstants.EMPID, loginConfig.getmUserInfo().getEmpId());

        spUtil.setString(CommConstants.GENDER, loginConfig.getmUserInfo()
                .getGender());
        spUtil.setString(CommConstants.AVATAR, loginConfig.getmUserInfo()
                .getAvatar());
        spUtil.setString(CommConstants.TOKEN, loginConfig.getmUserInfo()
                .getOpenFireToken());
        spUtil.setString(CommConstants.USERID, loginConfig.getmUserInfo().getId());
        spUtil.setInteger(CommConstants.CALL_COUNT, loginConfig.getmUserInfo().getCallCount());
        spUtil.setInteger(CommConstants.IS_LEADER, loginConfig.getmUserInfo().getIsLeader());

    }

    public LoginInfo getLoginConfig() {
        LoginInfo loginConfig = new LoginInfo();
        loginConfig.setUsername(spUtil.getString(CommConstants.USERNAME));
        loginConfig.setPassword(spUtil.getString(CommConstants.PASSWORD));
        UserInfo userInfo = new UserInfo();
        userInfo.setEmpId(spUtil.getString(CommConstants.EMPID));
        userInfo.setEmpAdname(spUtil.getString(CommConstants.EMPADNAME));
        userInfo.setEmpCname(spUtil.getString(CommConstants.EMPCNAME));
        userInfo.setAvatar(spUtil.getString(CommConstants.AVATAR));
        userInfo.setGender(spUtil.getString(CommConstants.GENDER));
        userInfo.setOpenFireToken(spUtil.getString(CommConstants.TOKEN));
        userInfo.setId(spUtil.getString(CommConstants.USERID));
        userInfo.setCallCount(spUtil.getInteger(CommConstants.CALL_COUNT));
        userInfo.setIsLeader(spUtil.getInteger(CommConstants.IS_LEADER));
        loginConfig.setmUserInfo(userInfo);

        return loginConfig;
    }

}
