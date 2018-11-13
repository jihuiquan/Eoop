package com.movit.platform.common.module.user.entities;

import java.io.Serializable;
import java.util.ArrayList;

public class UserInfo implements Cloneable, Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String empId;
	private String empAdname;
	private String empCname;
	private String avatar;
	private String gender;
	private String phone;
	private String mphone;
	private String mail;
	private int isLeader;//是否是领导:查看简历0、1
	private String actype;
	private String orgId;
	private String openFireToken;
	private String jobName;
	private String deptName;
	private int callCount;

	private String city;

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public int getCallCount() {
		return callCount;
	}

	public void setCallCount(int callCount) {
		this.callCount = callCount;
	}

	private String nickName;

	// 查询关键字
	private String fullNameSpell;
	private String firstNameSpell;

	ArrayList<String> toBeAttentionPO;
	ArrayList<String> attentionPO;

	String deltaFlag;

	@Override
	public String toString() {
		return "UserInfo [id=" + id + ", empId=" + empId + ", empAdname="
				+ empAdname + ", empCname=" + empCname + ", avatar=" + avatar
				+ ", gender=" + gender + ", phone=" + phone + ", mphone="
				+ mphone + ", mail=" + mail + ", actype=" + actype + ", orgId="
				+ orgId + ", openFireToken=" + openFireToken + ", city=" + city
				+ ", fullNameSpell=" + fullNameSpell + ", firstNameSpell="
				+ firstNameSpell + ", toBeAttentionPO=" + toBeAttentionPO
				+ ", attentionPO=" + attentionPO + ", deltaFlag=" + deltaFlag
				+ "]";
	}

	public Object clone() {
		UserInfo o = null;
		try {
			o = (UserInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return o;
	}

	@Override
	public boolean equals(Object o) {
		UserInfo userInfo = (UserInfo) o;
		if (this.id.equalsIgnoreCase(userInfo.getId())) {
			return true;
		} else {
			return false;
		}
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOpenFireToken() {
		return openFireToken;
	}

	public void setOpenFireToken(String openFireToken) {
		this.openFireToken = openFireToken;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmpId() {
		return empId;
	}

	public void setEmpId(String empId) {
		this.empId = empId;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getEmpAdname() {
		return empAdname;
	}

	public void setEmpAdname(String empAdname) {
		this.empAdname = empAdname;
	}

	public String getEmpCname() {
		return empCname;
	}

	public void setEmpCname(String empCname) {
		this.empCname = empCname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getMphone() {
		return mphone;
	}

	public void setMphone(String mphone) {
		this.mphone = mphone;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getActype() {
		return actype;
	}

	public void setActype(String actype) {
		this.actype = actype;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public ArrayList<String> getToBeAttentionPO() {
		return toBeAttentionPO;
	}

	public void setToBeAttentionPO(ArrayList<String> toBeAttentionPO) {
		this.toBeAttentionPO = toBeAttentionPO;
	}

	public ArrayList<String> getAttentionPO() {
		return attentionPO;
	}

	public void setAttentionPO(ArrayList<String> attentionPO) {
		this.attentionPO = attentionPO;
	}

	public String getDeltaFlag() {
		return deltaFlag;
	}

	public void setDeltaFlag(String deltaFlag) {
		this.deltaFlag = deltaFlag;
	}

	public String getFullNameSpell() {
		return fullNameSpell;
	}

	public void setFullNameSpell(String fullNameSpell) {
		this.fullNameSpell = fullNameSpell;
	}

	public String getFirstNameSpell() {
		return firstNameSpell;
	}

	public void setFirstNameSpell(String firstNameSpell) {
		this.firstNameSpell = firstNameSpell;
	}

	public int getIsLeader() {
		return isLeader;
	}

	public void setIsLeader(int isLeader) {
		this.isLeader = isLeader;
	}
}
