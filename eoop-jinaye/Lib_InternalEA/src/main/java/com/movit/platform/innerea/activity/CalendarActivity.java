package com.movit.platform.innerea.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.okhttp.utils.AesUtils;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.LogUtils;
import com.movit.platform.innerea.R;
import com.movit.platform.innerea.db.DBManager;
import com.movit.platform.innerea.widget.flexiblecalendar.FlexibleCalendarView;
import com.movit.platform.innerea.widget.flexiblecalendar.view.BaseCellView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Call;

/**
 * Created by scorpiokara on 15/9/8.
 */
public class CalendarActivity extends Activity implements
        FlexibleCalendarView.OnMonthChangeListener,
        FlexibleCalendarView.OnDateClickListener {

    TextView title;
    ImageView topLeft;
    ImageView topRight;
    FlexibleCalendarView calendarView;
    TextView dataTextView;
    Button prevMonth;
    Button nextMonth;
    String[] week = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
    // String[] week = { "周一", "周二", "周三", "周四", "周五", "周六", "周日" };

    TextView todayView;
    TextView fisrtTime;
    TextView lastTime;
    DialogUtils progressDialogUtil;
    // ArrayList<String> allLogs;
    long upGpsTime;
    long downGpsTime;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    ArrayList<String> logList = (ArrayList<String>) msg.obj;
                    DBManager manager = new DBManager(CalendarActivity.this,
                            MFSPHelper.getString(CommConstants.EMPADNAME));
                    manager.deleteAll();
                    for (int i = 0; i < logList.size(); i++) {
                        manager.insertGPSLog(logList.get(i));
                    }
                    manager.closeDb();
                    progressDialogUtil.dismiss();
                    Intent intent = new Intent(CalendarActivity.this,
                            CalendarActivity.class);
                    intent.putExtra("success", true);
                    startActivity(intent);
                    CalendarActivity.this.finish();
                    break;
                case 2:
                    progressDialogUtil.dismiss();
                    Toast.makeText(CalendarActivity.this,
                            getResources().getString(R.string.gps_syn_failed), Toast.LENGTH_LONG)
                            .show();
                    break;
                case 3:
                    synchronized (this) {
                        ArrayList<String> logList2 = (ArrayList<String>) msg.obj;
                        DBManager manager2 = new DBManager(CalendarActivity.this,
                                MFSPHelper.getString(CommConstants.EMPADNAME));
                        manager2.deleteAllLogsWithoutToday();
                        for (int i = 0; i < logList2.size(); i++) {
                            manager2.insertGPSLog(logList2.get(i));
                        }
                        manager2.closeDb();
                    }
                    progressDialogUtil.dismiss();
                    Toast.makeText(CalendarActivity.this, getResources().getString(R.string.gps_syn_success), Toast.LENGTH_SHORT).show();
                    initData();
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_calender);
        progressDialogUtil = DialogUtils.getInstants();
        if (getIntent().getBooleanExtra("success", true)) {
            Toast.makeText(this, getResources().getString(R.string.gps_syn_success), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.gps_syn_failed), Toast.LENGTH_SHORT).show();
        }
        initView();
        initData();
        //新城要求进界面时刷新数据一次
        //refreshData();
    }

    private void initView() {
        title = (TextView) findViewById(R.id.commen_top_title);
        topLeft = (ImageView) findViewById(R.id.commen_top_img_left);
        topRight = (ImageView) findViewById(R.id.commen_top_img_right);
        dataTextView = (TextView) findViewById(R.id.current_data);
        prevMonth = (Button) findViewById(R.id.btn_prev_month);
        nextMonth = (Button) findViewById(R.id.btn_next_month);
        calendarView = (FlexibleCalendarView) findViewById(R.id.calendar_view);
        fisrtTime = (TextView) findViewById(R.id.gps_first_time);
        lastTime = (TextView) findViewById(R.id.gps_last_time);
        todayView = (TextView) findViewById(R.id.gps_log_day);
        title.setText(getResources().getString(R.string.gps_calendar_record));
        calendarView.setShowDatesOutsideMonth(false);
        calendarView.setOnMonthChangeListener(this);
        calendarView.setOnDateClickListener(this);
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        topRight.setImageResource(R.drawable.icon_gps_getlog);
        topRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                progressDialogUtil.showDownLoadingDialog(CalendarActivity.this,
                        getResources().getString(R.string.dialog_syn_ing),
                        false);
                getGpsLogFromSever();
            }
        });
        nextMonth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.moveToNextMonth();
            }
        });
        prevMonth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.moveToPreviousMonth();
            }
        });
        String upTime = MFSPHelper.getString("upTime");
        String downTime = MFSPHelper.getString("downTime");
        try {
            upGpsTime = DateUtils.str2Calendar(upTime, "HH:mm:ss")
                    .getTimeInMillis();
            downGpsTime = DateUtils.str2Calendar(downTime, "HH:mm:ss")
                    .getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData() {

        setLogByDay("");
        calendarView.setCalendarView(new FlexibleCalendarView.ICalendarView() {
            @Override
            public BaseCellView getCellView(int position, View convertView,
                                            ViewGroup parent, boolean isWithinCurrentMonth) {
                BaseCellView cellView = (BaseCellView) convertView;
                if (cellView == null) {
                    LayoutInflater inflater = LayoutInflater
                            .from(CalendarActivity.this);
                    cellView = (BaseCellView) inflater.inflate(
                            R.layout.calendar_date_cell_view, null);
                }
                return cellView;
            }

            @Override
            public BaseCellView getWeekdayCellView(int position,
                                                   View convertView, ViewGroup parent) {
                BaseCellView cellView = (BaseCellView) convertView;
                if (cellView == null) {
                    LayoutInflater inflater = LayoutInflater
                            .from(CalendarActivity.this);
                    cellView = (BaseCellView) inflater.inflate(
                            R.layout.calendar_week_cell_view, null);
                }
                return cellView;
            }

            @Override
            public String getDayOfWeekDisplayValue(int dayOfWeek,
                                                   String defaultValue) {
                return week[dayOfWeek - 1];
            }
        });
        calendarView
                .setEventDataProvider(new FlexibleCalendarView.EventDataProvider() {
                    @Override
                    public List<Integer> getEventsForTheDay(int year,
                                                            int month, int day) {

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, day);

                        DBManager manager = new DBManager(
                                CalendarActivity.this, MFSPHelper
                                .getString(CommConstants.EMPADNAME));
                        String dayStr = DateUtils.date2Str(calendar,
                                "yyyy-MM-dd");
                        ArrayList<String> dayLogList = manager
                                .getLogByDay(dayStr);
                        manager.closeDb();
                        if (!dayLogList.isEmpty()) {
                            String logTime1 = dayLogList.get(0);
                            String day1 = logTime1.substring(0, 10);
                            String time1 = logTime1.substring(11);
                            Log.v("setEventDataProvider", "1 " + logTime1);
                            Calendar c1 = DateUtils.str2Calendar(time1,
                                    "HH:mm:ss");
                            long c1TimeInMillis = c1.getTimeInMillis();
                            if (!dayStr.equals(day1)) {// 明天 +24H
                                c1TimeInMillis += 24 * 3600 * 1000;
                            }
                            if (dayLogList.size() == 1) {
                                List<Integer> eventColors = new ArrayList<>(
                                        2);
                                eventColors.add(R.color.blue_gps);
//                                if (c1TimeInMillis < upGpsTime) {
//                                    eventColors.add(R.color.gps_green);
//                                } else {
//                                    eventColors.add(R.color.gps_red);
//                                }
                                //eventColors.add(R.color.gps_grey);
                                return eventColors;
                            } else if (dayLogList.size() == 2) {
                                String logTime2 = dayLogList.get(1);
                                String day2 = logTime2.substring(0, 10);
                                String time2 = logTime2.substring(11);
                                Log.v("setEventDataProvider", "2 " + logTime2);
                                Calendar c2 = DateUtils.str2Calendar(time2,
                                        "HH:mm:ss");
                                long c2TimeInMillis = c2.getTimeInMillis();
                                if (!dayStr.equals(day2)) {// 明天 +24H
                                    c2TimeInMillis += 24 * 3600 * 1000;
                                }
                                List<Integer> eventColors = new ArrayList<>(
                                        2);
                                eventColors.add(R.color.blue_gps);
                                eventColors.add(R.color.blue_gps);

//                                if (c1TimeInMillis < upGpsTime) {
//                                    eventColors.add(R.color.gps_green);
//                                } else {
//                                    eventColors.add(R.color.gps_red);
//                                }
//                                if (c2TimeInMillis > downGpsTime) {
//                                    eventColors.add(R.color.gps_green);
//                                } else {
//                                    eventColors.add(R.color.gps_red);
//                                }
                                return eventColors;
                            } else {
                                return new ArrayList<>();
                            }
                        } else {// 没有打卡记录 大于今天的不显示 或者是周末的时候
                            Calendar nowC = DateUtils.str2Calendar(
                                    DateUtils.getCurDateStr("yyyy-MM-dd"),
                                    "yyyy-MM-dd");
                            Calendar seclectC = DateUtils.str2Calendar(dayStr,
                                    "yyyy-MM-dd");
                            if (seclectC.getTimeInMillis() > nowC
                                    .getTimeInMillis()
                                    || seclectC.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                                    || seclectC.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                return new ArrayList<>();
                            } else {
                                List<Integer> eventColors = new ArrayList<Integer>(
                                        2);
//                                eventColors.add(R.color.gps_grey);
//                                eventColors.add(R.color.gps_grey);
                                return eventColors;
                            }
                        }
                    }

                });
        updateTitle(calendarView.getSelectedDateItem().getYear(), calendarView
                .getSelectedDateItem().getMonth());
    }

    private void refreshData() {
        progressDialogUtil.showDownLoadingDialog(CalendarActivity.this,
                getResources().getString(R.string.dialog_syn_ing),
                false);
        getGpsLogFromSever();
    }


    private void setLogByDay(String today) {
        String showDayStr = "";
        if ("" .equals(today)) {
            Calendar calendar = Calendar.getInstance();
            showDayStr = DateUtils.date2Str(calendar, "yyyy-MM-dd");
        } else {
            showDayStr = today;
        }
        todayView.setText(showDayStr);
        String dayStr = showDayStr.replace("年", "-").replace("月", "-")
                .replace("日", "-");
        DBManager manager = new DBManager(this,
                MFSPHelper.getString(CommConstants.EMPADNAME));
        ArrayList<String> dayLogList = manager.getLogByDay(dayStr);
        manager.closeDb();
        fisrtTime.setTextColor(getResources().getColor(R.color.title_color));
        lastTime.setTextColor(getResources().getColor(R.color.title_color));
        if (!dayLogList.isEmpty()) {
            setLogTextColor(dayLogList, dayStr);
        } else {
            fisrtTime.setText(getResources().getString(
                    R.string.gps_calendar_no_record));
            lastTime.setText(getResources().getString(
                    R.string.gps_calendar_no_record));
        }

    }

    public void setLogTextColor(ArrayList<String> dayLogList, String dayStr) {
        String logTime1 = dayLogList.get(0);
        String day1 = logTime1.substring(0, 10);
        String time1 = logTime1.substring(11);
        Calendar c1 = DateUtils.str2Calendar(time1, "HH:mm:ss");
        long c1TimeInMillis = c1.getTimeInMillis();
        if (!dayStr.equals(day1)) {// 明天 +24H
            c1TimeInMillis += 24 * 3600 * 1000;
        }
        if (dayLogList.size() == 1) {

            fisrtTime.setTextColor(getResources().getColor(
                    R.color.title_blue));
//            if (c1TimeInMillis < upGpsTime) {
//                fisrtTime.setTextColor(getResources().getColor(
//                        R.color.gps_green));
//            } else {
//                fisrtTime
//                        .setTextColor(getResources().getColor(R.color.gps_red));
//            }
            Calendar cf = DateUtils
                    .str2Calendar(logTime1, "yyyy-MM-dd HH:mm:ss");
            fisrtTime.setText(DateUtils.date2Str(cf, "yyyy-MM-dd HH:mm"));
            lastTime.setText(getResources().getString(
                    R.string.gps_calendar_no_record));
        } else if (dayLogList.size() >= 2) {
            String logTime2 = dayLogList.get(dayLogList.size() - 1);
            String day2 = logTime2.substring(0, 10);
            String time2 = logTime2.substring(11);
            Log.v("setEventDataProvider", "2 " + logTime2);
            Calendar c2 = DateUtils.str2Calendar(time2, "HH:mm:ss");
            long c2TimeInMillis = c2.getTimeInMillis();
            if (!dayStr.equals(day2)) {// 明天 +24H
                c2TimeInMillis += 24 * 3600 * 1000;
            }
            fisrtTime.setTextColor(getResources().getColor(
                    R.color.title_blue));
            lastTime.setTextColor(getResources()
                    .getColor(R.color.title_blue));

//            if (c1TimeInMillis < upGpsTime) {
//                fisrtTime.setTextColor(getResources().getColor(
//                        R.color.gps_green));
//            } else {
//                fisrtTime
//                        .setTextColor(getResources().getColor(R.color.gps_red));
//            }
//
//            if (c2TimeInMillis > downGpsTime) {
//                lastTime.setTextColor(getResources()
//                        .getColor(R.color.gps_green));
//            } else {
//                lastTime.setTextColor(getResources().getColor(R.color.gps_red));
//            }

            Calendar cf = DateUtils
                    .str2Calendar(logTime1, "yyyy-MM-dd HH:mm:ss");
            Calendar cl = DateUtils
                    .str2Calendar(logTime2, "yyyy-MM-dd HH:mm:ss");
            String timeFirst = DateUtils.date2Str(cf, "yyyy-MM-dd HH:mm");
            String timeLast = DateUtils.date2Str(cl, "yyyy-MM-dd HH:mm");

            Date date1 = cf.getTime();
            Date date2 = cl.getTime();
            if (date1.getTime() >= date2.getTime()) {
                fisrtTime.setText(timeLast);
                lastTime.setText(timeFirst);
            } else {
                fisrtTime.setText(timeFirst);
                lastTime.setText(timeLast);
            }


        }
    }

    @Override
    public void onDateClick(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        String dayStr = DateUtils.date2Str(calendar, "yyyy-MM-dd");
        setLogByDay(dayStr);
    }

    @Override
    public void onMonthChange(int year, int month,
                              @FlexibleCalendarView.Direction int direction) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        updateTitle(year, month);
        String dayStr = DateUtils.date2Str(cal, "yyyy-MM-dd");
        setLogByDay(dayStr);
    }

    private void updateTitle(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(year, month, 1);
        cal2.roll(Calendar.MONTH, 1);
        cal2.roll(Calendar.DAY_OF_YEAR, -1);
        // dataTextView.setText(DateUtil.date2Str(cal, "yyyy/MM/dd") + "-"
        // + DateUtil.date2Str(cal2, "yyyy/MM/dd"));
        dataTextView.setText(DateUtils.date2Str(cal, "yyyy-MM"));
    }

    /**
     * 获取当前人 三个月的打卡记录
     */
    public void getGpsLogFromSever() {
        JSONObject obj = new JSONObject();
        JSONObject rq = new JSONObject();
        try {
            obj.put("userId", MFSPHelper.getString(CommConstants.USERID));
            rq.put("secretMsg", AesUtils.getInstance().encrypt(obj.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpManager.postJsonWithToken(CommConstants.URL_GET_PUNSH_RECORD, rq.toString(), new StringCallback() {

            @Override
            public void onError(Call call, Exception e) {
                handler.sendEmptyMessage(2);
            }

            @Override
            public void onResponse(String response) throws JSONException {
                JsonObject jsonObject = new JsonParser().parse(response)
                        .getAsJsonObject();
                int code = jsonObject.get("ResponseCode").getAsInt();
                if (code == 1) {
                    JsonArray array = jsonObject.get("List")
                            .getAsJsonArray();
                    ArrayList<String> logList = new ArrayList<>();
                    for (int i = 0; i < array.size(); i++) {
                        JsonObject object = array.get(i).getAsJsonObject();
                        String logTime = object.get("punshTime")
                                .getAsString();
                        logList.add(logTime);
                    }
                    handler.obtainMessage(1, logList).sendToTarget();
                } else {
                    handler.sendEmptyMessage(2);
                }
            }
        });
    }
}
