package com.movit.platform.contacts.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.contacts.R;
import com.movit.platform.contacts.adapter.ContactsAdapter;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import java.util.ArrayList;
import java.util.List;

public class ContactsFragmentV2 extends Fragment {

  private PopupWindow popupWindow;
  private EditText searchText;
  private List<UserInfo> tempFriendList = new ArrayList<UserInfo>();
  private ContactsAdapter searchResultAdapter;
  private LinearLayout searchView;
  private ListView searchList;
  private ImageView searchClear;
  private InputMethodManager inputmanger;


  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_user_v2, null);
    initViews(rootView);
    initDatas();
    return rootView;
  }

  protected void initViews(View view) {
    View root =  view.findViewById(R.id.common_top_layout);
    ImageView back = (ImageView) view.findViewById(R.id.common_top_left);
    TextView title = (TextView) view.findViewById(R.id.tv_common_top_title);
    title.setTextColor(getResources().getColor(R.color.white));
    title.setText("通讯录");
    back.setVisibility(View.GONE);
    ImageView groupAdd = (ImageView) view.findViewById(R.id.common_top_right);

    groupAdd.setImageResource(R.drawable.icon_add);
    groupAdd.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        getPopupWindow();
        if (popupWindow.isShowing()) {
          popupWindow.dismiss();
        } else {
          popupWindow.showAsDropDown(v);
        }
      }
    });
    SharedPreUtils spUtil = new SharedPreUtils(getActivity());
    if (!"default".equals(spUtil.getString(BaseApplication.SKINTYPE))) {
      RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.common_top_layout);
      layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
    }
    root.setBackgroundColor(getResources().getColor(R.color.color_3fb0ff));
    searchText = (EditText) view.findViewById(R.id.search_key);
    searchClear = (ImageView) view.findViewById(R.id.search_clear);
    searchView = (LinearLayout) view.findViewById(R.id.search_view);
    searchList = (ListView) view.findViewById(R.id.search_list);
    searchClear.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        searchText.setText("");
        searchClear.setVisibility(View.INVISIBLE);
        inputmanger.hideSoftInputFromWindow(
            searchText.getWindowToken(), 0);
        sortFreshData("");
      }
    });
    searchText.setOnKeyListener(new OnKeyListener() {// 输入完后按键盘上的搜索键

      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER
            && event.getAction() == KeyEvent.ACTION_DOWN) {// 修改回车键功能
          String content = searchText.getText().toString();
          if (content != null && !"".equals(content)) {
            searchClear.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.VISIBLE);
            sortFreshData(content + "");
          } else {
            searchClear.setVisibility(View.INVISIBLE);
            searchView.setVisibility(View.GONE);
          }
          inputmanger.hideSoftInputFromWindow(
              searchText.getWindowToken(), 0);
        }
        return false;
      }
    });
  }

  private void initDatas() {
    inputmanger = (InputMethodManager) this.getActivity().getSystemService(
        Context.INPUT_METHOD_SERVICE);
    searchResultAdapter = new ContactsAdapter(tempFriendList, this.getActivity(), "");
    searchList.setAdapter(searchResultAdapter);
  }

  private void getPopupWindow() {
    if (null != popupWindow) {
      return;
    } else {
      initPopuptWindow();
    }
  }

  private void initPopuptWindow() {

    View contactView = LayoutInflater.from(getActivity())
        .inflate(R.layout.pop_window_contact, null);
    LinearLayout group_add = (LinearLayout) contactView
        .findViewById(R.id.pop_linearlayout_1);
    ImageView imageView1 = (ImageView) contactView
        .findViewById(R.id.pop_imageview_1);
    TextView textView1 = (TextView) contactView
        .findViewById(R.id.pop_textview_1);
    LinearLayout email_add = (LinearLayout) contactView
        .findViewById(R.id.pop_linearlayout_2);
    ImageView imageView2 = (ImageView) contactView
        .findViewById(R.id.pop_imageview_2);
    TextView textView2 = (TextView) contactView
        .findViewById(R.id.pop_textview_2);
    imageView1.setImageResource(R.drawable.icon_add_some);
    textView1.setText("发起群聊");
    imageView2.setImageResource(R.drawable.icon_add_email);
    textView2.setText("发起邮件");
    group_add.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (popupWindow != null && popupWindow.isShowing()) {
          popupWindow.dismiss();
        }

        Intent intent = new Intent();
        intent.putExtra("ACTION", "GROUP").putExtra("TITLE", "发起群聊")
            .putExtra(CommConstants.KEY_GROUP_TYPE, CommConstants.CHAT_TYPE_GROUP_PERSON);
        ((BaseApplication) getActivity().getApplication()).getUIController()
            .onIMOrgClickListener(getActivity(), intent, 0);

      }
    });
    email_add.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (popupWindow != null && popupWindow.isShowing()) {
          popupWindow.dismiss();
        }

        Intent intent = new Intent();
        intent.putExtra("TITLE", "发起邮件").putExtra("ACTION", "EMAIL");
        ((BaseApplication) getActivity().getApplication()).getUIController()
            .onIMOrgClickListener(getActivity(), intent, 0);

      }
    });
    popupWindow = new PopupWindow(contactView,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT);
    popupWindow.setFocusable(false);
    popupWindow.setOutsideTouchable(true);
    popupWindow.setBackgroundDrawable(new BitmapDrawable());
  }
  @Override
  public void onResume() {
    super.onResume();
    sortFreshData(searchText.getText().toString());
  }

  private void sortFreshData(String content) {
    tempFriendList.clear();
    if (content != null && !"".equals(content)) {
      UserDao dao = UserDao.getInstance(this.getActivity());
      List<UserInfo> searchList = dao.getAllUserInfosBySearch(content);
      dao.closeDb();
      tempFriendList.addAll(searchList);
      searchResultAdapter.setTitle("联系人");
      tempFriendList.add(0, new UserInfo());
      tempFriendList.add(1, new UserInfo());
      tempFriendList.add(2, new UserInfo());
    } else {
      tempFriendList.add(0, new UserInfo());
      tempFriendList.add(1, new UserInfo());
      tempFriendList.add(2, new UserInfo());

      ArrayList<String> idStrings = CommConstants.loginConfig.getmUserInfo()
          .getAttentionPO();
      UserDao dao = UserDao.getInstance(this.getActivity());
      for (int i = 0; i < idStrings.size(); i++) {
        UserInfo userInfo = dao.getUserInfoById(idStrings.get(i));
        if (userInfo != null) {
          tempFriendList.add(userInfo);
        }
      }
      dao.closeDb();
      searchResultAdapter.setTitle("我关注的人");
    }
    searchResultAdapter.setUserInfos(tempFriendList);
    searchResultAdapter.notifyDataSetChanged();
    if (!CommConstants.GET_ATTENTION_FINISH) {
      DialogUtils.getInstants().showLoadingDialog(getActivity(), "请稍候...", true);
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          if (CommConstants.GET_ATTENTION_FINISH) {
            DialogUtils.getInstants().dismiss();
            sortFreshData(searchText.getText().toString());
          } else {
            handler.postDelayed(this
                , 1500);
          }
        }
      }, 1500);
    }
  }

  private Handler handler = new Handler();

  @Override
  public void onDestroy() {
    super.onDestroy();
    tempFriendList.clear();
    tempFriendList = null;
  }
}
