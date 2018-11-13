package com.movit.platform.innerea.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.innerea.R;
import com.movit.platform.innerea.broadcast.AlarmReceiver;
import com.movit.platform.innerea.widget.switchButton.SwitchButton;

import java.util.Calendar;

public class AlarmSettingActivity extends Activity {

    TextView title;
    ImageView topLeft;
    ImageView topRight;

    TextView firstTime;
    TextView lastTime;
    SwitchButton firstSwitch;
    SwitchButton lastSwitch;

    public static final int INTERVAL = 1000 * 60 * 60 * 24;// 24h
    public static String FIRSTALARM = "firstAlarm";
    public static String LASTALARM = "lastAlarm";

    public static final int FIRSTALARM_ID = 911;
    public static final int LASTALARM_ID = 912;

    Button reset;

    String alarmUpTime = "";
    String alarmDownTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_setting);
        initView();
        initData();
    }

    private void initView() {
        title = (TextView) findViewById(R.id.commen_top_title);
        topLeft = (ImageView) findViewById(R.id.commen_top_img_left);
        topRight = (ImageView) findViewById(R.id.commen_top_img_right);

        firstTime = (TextView) findViewById(R.id.gps_first_time);
        lastTime = (TextView) findViewById(R.id.gps_last_time);
        firstSwitch = (SwitchButton) findViewById(R.id.gps_first_switch);
        lastSwitch = (SwitchButton) findViewById(R.id.gps_last_switch);
        reset = (Button) findViewById(R.id.gps_reset);
        title.setText(getResources().getString(R.string.dialog_setting));
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initData() {
        String upTime = MFSPHelper.getString("upTime");
        String downTime = MFSPHelper.getString("downTime");
        alarmUpTime = MFSPHelper.getString("alarmUpTime");
        alarmDownTime = MFSPHelper.getString("alarmDownTime");

        if ("".equals(alarmUpTime) && StringUtils.notEmpty(upTime)) {
            Calendar c = DateUtils.str2Calendar(upTime, "HH:mm");
            c.add(Calendar.MINUTE, -5);
            String timeString = DateUtils.date2Str(c, "HH:mm");
            alarmUpTime = timeString;
        }
        if ("".equals(alarmDownTime) && StringUtils.notEmpty(downTime)) {
            alarmDownTime = downTime;
        }

//        if ("".equals(alarmUpTime) && StringUtils.notEmpty(upTime)) {
//            Calendar c = DateUtils.str2Calendar(upTime, "HH:mm:ss");
//            c.add(Calendar.MINUTE, -5);
//            String timeString = DateUtils.date2Str(c, "HH:mm");
//            alarmUpTime = timeString;
//        }
//        if ("".equals(alarmDownTime) && StringUtils.notEmpty(downTime)) {
//            Calendar c = DateUtils.str2Calendar(downTime, "HH:mm:ss");
//            String timeString = DateUtils.date2Str(c, "HH:mm");
//            alarmDownTime = timeString;
//        }

        firstTime.setText(alarmUpTime);
        lastTime.setText(alarmDownTime);
        boolean firstON = MFSPHelper.getBoolean(FIRSTALARM,
                false);
        boolean lastON = MFSPHelper.getBoolean(LASTALARM,
                false);
        if (firstON) {
            firstSwitch.setChecked(true);
        }
        if (lastON) {
            lastSwitch.setChecked(true);
        }

        firstTime.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                final Calendar firstC = DateUtils.str2Calendar(alarmUpTime,
                        "HH:mm");

                MyTimePickerDialog picker = new MyTimePickerDialog(
                        AlarmSettingActivity.this, new OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view,
                                          int hourOfDay, int minute) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        alarmUpTime = DateUtils.date2Str(calendar,
                                "HH:mm");
                        if (firstSwitch.isChecked()) {
                            Log.v("firstSwitch", alarmUpTime);
                            MFSPHelper.setBoolean(FIRSTALARM, true);
                            MFSPHelper.setString("alarmUpTime", alarmUpTime);
                        } else {
                            firstSwitch.setChecked(true);
                        }

                        firstTime.setText(alarmUpTime);
                    }
                }, firstC.get(Calendar.HOUR_OF_DAY), firstC
                        .get(Calendar.MINUTE), true);
                picker.show();
            }
        });

        lastTime.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                final Calendar lastC = DateUtils.str2Calendar(alarmDownTime,
                        "HH:mm");

                MyTimePickerDialog picker = new MyTimePickerDialog(
                        AlarmSettingActivity.this, new OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view,
                                          int hourOfDay, int minute) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        alarmDownTime = DateUtils.date2Str(calendar,
                                "HH:mm");
                        if (lastSwitch.isChecked()) {
                            Log.v("lastSwitch", alarmDownTime);
                            MFSPHelper.setBoolean(LASTALARM, true);
                            MFSPHelper.setString("alarmDownTime",
                                    alarmDownTime);
                        } else {
                            lastSwitch.setChecked(true);
                        }
                        lastTime.setText(alarmDownTime);
                    }
                }, lastC.get(Calendar.HOUR_OF_DAY), lastC
                        .get(Calendar.MINUTE), true);
                picker.show();
            }
        });

        firstSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    MFSPHelper.setBoolean(FIRSTALARM, true);
                    MFSPHelper.setString("alarmUpTime", alarmUpTime);
                    setAlarm(AlarmSettingActivity.this, alarmUpTime,
                            AlarmSettingActivity.FIRSTALARM_ID);
                } else {
                    MFSPHelper.setBoolean(FIRSTALARM, false);
                }

            }
        });

        lastSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    MFSPHelper.setBoolean(LASTALARM, true);
                    MFSPHelper.setString("alarmDownTime", alarmDownTime);
                    setAlarm(AlarmSettingActivity.this, alarmDownTime,
                            AlarmSettingActivity.LASTALARM_ID);
                } else {
                    MFSPHelper.setBoolean(LASTALARM, false);
                }
            }
        });

        reset.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String upTime = MFSPHelper.getString("upTime");
                String downTime = MFSPHelper.getString("downTime");
                Calendar c1 = DateUtils.str2Calendar(upTime, "HH:mm");
                c1.add(Calendar.MINUTE, -5);
                String timeString1 = DateUtils.date2Str(c1, "HH:mm");
                alarmUpTime = timeString1;
                Calendar c2 = DateUtils.str2Calendar(downTime, "HH:mm");
                String timeString2 = DateUtils.date2Str(c2, "HH:mm");
                alarmDownTime = timeString2;
                //重置的时候默认关闭打开提醒
                MFSPHelper.setString("alarmUpTime", alarmUpTime);
                MFSPHelper.setString("alarmDownTime", alarmDownTime);
                firstTime.setText(alarmUpTime);
                lastTime.setText(alarmDownTime);
                firstSwitch.setChecked(false);
                lastSwitch.setChecked(false);
                MFSPHelper.setBoolean(FIRSTALARM, false);
                MFSPHelper.setBoolean(LASTALARM, false);

