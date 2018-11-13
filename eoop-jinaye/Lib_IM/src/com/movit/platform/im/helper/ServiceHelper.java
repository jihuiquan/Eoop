package com.movit.platform.im.helper;

import android.content.Context;
import android.content.Intent;

import com.movit.platform.im.service.ReConnectService;
import com.movit.platform.im.service.XMPPService;

public class ServiceHelper {

	private Context context;

	public ServiceHelper(Context context) {
		super();
		this.context = context;
	}

	public void startService() {
		// 聊天服务
		Intent chatServer = new Intent(context, XMPPService.class);
		context.startService(chatServer);
		// 自动恢复连接服务
		Intent reConnectService = new Intent(context, ReConnectService.class);
		context.startService(reConnectService);
	}

	/**
	 * 
	 * 销毁服务.
	 * 
	 * @author shimiso
	 * @update 2012-5-16 下午12:16:08
	 */
	public void stopService() {
		// 聊天服务
		Intent chatServer = new Intent(context, XMPPService.class);
		context.stopService(chatServer);
		// // 自动恢复连接服务
		Intent reConnectService = new Intent(context, ReConnectService.class);
		context.stopService(reConnectService);
	}

}
