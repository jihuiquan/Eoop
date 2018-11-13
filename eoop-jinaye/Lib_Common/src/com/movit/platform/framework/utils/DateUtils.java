package com.movit.platform.framework.utils;

/**   
 * 用一句话描述该文件做什么.
 * @title DateUtils.java
 * @package com.sinsoft.android.util
 * @author shimiso  
 * @update 2012-6-26 上午9:57:56  
 */

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期操作工具类.
 * 
 * @author shimiso
 */

public class DateUtils {

	public static final String yyyyMMdd = "yyyyMMdd";
	public static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT_FULL = "yyyy-MM-dd HH:mm:ss.sss";

	public static Date str2Date(String str) {
		return str2Date(str, null);
	}

	public static Date str2Date(String str, String format) {
		if (str == null || str.length() == 0) {
			return null;
		}
		if (format == null || format.length() == 0) {
			format = FORMAT;
		}
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			date = sdf.parse(str);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;

	}

	public static Calendar str2Calendar(String str) {
		return str2Calendar(str, null);

	}

	public static Calendar str2Calendar(String str, String format) {

		Date date = str2Date(str, format);
		if (date == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		return c;

	}

	public static String date2Str(Calendar c) {// yyyy-MM-dd HH:mm:ss
		return date2Str(c, null);
	}

	public static String date2Str(Calendar c, String format) {
		if (c == null) {
			return null;
		}
		return date2Str(c.getTime(), format);
	}

	public static String date2Str(Date d) {// yyyy-MM-dd HH:mm:ss
		return date2Str(d, null);
	}

	public static String date2Str(Date d, String format) {// yyyy-MM-dd HH:mm:ss
		if (d == null) {
			return null;
		}
		if (format == null || format.length() == 0) {
			format = FORMAT;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String s = sdf.format(d);
		return s;
	}

	public static String getCurDateStr() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		return c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-"
				+ c.get(Calendar.DAY_OF_MONTH) + "-"
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE)
				+ ":" + c.get(Calendar.SECOND);
	}

	/**
	 * 获得当前日期的字符串格式
	 * 
	 * @param format
	 * @return
	 */
	public static String getCurDateStr(String format) {
		Calendar c = Calendar.getInstance();
		return date2Str(c, format);
	}

	// 格式到秒
	public static String getMillon(long time) {

		return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(time);

	}

	// 格式到天
	public static String getDay(long time) {

		return new SimpleDateFormat("yyyy-MM-dd").format(time);

	}

	// 格式到毫秒
	public static String getSMillon(long time) {

		return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(time);

	}

	// 格式到天
	public static int getHour(long time) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(time));
		int h = c.get(Calendar.HOUR_OF_DAY);
		return h;
	}

	public static String getFormateDateWithTime(String formateTimne) {
		Date date = str2Date(formateTimne);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String today = sdf.format(new Date());
		if (today.equals(getDay(date.getTime()))) {// 如果今天显示具体时间
			String time = date2Str(date, "yyyy-MM-dd HH:mm");
			return time.substring(11);
		} else {
			Calendar c = Calendar.getInstance();
			Date now;// 今天的日�
			String week = "";
			try {
				now = sdf.parse(today);
				week = getWeekOfDay(now, sdf.parse(formateTimne).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if ("".equals(week)) {
				return getDay(date.getTime());
			} else {
				return week;
			}
		}
	}

	public static String getWeekOfDay(Date now, Long calltime) {
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		String week = "";
		for (int i = 0; i < 7; i++) {
			if (calltime >= c.getTimeInMillis()) {
				if (i == 1) {
					week = "昨天";
					return week;
				} else if (i == 2) {
					week = "前天";
					return week;
				} else {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(calltime);
					week = new DateFormatSymbols().getWeekdays()[calendar
							.get(Calendar.DAY_OF_WEEK)];
					return week;
				}
			}
			int day = c.get(Calendar.DAY_OF_YEAR);
			c.set(Calendar.DAY_OF_YEAR, day - 1);
		}
		return week;
	}

}
