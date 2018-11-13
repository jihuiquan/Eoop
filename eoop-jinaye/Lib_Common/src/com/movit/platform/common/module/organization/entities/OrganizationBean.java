package com.movit.platform.common.module.organization.entities;

import com.movit.platform.common.module.user.entities.UserInfo;

import java.io.Serializable;

public class OrganizationBean implements Serializable {

	private static final long serialVersionUID = 1L;
	String objName;
	UserInfo userInfo;

	public OrganizationBean(String objName, UserInfo userInfo) {
		super();
		this.objName = objName;
		this.userInfo = userInfo;
	}

	public String getObjName() {
		return objName;
	}

	public void setObjName(String objName) {
		this.objName = objName;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

}
