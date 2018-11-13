package com.jianye.smart.module.workbench.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;
import cn.com.xc.sdk.utils.DisplayUtil;
import com.jianye.smart.R;
import com.jianye.smart.view.MyRadioButton;
import java.util.List;

public class FragmentTabAdapter implements RadioGroup.OnCheckedChangeListener {

  private List<Fragment> fragments; // 一个tab页面对应一个Fragment
  private RadioGroup rgs; // 用于切换tab
  private FragmentActivity fragmentActivity; // Fragment所属的Activity
  private int fragmentContentId; // Activity中所要被替换的区域的id

  private int currentTab; // 当前Tab页面索引

  private OnRgsExtraCheckedChangedListener onRgsExtraCheckedChangedListener; // 用于让调用者在切换tab时候增加新的功能

  public FragmentTabAdapter(FragmentActivity fragmentActivity,
      List<Fragment> fragments, int fragmentContentId, RadioGroup rgs) {
    this.fragments = fragments;
    this.rgs = rgs;
    this.fragmentActivity = fragmentActivity;
    this.fragmentContentId = fragmentContentId;

    // 默认显示第一页
    FragmentTransaction ft = fragmentActivity.getSupportFragmentManager()
        .beginTransaction();
    ft.add(fragmentContentId, fragments.get(0));
    ft.add(fragmentContentId, fragments.get(1));
    ft.add(fragmentContentId, fragments.get(2));

//		if (fragments != null && fragments.size() > 2) {
//			ft.hide(fragments.postWithoutEncrypt(0));
//			ft.add(fragmentContentId, fragments.postWithoutEncrypt(2));
//		} else {
//			ft.add(fragmentContentId, fragments.postWithoutEncrypt(0));
//		}
    ft.commit();
    rgs.setOnCheckedChangeListener(this);
  }

  @Override
  public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
    for (int i = 0; i < rgs.getChildCount(); i++) {
      if (rgs.getChildAt(i).getId() == checkedId) {
        String actionStr = "";
        if (i == 0) {
          actionStr = "聊天";
        } else if (i == 1) {
          actionStr = "通讯录";
        } else if (i == 2) {
          actionStr = "首页";
        } else if (i == 3) {
          actionStr = "同事圈";
        } else if (i == 4) {
          actionStr = "我的";
        }
        if (radioGroup.getCheckedRadioButtonId() == R.id.radio_msg && ((MyRadioButton) radioGroup
            .findViewById(R.id.radio_msg)).isChecked()) {
          ((MyRadioButton) radioGroup.findViewById(R.id.radio_msg))
              .setDrawableSize(DisplayUtil.dip2px(fragmentActivity, 40));
          ((MyRadioButton) radioGroup.findViewById(R.id.radio_msg))
              .setCompoundDrawablePadding(-DisplayUtil.dip2px(fragmentActivity, 16));
          ((MyRadioButton) radioGroup.findViewById(R.id.radio_msg)).setText("");
        } else if (radioGroup.getCheckedRadioButtonId() != R.id.radio_msg
            && !((MyRadioButton) radioGroup.findViewById(R.id.radio_msg)).isChecked()) {
          ((MyRadioButton) radioGroup.findViewById(R.id.radio_msg))
              .setDrawableSize(DisplayUtil.dip2px(fragmentActivity, 22));
          ((MyRadioButton) radioGroup.findViewById(R.id.radio_msg))
              .setCompoundDrawablePadding(DisplayUtil.dip2px(fragmentActivity, 0));
          ((MyRadioButton) radioGroup.findViewById(R.id.radio_msg)).setText("工作台");
        }
        Fragment fragment = fragments.get(i);
        FragmentTransaction ft = obtainFragmentTransaction(i);

        getCurrentFragment().onPause(); // 暂停当前tab
        // getCurrentFragment().onStop(); // 暂停当前tab

        if (fragment.isAdded()) {
          // fragment.onStart(); // 启动目标tab的onStart()
          fragment.onResume(); // 启动目标tab的onResume()
        } else {
          ft.add(fragmentContentId, fragment);
        }
        showTab(i); // 显示目标tab
        ft.commit();

        // 如果设置了切换tab额外功能功能接口
        if (null != onRgsExtraCheckedChangedListener) {
          onRgsExtraCheckedChangedListener.OnRgsExtraCheckedChanged(
              radioGroup, checkedId, i);
        }
        break;
      }
    }
  }

  /**
   * 切换tab
   */
  private void showTab(int idx) {
    for (int i = 0; i < fragments.size(); i++) {
      Fragment fragment = fragments.get(i);
      FragmentTransaction ft = obtainFragmentTransaction(idx);

      if (idx == i) {
        ft.show(fragment);
      } else {
        ft.hide(fragment);
      }
      ft.commit();
    }
    currentTab = idx; // 更新目标tab为当前tab
  }

  /**
   * 获取一个带动画的FragmentTransaction
   */
  private FragmentTransaction obtainFragmentTransaction(int index) {
    FragmentTransaction ft = fragmentActivity.getSupportFragmentManager()
        .beginTransaction();
    // 设置切换动画
    if (index > currentTab) {
      ft.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out);
    } else {
      ft.setCustomAnimations(R.anim.slide_right_in,
          R.anim.slide_right_out);
    }
    return ft;
  }

  public int getCurrentTab() {
    return currentTab;
  }

  public Fragment getCurrentFragment() {
    return fragments.get(currentTab);
  }

  public OnRgsExtraCheckedChangedListener getOnRgsExtraCheckedChangedListener() {
    return onRgsExtraCheckedChangedListener;
  }

  public void setOnRgsExtraCheckedChangedListener(
      OnRgsExtraCheckedChangedListener onRgsExtraCheckedChangedListener) {
    this.onRgsExtraCheckedChangedListener = onRgsExtraCheckedChangedListener;
  }

  /**
   * 切换tab额外功能功能接口
   */
  public static class OnRgsExtraCheckedChangedListener {

    public void OnRgsExtraCheckedChanged(RadioGroup radioGroup,
        int checkedId, int index) {

    }
  }

}
