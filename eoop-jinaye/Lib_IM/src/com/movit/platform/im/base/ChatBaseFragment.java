package com.movit.platform.im.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class ChatBaseFragment extends Fragment {
	
	protected View mRootView;

	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		initViews();
		initDatas();
		return mRootView;
	}

	protected abstract void initViews();
	protected abstract void initDatas();
	protected abstract void resumeDatas();
	
	public View findViewById(int id) {
		return mRootView.findViewById(id);
	}

	@Override
	public void onResume() {
		super.onResume();
		resumeDatas();
	}
}