//                setAlarm(AlarmSettingActivity.this, alarmUpTime, FIRSTALARM_ID);
//                MFSPHelper.setBoolean(FIRSTALARM, true);
//                MFSPHelper.setString("alarmUpTime", alarmUpTime);
//                if (firstSwitch.isChecked()) {
//                    Log.v("firstSwitch", alarmUpTime);
//                    setAlarm(AlarmSettingActivity.this, alarmUpTime,
//                            FIRSTALARM_ID);
//                    MFSPHelper.setBoolean(FIRSTALARM, true);
//                    MFSPHelper.setString("alarmUpTime", alarmUpTime);
//                } else {
//                    firstSwitch.setChecked(true);
//                }

//
//                if (lastSwitch.isChecked()) {
//                    setAlarm(AlarmSettingActivity.this, alarmDownTime,
//                            LASTALARM_ID);
//                    MFSPHelper.setBoolean(LASTALARM, true);
//                    MFSPHelper.setString("alarmDownTime", alarmDownTime);
//                } else {
//                    lastSwitch.setChecked(true);
//                }


            }
        });
    }

    class MyTimePickerDialog extends TimePickerDialog {

        public MyTimePickerDialog(Context context, OnTimeSetListener callBack,
                                  int hourOfDay, int minute, boolean is24HourView) {
            super(context, callBack, hourOfDay, minute, is24HourView);
        }

        @Override
        protected void onStop() {
            // super.onStop();
        }


        @Override
        public void onClick(DialogInterface dialog, int which) {
            //焦点释放处理
            if (dialog instanceof TimePickerDialog) {
                ((TimePickerDialog) dialog).getWindow().getDecorView().clearFocus();
            }
            super.onClick(dialog, which);
        }
    }

    public static void setAlarm(Context context, String alarmTime, int id) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.setAction("com.movitech.shimaoren.alarm.gps");
        alarmIntent.putExtra("id", id);
        PendingIntent sender = PendingIntent.getBroadcast(context, id,
                alarmIntent, 0);
        Calendar alarmCalendar = DateUtils.str2Calendar(alarmTime, "HH:mm");
        Calendar calendar = Calendar.getInstance();
        alarmCalendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        long startTime = alarmCalendar.getTimeInMillis();
        if (calendar.getTimeInMillis() > startTime) {
            startTime += AlarmSettingActivity.INTERVAL;
        }
        AlarmManager am = (AlarmManager) context
                .getSystemService(context.ALARM_SERVICE);
        long l = startTime - calendar.getTimeInMillis();
        am.setRepeating(AlarmManager.RTC_WAKEUP, l,
                AlarmSettingActivity.INTERVAL, sender);
    }

    public static void cancelAlarm(Context context, int id) {
        if (context == null) return;
        Intent alarmIntent = new Intent("com.movitech.shimaoren.alarm.gps");
        alarmIntent.putExtra("id", id);
        AlarmManager am = (AlarmManager) context
                .getSystemService(context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, id,
                alarmIntent, 0);
        am.cancel(sender);
    }

}
