package com.jianye.smart.module.workbench.bdo.model;

public class BDOCloud {
	String url;
	String title;
	String time;
	String icon;
	String type;

	public BDOCloud(String url, String title, String time, String icon,
			String type) {
		super();
		this.url = url;
		this.title = title;
		this.time = time;
		this.icon = icon;
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
