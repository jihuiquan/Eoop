package com.jianye.smart.module.workbench.model;

import java.io.Serializable;

public class WorkTable implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 类型(type) "builtin_app","html5","native_app","thirdparty_app" 平台自带应用
	 * ,客户端的html5应用 ,手机的native应用 , 手机的third-part应用 状态码(status)
	 * "available","unavailable","offline" 应用的状态正常 , 应用为禁用状态 , 应用已下架
	 * 是否启用(display) "show","hide" 客户端用户已启用 , 客户端用户已禁用
	 */
	public WorkTable() {
	}

	String id;
	String name;
	String picture;
	String order;
	String status;
	String display;
	String type;
	String android_access_url;
	String remarks;
	String unread_host_address;
	String unreadUrl;
	String sso_url;
	String sso_method;
	String isToken;
	String token;

	@Override
	public String toString() {
		return "WorkTable{" +
				"name='" + name + '\'' +
				", status='" + status + '\'' +
				", android_access_url='" + android_access_url + '\'' +
				", remarks='" + remarks + '\'' +
				", display='" + display + '\'' +
				'}';
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAndroid_access_url() {
		return android_access_url;
	}

	public void setAndroid_access_url(String android_access_url) {
		this.android_access_url = android_access_url;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getUnread_host_address() {
		return unread_host_address;
	}

	public void setUnread_host_address(String unread_host_address) {
		this.unread_host_address = unread_host_address;
	}

	public String getUnreadUrl() {
		return unreadUrl;
	}

	public void setUnreadUrl(String unreadUrl) {
		this.unreadUrl = unreadUrl;
	}

	public String getSso_url() {
		return sso_url;
	}

	public void setSso_url(String sso_url) {
		this.sso_url = sso_url;
	}

	public String getSso_method() {
		return sso_method;
	}

	public void setSso_method(String sso_method) {
		this.sso_method = sso_method;
	}

	public String getIsToken() {
		return isToken;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setIsToken(String token) {
		isToken = token;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkTable other = (WorkTable) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
