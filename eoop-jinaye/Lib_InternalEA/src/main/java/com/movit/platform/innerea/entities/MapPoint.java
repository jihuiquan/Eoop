package com.movit.platform.innerea.entities;

import java.io.Serializable;

public class MapPoint implements Serializable {

	int id;
	String ProjectName;
	String Address;
	String Longitude;
	String Latitude;
	int IsEnable;
	String CreatDateTime;
	String CreatUserId;
	String CreatUserName;
	String PropertyNames;
	String PropertyValues;
	
	String RoundRange;
	String UpTime;
	String DownTime;

	public MapPoint() {
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getProjectName() {
		return ProjectName;
	}


	public void setProjectName(String projectName) {
		ProjectName = projectName;
	}


	public String getAddress() {
		return Address;
	}


	public void setAddress(String address) {
		Address = address;
	}


	public String getLongitude() {
		return Longitude;
	}


	public void setLongitude(String longitude) {
		Longitude = longitude;
	}


	public String getLatitude() {
		return Latitude;
	}


	public void setLatitude(String latitude) {
		Latitude = latitude;
	}


	public int getIsEnable() {
		return IsEnable;
	}


	public void setIsEnable(int isEnable) {
		IsEnable = isEnable;
	}


	public String getCreatDateTime() {
		return CreatDateTime;
	}


	public void setCreatDateTime(String creatDateTime) {
		CreatDateTime = creatDateTime;
	}


	public String getCreatUserId() {
		return CreatUserId;
	}


	public void setCreatUserId(String creatUserId) {
		CreatUserId = creatUserId;
	}


	public String getCreatUserName() {
		return CreatUserName;
	}


	public void setCreatUserName(String creatUserName) {
		CreatUserName = creatUserName;
	}


	public String getPropertyNames() {
		return PropertyNames;
	}


	public void setPropertyNames(String propertyNames) {
		PropertyNames = propertyNames;
	}


	public String getPropertyValues() {
		return PropertyValues;
	}


	public void setPropertyValues(String propertyValues) {
		PropertyValues = propertyValues;
	}


	public String getRoundRange() {
		return RoundRange;
	}


	public void setRoundRange(String roundRange) {
		RoundRange = roundRange;
	}


	public String getUpTime() {
		return UpTime;
	}


	public void setUpTime(String upTime) {
		UpTime = upTime;
	}


	public String getDownTime() {
		return DownTime;
	}


	public void setDownTime(String downTime) {
		DownTime = downTime;
	}

	

}
