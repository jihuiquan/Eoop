package com.movit.platform.common.module.user.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.entities.UserInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class UserDao {
	private static SQLiteDatabase database;
	public final static String DATABASE_FILENAME = "eoop.db"; // 这个是DB文件名字
	public final String PACKAGE_NAME; // 这个是自己项目包路径
	public final String DATABASE_PATH;// 获取存储位置地址

	public final String TABLE_ORG = "Orgunit";
	public final String TABLE_USER = "User";

	private static UserDao manager;

	private UserDao(Context mContext) {
		PACKAGE_NAME = mContext.getPackageName();
		DATABASE_PATH = "/data"
				+ Environment.getDataDirectory().getAbsolutePath() + "/"
				+ PACKAGE_NAME + "/databases";
		openDatabase();
	}

	public static UserDao getInstance(Context mContext) {
		if (manager == null || !database.isOpen()) {
			manager = new UserDao(mContext);
		}
		return manager;
	}

	private void openDatabase() {
		try {
			String databaseFilename = getUserDBFile();
			File dir = new File(DATABASE_PATH);
			boolean mkOk = true;
			if (!dir.exists()) {
				mkOk = dir.mkdir();
			}
			if (mkOk && !(new File(databaseFilename)).exists()
					&& (new File(CommConstants.SD_DOWNLOAD,getUserDBFileName())).exists()) {

				InputStream is = new FileInputStream(CommConstants.SD_DOWNLOAD
						+ getUserDBFileName());

				FileOutputStream fos = new FileOutputStream(databaseFilename);
				byte[] buffer = new byte[8 * 1024];
				int count;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}
			database = SQLiteDatabase.openOrCreateDatabase(databaseFilename,
					null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getUserDBFile() {
		return DATABASE_PATH + "/" + DATABASE_FILENAME;
	}

	private String getUserDBFileName() {
		return DATABASE_FILENAME;
	}

	public ArrayList<OrganizationTree> getAllOrgunitions() {

		Cursor c = database.rawQuery("SELECT * FROM " + TABLE_ORG +" order by sort", null);

		ArrayList<OrganizationTree> nodes = new ArrayList<OrganizationTree>();
		while (c.moveToNext()) {
			String orgId = c.getString(c.getColumnIndex("orgId"));
			String parentId = c.getString(c.getColumnIndex("parentId"));
			String objname = c.getString(c.getColumnIndex("objName"));
			OrganizationTree org = new OrganizationTree();
			org.setId(orgId);
			org.setParentId(parentId);
			org.setObjname(objname);
			nodes.add(org);
		}
		if (c != null) {
			c.close();
		}
		return nodes;
	}

	public ArrayList<UserInfo> getAllUserInfos() {
		Cursor c = database.rawQuery("SELECT * FROM " + TABLE_USER+" order by sort", null);

		ArrayList<UserInfo> nodes = new ArrayList<UserInfo>();
		while (c.moveToNext()) {
			nodes.add(convertUserInfo(c));
		}
		if (c != null) {
			c.close();
		}
		return nodes;
	}

	public OrganizationTree getOrganizationByName(String objName) {
		Cursor c = database.query(TABLE_ORG, null, "objName = ?",
				new String[] { objName }, null, null, null);
		if (c.moveToNext()) {
			OrganizationTree org = new OrganizationTree();
			String orgId = c.getString(c.getColumnIndex("orgId"));
			String parentId = c.getString(c.getColumnIndex("parentId"));
			String objname = c.getString(c.getColumnIndex("objName"));
			org.setObjname(objname);
			org.setId(orgId);
			org.setParentId(parentId);
			if (c != null) {
				c.close();
			}
			return org;
		}
		if (c != null) {
			c.close();
		}
		return null;
	}

	public OrganizationTree getOrganizationByOrgId(String orgid) {
		Cursor c = database.query(TABLE_ORG, null, "orgId = ?",
				new String[] { orgid }, null, null, null);
		if (c.moveToNext()) {
			OrganizationTree org = new OrganizationTree();
			String orgId = c.getString(c.getColumnIndex("orgId"));
			String parentId = c.getString(c.getColumnIndex("parentId"));
			String objname = c.getString(c.getColumnIndex("objName"));
			org.setObjname(objname);
			org.setId(orgId);
			org.setParentId(parentId);
			if (c != null) {
				c.close();
			}
			return org;
		}
		if (c != null) {
			c.close();
		}
		return null;
	}

	public ArrayList<OrganizationTree> getAllOrganizationsByParentId(String id) {
		Cursor c = database.query(TABLE_ORG, null, "parentId = ?",
				new String[] { id }, null, null, null);
		ArrayList<OrganizationTree> nodes = new ArrayList<>();
		while (c.moveToNext()) {
			OrganizationTree org = new OrganizationTree();
			String orgId = c.getString(c.getColumnIndex("orgId"));
			String parentId = c.getString(c.getColumnIndex("parentId"));
			String objname = c.getString(c.getColumnIndex("objName"));
			org.setObjname(objname);
			org.setId(orgId);
			org.setParentId(parentId);
			nodes.add(org);
		}
		if (!c.isClosed()) {
			c.close();
		}
		return nodes;
	}

	public ArrayList<UserInfo> getAllUserInfosByOrgId(String orgId) {
		Cursor c = database.query(TABLE_USER, null, "orgId = ?",
				new String[] { orgId }, null, null, null);
		ArrayList<UserInfo> nodes = new ArrayList<UserInfo>();
		while (c.moveToNext()) {
			nodes.add(convertUserInfo(c));
		}
		if (!c.isClosed()) {
			c.close();
		}
		return nodes;
	}

	public UserInfo getUserInfoById(String id) {
		Cursor c = database.query(TABLE_USER, null, "userId = ?",
				new String[] { id }, null, null, null);
		if (c.moveToNext()) {
			return convertUserInfo(c);
		}
		if (!c.isClosed()) {
			c.close();
		}
		return null;
	}

	public ArrayList<UserInfo> getAllUserInfosBySearch(String search) {
		String sql = "SELECT * FROM "
				+ TABLE_USER
				+ " where empAdname like ? or empCname like ? or fullNameSpell like ? or firstNameSpell like ? or phone like ? or mphone like ? COLLATE NOCASE";
		Cursor c = database.rawQuery(sql, new String[] { "%" + search + "%",
				"%" + search + "%", "%" + search + "%", "%" + search + "%",
				"%" + search + "%", "%" + search + "%" });
		ArrayList<UserInfo> nodes = new ArrayList<UserInfo>();
		while (c.moveToNext()) {
			nodes.add(convertUserInfo(c));
		}
		if (!c.isClosed()) {
			c.close();
		}
		return nodes;
	}

	public UserInfo getUserInfoByADName(String adName) {
		Cursor c = database.query(TABLE_USER, null,
				"empAdname = ? COLLATE NOCASE", new String[] { adName }, null,
				null, null);
		if (c.moveToNext()) {

			return convertUserInfo(c);
		}
		if (!c.isClosed()) {
			c.close();
		}
		return null;
	}

	public UserInfo convertUserInfo(Cursor c) {
		UserInfo userInfo = new UserInfo();
		String userId = c.getString(c.getColumnIndex("userId"));
		String empId = c.getString(c.getColumnIndex("empId"));
		String empAdname = c.getString(c.getColumnIndex("empAdname"));
		String empCname = c.getString(c.getColumnIndex("empCname"));
		String avatar = c.getString(c.getColumnIndex("avatar"));
		String gender = c.getString(c.getColumnIndex("gender"));
		String phone = c.getString(c.getColumnIndex("phone"));
		String mphone = c.getString(c.getColumnIndex("mphone"));
		String mail = c.getString(c.getColumnIndex("mail"));
		String actype = c.getString(c.getColumnIndex("actype"));
		String orgId = c.getString(c.getColumnIndex("orgId"));
		String city = c.getString(c.getColumnIndex("cityName"));
		String deptName = c.getString(c.getColumnIndex("deptName"));
		String jobName = c.getString(c.getColumnIndex("jobName"));

		String fullNameSpell = c.getString(c.getColumnIndex("fullNameSpell"));
		String firstNameSpell = c.getString(c.getColumnIndex("firstNameSpell"));

		userInfo.setId(userId);
		userInfo.setActype(actype);
		userInfo.setAvatar(avatar);
		userInfo.setEmpAdname(empAdname);
		userInfo.setEmpCname(empCname);
		userInfo.setEmpId(empId);
		userInfo.setGender(gender);
		userInfo.setMail(mail);
		userInfo.setMphone(mphone);
		userInfo.setOrgId(orgId);
		userInfo.setPhone(phone);
		userInfo.setCity(city);
		userInfo.setFullNameSpell(fullNameSpell);
		userInfo.setFirstNameSpell(firstNameSpell);
		userInfo.setDeptName(deptName);
		userInfo.setJobName(jobName);
		return userInfo;
	}

	public void updateOrgByFlags(OrganizationTree org) {
		if ("create".equals(org.getDeltaFlag())) {
			replaceOrgunition(org);
		} else if ("delete".equals(org.getDeltaFlag())) {
			deleteOrg(org);
		} else if ("update".equals(org.getDeltaFlag())) {
			replaceOrgunition(org);
		}
	}

	public void updateUserByFlags(UserInfo user) {
		if ("create".equals(user.getDeltaFlag())) {
			replaceUser(user);
		} else if ("delete".equals(user.getDeltaFlag())) {
			deleteUser(user);
		} else if ("update".equals(user.getDeltaFlag())) {
			replaceUser(user);
		}
	}

	public int replaceOrgunition(OrganizationTree org) {
		CommConstants.allOrgunits.remove(org);
		CommConstants.allOrgunits.add(org);
		ContentValues values = new ContentValues();
		values.put("orgId", org.getId());
		values.put("parentId", org.getParentId());
		values.put("objName", org.getObjname());
		long sid = database.replace(TABLE_ORG, null, values);
		return (int) sid;
	}

	public int replaceUser(UserInfo user) {
		if(null!= CommConstants.allUserInfos){
			CommConstants.allUserInfos.remove(user);
		}else{
			CommConstants.allUserInfos = new ArrayList<UserInfo>();
		}
		CommConstants.allUserInfos.add(user);
		ContentValues values = new ContentValues();
		values.put("userId", user.getId());
		values.put("empId", user.getEmpId());
		values.put("empAdname", user.getEmpAdname());
		values.put("empCname", user.getEmpCname());
		values.put("avatar", user.getAvatar());
		values.put("gender", user.getGender());
		values.put("phone", user.getPhone());
		values.put("mphone", user.getMphone());
		values.put("mail", user.getMail());
		values.put("actype", user.getActype());
		values.put("orgId", user.getOrgId());
		values.put("cityName", user.getCity());
		values.put("fullNameSpell", user.getFullNameSpell());
		values.put("firstNameSpell", user.getFirstNameSpell());

		long sid = database.replace(TABLE_USER, null, values);
		return (int) sid;
	}

	public void deleteUser(UserInfo user) {
		CommConstants.allUserInfos.remove(user);
		database.delete(TABLE_USER, "userId = ?", new String[] { user.getId() });
	}

	public void deleteOrg(OrganizationTree orgu) {
		CommConstants.allOrgunits.remove(orgu);
		database.delete(TABLE_ORG, "orgId = ?", new String[] { orgu.getId() });
	}

	/**
	 * 关闭数据库
	 */
	public void closeDb() {
//		database.close();
	}

	public boolean deleteDb() {
		if (null != database) {
			database.close();
		}
		String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;
		File file = new File(databaseFilename);
		return file.exists() && file.delete();
	}

}
