package com.jianye.smart.utils;

import com.jianye.smart.module.workbench.attendance.model.Attendance;
import com.jianye.smart.module.workbench.model.WorkTable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Json2ObjUtils {

	public static ArrayList<WorkTable> getAllmodules(String json)
			throws Exception {
		JSONObject object = new JSONObject(json);
		boolean ok = object.getBoolean("ok");
		ArrayList<WorkTable> tables = new ArrayList<WorkTable>();
		if (ok) {
			JSONArray array = object.getJSONArray("objValue");
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsonObject = array.getJSONObject(i);
				WorkTable workTable = new WorkTable();
				String id = "";
				if (jsonObject.has("id")) {
					id = jsonObject.getString("id");
				}
				String name = "";
				if (jsonObject.has("name")) {
					name = jsonObject.getString("name");
				}
				String picture = "";
				if (jsonObject.has("picture")) {
					picture = jsonObject.getString("picture");
				}
				String order = "";
				if (jsonObject.has("order")) {
					order = jsonObject.getString("order");
				}
				String status = "";
				if (jsonObject.has("status")) {
					status = jsonObject.getString("status");
				}
				String display = "";
				if (jsonObject.has("display")) {
					display = jsonObject.getString("display");
				}
				String type = "";
				if (jsonObject.has("type")) {
					type = jsonObject.getString("type");
				}
				String android_access_url = "";
				if (jsonObject.has("android_access_url")) {
					android_access_url = jsonObject
							.getString("android_access_url");
				}
                String remarks = "";
                if (jsonObject.has("remarks")) {
                    remarks = jsonObject.getString("remarks");
                }
				String unread_host_address = "";
                if (jsonObject.has("unread_host_address")) {
					unread_host_address = jsonObject.getString("unread_host_address");
                }

				String unreadUrl = "";
				if (jsonObject.has("unreadUrl")) {
					unreadUrl = jsonObject.getString("unreadUrl");
				}

				workTable.setId(id);
				workTable.setName(name);
				workTable.setPicture(picture);
				workTable.setOrder(order);
				workTable.setStatus(status);
				workTable.setDisplay(display);
				workTable.setType(type);
				workTable.setAndroid_access_url(android_access_url);
                workTable.setRemarks(remarks);
                workTable.setUnread_host_address(unread_host_address);
				workTable.setUnreadUrl(unreadUrl);
				tables.add(workTable);
			}
		}
		return tables;
	}

	public static ArrayList<Attendance> getAttendanceListData(String json)
			throws Exception {
		JSONObject object = new JSONObject(json);
		boolean ok = object.getBoolean("ok");
		ArrayList<Attendance> list = new ArrayList<Attendance>();
		if (ok) {
			JSONArray array = object.getJSONArray("objValue");
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsonObject = array.getJSONObject(i);
				Attendance attendance = new Attendance();
				String attdendanceId = "";
				if (jsonObject.has("attdendanceId")) {
					attdendanceId = jsonObject.getString("attdendanceId");
				}
				String position = "";
				if (jsonObject.has("position")) {
					position = jsonObject.getString("position");
				}
				String attendanceTime = "";
				if (jsonObject.has("attendanceTime")) {
					attendanceTime = jsonObject.getString("attendanceTime");
				}
				String picture = "";
				if (jsonObject.has("picture")) {
					picture = jsonObject.getString("picture");
				}
				String cause = "";
				if (jsonObject.has("cause")) {
					cause = jsonObject.getString("cause");
				}
				attendance.setLocation(position);
				attendance.setReason(cause);
				attendance.setTime(attendanceTime);
				attendance.setPhotos(picture);
				attendance.setId(attdendanceId);
				list.add(attendance);
			}
		}
		return list;
	}
}
