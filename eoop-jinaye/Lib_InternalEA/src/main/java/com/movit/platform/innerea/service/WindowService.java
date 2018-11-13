package com.movit.platform.innerea.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.movit.platform.innerea.R;

import java.io.IOException;

public class WindowService extends Service implements OnClickListener {

    private WindowManager wManager;// 窗口管理者
    private WindowManager.LayoutParams mParams;// 窗口的属性
    private View myView;
    private boolean flag = true;

    MediaPlayer mMediaPlayer;
    TextView show;
    String content;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        wManager = (WindowManager) getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams();
        // mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;//
        // 系统提示window
        mParams.type = WindowManager.LayoutParams.TYPE_TOAST;// 小米的情况 需要这样设置才会弹出
        mParams.format = PixelFormat.TRANSLUCENT;// 支持透明
        // mParams.format = PixelFormat.RGBA_8888;
        mParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;// 焦点
        mParams.width = WindowManager.LayoutParams.MATCH_PARENT;// 窗口的宽和高
        mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mParams.x = 0;// 窗口位置的偏移量
        mParams.y = 0;
        // mParams.alpha = 0.1f;//窗口的透明度
        myView = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.dialog_show_alarm, null);
        show = (TextView) myView.findViewById(R.id.push_content_view);
        TextView textView = (TextView) myView.findViewById(R.id.push_ok_btn);
        textView.setOnClickListener(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (flag) {
            flag = false;
//			Vibrator mVibrator = (Vibrator) getApplicationContext()
//					.getSystemService(Context.VIBRATOR_SERVICE);
//			mVibrator.vibrate(2000);
//			startPlayer(getApplicationContext());
            content = intent.getStringExtra("content");
            show.setText(content);
            wManager.addView(myView, mParams);// 添加窗口
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // 获取系统默认铃声的Uri
    private Uri getSystemDefultRingtoneUri(Context context) {
        return RingtoneManager.getActualDefaultRingtoneUri(context,
                RingtoneManager.TYPE_RINGTONE);
    }

    private void startPlayer(Context context) {
        mMediaPlayer = MediaPlayer.create(context,
                getSystemDefultRingtoneUri(context));
        mMediaPlayer.setLooping(true);
        try {
            mMediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
        mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        });
    }

    private void stopPlayer() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        if (myView.getParent() != null)
            wManager.removeView(myView);// 移除窗口
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        flag = true;
        if (myView.getParent() != null) {
            wManager.removeView(myView);// 移除窗口
            stopPlayer();
            stopSelf();
        }
    }

}
