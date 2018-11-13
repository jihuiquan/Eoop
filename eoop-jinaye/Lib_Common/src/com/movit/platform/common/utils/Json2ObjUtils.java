package com.movit.platform.common.utils;

import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.entities.UserInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Json2ObjUtils {

	public static OrganizationTree getOrgunFromJson(String json) throws Exception {
		OrganizationTree orgu = new OrganizationTree();
		JSONObject jsonObject = new JSONObject(json);
		String id = "";
		if (jsonObject.has("id")) {
			id = jsonObject.getString("id");
		}
		String objname = "";
		if (jsonObject.has("objname")) {
			objname = jsonObject.getString("objname");
		}
		String parentId = "";
		if (jsonObject.has("parentId")) {
			parentId = jsonObject.getString("parentId");
		}
		String deltaFlag = "";
		if (jsonObject.has("deltaFlag")) {
			deltaFlag = jsonObject.getString("deltaFlag");
		}

		orgu.setId(id);
		orgu.setObjname(objname);
		orgu.setParentId(parentId);
		orgu.setDeltaFlag(deltaFlag);
		return orgu;
	}

	public static UserInfo getUserInfoFromJson(String json) throws Exception {

		UserInfo userInfo = new UserInfo();
		JSONObject jsonObject = new JSONObject(json);
		String id = "";
		if (jsonObject.has("id")) {
			id = jsonObject.getString("id");
		}
		String empId = "";
		if (jsonObject.has("empId")) {
			empId = jsonObject.getString("empId");
		}
		String empAdname = "";
		if (jsonObject.has("empAdname")) {
			empAdname = jsonObject.getString("empAdname");
		}
		String empCname = "";
		if (jsonObject.has("empCname")) {
			empCname = jsonObject.getString("empCname");
		}
		String avatar = "";
		if (jsonObject.has("avatar")) {
			avatar = jsonObject.getString("avatar");
		}
		String gender = "";
		if (jsonObject.has("gender")) {
			gender = jsonObject.getString("gender");
		}
		String phone = "";
		if (jsonObject.has("phone")) {
			phone = jsonObject.getString("phone");
		}
		String mphone = "";
		if (jsonObject.has("mphone")) {
			mphone = jsonObject.getString("mphone");
		}
		String mail = "";
		if (jsonObject.has("mail")) {
			mail = jsonObject.getString("mail");
		}
		String actype = "";
		if (jsonObject.has("actype")) {
			actype = jsonObject.getString("actype");
		}
		String orgId = "";
		if (jsonObject.has("orgId")) {
			orgId = jsonObject.getString("orgId");
		}
		String openFireToken = "";
		if (jsonObject.has("openFireToken")) {
			openFireToken = jsonObject.getString("openFireToken");
		}
		String city = "";
		if (jsonObject.has("cityname")) {
			city = jsonObject.getString("cityname");
		}

		String nickName = "";
		if (jsonObject.has("nickName")) {
			nickName = jsonObject.getString("nickName");
		}

		int isLeader = 0;
		if (jsonObject.has("isLeader")) {
			isLeader = jsonObject.optInt("isLeader");
		}

		int callCount = 5;
		if (jsonObject.has("callCount")) {
			try{
				callCount = Integer.parseInt(jsonObject.getString("callCount"));
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		ArrayList<String> toBeAttentionPO = new ArrayList<String>();
		if (jsonObject.has("toBeAttentionPO")
				&& !jsonObject.isNull("toBeAttentionPO")) {
			try {
				JSONArray array = jsonObject.getJSONArray("toBeAttentionPO");
				for (int i = 0; i < array.length(); i++) {
					JSONObject object = array.getJSONObject(i);
					String userid = "";
					if (object.has("userid")) {
						userid = object.getString("userid");
						if (!toBeAttentionPO.contains(userid)) {
							toBeAttentionPO.add(userid);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		ArrayList<String> attentionPO = new ArrayList<String>();
		if (jsonObject.has("attentionPO") && !jsonObject.isNull("attentionPO")) {
			try {
				JSONArray array = jsonObject.getJSONArray("attentionPO");
				for (int i = 0; i < array.length(); i++) {
					JSONObject object = array.getJSONObject(i);
					String attentionid = "";
					if (object.has("attentionid")) {
						attentionid = object.getString("attentionid");
						if (!attentionPO.contains(attentionid)) {
							attentionPO.add(attentionid);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		String deltaFlag = "";
		if (jsonObject.has("deltaFlag")) {
			deltaFlag = jsonObject.getString("deltaFlag");
		}

		String fullNameSpell = "";
		if (jsonObject.has("fullNameSpell")) {
			fullNameSpell = jsonObject.getString("fullNameSpell");
		}
		String firstNameSpell = "";
		if (jsonObject.has("firstNameSpell")) {
			firstNameSpell = jsonObject.getString("firstNameSpell");
		}

		userInfo.setId(id);
		userInfo.setActype(actype);
		userInfo.setAvatar(avatar);
		userInfo.setEmpAdname(empAdname);
		userInfo.setEmpCname(empCname);
		userInfo.setEmpId(empId);
		userInfo.setGender(gender);
		userInfo.setMail(mail);
		userInfo.setMphone(mphone);
		userInfo.setOpenFireToken(openFireToken);
		userInfo.setOrgId(orgId);
		userInfo.setPhone(phone);
		userInfo.setCity(city);
		userInfo.setNickName(nickName);
		userInfo.setCallCount(callCount);
		userInfo.setToBeAttentionPO(toBeAttentionPO);
		userInfo.setAttentionPO(attentionPO);
		userInfo.setDeltaFlag(deltaFlag);
		userInfo.setFullNameSpell(fullNameSpell);
		userInfo.setFirstNameSpell(firstNameSpell);
		userInfo.setIsLeader(isLeader);
		return userInfo;
	}
	
}
