package com.movit.platform.sc.module.zone.activity;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.sc.base.ZoneBaseActivity;
import com.movit.platform.sc.module.zone.adapter.ZoneAdapter;
import com.movit.platform.sc.entities.Zone;
import com.movit.platform.sc.module.zone.constant.ZoneConstants;

public class ZoneOwnActivity extends ZoneBaseActivity implements
        OnScrollListener {
    String userid;
    boolean isDelOK = false;
    ArrayList<Zone> currentTop = new ArrayList<Zone>();

    @Override
    protected void initTopView() {
        Intent intent = getIntent();
        userid = intent.getStringExtra("userId");
        if (spUtil.getString(CommConstants.USERID).equals(userid)) {
            title.setText("我的主页");
        } else {
            title.setText("个人主页");
        }
        topRight.setVisibility(View.GONE);
    }

    @Override
    protected void initDatas() {
        DialogUtils.getInstants().showLoadingDialog(ZoneOwnActivity.this, "正在加载...",
                false);
        zoneList.clear();
        zoneManager.getPersonalZoneList(userid,
                spUtil.getString("refreshTime"), "", "", "", "", "", handler);
    }

    @Override
    protected void setAdapter() {
        adapter = new ZoneAdapter(ZoneOwnActivity.this, zoneList,
                handler, ZoneAdapter.TYPE_OTHER, userid,
                DialogUtils.getInstants(),zoneManager);
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

    public void refreshData() {
        // if (adapter != null) {
        // progressDialogUtil.showLoadingDialog(ZoneOwnActivity.this,
        // "正在加载...", false);
        // handler.postDelayed(new Runnable() {
        //
        // @Override
        // public void run() {
        // if (zoneListView.ismPullLoading()
        // || zoneListView.ismPullRefreshing()) {
        // handler.postDelayed(this, 500);
        // } else {
        // zoneList.clear();
        // ZoneManager zoneManager = new ZoneManager(
        // ZoneOwnActivity.this);
        // zoneManager.getPersonalZoneList(userid,
        // spUtil.getString("refreshTime"), "", "", "",
        // "", "", handler);
        // }
        // }
        // }, 500);
        // }
    }

    @Override
    public void onRefresh() {
        if (zoneList.isEmpty()) {
            zoneManager.getPersonalZoneList(userid,
                    spUtil.getString("refreshTime"), "", "", "", "", "",
                    handler);

        } else {
            String tCreateTime = "";
            for (int i = 0; i < zoneList.size(); i++) {
                if (zoneList.get(i).getiTop() != null
                        && zoneList.get(i).getiTop().equals("0")) {
                    tCreateTime = zoneList.get(i).getdCreateTime();
                    break;
                }
            }
            zoneManager.getPersonalZoneList(userid,
                    spUtil.getString("refreshTime"), tCreateTime,
                    zoneList.get(zoneList.size() - 1).getdCreateTime(), "0",
                    "", "", handler);

        }
    }

    @Override
    public void onLoadMore() {
        String tCreateTime = "";
        for (int i = 0; i < zoneList.size(); i++) {
            if (zoneList.get(i).getiTop() != null
                    && zoneList.get(i).getiTop().equals("0")) {
                tCreateTime = zoneList.get(i).getdCreateTime();
                break;
            }
        }
        if (!zoneList.isEmpty()) {
            zoneManager.getPersonalZoneList(userid,
                    spUtil.getString("refreshTime"), tCreateTime,
                    zoneList.get(zoneList.size() - 1).getdCreateTime(), "1",
                    "", "", handler);
        } else {
            zoneListView.stopLoadMore();
        }

    }

    @Override
    protected void dealHandlers(Message msg) {
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
                ArrayList<Zone> temp = new ArrayList<Zone>();
                temp.addAll(zoneList);
                for (int i = 0; i < temp.size(); i++) {
                    if (temp.get(i).getiTop() != null
                            && temp.get(i).getiTop().equals("1")) {
                        zoneList.remove(temp.get(i));
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
                                Log.v("top", "插入" + j);
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
                zoneListView.stopRefresh();
                zoneListView.setRefreshTime(DateUtils.date2Str(new Date()));
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                } else {
                    setAdapter();
                }

                break;
            case ZoneConstants.ZONE_MORE_RESULT:
                ArrayList<Zone> temp2 = new ArrayList<Zone>();
                temp2.addAll(zoneList);
                for (int i = 0; i < temp2.size(); i++) {
                    if (temp2.get(i).getiTop() != null
                            && temp2.get(i).getiTop().equals("1")) {
                        zoneList.remove(temp2.get(i));
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
                                    temp2.get(j - 1).getdCreateTime()).getTime();
                            if (currTime < time2 && currTime > time1) {
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
                zoneListView.stopLoadMore();
                adapter.notifyDataSetChanged();
                break;
            case ZoneConstants.ZONE_SAY_DEL_RESULT:
                DialogUtils.getInstants().dismiss();
                try {
                    String result = (String) msg.obj;
                    JSONObject jsonObject = new JSONObject(result);
                    int code = jsonObject.getInt("code");
                    if (0 == code) {
                        int postion = msg.arg1;
                        zoneList.remove(postion);
                        adapter.notifyDataSetChanged();
                        isDelOK = true;
                    } else {
                        ToastUtils.showToast(ZoneOwnActivity.this, "删除失败！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.showToast(ZoneOwnActivity.this, "删除失败！");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ZoneConstants.ZONE_CLICK_AVATAR) {
            if (resultCode == ZoneConstants.ZONE_SAY_DEL_RESULT) {// 删除成功
                onRefresh();
                isDelOK = true;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isDelOK) {
            setResult(ZoneConstants.ZONE_SAY_DEL_RESULT);
            finish();
            //TODO anna
//			TempConstants.popActivity(this);
        } else {
            super.onBackPressed();
        }
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

}
