package com.movit.platform.framework.utils;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.movit.platform.common.constants.CommConstants;

public class SharedPreUtils {
	private SharedPreferences sp;
	//用于判断是否自动登录，从而决定是否在首页面调用升级接口
	public static final String autoLoginThisTime = "AUTO_LOGIN_THIS_TIME";

	public SharedPreUtils(Context paramContext) {
		this.sp = paramContext.getSharedPreferences(CommConstants.LOGIN_SET,
				Context.MODE_PRIVATE);

	}

	public String getString(String key) {
		return this.sp.getString(key, "");
	}

	public void setString(String key, String value) {
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public int getInteger(String key) {
		return this.sp.getInt(key, -1);
	}

	public void setInteger(String key, int value) {
		Editor editor = sp.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public boolean getBoolean(String key, boolean bool) {
		return this.sp.getBoolean(key, bool);
	}

	public void setBoolean(String key, Boolean value) {
		Editor editor = sp.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public Set<String> getStringSet(String key) {
		return this.sp.getStringSet(key, new HashSet<String>());
	}

	public void setStringSet(String key, Set<String> value) {
		Editor editor = sp.edit();
		editor.putStringSet(key, value);
		editor.commit();
	}

	public long getLong(String key) {
		return this.sp.getLong(key, -1);
	}

	public void setLong(String key, long value) {
		Editor editor = sp.edit();
		editor.putLong(key, value);
		editor.commit();
	}
}