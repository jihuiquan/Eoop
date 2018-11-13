package com.movit.platform.innerea.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.movit.platform.innerea.service.AlarmService;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, AlarmService.class);
		serviceIntent.putExtra("id", intent.getIntExtra("id", 0));
		context.startService(serviceIntent);
	}
}
