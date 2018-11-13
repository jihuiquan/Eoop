package com.movit.platform.sc.module.zone.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.movit.platform.common.api.IZoneManager;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.faceview.FaceViewPage;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.widget.SelectPicPopup;
import com.movit.platform.framework.view.xlistview.XListView;
import com.movit.platform.framework.view.xlistview.XListView.IXListViewListener;
import com.movit.platform.sc.R;
import com.movit.platform.sc.base.BaseFragment;
import com.movit.platform.sc.entities.Comment;
import com.movit.platform.sc.entities.Zone;
import com.movit.platform.sc.module.zone.adapter.ZoneAdapter;
import com.movit.platform.sc.module.zone.constant.ZoneConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressLint("ValidFragment")
public class ZoneFragment extends BaseFragment implements
        IXListViewListener, OnTouchListener, OnClickListener, OnScrollListener {

    protected RelativeLayout topLeftrl;
    protected RelativeLayout zone_top_bar;
    protected ImageView message;
    protected TextView title;
    protected ImageView pencil;

    protected TextView dian;
    protected XListView zoneListView;
    protected ZoneAdapter adapter;
    protected List<Zone> zoneList = new ArrayList<Zone>();

    private LinearLayout mBottomView;
    private FaceViewPage faceViewPage;
    private boolean mIsFaceShow = false;// 是否显示表情
    private Button mFaceSwitchBtn;// 切换表情的button
    private Button mSendMsgBtn;// 发送消息button
    private EditText mEditText;// 消息输入框
    private InputMethodManager mInputMethodManager;

    private SharedPreUtils spUtil;

    private int currentPos;
    private Comment curComment;
    private boolean visitTop = false;

    private SelectPicPopup popWindow;
    private ArrayList<Zone> currentTop = new ArrayList<Zone>();

    protected IZoneManager zoneManager;
    private IZoneFragment _zoneFragment;

    public interface IZoneFragment {
        public void setBottomTabStatus(boolean isShow);
    }

    public ZoneFragment(IZoneFragment zoneFragment) {
        this._zoneFragment = zoneFragment;
    }

    protected Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            ArrayList<String> delList = data.getStringArrayList("delList");
            ArrayList<Zone> newList = (ArrayList<Zone>) data
                    .getSerializable("newList");
            ArrayList<Zone> oldList = (ArrayList<Zone>) data
                    .getSerializable("oldList");
            ArrayList<Zone> topList = (ArrayList<Zone>) data
                    .getSerializable("topList");

            switch (msg.what) {
                case ZoneConstants.ZONE_LIST_RESULT:
                    DialogUtils.getInstants().dismiss();

                    if (zoneList.isEmpty()) {
                        visitTop = true;
                    }

                    ArrayList<Zone> temp = new ArrayList<Zone>();
                    temp.addAll(zoneList);
                    for (int i = 0; i < temp.size(); i++) {
                        if (temp.get(i).getiTop() != null) {
                            if (temp.get(i).getiTop().equals("1")) {
                                zoneList.remove(temp.get(i));
                            }
                        }
                    }
                    temp.clear();
                    temp.addAll(zoneList);
                    for (int i = 0; i < currentTop.size(); i++) {
                        long currTime = DateUtils.str2Date(
                                currentTop.get(i).getdCreateTime()).getTime();
                        for (int j = 0; j < temp.size(); j++) {
                            if (j != 0) {
                                long time1 = DateUtils.str2Date(
                                        temp.get(j).getdCreateTime()).getTime();
                                long time2 = DateUtils.str2Date(
                                        temp.get(j - 1).getdCreateTime()).getTime();
                                if (currTime < time2 && currTime > time1) {
                                    currentTop.get(i).setiTop("0");
                                    zoneList.add(j, currentTop.get(i));
                                }
                            }

                        }
                    }
                    temp.clear();
                    temp.addAll(zoneList);
                    // 去除当前删除的，然后去除当前置顶的
                    for (int i = 0; i < temp.size(); i++) {
                        if (delList.contains(temp.get(i).getcId())) {
                            zoneList.remove(temp.get(i));
                        }
                        if (topList.contains(temp.get(i))) {
                            zoneList.remove(temp.get(i));
                        }
                    }
                    temp = null;
                    for (int j = 0; j < zoneList.size(); j++) {
                        for (int j2 = 0; j2 < oldList.size(); j2++) {
                            if (zoneList.get(j).equals(oldList.get(j2))) {
                                zoneList.remove(j);
                                zoneList.add(j, oldList.get(j2));
                            }
                        }
                    }
                    zoneList.addAll(0, newList);
                    zoneList.addAll(0, topList);
                    currentTop.clear();
                    currentTop.addAll(topList);

                    // ArrayList<Zone> arrayList = (ArrayList<Zone>) msg.obj;
                    // zoneList.addAll(0, arrayList);
                    zoneListView.stopRefresh();
                    zoneListView.setRefreshTime(DateUtils.date2Str(new Date()));
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    } else {
                        setArapter();

                    }
                    if (visitTop) {
                        zoneListView.setSelection(0);
                        visitTop = false;
                    }
                    break;
                case ZoneConstants.ZONE_MORE_RESULT:
                    ArrayList<Zone> temp2 = new ArrayList<Zone>();
                    temp2.addAll(zoneList);
                    for (int i = 0; i < temp2.size(); i++) {
                        if (temp2.get(i).getiTop() != null) {
                            if (temp2.get(i).getiTop().equals("1")) {
                                zoneList.remove(temp2.get(i));
                            }
                        }
                    }
                    temp2.clear();
                    temp2.addAll(zoneList);
                    for (int i = 0; i < currentTop.size(); i++) {
                        long currTime = DateUtils.str2Date(
                                currentTop.get(i).getdCreateTime()).getTime();
                        for (int j = 0; j < temp2.size(); j++) {
                            if (j != 0) {
                                long time1 = DateUtils.str2Date(
                                        temp2.get(j).getdCreateTime()).getTime();
                                long time2 = DateUtils.str2Date(
                                        temp2.get(j - 1).getdCreateTime())
                                        .getTime();
                                if (currTime < time2 && currTime > time1) {
                                    Log.v("top", "插入" + j);
                                    currentTop.get(i).setiTop("0");
                                    zoneList.add(j, currentTop.get(i));
                                }
                            }
                        }
                    }
                    temp2.clear();
                    temp2.addAll(zoneList);
                    // 去除当前删除的，然后去除当前置顶的
                    for (int i = 0; i < temp2.size(); i++) {
                        if (delList.contains(temp2.get(i).getcId())) {
                            zoneList.remove(temp2.get(i));
                        }
                        if (topList.contains(temp2.get(i))) {
                            zoneList.remove(temp2.get(i));
                        }
                    }
                    temp2 = null;
                    for (int j = 0; j < zoneList.size(); j++) {
                        for (int j2 = 0; j2 < oldList.size(); j2++) {
                            if (zoneList.get(j).equals(oldList.get(j2))) {
                                zoneList.remove(j);
                                zoneList.add(j, oldList.get(j2));
                            }
                        }
                    }
                    zoneList.addAll(newList);
                    zoneList.addAll(0, topList);
                    currentTop.clear();
                    currentTop.addAll(topList);
                    // ArrayList<Zone> moreList = (ArrayList<Zone>) msg.obj;
                    // zoneList.addAll(moreList);

                    zoneListView.stopLoadMore();
                    adapter.notifyDataSetChanged();
                    break;
                case ZoneConstants.ZONE_CLICK_AVATAR:// click avatar
                    String userId = (String) msg.obj;

                    ArrayList<String> idStrings = CommConstants.loginConfig.getmUserInfo()
                            .getAttentionPO();

                    try {
                        boolean gozone = getActivity().getPackageManager().getApplicationInfo(
                                getActivity().getPackageName(), PackageManager.GET_META_DATA).metaData
                                .getBoolean("CHANNEL_ATTENTION_SEEZONE", false);
                        if (gozone) {
                            if (StringUtils.notEmpty(userId) && (spUtil.getString(CommConstants.USERID).equals(userId) || (null != idStrings && idStrings.contains(userId)))) {
                                Intent intent = new Intent();
                                intent.putExtra("userId", userId);
                                ((BaseApplication) ZoneFragment.this.getActivity().getApplication()).getUIController().onZoneOwnClickListener(ZoneFragment.this, intent, ZoneConstants.ZONE_CLICK_AVATAR);
                            } else {
                                UserDao dao = UserDao.getInstance(ZoneFragment.this.getActivity());
                                String empCname = dao.getUserInfoById(userId).getEmpCname();
                                dao.closeDb();
                                ToastUtils.showToast(ZoneFragment.this.getActivity(), "关注" + empCname + "后才能查看同事圈");
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("userId", userId);
                            ((BaseApplication) ZoneFragment.this.getActivity().getApplication()).getUIController().onZoneOwnClickListener(ZoneFragment.this, intent, ZoneConstants.ZONE_CLICK_AVATAR);
                        }
                    } catch (PackageManager.NameNotFoundException e1) {
                        e1.printStackTrace();
                    }


                    break;
                case ZoneConstants.ZONE_NICE_RESULT:
                    DialogUtils.getInstants().dismiss();
                    try {
                        String result = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(result);
                        int code = jsonObject.getInt("code");
                        if (0 == code) {
                            int postion = msg.arg1;
                            int isNice = msg.arg2;
                            Zone zone = zoneList.get(postion);
                            if (isNice == 0) {// 赞
                                if (zone.getLikers() == null) {
                                    ArrayList<String> likser = new ArrayList<String>();
                                    likser.add(spUtil.getString(CommConstants.USERID));
                                    zone.setLikers(likser);
                                } else {
                                    zone.getLikers().add(0,
                                            spUtil.getString(CommConstants.USERID));
                                }
                            } else if (isNice == 1) {// 取消赞
                                if (zone.getLikers() != null) {
                                    zone.getLikers().remove(
                                            spUtil.getString(CommConstants.USERID));
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            ToastUtils.showToast(getActivity(), "点赞失败！");
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.showToast(getActivity(), "点赞失败！");
                    }
                    break;
                case ZoneConstants.ZONE_CLICK_COMMENT:// 评论
                    currentPos = (Integer) msg.obj;
                    int commentLine = msg.arg1;
                    int type = msg.arg2;
                    if (type == 0) {
                        mEditText.setHint("评论");
                        curComment = null;
                    } else if (type == 1) {// 在评论
                        List<Comment> comments = zoneList.get(currentPos)
                                .getComments();
                        curComment = comments.get(commentLine);
                        UserDao dao = UserDao.getInstance(getActivity());
                        UserInfo userInfo = dao.getUserInfoById(curComment
                                .getUserId());
                        if (userInfo != null) {
                            mEditText.setHint("回复：" + userInfo.getEmpCname());
                        }
                    }
                    // 弹出输入框
                    mBottomView.setVisibility(View.VISIBLE);
                    mEditText.requestFocus();
                    mInputMethodManager.showSoftInput(mEditText, 0);

                    //TODO add by anna
                    _zoneFragment.setBottomTabStatus(false);
                    break;
                case ZoneConstants.ZONE_COMMENT_RESULT:
                    DialogUtils.getInstants().dismiss();
                    try {
                        String comment_result = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(comment_result);
                        int code = jsonObject.getInt("code");
                        if (0 == code) {
                            int postion = msg.arg1;
                            Zone zone = zoneList.get(postion);

                            JSONObject itemObject = jsonObject
                                    .getJSONObject("item");
                            Comment comment = new Comment();
                            if (itemObject.has("sContent")) {
                                comment.setContent(itemObject.getString("sContent"));
                            }
                            if (itemObject.has("cUserId")) {
                                comment.setUserId(itemObject.getString("cUserId"));
                            }
                            if (itemObject.has("cToUserId")) {
                                comment.setTouserId(itemObject
                                        .getString("cToUserId"));
                            }
                            if (itemObject.has("cId")) {
                                comment.setcId(itemObject.getString("cId"));
                            }
                            if (itemObject.has("cParentId")) {
                                String cParentId = itemObject
                                        .getString("cParentId");
                                comment.setParnetId(cParentId);
                            }
                            if (itemObject.has("cRootId")) {
                                String cRootId = itemObject.getString("cRootId");
                                comment.setRootId(cRootId);
                            }
                            if (itemObject.has("cSayId")) {
                                String cSayId = itemObject.getString("cSayId");
                                comment.setSayId(cSayId);
                            }
                            if (zone.getComments() == null) {
                                ArrayList<Comment> comments = new ArrayList<Comment>();
                                comments.add(comment);
                                zone.setComments(comments);
                            } else {
                                zone.getComments().add(comment);
                            }
                            mInputMethodManager.hideSoftInputFromWindow(
                                    mEditText.getWindowToken(), 0);
                            faceViewPage.getmFaceRoot().setVisibility(View.GONE);
                            mBottomView.setVisibility(View.GONE);
                            mIsFaceShow = false;

                            _zoneFragment.setBottomTabStatus(true);
                            mEditText.setText("");
                            adapter.notifyDataSetChanged();
                        } else {
                            ToastUtils.showToast(getActivity(), "发表评论失败！");
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.showToast(getActivity(), "发表评论失败！");
                    }
                    break;
                case ZoneConstants.ZONE_MESSAGE_COUNT_RESULT:// 轮询 消息提醒
                    showRedPoint((String) msg.obj);
                    break;
                case ZoneConstants.ZONE_CLICK_COMMENT_TO_DEL:
                    int delCommentLine = msg.arg1;
                    int delPostion = msg.arg2;

                    // 弹出菜单popWindow
                    InputMethodManager imm = (InputMethodManager) getActivity()
                            .getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(mRootView.getWindowToken(), 0);
                    }
                    // 实例化SelectPicPopupWindow
                    popWindow = new SelectPicPopup(getActivity(),
                            new ItemsOnClick(delCommentLine, delPostion));
                    popWindow.showDel();
                    // 显示窗口
                    popWindow.showAtLocation(
                            getActivity().findViewById(R.id.zone_listview),
                            Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置

                    break;
                case ZoneConstants.ZONE_COMMENT_DEL_RESULT:
                    DialogUtils.getInstants().dismiss();
                    try {
                        String comment_result = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(comment_result);
                        int code = jsonObject.getInt("code");
                        if (0 == code) {
                            int postion = msg.arg1;
                            int delLine = msg.arg2;
                            Zone zone = zoneList.get(postion);
                            zone.getComments().remove(delLine);
                            adapter.notifyDataSetChanged();
                        } else {
                            ToastUtils.showToast(getActivity(), "删除失败！");
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case ZoneConstants.ZONE_ERROR_RESULT:
                    DialogUtils.getInstants().dismiss();
                    String errorMsg = (String) msg.obj;
                    ToastUtils.showToast(getActivity(), errorMsg);
                    zoneListView.stopRefresh();
                    zoneListView.stopLoadMore();
                    if (adapter == null) {
                        setArapter();
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    protected void showRedPoint(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            int code = jsonObject.getInt("code");
            if (code == 0) {
                if (jsonObject.has("val")) {
                    String val = jsonObject.getString("val");
                    if ("0".equals(val)) {
                        dian.setVisibility(View.GONE);
                    } else {
                        dian.setText(val);
                        dian.setVisibility(View.VISIBLE);
                    }
                } else {
                    dian.setVisibility(View.GONE);
                }
            } else {
                dian.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                dian.setVisibility(View.GONE);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spUtil = new SharedPreUtils(getActivity());
        zoneManager = ((BaseApplication) getActivity().getApplication()).getManagerFactory().getZoneManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.sc_fragment_zone, null, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void initViews() {
        mInputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(getActivity().INPUT_METHOD_SERVICE);

        zone_top_bar = (RelativeLayout) findViewById(R.id.zone_top_bar);

        if (!"default".equals(spUtil.getString(BaseApplication.SKINTYPE))) {
            zone_top_bar.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }

        message = (ImageView) findViewById(R.id.common_top_img_left);
        title = (TextView) findViewById(R.id.tv_common_top_title);
        pencil = (ImageView) findViewById(R.id.common_top_img_right);
        zoneListView = (XListView) findViewById(R.id.zone_listview);
        topLeftrl = (RelativeLayout) findViewById(R.id.common_top_left_rl);
        dian = (TextView) findViewById(R.id.common_top_main_dian);
        mBottomView = (LinearLayout) findViewById(R.id.zone_bottom_rootview);
        mFaceSwitchBtn = (Button) findViewById(R.id.zone_input_face);
        mSendMsgBtn = (Button) findViewById(R.id.zone_input_send);
        mEditText = (EditText) findViewById(R.id.zone_input_text);
        mBottomView.setVisibility(View.GONE);
        dian.setVisibility(View.GONE);
        zoneListView.setOnTouchListener(this);
        mEditText.setOnTouchListener(this);
        mFaceSwitchBtn.setOnClickListener(this);
        mSendMsgBtn.setOnClickListener(this);

        initFacePage();// 初始化表情页面

        mEditText.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mIsFaceShow) {
                        faceViewPage.getmFaceRoot().setVisibility(View.GONE);
                        mIsFaceShow = false;
                        mFaceSwitchBtn
                                .setBackgroundResource(R.drawable.m_chat_emotion_selector);
                        return true;
                    }
                }
                return false;
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    mSendMsgBtn.setEnabled(true);
                } else {
                    mSendMsgBtn.setEnabled(false);
                }
            }
        });

        pencil.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //跳转到发布消息界面
                Intent intent = new Intent();
                ((BaseApplication) ZoneFragment.this.getActivity().getApplication()).getUIController().onZonePublishClickListener(ZoneFragment.this, intent, 1);
            }
        });
        topLeftrl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //跳转到消息列表界面
                Intent intent = new Intent();
                ((BaseApplication) ZoneFragment.this.getActivity().getApplication()).getUIController().onZoneMsgClickListener(ZoneFragment.this, intent, 2);
            }
        });
    }

    private void initFacePage() {
        faceViewPage = new FaceViewPage(mEditText, mRootView);
        faceViewPage.initFacePage();

    }

    @Override
    protected void initDatas() {
        DialogUtils.getInstants().showLoadingDialog(getActivity(), "正在加载...", false);
        zoneList.clear();

        UserDao dao = UserDao.getInstance(getActivity());
        String officeId = dao.getUserInfoById(spUtil.getString(CommConstants.USERID)).getOrgId();
        dao.closeDb();
        zoneManager.getZoneListData(officeId, "", "", "", "", "", "", handler);
    }

    private void setArapter() {
        adapter = new ZoneAdapter(getActivity(), zoneList,
                handler, ZoneAdapter.TYPE_MAIN,
                spUtil.getString(CommConstants.USERID), DialogUtils.getInstants(), zoneManager);
        zoneListView.setAdapter(adapter);
        if (zoneList.isEmpty()) {
            zoneListView.setPullLoadEnable(false);
        } else {
            zoneListView.setPullLoadEnable(true);
        }
        zoneListView.setPullRefreshEnable(true);
        zoneListView.setXListViewListener(this);
        zoneListView.setOnScrollListener(this);
        zoneListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
            }
        });
    }

    @Override
    protected void resumeDatas() {
        zoneManager.messagecount(handler);
    }

    public void refreshDian(String val) {
        if (dian != null) {
            if ("0".equals(val)) {
                dian.setVisibility(View.GONE);
            } else {
                dian.setText(val);
                dian.setVisibility(View.VISIBLE);
            }
        }
    }

    public void notifyList() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void refreshData(final boolean isClear) {
        if (adapter != null) {
            DialogUtils.getInstants()
                    .showLoadingDialog(getActivity(), "正在加载...", false);
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (zoneListView.ismPullLoading()
                            || zoneListView.ismPullRefreshing()) {
                        handler.postDelayed(this, 500);
                    } else {
                        if (isClear) {
                            zoneList.clear();
                        }

                        UserDao dao = UserDao.getInstance(getActivity());
                        String officeId = dao.getUserInfoById(spUtil.getString(CommConstants.USERID)).getOrgId();
                        dao.closeDb();

                        zoneManager.getZoneListData(
                                officeId, spUtil.getString("refreshTime"), "", "", "",
                                "", "", handler);
                    }
                }
            }, 500);
        }
    }

    @Override
    public void onRefresh() {
        if (zoneList.isEmpty()) {
            UserDao dao = UserDao.getInstance(getActivity());
            String officeId = dao.getUserInfoById(spUtil.getString(CommConstants.USERID)).getOrgId();
            dao.closeDb();

            zoneManager.getZoneListData(officeId, spUtil.getString("refreshTime"), "",
                    "", "", "", "", handler);
        } else {
            String tCreateTime = "";
            for (int i = 0; i < zoneList.size(); i++) {
                if (zoneList.get(i).getiTop() != null) {
                    if (zoneList.get(i).getiTop().equals("0")) {
                        tCreateTime = zoneList.get(i).getdCreateTime();
                        break;
                    }
                }
            }

            UserDao dao = UserDao.getInstance(getActivity());
            String officeId = dao.getUserInfoById(spUtil.getString(CommConstants.USERID)).getOrgId();
            dao.closeDb();

            zoneManager.getZoneListData(officeId, spUtil.getString("refreshTime"),
                    tCreateTime, zoneList.get(zoneList.size() - 1)
                            .getdCreateTime(), "0", "", "", handler);
        }
    }

    @Override
    public void onLoadMore() {
        String tCreateTime = "";
        for (int i = 0; i < zoneList.size(); i++) {
            if (zoneList.get(i).getiTop() != null) {
                if (zoneList.get(i).getiTop().equals("0")) {
                    tCreateTime = zoneList.get(i).getdCreateTime();
                    break;
                }
            }
        }
        if (!zoneList.isEmpty()) {

            UserDao dao = UserDao.getInstance(getActivity());
            String officeId = dao.getUserInfoById(spUtil.getString(CommConstants.USERID)).getOrgId();
            dao.closeDb();

            zoneManager.getZoneListData(officeId, spUtil.getString("refreshTime"),
                    tCreateTime, zoneList.get(zoneList.size() - 1)
                            .getdCreateTime(), "1", "", "", handler);
        } else {
            zoneListView.stopLoadMore();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == 1) {// 发表成功
                visitTop = true;
                onRefresh();// 滑到top
            }
        } else if (requestCode == ZoneConstants.ZONE_CLICK_AVATAR) {
            if (resultCode == ZoneConstants.ZONE_SAY_DEL_RESULT) {// 删除成功
                onRefresh();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.zone_input_face) {
            if (!mIsFaceShow) {
                handler.postDelayed(new Runnable() {
                    // 解决此时界面会变形，有闪烁的现象
                    @Override
                    public void run() {
                        mFaceSwitchBtn
                                .setBackgroundResource(R.drawable.m_chat_keyboard_selector);
                        faceViewPage.getmFaceRoot().setVisibility(View.VISIBLE);
                        mIsFaceShow = true;
                    }
                }, 80);
                mInputMethodManager.hideSoftInputFromWindow(
                        mEditText.getWindowToken(), 0);
            } else {
                mEditText.requestFocus();
                mInputMethodManager.showSoftInput(mEditText, 0);
                mFaceSwitchBtn
                        .setBackgroundResource(R.drawable.m_chat_emotion_selector);
                faceViewPage.getmFaceRoot().setVisibility(View.GONE);
                mIsFaceShow = false;
                _zoneFragment.setBottomTabStatus(false);
            }
        } else if (id == R.id.zone_input_send) {
            String content = mEditText.getText().toString().trim();
            if ("".equals(content)) {
                return;
            }
            DialogUtils.getInstants()
                    .showLoadingDialog(getActivity(), "发表评论...", false);
            Zone zone = zoneList.get(currentPos);
            if (curComment == null) {
                zoneManager.comment(zone.getcId(),
                        spUtil.getString(CommConstants.USERID), "0", content, "0",
                        "0", currentPos, handler);
            } else {
                zoneManager.comment(zone.getcId(),
                        spUtil.getString(CommConstants.USERID),
                        curComment.getUserId(), content, curComment.getcId(),
                        "0", currentPos, handler);
            }
        } else {
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if (id == R.id.zone_input_text) {
            mInputMethodManager.showSoftInput(mEditText, 0);
            mFaceSwitchBtn
                    .setBackgroundResource(R.drawable.m_chat_emotion_selector);
            faceViewPage.getmFaceRoot().setVisibility(View.GONE);
            mIsFaceShow = false;
        } else if (id == R.id.zone_listview) {
            mInputMethodManager.hideSoftInputFromWindow(
                    mEditText.getWindowToken(), 0);
            mFaceSwitchBtn
                    .setBackgroundResource(R.drawable.m_chat_emotion_selector);
            faceViewPage.getmFaceRoot().setVisibility(View.GONE);
            mBottomView.setVisibility(View.GONE);
            _zoneFragment.setBottomTabStatus(true);
            mIsFaceShow = false;
        } else {
        }
        return false;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int itemsLastIndex = adapter.getCount() - 1; // 数据集最后一项的索引
        int lastIndex = itemsLastIndex + 2; // 加上底部的loadMoreView项 和顶部刷新的
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
                && visibleLastIndex == lastIndex) {
            // 如果是自动加载,可以在这里放置异步加载数据的代码
            zoneListView.startLoadMore();
        }
    }

    int visibleLastIndex;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }

    private class ItemsOnClick implements OnClickListener {
        int delCommentLine;
        int delPostion;

        public ItemsOnClick(int delCommentLine, int currentPos) {
            super();
            this.delCommentLine = delCommentLine;
            this.delPostion = currentPos;
        }

        @Override
        public void onClick(View v) {
            popWindow.dismiss();
            int id = v.getId();
            if (id == R.id.btn_del) {
                Zone zone = zoneList.get(delPostion);
                DialogUtils.getInstants().showLoadingDialog(getActivity(), "请稍候...",
                        false);
                zoneManager.commentdel(zone.getComments().get(delCommentLine)
                        .getcId(), delPostion, delCommentLine, handler);
            }
        }
    }

}
