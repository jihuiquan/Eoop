package com.movit.platform.common.module.organization.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.movit.platform.common.R;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.organization.fragment.OrgFragment;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.ActivityUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrgActivity extends FragmentActivity {

    public static final int ORG_CLICK_AVATAR = 100001;
    public static final String ORG_CLICK_AVATAR_FLAG = "org_click_avatar_flag";
    protected TextView mTopTitle, topRight;
    private ImageView mTopLeftImage, mTopRefresh;
    private String isFromOrg, titleStr, actionStr;

    protected int ctype = -1;

    public static List<UserInfo> originalUserInfos;
    public static Map<String, UserInfo> orgCheckedMap = new HashMap<String, UserInfo>();
    public static Map<String, OrganizationTree> orgCheckedCatMap = new HashMap<String, OrganizationTree>();
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comm_activity_organization);

        intent = getReceivedIntent();

        initView();
        initData();

        FragmentManager fragmentManager = this.getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.common_fragment, new OrgFragment(),
                "UserFragment");
        transaction.commitAllowingStateLoss();
    }

    @SuppressWarnings("unchecked")
    private void initData() {
        orgCheckedMap.clear();
        orgCheckedCatMap.clear();
        if (originalUserInfos != null) {
            originalUserInfos.clear();
        }

        originalUserInfos = (List<UserInfo>) intent
                .getSerializableExtra("userInfos");

        CommonHelper tools = new CommonHelper(this);

        if ("@".equals(actionStr) || "WebView".equals(actionStr)) {
            List<UserInfo> atUserInfos = (List<UserInfo>) intent
                    .getSerializableExtra("atUserInfos");
            List<OrganizationTree> atOrgunitionLists = (List<OrganizationTree>) intent
                    .getSerializableExtra("atOrgunitionLists");
            if (atUserInfos != null) {
                for (int i = 0; i < atUserInfos.size(); i++) {
                    orgCheckedMap.put(atUserInfos.get(i).getEmpAdname(),
                            atUserInfos.get(i));
                }
            }
            if (atOrgunitionLists != null) {
                for (int i = 0; i < atOrgunitionLists.size(); i++) {
                    orgCheckedCatMap.put(atOrgunitionLists.get(i).getObjname(),
                            atOrgunitionLists.get(i));
                }
            }
            if ("WebView".equals(actionStr)) {
                UserInfo userInfo = tools.getLoginConfig().getmUserInfo();
                if (originalUserInfos == null) {
                    originalUserInfos = new ArrayList<UserInfo>();
                }
                if (!originalUserInfos.contains(userInfo)) {
                    originalUserInfos.add(userInfo);
                }
            }
        } else {
            UserInfo userInfo = tools.getLoginConfig().getmUserInfo();
            if (originalUserInfos == null) {
                originalUserInfos = new ArrayList<UserInfo>();
            }
            if (!originalUserInfos.contains(userInfo)) {
                originalUserInfos.add(userInfo);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (originalUserInfos != null) {
            originalUserInfos.clear();
        }
        if (orgCheckedMap != null) {
            orgCheckedMap.clear();
        }
    }

    private void initView() {
        mTopTitle = (TextView) findViewById(R.id.tv_common_top_title);
        mTopLeftImage = (ImageView) findViewById(R.id.common_top_img_left);
        topRight = (TextView) findViewById(R.id.common_top_img_right);
        mTopRefresh = (ImageView) findViewById(R.id.common_top_right_refresh);

        SharedPreUtils spUtil = new SharedPreUtils(this);
        if (!"default".equals(spUtil.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }

        topRight.setText("确定");
        if ("GROUP".equals(actionStr) || "EMAIL".equals(actionStr)
                || "@".equals(actionStr) || "WebView".equals(actionStr)) {
            mTopTitle.setText(titleStr);
        } else if ("Y".equalsIgnoreCase(isFromOrg)) {
            mTopTitle.setText("组织架构");
        } else {
            mTopTitle.setText("发起群聊");
        }

        mTopLeftImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if ("Y".equalsIgnoreCase(isFromOrg)) {
            topRight.setVisibility(View.GONE);
            mTopRefresh.setVisibility(View.VISIBLE);
        } else {
            topRight.setVisibility(View.VISIBLE);
        }
        mTopRefresh.setVisibility(View.GONE);
        mTopRefresh.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogUtils.getInstants().showLoadingDialog(
                        OrgActivity.this, "正在刷新...", false);
            }
        });
        topRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                topRight.setClickable(false);
                if ("EMAIL".equals(actionStr)) {
                    //
                    emailEventListener();
                } else if ("@".equals(actionStr) || "WebView".equals(actionStr)) {
                    ATEventListener();
                } else {

                    Log.d("OrgActivity", "ctype=" + ctype);

                    if (ctype == CommConstants.CHAT_TYPE_GROUP) {
                        if (orgCheckedMap == null || orgCheckedMap.isEmpty()) {
                            topRight.setClickable(true);
                            return;
                        }
                        if (orgCheckedMap.size() > 15) {
                            ToastUtils.showToast(OrgActivity.this,
                                    "您每次只能选15人！");
                            topRight.setClickable(true);
                            return;
                        }

                        DialogUtils.getInstants().showLoadingDialog(
                                OrgActivity.this, "正在添加成员...", false);
                        topRight.setClickable(true);
                        // 添加群组成员
                        addGroupMembers();
                    } else {
                        //创建群组
                        createGroup();
                    }
                }
            }
        });
    }

    public void addGroupMembers() {
        String memberIdsString = "";
        Iterator<Map.Entry<String, UserInfo>> it = orgCheckedMap
                .entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, UserInfo> entry = it.next();
            UserInfo val = entry.getValue();
            if (val.getId() != null
                    && !"".equalsIgnoreCase(val.getId())) {
                memberIdsString += val.getId() + ",";
            }

        }
        String memberIds = memberIdsString.substring(0,
                memberIdsString.length() - 1);

        ((BaseApplication) this.getApplication()).getManagerFactory().getGroupManager().addMembers(getReceivedIntent().getStringExtra("groupId"), memberIds, initHandler());
    }

    public void createGroup() {
        SharedPreUtils spUtil = new SharedPreUtils(this);
        String cname = spUtil.getString(CommConstants.EMPCNAME);
        String memberIdsString = "";
        List<String> subjects = new ArrayList<String>();
        subjects.add(cname);
        if (ctype == CommConstants.CHAT_TYPE_SINGLE) {
            // 把原来聊天的成员也加
            for (int i = 0; i < originalUserInfos.size() - 1; i++) {
                if (StringUtils.notEmpty(originalUserInfos
                        .get(i).getId())) {
                    memberIdsString += originalUserInfos.get(i)
                            .getId() + ",";
                    subjects.add(originalUserInfos.get(i)
                            .getEmpCname());
                }
            }
        } else {
            if (orgCheckedMap == null
                    || orgCheckedMap.isEmpty()) {
                topRight.setClickable(true);
                return;
            }
        }

        if (orgCheckedMap.size() > 15) {
            ToastUtils.showToast(this, "您每次只能选15人！");
            topRight.setClickable(true);
            return;
        }

        DialogUtils.getInstants().showLoadingDialog(
                this, "正在创建房间...",
                false);
        topRight.setClickable(true);
        Iterator<Map.Entry<String, UserInfo>> it = orgCheckedMap
                .entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, UserInfo> entry = it.next();
            UserInfo val = entry.getValue();
            if (val.getId() != null
                    && !"".equalsIgnoreCase(val.getId())) {
                memberIdsString += val.getId() + ",";
                subjects.add(val.getEmpCname());
            }
        }

        String memberIds = memberIdsString.substring(0,
                memberIdsString.length() - 1);

        //TODO modify by anna
        String cnames = "";
        switch (getIntent().getIntExtra(CommConstants.KEY_GROUP_TYPE, 0)) {
            case CommConstants.CHAT_TYPE_GROUP_PERSON:
                //实名群聊群名称生成规则：XX、XX等X人
                cnames = getRealNameGroup(subjects);
                break;
            case CommConstants.CHAT_TYPE_GROUP_ANS:
                //匿名群聊群名称生成规则：XX的匿名群组（XX为创建者即群组）
                cnames = getAnsGroup(cname);
                break;
            default:
                break;
        }
        ((BaseApplication) this.getApplication()).getManagerFactory().getGroupManager().createGroup(
                memberIds, cnames, cname + "创建的群", getIntent().getIntExtra(CommConstants.KEY_GROUP_TYPE, 0), initHandler());
    }

    //匿名群聊群名称生成规则：XX的匿名群组（XX为创建者即群组）
    private String getAnsGroup(String cname) {
        return cname + "的匿名群组";
    }

    //实名群聊群名称生成规则：XX、XX等X人
    private String getRealNameGroup(List<String> subjects) {
        String cnames = "";
        for (int i = 0; i < subjects.size(); i++) {
            cnames += subjects.get(i).split("\\.")[0] + "、";
            if (i == 1) {
                break;
            }
        }
        cnames = cnames.subSequence(0, cnames.length() - 1)
                + "等" + subjects.size() + "人";
        return cnames;
    }

    protected Handler initHandler() {
        return new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                List<Map<String, Object>> listmMaps = new ArrayList<Map<String, Object>>();
                Map<String, Object> child = new HashMap<String, Object>();
                listmMaps.add(child);

                switch (msg.what) {

                    case 2:
                        DialogUtils.getInstants().dismiss();

                        String[] arr = (String[]) msg.obj;

                        Bundle bundle = new Bundle();
                        bundle.putString("room", arr[0]);
                        bundle.putString("subject", arr[1]);
                        bundle.putInt(CommConstants.KEY_GROUP_TYPE, getIntent().getIntExtra(CommConstants.KEY_GROUP_TYPE, 0));

                        ((BaseApplication) OrgActivity.this.getApplication()).getUIController().startMultChat(OrgActivity.this, bundle);
                        setResult(RESULT_OK);
                        finish();
                        orgCheckedMap.clear();
                        break;
                    case 3:
                        DialogUtils.getInstants().dismiss();
                        ToastUtils.showToast(OrgActivity.this,
                                "对不起，创建失败！请重新创建");
                        break;
                    case 4:
                        // 更新group 统一由IMChatService 管理
                        setResult(1);
                        DialogUtils.getInstants().dismiss();
                        finish();
                        break;
                    case 5:
                        DialogUtils.getInstants().dismiss();
                        ToastUtils.showToast(OrgActivity.this, "对不起，邀请失败");
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void emailEventListener() {
        try {
            if (orgCheckedMap == null || orgCheckedMap.isEmpty()) {
                topRight.setClickable(true);
                return;
            }
            List<String> emaiList = new ArrayList<String>();
            Set<String> keys = orgCheckedMap.keySet();
            UserInfo u;
            for (String s : keys) {
                u = orgCheckedMap.get(s);
                if (!StringUtils.empty(u.getMail())) {
                    emaiList.add(u.getMail().trim());
                }
            }
            String[] emails = emaiList.toArray(new String[emaiList
                    .size()]);
            ActivityUtils.sendMails(OrgActivity.this,
                    emails);
        } catch (Exception e) {
            e.printStackTrace();
        }
        topRight.setClickable(true);
    }

    private void ATEventListener() {
        if ("@".equals(actionStr)) {
            if (orgCheckedMap == null || orgCheckedMap.isEmpty()) {
                topRight.setClickable(true);
                return;
            }
        }
        // orgUserInfos intent 传递的已选择的人
        ArrayList<UserInfo> atUserInfos = new ArrayList<UserInfo>();
        if ("WebView".equals(actionStr)) {
            for (int i = 0; i < originalUserInfos.size(); i++) {
                atUserInfos.add(originalUserInfos.get(i));
            }
        }

        if ("WebView".equals(actionStr)) {
            if (orgCheckedMap.size() > 15) {
                ToastUtils.showToast(OrgActivity.this,
                        "您每次只能选15人！");
                topRight.setClickable(true);
                return;
            }
        }

        Set<String> keys = orgCheckedMap.keySet();
        for (String key : keys) {
            UserInfo userInfo = orgCheckedMap.get(key);
            if (!atUserInfos.contains(userInfo)) {
                atUserInfos.add(userInfo);
            }
        }
        ArrayList<OrganizationTree> atOrgunitionLists = new ArrayList<OrganizationTree>();
        Set<String> catKeys = orgCheckedCatMap.keySet();
        for (String cat : catKeys) {
            atOrgunitionLists.add(orgCheckedCatMap.get(cat));
        }
        topRight.setClickable(true);

        Intent intent = new Intent();
        intent.putExtra("atUserInfos", atUserInfos);
        intent.putExtra("atOrgunitionLists", atOrgunitionLists);
        setResult(1, intent);
        finish();
        return;
    }

    public Intent getReceivedIntent() {

        Intent intent = getIntent();
        isFromOrg = intent.getStringExtra("IS_FROM_ORG");
        titleStr = intent.getStringExtra("TITLE");
        actionStr = intent.getStringExtra("ACTION");
        ctype = intent.getIntExtra("ctype", -1);
        return intent;
    }

}
