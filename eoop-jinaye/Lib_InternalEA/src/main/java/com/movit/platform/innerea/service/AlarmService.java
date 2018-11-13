package com.movit.platform.innerea.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.innerea.R;
import com.movit.platform.innerea.activity.AlarmSettingActivity;
import com.movit.platform.innerea.activity.MapGPSActivity;

import java.util.Calendar;
import java.util.Date;

public class AlarmService extends Service {


    private String oldFirstAlertTime = "", oldLastAlertTime = "";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int id = intent.getIntExtra("id", 0);
        boolean FIRSTALARM = MFSPHelper.getBoolean(AlarmSettingActivity.FIRSTALARM, false);
        boolean LASTALARM = MFSPHelper.getBoolean(AlarmSettingActivity.LASTALARM, false);
        Calendar calendar = Calendar.getInstance();
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        if (week == Calendar.SUNDAY || week == Calendar.SATURDAY) {
            return Service.START_STICKY;
        }
        String alarmUpTime = MFSPHelper.getString("alarmUpTime");
        String alarmDownTime = MFSPHelper.getString("alarmDownTime");
        String nowTime = DateUtils.date2Str(new Date(), "HH:mm");
        //Toast.makeText(AlarmService.this, nowTime + "===" + nowTime, Toast.LENGTH_SHORT).show();
        if (FIRSTALARM && id == AlarmSettingActivity.FIRSTALARM_ID) {
            // 弹出通知
            if (!TextUtils.isEmpty(nowTime) && !TextUtils.isEmpty(alarmUpTime) && nowTime.equals(alarmUpTime)) {
                oldFirstAlertTime = DateUtils.date2Str(new Date(), "yyyy-MM-dd HH:mm");
                sendNotification(this, getResources().getString(R.string.gps_alarm_info));
                if (isRunningForeground(this)) {
                    Intent i = new Intent();
                    i.putExtra("content", getResources().getString(R.string.gps_alarm_info));
                    i.setClass(this, WindowService.class);
                    this.startService(i);
                }
            }
        }
        if (LASTALARM && id == AlarmSettingActivity.LASTALARM_ID) {
            if (!TextUtils.isEmpty(nowTime) && !TextUtils.isEmpty(alarmDownTime) && nowTime.equals(alarmDownTime)) {
                oldLastAlertTime = DateUtils.date2Str(new Date(), "yyyy-MM-dd HH:mm");
                sendNotification(this, getResources().getString(R.string.gps_alarm_info));
                if (isRunningForeground(this)) {
                    Intent i = new Intent();
                    i.putExtra("content", getResources().getString(R.string.gps_alarm_info));
                    i.setClass(this, WindowService.class);
                    this.startService(i);
                }
            }
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(receiver, intentFilter);
        return Service.START_STICKY;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_TICK)) {
                boolean FIRSTALARM = MFSPHelper.getBoolean(
                        AlarmSettingActivity.FIRSTALARM, false);
                boolean LASTALARM = MFSPHelper.getBoolean(
                        AlarmSettingActivity.LASTALARM, false);
                String alarmUpTime = MFSPHelper.getString("alarmUpTime");
                String alarmDownTime = MFSPHelper.getString("alarmDownTime");
                String nowTime = DateUtils.date2Str(new Date(), "HH:mm");
                //Toast.makeText(AlarmService.this, nowTime + "===" + nowTime, Toast.LENGTH_SHORT).show();
                Calendar calendar = Calendar.getInstance();
                int week = calendar.get(Calendar.DAY_OF_WEEK);
                if (week == Calendar.SUNDAY || week == Calendar.SATURDAY) {
                    return;
                }
                if (FIRSTALARM) {
                    String s = DateUtils.date2Str(new Date(), "yyyy-MM-dd HH:mm");
                    if (nowTime.equals(alarmUpTime) && !s.equals(oldFirstAlertTime)) {
                        oldFirstAlertTime = s;
                        sendNotification(context, getResources().getString(R.string.gps_alarm_info));
                        if (isRunningForeground(context)) {
                            Intent i = new Intent();
                            i.putExtra("content", getResources().getString(R.string.gps_alarm_info));
                            i.setClass(context, WindowService.class);
                            context.startService(i);
                        }
                    }
                }

                if (LASTALARM) {
                    String s = DateUtils.date2Str(new Date(), "yyyy-MM-dd HH:mm");
                    if (nowTime.equals(alarmDownTime) && !s.equals(oldLastAlertTime)) {
                        oldLastAlertTime = s;
                        sendNotification(context, getResources().getString(R.string.gps_alarm_info));
                        if (isRunningForeground(context)) {
                            Intent i = new Intent();
                            i.putExtra("content", getResources().getString(R.string.gps_alarm_info));

                            i.setClass(context, WindowService.class);
                            context.startService(i);
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private boolean isRunningForeground(Context context) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName)
                && currentPackageName.equals(context.getPackageName())) {
            return true;
        }
        return false;
    }

    public void sendNotification(Context context, String tipStr) {
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);


        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName(context.getPackageName(),
                MapGPSActivity.class.getName()));
        intent.setComponent(((BaseApplication) ((AlarmService) context).getApplication()).getUIController().getMainComponentName(context));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        // 当点击消息时就会向系统发送openintent意图
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 新建一个通知，指定其图标和标题
        // 第一个参数为图标，第二个参数为标题，第三个参数为通知时间
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.icon)
                .setWhen(System.currentTimeMillis())
                // 设置时间发生时间
                .setAutoCancel(true)
                // 设置可以清除
                .setContentTitle(getResources().getString(R.string.app_name))
                // 设置下拉列表里的标题
                .setContentText(tipStr)
                // 设置上下文内容
                .setContentIntent(contentIntent)
                .setDefaults(
                        Notification.DEFAULT_SOUND
                                | Notification.DEFAULT_VIBRATE);

        Notification n = builder.getNotification();// 获取一个Notification
        mNotificationManager.notify(110, n);// 显示通知 break;
    }

}
