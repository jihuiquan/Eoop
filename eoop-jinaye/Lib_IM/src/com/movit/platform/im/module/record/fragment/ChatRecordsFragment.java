package com.movit.platform.im.module.record.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.entities.TokenBean;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.core.okhttp.callback.StringCallback2;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.LogUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.swipeLayout.implments.SwipeItemMangerImpl;
import com.movit.platform.im.R;
import com.movit.platform.im.base.ChatBaseFragment;
import com.movit.platform.im.broadcast.MessageReceiver;
import com.movit.platform.im.broadcast.SystemReceiver;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.db.IMDBFactory;
import com.movit.platform.im.db.SessionManager;
import com.movit.platform.im.manager.IMManager;
import com.movit.platform.im.module.group.activity.GroupChatActivity;
import com.movit.platform.im.module.group.entities.Group;
import com.movit.platform.im.module.msg.activity.SystemMsgActivity;
import com.movit.platform.im.module.msg.helper.MsgListComparator;
import com.movit.platform.im.module.record.adapter.ChatRecordsAdapter;
import com.movit.platform.im.utils.BuildQueryString;
import com.movit.platform.im.widget.CurPopup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import okhttp3.Call;
import org.json.JSONException;

public class ChatRecordsFragment extends ChatBaseFragment implements
        OnRefreshListener, CurPopup.PopupListener ,SystemReceiver.CallBack {

    private TextView title;

    private View headerView;
    private ListView mSwipeListView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView refreshTime, tipsText;

    private EditText searchKey;
    private ImageView searchClear;

    private Context context;
    private SessionManager sessionManager;
    private InputMethodManager inputmanger;
    private ChatRecordsAdapter recentAdapter;

    private List<MessageBean> tempDatas = new ArrayList<>();
    private Set<String> markUnReadIds, markReadIds;

    public static final int POP_MORE_BTN = 1;
    public static final int POP_DEL_BTN = 2;
    public static final int MARK_READ_STATUS = 6;
    public static final int LEAVE_SESSION = 7;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case POP_DEL_BTN:
                    int postion = (Integer) msg.obj;
                    HttpManager.getJson(
                        "http://gzt.jianye.com.cn/eop-im/im/updateContact?contactId=" + tempDatas
                            .get(postion).getContactId(), new StringCallback2() {
                            @Override
                            public void onError(Call call, Exception e) {
                                ToastUtils.showToast(getActivity(),"删除失败");
                            }

                            @Override
                            public void onResponse(String response) throws JSONException {
                                JSONObject object = JSON.parseObject(response);
                                if (!object.getBoolean("result")){
                                    ToastUtils.showToast(getActivity(),"删除失败");
                                }
                            }
                        });
                    if (swipeRefreshLayout.isRefreshing()) {
                        postion = postion - 1;
                    }
                    if (postion >= tempDatas.size()) {
                        recentAdapter = null;
                        sortFreshData(searchKey.getText().toString());
                        return;
                    }
                    if (tempDatas.get(postion).getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                        sessionManager.deleteSession(tempDatas.get(postion).getFriendId());
                        // 离开单人聊天会话：
                        IMManager.leavePrivateSession(tempDatas.get(postion).getFriendId());
                    } else if (tempDatas.get(postion).getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                        sessionManager.deleteSession(tempDatas.get(postion).getRoomId());
                        // 离开群组聊天会话
                        IMManager.leaveGroupSession(tempDatas.get(postion).getRoomId());
                    } else if (tempDatas.get(postion).getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                        IMConstants.sysMsgList.clear();
                    }
//                    sessionManager.closeDb();
                    IMConstants.contactListDatas.remove(tempDatas.get(postion));
                    setPoint();
                    recentAdapter = null;
                    sortFreshData(searchKey.getText().toString());
                    break;
                case POP_MORE_BTN:
                    break;
                case MARK_READ_STATUS:
                    int position = (Integer) msg.obj;
                    MessageBean bean = tempDatas.get(position);

                    if (bean.getMarkReadStatus() == -1) {
                        if (bean.getUnReadCount() == 0) {
                            bean.setMarkReadStatus(1);
                        } else {
                            bean.setMarkReadStatus(0);
                            handler.obtainMessage(
                                    ChatRecordsFragment.LEAVE_SESSION,
                                    position).sendToTarget();
                        }
                    } else if (bean.getMarkReadStatus() == 0) {
                        bean.setMarkReadStatus(1);
                    } else {
                        bean.setMarkReadStatus(0);
                    }
                    recentAdapter.notifyDataSetChanged();

                    if (bean.getMarkReadStatus() == 1) {
                        saveReadStatus(bean, 1);
                    } else {
                        saveReadStatus(bean, 0);
                    }

                    setPoint();

                    break;
                case 5:
//                    if (recentAdapter != null) {
//                        setPoint();
//                        sortFreshData(searchKey.getText().toString());
//                    }
                    onRefresh();
                    break;
                case LEAVE_SESSION:
                    int leave_session_position = (Integer) msg.obj;
                    IMConstants.contactListDatas.get(leave_session_position).setUnReadCount(0);
                    if (tempDatas.get(leave_session_position).getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                        // 离开单人聊天会话：
                        IMManager.leavePrivateSession(tempDatas.get(leave_session_position).getFriendId());
                    } else if (tempDatas.get(leave_session_position).getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                        // 离开群组聊天会话
                        IMManager.leaveGroupSession(tempDatas.get(leave_session_position).getRoomId());
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //保存消息的已读未读状态
    private void saveReadStatus(MessageBean bean, int type) {
        if (bean.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
//            if (bean.getRsflag() == 1) {
//                addSet(bean.getCuserId(), type);
//            } else {
            addSet(bean.getFriendId(), type);
//            }
        } else if (bean.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
            addSet(bean.getRoomId(), type);
        }

        MFSPHelper.setStringSet(CommConstants.MARK_UNREAD_IDS, markUnReadIds);
        MFSPHelper.setStringSet(CommConstants.MARK_READ_IDS, markReadIds);
    }

    private void addSet(String id, int type) {
        if (type == 0) {
            markReadIds.add(id);
            markUnReadIds.remove(id);
        } else {
            markUnReadIds.add(id);
            markReadIds.remove(id);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inputmanger = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        context = getActivity();
        sessionManager = IMDBFactory.getInstance(context).getSessionManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater
                .inflate(R.layout.im_fragment_chat_recent, null, false);

        IntentFilter filter = new IntentFilter();
        filter.addAction(CommConstants.ACTION_NEW_MESSAGE);
        filter.addAction(CommConstants.ACTION_GROUP_LIST_RESPONSE);
        getActivity().registerReceiver(messageReceiver, filter);

//        IntentFilter filter3 = new IntentFilter();
//        filter3.addAction(CommConstants.ACTION_MY_INVITE);
//        filter3.addAction(CommConstants.ACTION_MY_KICKED);
//        filter3.addAction(CommConstants.ACTION_GROUP_DISPALYNAME_CHANGES);
//        filter3.addAction(CommConstants.ACTION_GROUP_DISSOLVE_CHANGES);
//        filter3.addAction(CommConstants.ACTION_GROUP_MEMBERS_CHANGES);
//        getActivity().registerReceiver(systemReceiver, filter3);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(CommConstants.ACTION_CONTACT_LIST);
        getActivity().registerReceiver(contactListReceiver, filter2);


        IntentFilter filter4 = new IntentFilter();
        filter4.addAction(CommConstants.ACTION_XMPP_LOGIN);
        getActivity().registerReceiver(xmppLoginReceiver, filter4);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private BroadcastReceiver xmppLoginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(CommConstants.ACTION_XMPP_LOGIN.equals(action)){
                String type = intent.getStringExtra("type");
                if(CommConstants.TYPE_XMPP_LOGIN_SUCCESS.equals(type)){
                    CommConstants.loginXmppTime += 1;
                    title.setText(R.string.top_chat);
                    onRefresh();
                    Log.d("chatRecords", "login_success: "+CommConstants.loginXmppTime);
                }else if(CommConstants.TYPE_JUST_TIPS.equals(type)){
                    title.setText(R.string.top_chat_offline);
                    CommConstants.loginXmppTime += 1;
                    Log.d("chatRecords", "login_faile: "+ +CommConstants.loginXmppTime);
                }
            }
        }
    };


    private MessageReceiver messageReceiver = new MessageReceiver(new MessageReceiver.CallBack() {

        @Override
        public void setRedPoint(int point) {

        }

        @Override
        public void updateRecordList(Map<String, Integer> recordMap) {
            onRefresh();
        }

        @Override
        public void receiveNewMessage(MessageBean messageBean) {

            if (messageBean.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                clearReadStatus(messageBean.getFriendId());
            }else if (messageBean.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                clearReadStatus(messageBean.getRoomId());
            }
        }

        @Override
        public void receiveSessionList(List<MessageBean> messageBeans, String atMessage) {

        }
    });

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(messageReceiver);
//            getActivity().unregisterReceiver(systemReceiver);
            getActivity().unregisterReceiver(contactListReceiver);
            getActivity().unregisterReceiver(xmppLoginReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tempDatas.clear();
        tempDatas = null;
    }

    @SuppressLint("InlinedApi")
    @Override
    protected void initViews() {

        initTitleBar();

        mSwipeListView = (ListView) findViewById(R.id.recent_listview);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        headerView = LayoutInflater.from(context).inflate(
                R.layout.im_header_recent_list, null);
        refreshTime = (TextView) headerView.findViewById(R.id.onrefresh_time);
        // 设置刷新时动画的颜色，可以设置4个
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        swipeRefreshLayout.setOnRefreshListener(this);

        //初始化搜索框
        initSearchLayout();
        //判断是否需要显示提示语句
        //首次安装，首次使用IM模块时，提示下拉刷新可以查看更多聊天记录，否则不提示。
        showTips();
    }

    private void initSearchLayout() {
        searchKey = (EditText) findViewById(R.id.search_key);
        searchClear = (ImageView) findViewById(R.id.search_clear);
        searchClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                searchKey.setText("");
                searchClear.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setEnabled(true);
                inputmanger.hideSoftInputFromWindow(searchKey.getWindowToken(), 0);
                sortFreshData("");
            }
        });

        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        searchKey.setOnKeyListener(new OnKeyListener() {// 输入完后按键盘上的搜索键

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {// 修改回车键功能
                    String content = searchKey.getText().toString();
                    if (content != null
                            && !"".equalsIgnoreCase(content)) {
                        searchClear.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setEnabled(false);
                    } else {
                        searchClear.setVisibility(View.INVISIBLE);
                        swipeRefreshLayout.setEnabled(true);
                        inputmanger.hideSoftInputFromWindow(
                                searchKey.getWindowToken(), 0);
                    }
                    sortFreshData(content);
                    inputMethodManager.hideSoftInputFromWindow(
                            searchKey.getWindowToken(), 0);
                }
                return false;
            }
        });
    }

    private void showTips() {
        tipsText = (TextView) findViewById(R.id.tips_text);
        boolean isShowTips = MFSPHelper.getBoolean("isShowTips", true);

        if (!isShowTips) {
            try {
                int nowVersion = getActivity().getPackageManager()
                        .getPackageInfo(getActivity().getPackageName(),
                                PackageManager.GET_META_DATA).versionCode;
                int originalVersion = MFSPHelper
                        .getInteger(CommConstants.ORIGINAL_VERSION);
                if (nowVersion > originalVersion) {
                    isShowTips = true;
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (isShowTips) {
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    tipsText.setVisibility(View.GONE);
                    MFSPHelper.setBoolean("isShowTips", false);
                }
            }, 5 * 1000);
        } else {
            tipsText.setVisibility(View.GONE);
        }
    }

    private PopupWindow popupWindow;

    protected void initTitleBar() {

        title = (TextView) findViewById(R.id.tv_common_top_title);
        title.setText(getActivity().getResources().getString(R.string.top_chat));
        ImageView back = (ImageView) findViewById(R.id.common_top_img_left);
        back.setVisibility(View.GONE);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        ImageView groupAdd = (ImageView) findViewById(R.id.common_top_right_refresh);
//        groupAdd.setBackgroundResource(R.drawable.group_add);
        groupAdd.setVisibility(View.VISIBLE);
        groupAdd.setImageResource(R.drawable.group_add);

        View popupView = LayoutInflater.from(getActivity()).inflate(
                R.layout.popup_for_group_chat, null);
        popupWindow = new CurPopup(popupView, this, getActivity());

        groupAdd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//                if (popupWindow.isShowing()) {
//                    popupWindow.dismiss();
//                } else {
//                    popupWindow.showAsDropDown(v);
//                }
                Intent intent = new Intent();
                intent.putExtra("ACTION", "GROUP").putExtra("TITLE", getString(R.string.group_chat)).putExtra(CommConstants.KEY_GROUP_TYPE, CommConstants.CHAT_TYPE_GROUP_PERSON);
                ((BaseApplication) ChatRecordsFragment.this.getActivity().getApplication()).getUIController().onIMOrgClickListener(ChatRecordsFragment.this.getActivity(), intent, 0);
            }
        });

        if (!"default".equals(MFSPHelper.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }
    }

    private BroadcastReceiver systemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action;
            try {
                action = intent.getAction();
                if (CommConstants.ACTION_MY_KICKED.equals(action)) {
                    LogUtils.v("systemReceiver", "kicked");
                    String roomName = intent.getStringExtra("roomName");
                    String displayName = intent.getStringExtra("displayName");
                    for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                        if (roomName.equalsIgnoreCase(IMConstants.contactListDatas.get(i).getRoomId())
                                && IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                            IMConstants.contactListDatas.remove(i);
                            break;
                        }
                    }

                    // 再添加系统消息
                    MessageBean messageBean = new MessageBean();
                    messageBean.setRoomId(roomName);

                    Date d = new Date();
                    messageBean.setTimestamp(DateUtils.date2Str(d,
                            DateUtils.FORMAT_FULL));
                    messageBean.setFormateTime(DateUtils.date2Str(d, DateUtils.FORMAT_FULL));
                    messageBean.setCtype(CommConstants.CHAT_TYPE_SYSTEM);
                    messageBean.setContent(getString(R.string.admin_removed_you));
                    messageBean.setMtype(CommConstants.MSG_TYPE_KICK);
                    messageBean.setUnReadCount(1);
                    messageBean.setSubject(displayName);
                    if (!IMConstants.sysMsgList.isEmpty()) {
                        for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                            if (IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                                int count = IMConstants.contactListDatas.get(i).getUnReadCount();
                                messageBean.setUnReadCount(count + 1);
                                IMConstants.contactListDatas.remove(i);
                                break;
                            }
                        }
                    }
                    IMConstants.contactListDatas.add(0, messageBean);
                    IMConstants.sysMsgList.add(0, messageBean);
                } else if (CommConstants.ACTION_MY_INVITE.equals(action)) {
                    LogUtils.v("systemReceiver", "invite");
                    String roomName = intent.getStringExtra("roomName");
                    String inviter = intent.getStringExtra("inviter");
                    String invitee = intent.getStringExtra("invitee");
                    final Group group = (Group) intent
                            .getSerializableExtra("group");

                    MessageBean messageBean = new MessageBean();
                    messageBean.setRoomId(roomName);
                    String cinviter = getCName(inviter, messageBean);
                    String cinvitee = getCName(invitee, messageBean);

                    Date d = new Date();
                    messageBean.setTimestamp(DateUtils.date2Str(d,
                            DateUtils.FORMAT_FULL));
                    messageBean.setFormateTime(DateUtils.date2Str(d, DateUtils.FORMAT_FULL));
                    messageBean.setCtype(CommConstants.CHAT_TYPE_SYSTEM);
                    messageBean.setContent(cinviter + getString(R.string.invite_join_group));
                    messageBean.setCuserId(cinvitee);
                    messageBean.setFriendId(cinviter);
                    messageBean.setMtype(CommConstants.MSG_TYPE_INVITE);
                    messageBean.setUnReadCount(1);
                    messageBean.setSubject(group.getDisplayName());

                    if (!IMConstants.sysMsgList.isEmpty()) {
                        for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                            if (IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                                int count = IMConstants.contactListDatas.get(i).getUnReadCount();
                                messageBean.setUnReadCount(count + 1);
                                IMConstants.contactListDatas.remove(i);
                                break;
                            }
                        }
                    }
                    IMConstants.contactListDatas.add(0, messageBean);
                    IMConstants.sysMsgList.add(0, messageBean);

                } else if (CommConstants.ACTION_GROUP_MEMBERS_CHANGES.equals(action)) {
                    // 群成员改变
                    LogUtils.v("systemReceiver", "membersChange");
                    String type = intent.getStringExtra("type");
                    String roomName = intent.getStringExtra("groupName");
                    String displayName = intent.getStringExtra("displayName");
                    String affecteds = intent.getStringExtra("affecteds");
                    if (null == type) {
                        //APP登录时，getGroupList是异步获取的，获取成功后会发送广播ACTION_GROUP_MEMBERS_CHANGES
                        //这个时候Intent是无对象传递的
                        //收到广播后，刷新列表页面，保证列表页面的groupName是有值的
                        if(null!=recentAdapter){
                            recentAdapter.notifyDataSetChanged();
                        }
                        return;
                    }
                    String adname = MFSPHelper.getString(CommConstants.EMPADNAME);
                    String userId = MFSPHelper.getString(CommConstants.USERID);
                    if (type.equals("0")) {
                        // 自己加入群组的时候
                        if (affecteds.equalsIgnoreCase(adname)) {
                            return;
                        }
                    } else if (type.equals("1")) {
                        // 有人被踢了
                        // 管理员踢人的时候，不需要收到消息
                        Group group = IMConstants.groupsMap.get(roomName);
                        if (userId.equals(group.getCreaterId())) {
                            return;
                        }
                    } else if (type.equals("4")) {
                        // 自己退出
                        if (affecteds.equalsIgnoreCase(adname)) {
                            // 先删除原来的群组消息
                            for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                                if (roomName.equalsIgnoreCase(IMConstants.contactListDatas.get(i).getRoomId())
                                        && IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                                    IMConstants.contactListDatas.remove(i);
                                    break;
                                }
                            }
                            setPoint();
                            sortFreshData(searchKey.getText().toString());
                            return;
                        }
                    }
                    boolean flag = affecteds.contains(adname);

                    MessageBean messageBean = new MessageBean();
                    Date d = new Date();
                    messageBean.setTimestamp(DateUtils.date2Str(d,
                            DateUtils.FORMAT_FULL));
                    messageBean.setFormateTime(DateUtils.date2Str(d, DateUtils.FORMAT_FULL));
                    messageBean.setRoomId(roomName);
                    messageBean.setCtype(CommConstants.CHAT_TYPE_SYSTEM);

                    String names = "";
                    String[] members = affecteds.split(",");
                    int num = members.length;
                    if (flag) {
                        if (adname.equals(members[0])) {
                            names = getCName(members[1], messageBean);
                        } else {
                            names = getCName(members[0], messageBean);
                        }
                        num = members.length - 1;
                        if (members.length > 2) {
                            names = names + getString(R.string.etc) + num + getString(R.string.unit_person);
                        }
                    } else {
                        names = getCName(members[0], messageBean);
                        if (members.length > 1) {
                            names = names + getString(R.string.etc) + num + getString(R.string.unit_person);
                        }
                    }

                    /**
                     * 0表示新增成员通知; 1表示踢出成员通知; 2表示变更displayName通知; 3解散群通知;4用户退群通知
                     */
                    if (type.equals("0")) {
                        messageBean.setContent(names + getString(R.string.join) + displayName);
                        messageBean.setRsflag(0);// 使用rsflag属性做判断
                    } else if (type.equals("1")) {
                        messageBean.setContent(names + getString(R.string.beremoved_from_group));
                        messageBean.setRsflag(1);
                    } else if (type.equals("4")) {
                        messageBean.setContent(names + getString(R.string.exited) + displayName);
                        messageBean.setRsflag(4);
                    }
                    messageBean.setMtype(CommConstants.MSG_TYPE_MEMBERS_CHANGE);
                    messageBean.setUnReadCount(1);
                    messageBean.setSubject(displayName);
                    messageBean.setFriendId(names);

                    if (!IMConstants.sysMsgList.isEmpty()) {
                        for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                            if (IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                                int count = IMConstants.contactListDatas.get(i).getUnReadCount();
                                messageBean.setUnReadCount(count + 1);
                                IMConstants.contactListDatas.remove(i);
                                break;
                            }
                        }
                    }
                    IMConstants.contactListDatas.add(0, messageBean);
                    IMConstants.sysMsgList.add(0, messageBean);

                } else if (CommConstants.ACTION_GROUP_DISPALYNAME_CHANGES
                        .equals(action)) {
                    String roomName = intent.getStringExtra("roomName");
                    String displayName = intent.getStringExtra("displayName");
                    for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                        if (roomName.equalsIgnoreCase(IMConstants.contactListDatas.get(i).getRoomId())
                                && IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                            IMConstants.contactListDatas.get(i).setSubject(displayName);
                        }
                    }

                } else if (CommConstants.ACTION_GROUP_DISSOLVE_CHANGES
                        .equals(action)) {
                    // 群解散
                    LogUtils.v("systemReceiver", "dissolve");
                    String roomName = intent.getStringExtra("roomName");
                    String displayName = intent.getStringExtra("displayName");
                    // // 先删除原来的群组消息
                    for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                        if (roomName.equalsIgnoreCase(IMConstants.contactListDatas.get(i).getRoomId())
                                && IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                            IMConstants.contactListDatas.remove(i);
                            break;
                        }
                    }

                    IMConstants.groupsMap.remove(roomName);

                    // 再添加系统消息
                    MessageBean messageBean = new MessageBean();
                    Date d = new Date();
                    messageBean.setTimestamp(DateUtils.date2Str(d,
                            DateUtils.FORMAT_FULL));
                    messageBean.setFormateTime(DateUtils.date2Str(d, DateUtils.FORMAT_FULL));
                    messageBean.setRoomId(roomName);
                    messageBean.setCtype(CommConstants.CHAT_TYPE_SYSTEM);
                    messageBean.setContent(getString(R.string.admin_dissovled) + displayName);
                    messageBean.setMtype(CommConstants.MSG_TYPE_DISSOLVE);
                    messageBean.setUnReadCount(1);
                    messageBean.setSubject(displayName);

                    if (!IMConstants.sysMsgList.isEmpty()) {
                        for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                            if (IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                                int count = IMConstants.contactListDatas.get(i).getUnReadCount();
                                messageBean.setUnReadCount(count + 1);
                                IMConstants.contactListDatas.remove(i);
                                break;
                            }
                        }
                    }
                    IMConstants.contactListDatas.add(0, messageBean);
                    IMConstants.sysMsgList.add(0, messageBean);
                }
                handler.sendEmptyMessage(5);
                IMConstants.Dingdong(context);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    };

    public String getCName(String adname, MessageBean messageBean) {

        if (StringUtils.notEmpty(adname)) {

            UserDao dao = UserDao.getInstance(context);
            UserInfo userInfo = dao.getUserInfoByADName(adname);

            String cname = "";

            switch (IMConstants.groupsMap.get(messageBean.getRoomId()).getType()) {
                case CommConstants.CHAT_TYPE_GROUP_PERSON:
                    cname = userInfo.getEmpCname().split("\\.")[0];
                    break;
                case CommConstants.CHAT_TYPE_GROUP_ANS:
                    cname = IMConstants.ansGroupMembers.get(messageBean.getRoomId() + "," + userInfo.getId());
                    break;
                default:
                    break;
            }

            return cname;
        }
        return "";
    }

    @Override
    protected void initDatas() {
    }

    protected void sortFreshData(String content) {

        tempDatas.clear();
        UserInfo userInfo;
        content = content.trim().toUpperCase();
        if (IMConstants.contactListDatas != null) {
            if (StringUtils.notEmpty(content)) {
                for (MessageBean messageBean : IMConstants.contactListDatas) {
                    if (messageBean.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                        userInfo = messageBean.getUserInfo();
                        String query = new BuildQueryString()
                                .buildQueryName(userInfo);
                        if (userInfo != null && query.contains(content)) {
                            tempDatas.add(messageBean);
                        }
                    } else if (messageBean.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                        String subjectName = (messageBean.getSubject() + BuildQueryString
                                .buildQueryName(messageBean.getSubject()))
                                .toUpperCase();
                        if (messageBean.getSubject() != null
                                && subjectName.contains(content)) {
                            tempDatas.add(messageBean);
                        }
                    }
                }
            } else {
                tempDatas.addAll(IMConstants.contactListDatas);
            }
            for (MessageBean mb : tempDatas) {
                if (mb.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                    setReadStatus(mb, mb.getFriendId());
                } else if (mb.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                    setReadStatus(mb, mb.getRoomId());
                } else {
                    mb.setMarkReadStatus(-1);
                }
            }
        }
        if (recentAdapter == null) {
            setAdapter();
        } else {
            List<Integer> openItems = recentAdapter.getOpenItems();
            if (openItems.get(0) != -1) {
                recentAdapter.closeItem(openItems.get(0));
            }
            recentAdapter.notifyDataSetChanged();
        }
    }

    private void setReadStatus(MessageBean mb, String id) {
        if (markUnReadIds.contains(id)) {
            mb.setMarkReadStatus(1);
        } else if (markReadIds.contains(id)) {
            mb.setMarkReadStatus(0);
        } else {
            mb.setMarkReadStatus(-1);
        }
    }

    public void setAdapter() {
        recentAdapter = new ChatRecordsAdapter(getActivity(), tempDatas, handler);
        mSwipeListView.setAdapter(recentAdapter);
        recentAdapter.setMode(SwipeItemMangerImpl.Mode.Single);
        mSwipeListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (swipeRefreshLayout.isRefreshing()) {
                    position = position - 1;
                }
                List<Integer> openItems = recentAdapter.getOpenItems();
                if (openItems.get(0) != -1) {
                    recentAdapter.closeItem(openItems.get(0));
                    return;
                }

                if (position < 0)
                    return;

                MessageBean bean = tempDatas.get(position);
                if (bean.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                    //进入聊天页面
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userInfo", bean.getUserInfo());
                    if (bean.getUnReadCount() == 0 && bean.getMarkReadStatus() != 0) {
                        bundle.putBoolean("hasNewMes", false);
                    } else {
                        bundle.putBoolean("hasNewMes", true);
                    }
                    clearReadStatus(bean.getFriendId());
                    ((BaseApplication) ChatRecordsFragment.this.getActivity().getApplication()).getUIController().startPrivateChat(ChatRecordsFragment.this.getActivity(), bundle);

                } else if (bean.getCtype() == CommConstants.CHAT_TYPE_GROUP
                        || CommConstants.GROUP_ADMIN.equalsIgnoreCase(bean.getFriendId())) {

                    Intent intent = new Intent(getActivity(),
                            GroupChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("room", bean.getRoomId());
                    bundle.putString("subject", bean.getSubject());
                    if (bean.getUnReadCount() == 0 && bean.getMarkReadStatus() != 1) {
                        bundle.putBoolean("hasNewMes", false);
                    } else {
                        bundle.putBoolean("hasNewMes", true);
                    }
                    bundle.putInt(CommConstants.KEY_GROUP_TYPE, StringUtils.notEmpty(bean.getGroupType())?Integer.valueOf(bean.getGroupType()):-1);
                    intent.putExtras(bundle);
                    clearReadStatus(bean.getRoomId());
                    startActivity(intent);
                } else if (bean.getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                    startActivityForResult(new Intent(getActivity(),
                            SystemMsgActivity.class), 99);
                }
            }
        });
    }

    public void clearReadStatus(String id) {
        markReadIds.remove(id);
        markUnReadIds.remove(id);
        MFSPHelper.setStringSet(CommConstants.MARK_UNREAD_IDS, markUnReadIds);
        MFSPHelper.setStringSet(CommConstants.MARK_READ_IDS, markReadIds);

        sortFreshData(searchKey.getText().toString());
    }

    public void sortMsgs() {
        Collections.sort(IMConstants.contactListDatas, new MsgListComparator());
    }

    public void setPoint() {
        Intent intent = new Intent(CommConstants.ACTION_SET_REDPOINT);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 99) {
            for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                if (IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                    IMConstants.contactListDatas.remove(i);
                    break;
                }
            }
        }
        handler.sendEmptyMessage(5);
    }

    @Override
    protected void resumeDatas() {
        IMConstants.sysCallback = this;
        markUnReadIds = MFSPHelper.getStringSet(CommConstants.MARK_UNREAD_IDS);
        markReadIds = MFSPHelper.getStringSet(CommConstants.MARK_READ_IDS);
        setPoint();
        if (searchKey != null && searchKey.getText() != null) {
            sortFreshData(searchKey.getText().toString());
        }
    }

    private BroadcastReceiver contactListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context cont, final Intent intent) {
            if (CommConstants.ACTION_CONTACT_LIST.equals(intent.getAction())) {

                // 下拉刷新获取全部数据
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                    mSwipeListView.removeHeaderView(headerView);
                } else {
                    //客户端新增标记为已读、标记为未读功能，该功能为纯客户端行为
                    //服务器端API getContactList返回的是全部的sessionList
                    //故此时需要过滤掉被客户端标记为未读的sessionList，这部分不需要显示，否则客户端行为将出错
//                    filterContactList();
                    sessionManager.deleteAllSession();
                    for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                        sessionManager.insertSession(IMConstants.contactListDatas.get(i));
                    }
                }

                if (!IMConstants.sysMsgList.isEmpty()) {
                    IMConstants.contactListDatas.add(0, IMConstants.sysMsgList.get(0));
                }
                sortMsgs();
                setPoint();
                recentAdapter = null;
                sortFreshData(searchKey.getText().toString());
                CommConstants.isCome = true;
            }
        }
    };

    //客户端新增标记为已读、标记为未读功能，该功能为纯客户端行为
    //服务器端API getContactList返回的是全部的sessionList
    //故此时需要过滤掉被客户端标记为未读的sessionList，这部分不需要显示，否则客户端行为将出错
    private void filterContactList(){
        try {
            ArrayList<String> sessionListDatas = sessionManager.getSessionList();
            if(null!=sessionListDatas && sessionListDatas.size()>0){
                List<MessageBean> tempList = new ArrayList<>();
                for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                    MessageBean bean = IMConstants.contactListDatas.get(i);
                    if (bean.getUnReadCount() == 0) {
                        if (bean.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                            if (sessionListDatas.contains(bean.getFriendId()
                                    .toLowerCase())) {
                                tempList.add(bean);
                            }
                        } else if (bean.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                            if (sessionListDatas.contains(bean.getRoomId().toLowerCase())) {
                                tempList.add(bean);
                            }
                        }
                    } else {
                        tempList.add(bean);
                    }
                }

                if(tempList.size()>0){
                    sessionManager.deleteAllSession();
                    for (int i = 0; i < tempList.size(); i++) {
                        sessionManager.insertSession(tempList.get(i));
                    }
                    IMConstants.contactListDatas.clear();
                    IMConstants.contactListDatas.addAll(tempList);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sessionManager.closeDb();
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        mSwipeListView.setAdapter(null);
        mSwipeListView.addHeaderView(headerView);
        mSwipeListView.setAdapter(recentAdapter);
        refreshTime
                .setText(getString(R.string.refresh_time) + DateUtils.date2Str(new Date(), "HH:mm:ss"));
        IMManager.getContactList(context, new IMManager.CallBack() {
            @Override
            public void refreshUI(List<MessageBean> contactList) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                    mSwipeListView.removeHeaderView(headerView);
                }else {
                    //客户端新增标记为已读、标记为未读功能，该功能为纯客户端行为
                    //服务器端API getContactList返回的是全部的sessionList
                    //故此时需要过滤掉被客户端标记为未读的sessionList，这部分不需要显示，否则客户端行为将出错
//                    filterContactList();
                    sessionManager.deleteAllSession();
                    for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                        sessionManager.insertSession(IMConstants.contactListDatas.get(i));
                    }
                }

                if (!IMConstants.sysMsgList.isEmpty()) {
                    IMConstants.contactListDatas.add(0, IMConstants.sysMsgList.get(0));
                }
                sortMsgs();
                setPoint();
                recentAdapter = null;

//                if(null != contactList){
//                    IMConstants.contactListDatas = contactList;
//                }
//                else {
//                    IMConstants.contactListDatas.clear();
//                }

                //通知更新聊天未读数
                Intent intent = new Intent(CommConstants.ACTION_SET_REDPOINT);
                intent.setPackage(context.getPackageName());
                context.sendBroadcast(intent);

                sortFreshData(searchKey.getText().toString());
                CommConstants.isCome = true;

            }
        });
    }

    @Override
    public void onWindowItemClickListener(final int viewId) {
        Intent intent = new Intent();
        if (viewId == R.id.pop_linearlayout_1) {
            intent.putExtra("ACTION", "GROUP").putExtra("TITLE", getString(R.string.group_chat)).putExtra(CommConstants.KEY_GROUP_TYPE, CommConstants.CHAT_TYPE_GROUP_PERSON);
        } else if (viewId == R.id.pop_linearlayout_2) {
            intent.putExtra("ACTION", "GROUP").putExtra("TITLE", getString(R.string.nick_group_chat)).putExtra(CommConstants.KEY_GROUP_TYPE, CommConstants.CHAT_TYPE_GROUP_ANS);
        } else if (viewId == R.id.pop_linearlayout_3) {
            intent.putExtra("ACTION", "MEETING").putExtra("TITLE", getString(R.string.video_meeting)).putExtra(CommConstants.KEY_GROUP_TYPE, CommConstants.CHAT_TYPE_GROUP_PERSON);
        }
        ((BaseApplication) ChatRecordsFragment.this.getActivity().getApplication()).getUIController().onIMOrgClickListener(ChatRecordsFragment.this.getActivity(), intent, 0);
    }

    @Override
    public void afterKicked(String roomName, String displayName) {
        if (recentAdapter != null) {
            setPoint();
            sortFreshData(searchKey.getText().toString());
        }
    }

    @Override
    public void afterInvited(String roomName, String inviter, String invitee, Group group) {
//        if (recentAdapter != null) {
//            setPoint();
//            sortFreshData(searchKey.getText().toString());
//        }
        onRefresh();
    }

    @Override
    public void afterMemberChanged(String roomName, String affecteds, String displayName, String type) {
        if (recentAdapter != null) {
            setPoint();
            sortFreshData(searchKey.getText().toString());
        }
    }

    @Override
    public void afterGroupNameChanged(String roomName, String displayName) {
        if (recentAdapter != null) {
            setPoint();
            sortFreshData(searchKey.getText().toString());
        }
    }

    @Override
    public void afterGroupDisolved(String roomName, String displayName) {
        if (recentAdapter != null) {
            setPoint();
            sortFreshData(searchKey.getText().toString());
        }
    }
}
