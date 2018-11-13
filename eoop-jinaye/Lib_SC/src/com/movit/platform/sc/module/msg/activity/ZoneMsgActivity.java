/*
 * Copyright (C) 2015 Bright Yu Haiyang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author: y.haiyang@qq.com
 */

package com.movit.platform.sc.module.msg.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.movit.platform.common.api.IZoneManager;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.xlistview.XListView;
import com.movit.platform.framework.view.xlistview.XListView.IXListViewListener;
import com.movit.platform.sc.R;
import com.movit.platform.sc.entities.ZoneMessage;
import com.movit.platform.sc.module.msg.adapter.ZoneMsgAdapter;
import com.movit.platform.sc.module.zone.constant.ZoneConstants;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class ZoneMsgActivity extends Activity implements
        IXListViewListener {
    private TextView title;
    private ImageView topLeft;
    private TextView topRight;
    private TextView msgTextView;

    private XListView listView;
    private ZoneMsgAdapter adapter;
    protected ArrayList<ZoneMessage> mDatas = new ArrayList<ZoneMessage>();

    protected IZoneManager zoneManager;

    private int curPostion;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DialogUtils.getInstants().dismiss();
            switch (msg.what) {
                case ZoneConstants.ZONE_LIST_RESULT:
                    DialogUtils.getInstants().dismiss();
                    mDatas.clear();
                    ArrayList<ZoneMessage> arrayList = (ArrayList<ZoneMessage>) msg.obj;
                    mDatas.addAll(0, arrayList);
                    listView.stopRefresh();
                    listView.setRefreshTime(DateUtils.date2Str(new Date()));
                    if (mDatas.isEmpty()) {
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        setArapter();
                        int count = 0;
                        for (int i = 0; i < mDatas.size(); i++) {
                            if (mDatas.get(i).getiHasRead() == 0) {
                                count++;
                            }
                        }
                        msgTextView.setText("共有" + mDatas.size() + "条消息，" + count
                                + "条未读");
                    }
                    break;
                case ZoneConstants.ZONE_MSG_DEL_RESULT:
                    try {
                        String result = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(result);
                        int code = jsonObject.getInt("code");
                        if (code == 0) {
                            mDatas.clear();
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                            msgTextView.setText("共有0条消息，0条未读");
                        } else {
                            ToastUtils.showToast(ZoneMsgActivity.this, "清空失败！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.showToast(ZoneMsgActivity.this, "清空失败！");
                    }
                    break;
                case ZoneConstants.ZONE_ERROR_RESULT:
                    DialogUtils.getInstants().dismiss();
                    String errorMsg = (String) msg.obj;
                    ToastUtils.showToast(ZoneMsgActivity.this, errorMsg);
                    listView.stopRefresh();
                    listView.stopLoadMore();
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    } else {
                        setArapter();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sc_activity_zone_msg);

//        zoneManager = new ZoneManager(this);
        zoneManager = ((BaseApplication) this.getApplication()).getManagerFactory().getZoneManager();

        iniView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        iniData();
    }

    private void iniView() {
        listView = (XListView) findViewById(R.id.zone_msg_listview);
        title = (TextView) findViewById(R.id.tv_common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_img_left);
        topRight = (TextView) findViewById(R.id.common_top_img_right);
        msgTextView = (TextView) findViewById(R.id.zone_msg_txt);
        title.setText("动态消息");
        topRight.setText("清空");
        topRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                zoneManager.messagedel(handler);
            }
        });
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        SharedPreUtils spUtil = new SharedPreUtils(this);
        if (!"default".equals(spUtil.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }
    }

    private void iniData() {
        DialogUtils.getInstants().showLoadingDialog(this, "正在加载...", false);
        zoneManager.messages(handler);
    }

    private void setArapter() {
        adapter = new ZoneMsgAdapter(this, mDatas);
        listView.setAdapter(adapter);
        listView.setPullLoadEnable(false);
        listView.setPullRefreshEnable(false);
        listView.setXListViewListener(this);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                curPostion = position - 1;

                ArrayList<String> idStrings = CommConstants.loginConfig.getmUserInfo()
                        .getAttentionPO();

                SharedPreUtils spUtil = new SharedPreUtils(ZoneMsgActivity.this);
                String curUserId = spUtil.getString(CommConstants.USERID);

                UserDao dao = UserDao.getInstance(ZoneMsgActivity.this);
                String empCName = dao.getUserInfoById(mDatas.get(curPostion).getcUserId()).getEmpCname();
                dao.closeDb();
                try {
                    boolean gozone = getPackageManager().getApplicationInfo(
                            getPackageName(), PackageManager.GET_META_DATA).metaData
                            .getBoolean("CHANNEL_ATTENTION_SEEZONE", false);
                    if (gozone) {
                        if (mDatas.get(curPostion).getcUserId().equalsIgnoreCase(curUserId)
                                || idStrings.contains(mDatas.get(curPostion).getcUserId())) {
                            Intent intent = new Intent();
                            intent.putExtra("sayId", mDatas.get(curPostion).getcSayId());
                            ((BaseApplication) ZoneMsgActivity.this.getApplication()).getUIController().onZoneMsgDetailClickListener(ZoneMsgActivity.this, intent, 1);
                        } else {
                            ToastUtils.showToast(ZoneMsgActivity.this, "关注" + empCName + "后才能查看说说详情");
                        }
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra("sayId", mDatas.get(curPostion).getcSayId());
                        ((BaseApplication) ZoneMsgActivity.this.getApplication()).getUIController().onZoneMsgDetailClickListener(ZoneMsgActivity.this, intent, 1);
                    }
                } catch (PackageManager.NameNotFoundException e1) {
                    e1.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onRefresh() {
        listView.stopRefresh();
    }

    @Override
    public void onLoadMore() {
        listView.stopLoadMore();
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        switch (arg1) {
            case 1:
                try {
                    mDatas.get(curPostion).setiHasRead(1);
                    int count = 0;
                    for (int i = 0; i < mDatas.size(); i++) {
                        if (mDatas.get(i).getiHasRead() == 0) {
                            count++;
                        }
                    }
                    msgTextView.setText("共有" + mDatas.size() + "条消息，" + count
                            + "条未读");
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        super.onActivityResult(arg0, arg1, arg2);
    }

}
