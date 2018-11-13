package com.movit.platform.framework.listener;

public interface UpdataBarListerner {

	public void onUpdate(int value, int status);

	public void onError(int value, int status);
}
