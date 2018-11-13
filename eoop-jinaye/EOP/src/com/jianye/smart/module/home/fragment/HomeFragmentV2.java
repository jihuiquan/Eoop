package com.jianye.smart.module.home.fragment;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.com.xc.sdk.utils.ViewUtil;
import cn.com.xc.sdk.widget.tablayout.NoScrollViewPager;
import cn.com.xc.sdk.widget.tablayout.indicator.CircleIndicator;
import com.jianye.smart.module.workbench.manager.WorkTableClickDelagate;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.activity.UserDetailActivity;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.Callback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.im.module.record.activity.ChatRecordsActivityV2;
import com.movit.platform.innerea.activity.AlarmSettingActivity;
import com.jianye.smart.R;
import com.jianye.smart.activity.MainActivity;
import com.jianye.smart.module.qrcode.MyCodeActivity;
import com.jianye.smart.module.qrcode.TwoDimensionalCodeActivity;
import com.jianye.smart.module.workbench.constants.Constants;
import com.jianye.smart.module.workbench.manager.WorkTableManage;
import com.jianye.smart.module.workbench.model.WorkTable;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Response;

public class HomeFragmentV2 extends Fragment implements HomeView, OnClickListener,
    SwipeRefreshLayout.OnRefreshListener {

  private ViewPager prgTopViewPager;
  private CircleIndicator prgTopPageIndicator;
  private ViewPager prgBViewPager;
  private CircleIndicator prgBPageIndicator;
  private TabLayout prgMTableLayout;
  private NoScrollViewPager prgMViewPager;
  private HomePresenter homePresenter;
  private ImageView avatar;
  private TextView avatarName;
  private ImageView imgChat;
  public TextView numChat;
  private ImageView imgMail;
  private PopupWindow popupWindow;
  private EopHomePagerAdapter topPagerAdapter;
  private ImageView imgScan;

  private EopHomePagerAdapter bPagerAdapter;
  private WorkTableManage workTableManage;
  private DialogUtils progressDialogUtil;
  private HomeBannerFragment[] mBannerFragments;
  private SharedPreUtils spUtil;
  private SwipeRefreshLayout swipeRefresh;
  private View topView;
  private View bottomView;
  private View middleView;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = bindViews();
    progressDialogUtil = DialogUtils.getInstants();
    spUtil = new SharedPreUtils(getActivity());
    homePresenter = new HomePresenter(this, new HomeModel());
    workTableManage = new WorkTableManage(getActivity());
    progressDialogUtil.showLoadingDialog(getActivity(), "正在加载...", false);
    return view;
  }

  private void getCookie() {
    OkHttpUtils.get().url(
        "http://61.136.122.245:8075/WebReport/ReportServer?op=fs_load&cmd=sso&fr_username="+ CommConstants.loginConfig.getmUserInfo().getEmpAdname()
            +"&fr_password="+ CommConstants.loginConfig.getPassword() +"&fr_remember=true")
        .build().execute(new Callback() {

      @Override
      public Object parseNetworkResponse(Response response) throws Exception {
        List<String> cookies = response.headers("Set-Cookie");
        String cookie = cookies.toString().replace("[", "").replace("]", "");
        if (cookie.contains("JSESSIONID=")){
          new SharedPreUtils(getActivity()).setString(response.request().url().toString(), cookie);
        }
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            homePresenter.init();
          }
        });
        return null;
      }

      @Override
      public void onError(Call call, Exception e) {
      }

      @Override
      public void onResponse(Object response) throws org.json.JSONException {
      }
    });
  }

  @NonNull
  private View bindViews() {
    View view = LayoutInflater.from(getActivity())
        .inflate(R.layout.eop_fragment_home_v2, null, false);
    swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.eop_fragment_home_swp);
    avatar = (ImageView) view.findViewById(R.id.home_avatar);
    avatarName = (TextView) view.findViewById(R.id.home_avatar_name);
    topView = view.findViewById(R.id.eop_fragment_home_top);
    bottomView = view.findViewById(R.id.eop_fragment_home_bottom);
    middleView = view.findViewById(R.id.eop_fragment_home_middle);
    imgChat = (ImageView) view.findViewById(R.id.common_top_img_chat);
    numChat = (TextView) view.findViewById(R.id.common_top_img_chat_num);
    imgMail = (ImageView) view.findViewById(R.id.common_top_img_mail);
    imgScan = (ImageView) view.findViewById(R.id.common_top_img_scan);
    prgTopViewPager = (ViewPager) view.findViewById(R.id.eop_fragment_home_top_prg_viewpager);
    prgTopPageIndicator = (CircleIndicator) view.findViewById(R.id.eop_fragment_home_top_prg_dot);
    prgMTableLayout = (TabLayout) view.findViewById(R.id.eop_fragment_home_m_tab);
    prgMViewPager = (NoScrollViewPager) view.findViewById(R.id.eop_fragment_home_m_viewpager);
    prgBViewPager = (ViewPager) view.findViewById(R.id.eop_fragment_home_bottom_prg_viewpager);
    prgBPageIndicator = (CircleIndicator) view.findViewById(R.id.eop_fragment_home_bottom_prg_dot);
    swipeRefresh.setOnRefreshListener(this);
    ViewUtil.setOnClickListener(this, avatar, imgChat, imgMail, imgScan);
    return view;
  }

  @Override
  public void getUserData(UserInfo userInfo) {
    int defAvatar = R.drawable.avatar_male;
    if ("男".equals(userInfo.getGender())) {
      defAvatar = R.drawable.avatar_male;
    } else if ("女".equals(userInfo.getGender())) {
      defAvatar = R.drawable.avatar_female;
    }
    Picasso.with(getActivity()).load(CommConstants.URL_DOWN + userInfo.getAvatar())
        .placeholder(defAvatar).error(defAvatar)
        .into(avatar);
    avatarName.setText(userInfo.getEmpCname());
  }

  @Override
  public void getTableData(HomeBean homeData) {
    WorkTableClickDelagate.JIANYE_MYERP_SHENPI = "";//代办事项中的明源标签
    progressDialogUtil.dismiss();
    swipeRefresh.setRefreshing(false);
    topPagerAdapter = new EopHomePagerAdapter(getActivity(), fliterData(homeData.type1),
        prgTopPageIndicator, 1, 4);
    bPagerAdapter = new EopHomePagerAdapter(getActivity(), fliterData(homeData.type3),
        prgBPageIndicator, 0, 4);
    prgTopViewPager.setAdapter(topPagerAdapter);
    prgTopPageIndicator.setViewPager(prgTopViewPager);
    prgBViewPager.setAdapter(bPagerAdapter);
    prgBPageIndicator.setViewPager(prgBViewPager);
    topView.setVisibility(topPagerAdapter.getCount() <= 0 ? View.GONE : View.VISIBLE);
    bottomView.setVisibility(bPagerAdapter.getCount() <= 0 ? View.GONE : View.VISIBLE);
    if (homeData.division_info != null) {
      middleView.setVisibility(homeData.division_info.size() <= 0 ? View.GONE : View.VISIBLE);
      String[] titles = new String[homeData.division_info.size()];
      mBannerFragments = new HomeBannerFragment[homeData.division_info.size()];
      for (int i = 0; i < homeData.division_info.size(); i++) {
        titles[i] = homeData.division_info.get(i).name;
        mBannerFragments[i] = new HomeBannerFragment(
            String.valueOf(homeData.division_info.get(i).url));
      }
      EopHomeFragmentPagerAdapter pagerAdapter = new EopHomeFragmentPagerAdapter(
          getChildFragmentManager(), mBannerFragments, titles);
      prgMViewPager.setAdapter(pagerAdapter);
      prgMTableLayout.setupWithViewPager(prgMViewPager);
      prgMTableLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
      if (homeData.division_info != null) {
        for (int i = 0; i < homeData.division_info.size(); i++) {
          TextView view = new TextView(getContext());
          view.getPaint().setFakeBoldText(0 == i);
          view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
          view.setTextColor(getResources().getColorStateList(R.color.selector_home_m_tab_color));
          prgMTableLayout.getTabAt(i).setCustomView(pagerAdapter.getTabView(view, i));
        }
      }
      prgMTableLayout.addOnTabSelectedListener(new OnTabSelectedListener() {
        @Override
        public void onTabSelected(Tab tab) {
          if (tab == null || tab.getCustomView() == null) {
            return;
          }
          ((TextView) tab.getCustomView()).getPaint().setFakeBoldText(true);
        }

        @Override
        public void onTabUnselected(Tab tab) {
          if (tab == null || tab.getCustomView() == null) {
            return;
          }
          ((TextView) tab.getCustomView()).getPaint().setFakeBoldText(false);
        }

        @Override
        public void onTabReselected(Tab tab) {
          if (tab == null || tab.getCustomView() == null) {
            return;
          }
          ((TextView) tab.getCustomView()).getPaint().setFakeBoldText(false);
        }
      });
      prgMTableLayout.setVisibility(View.VISIBLE);
      prgMViewPager.setVisibility(View.VISIBLE);
    } else {
      middleView.setVisibility(View.GONE);
    }
    List<WorkTable> workTables = new ArrayList<>();
    workTables.addAll(fliterData(homeData.type1));
    workTables.addAll(fliterData(homeData.type3));
    workTableManage.getAllUnreadNumber(handler, workTables);
    progressDialogUtil.dismiss();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.home_avatar:
        Intent intent = new Intent(getActivity(), UserDetailActivity.class);
        intent.putExtra("userInfo", CommConstants.loginConfig.getmUserInfo());
        getActivity().startActivity(intent);
        break;
      case R.id.common_top_img_chat:
        startActivity(new Intent(getActivity(), ChatRecordsActivityV2.class));
        break;
      case R.id.common_top_img_mail:
        Intent eintent = new Intent();
        eintent.putExtra("TITLE", "发起邮件");
        eintent.putExtra("ACTION", "EMAIL");
        ((BaseApplication) getActivity().getApplication()).getUIController()
            .onIMOrgClickListener(getActivity(), eintent, 0);
        break;
      case R.id.common_top_img_scan:
        getPopupWindow();
        if (popupWindow.isShowing()) {
          popupWindow.dismiss();
        } else {
          popupWindow.showAsDropDown(v);
        }
        break;
      default:
        break;
    }
  }

  private void getPopupWindow() {
    if (null != popupWindow) {
      return;
    } else {
      initPopupWindow();
    }
  }

  private void initPopupWindow() {
    View view = LayoutInflater.from(getActivity()).inflate(R.layout.contact_pop_window, null);
    LinearLayout group_add = (LinearLayout) view.findViewById(R.id.pop_linearlayout_1);
    ImageView imageView1 = (ImageView) view.findViewById(R.id.pop_imageview_1);
    TextView textView1 = (TextView) view.findViewById(R.id.pop_textview_1);
    LinearLayout email_add = (LinearLayout) view.findViewById(R.id.pop_linearlayout_2);
    ImageView imageView2 = (ImageView) view.findViewById(R.id.pop_imageview_2);
    TextView textView2 = (TextView) view.findViewById(R.id.pop_textview_2);
    imageView1.setImageResource(R.drawable.scan_ico_scanning);
    textView1.setText("扫一扫");
    imageView2.setImageResource(R.drawable.scan_ico_download);
    textView2.setText("下载我");
    group_add.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (popupWindow != null && popupWindow.isShowing()) {
          popupWindow.dismiss();
        }
        Intent intent = new Intent();
        intent.setClass(getActivity(), MyCodeActivity.class);
        startActivity(intent);
      }
    });
    email_add.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (popupWindow != null && popupWindow.isShowing()) {
          popupWindow.dismiss();
        }
        startActivity(new Intent(getActivity(), TwoDimensionalCodeActivity.class));
      }
    });
    popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT);
    popupWindow.setFocusable(false);
    popupWindow.setOutsideTouchable(true);
    popupWindow.setBackgroundDrawable(new BitmapDrawable());
  }

  public Map<String, Integer> unReadNums = new HashMap<>();

  Handler handler = new Handler() {//主要处理未读消息

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case 1:
          break;
        case Constants.GET_UNREAD_TASK_MANAGE:
          unReadNums.put(WorkTableManage.TASK_MANAGE, (Integer) msg.obj);
          refreshUnread();
          break;
        case Constants.GET_UNREAD_DIARY:
          unReadNums.put(WorkTableManage.FUTURELAND_DIARY, (Integer) msg.obj);
          refreshUnread();
          break;
        case Constants.GET_TASK_UNREAD_NUM_RESULT:
          unReadNums.put(WorkTableManage.FUTURELAND_MANAGE, (Integer) msg.obj);
          refreshUnread();
          break;
        case Constants.GET_UNREAD_MING_YUAN_RESULT:// 明源流程
          unReadNums.put(WorkTableManage.MOBILEWORKFLOW, (Integer) msg.obj);
          unReadNums.put(WorkTableManage.MINGYUAN, (Integer) msg.obj);
          refreshUnread();
          break;
        case Constants.GET_UNREAD_EKP_RESULT://EKP流程审批
          unReadNums.put(WorkTableManage.FUTURELAND_APPROVAL, (Integer) msg.obj);
          refreshUnread();
          break;
        case Constants.GET_UNREAD_JING_YOU_RESULT://竟优流程审批
          unReadNums.put(WorkTableManage.LIVE, (Integer) msg.obj);
          refreshUnread();
          break;
        case Constants.GET_UNREAD_BID_OPENING_RESULT://移动开标待办数
          unReadNums.put(WorkTableManage.PROCUREMENT, (Integer) msg.obj);
          refreshUnread();
          break;
        case Constants.GET_UNREAD_SCHEDULE_TASK://运营管理未读
          unReadNums.put(WorkTableManage.SCHEDULE_TASK, (Integer) msg.obj);
          refreshUnread();
          break;
        case Constants.GET_UNREAD_DIARY_REPORT:
          unReadNums.put(WorkTableManage.DIARY_REPORT, (Integer) msg.obj);
          refreshUnread();
          break;
//        case PERSIMMIONRESULT:
//          saveAndShowWorkTable((String) msg.obj);
//          break;
//        case Constants.PERSONALMODULES_RESULT:
//          checkPermissions((String) msg.obj);
//          break;
//        case MODULE_ERROR:
//          progressDialogUtil.dismiss();
//          EOPApplication.showToast(getActivity(), "获取信息失败！");
//          setAdapter();
//          break;
//        case 33:// model del
//          WorkTable table = (WorkTable) msg.obj;
//          // 保存本地
//          WorkTable more = new WorkTable();
//          more.setId("more");
//          myWorkTables.remove(more);
//          String myjson = JSONArray.toJSONString(myWorkTables);
//          spUtil.setString("myWorkTables", myjson);
//          myWorkTables.add(more);
//
//          otherWorkTables.add(table);
//          String otherjson = JSONArray.toJSONString(otherWorkTables);
//          spUtil.setString("otherWorkTables", otherjson);
//          break;
//        case 34:
//          final WorkTable moreTable = new WorkTable();
//          moreTable.setId("more");
//          myWorkTables.remove(moreTable);
//          final String myjsons = JSONArray.toJSONString(myWorkTables);
//          spUtil.setString("myWorkTables", myjsons);
//          myWorkTables.add(moreTable);
//          break;
        case Constants.GET_UNREAD_TASK_ALL:
          String message = (String) msg.obj;
          final String[] strings = message.split("---");
          unReadNums.put(strings[0], Integer.valueOf(strings[1]));
          refreshUnread();
          break;
        default:
          progressDialogUtil.dismiss();
          break;
      }
    }

  };

  private void refreshUnread() {
    if (null != topPagerAdapter) {
      topPagerAdapter.setUnread(unReadNums);
    }
    if (null != bPagerAdapter) {
      bPagerAdapter.setUnread(unReadNums);
    }
    progressDialogUtil.dismiss();
  }

  @Override
  public void onResume() {
    super.onResume();
    getCookie();
  }

  private ArrayList<WorkTable> fliterData(List<WorkTable> data) {
    ArrayList<WorkTable> tmpData = new ArrayList<>();
    for (int i = 0; i < data.size(); i++) {
      WorkTable workTable = data.get(i);
      if ("innerea".equals(workTable.getAndroid_access_url())) {
        if (Constants.STATUS_AVAILABLE.equals(workTable.getStatus())) {
          if (MFSPHelper.getBoolean(AlarmSettingActivity.FIRSTALARM, false)) {
            AlarmSettingActivity.setAlarm(getActivity(), MFSPHelper.getString("alarmUpTime"),
                AlarmSettingActivity.FIRSTALARM_ID);
          } else {
            AlarmSettingActivity.cancelAlarm(getActivity(), AlarmSettingActivity.FIRSTALARM_ID);
          }
          if (MFSPHelper.getBoolean(AlarmSettingActivity.LASTALARM, false)) {
            AlarmSettingActivity.setAlarm(getActivity(), MFSPHelper.getString("alarmDownTime"),
                AlarmSettingActivity.LASTALARM_ID);
          } else {
            System.out.println(MFSPHelper.getString("alarmDownTime"));
            AlarmSettingActivity.cancelAlarm(getActivity(), AlarmSettingActivity.LASTALARM_ID);
          }
        } else {
          AlarmSettingActivity.cancelAlarm(getActivity(), AlarmSettingActivity.FIRSTALARM_ID);
          AlarmSettingActivity.cancelAlarm(getActivity(), AlarmSettingActivity.LASTALARM_ID);
        }
      }
      if (Constants.STATUS_OFFLINE.equals(data.get(i).getStatus())) {
        continue;
      }
      if (Constants.STATUS_AVAILABLE.equals(data.get(i).getStatus())) {
        tmpData.add(data.get(i));
      }
    }
    return tmpData;
  }

  @Override
  public void onRefresh() {
    getCookie();
  }
}
