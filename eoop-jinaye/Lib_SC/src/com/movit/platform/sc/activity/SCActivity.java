package com.movit.platform.sc.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.movit.platform.sc.R;
import com.movit.platform.sc.module.zone.fragment.ZoneFragment;

public class SCActivity extends FragmentActivity implements ZoneFragment.IZoneFragment{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sc_activity);
		FragmentManager fragmentManager = this.getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.add(R.id.common_fragment,new ZoneFragment(this),"EmptyFragment");
		transaction.commitAllowingStateLoss();
	}

	@Override
	public void setBottomTabStatus(boolean isShow) {

	}
}
