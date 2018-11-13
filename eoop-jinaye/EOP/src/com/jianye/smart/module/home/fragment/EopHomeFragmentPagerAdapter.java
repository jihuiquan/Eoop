package com.jianye.smart.module.home.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.widget.TextView;

/**
 * @ClassName: EopHomeFragmentPagerAdapter
 * @Description:
 * @Author: chao
 * @Data 2017-08-04 10:45
 */

public class EopHomeFragmentPagerAdapter extends FragmentPagerAdapter {

  private String[] titles;
  private HomeBannerFragment[] fragments;

  public EopHomeFragmentPagerAdapter(FragmentManager fm, HomeBannerFragment[] fragments,
      String[] titles) {
    super(fm);
    this.titles = titles;
    this.fragments = fragments;
  }

  @Override
  public Fragment getItem(int position) {
    return fragments[position];
  }

  @Override
  public int getCount() {
    return fragments.length;
  }

//  @Override
//  public CharSequence getPageTitle(int position) {
//    return titles[position];
//  }

  public View getTabView(TextView tv, int position) {
    tv.setText(titles[position]);
    return tv;
  }
}
