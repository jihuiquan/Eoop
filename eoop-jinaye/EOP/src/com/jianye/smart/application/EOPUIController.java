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

package com.jianye.smart.application;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.movit.platform.common.application.UIController;
import com.movit.platform.common.module.organization.activity.OrgActivity;
import com.movit.platform.common.module.user.activity.UserDetailActivity;
import com.movit.platform.framework.utils.LogUtils;
import com.movit.platform.im.module.group.activity.GroupChatActivity;
import com.movit.platform.im.module.group.activity.GroupListActivity;
import com.movit.platform.im.module.single.activity.ChatActivity;
import com.movit.platform.sc.module.msg.activity.ZoneMsgActivity;
import com.movit.platform.sc.module.msg.activity.ZoneMsgDetailActivity;
import com.movit.platform.sc.module.zone.activity.ZonePublishActivity;
import com.movit.platform.sc.module.zone.activity.ZoneOwnActivity;
import com.jianye.smart.activity.MainActivity;

/**
 * Created by Louanna.Lu on 2015/11/2.
 */
public class EOPUIController implements UIController {

    private String TAG = this.getClass().getSimpleName();

    @Override
    public void startMainActivity(Activity activity, Intent intent, int flag) {
        intent.setClass(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    @Override
    public void onOwnHeadClickListener(Activity activity, Intent intent,int flag) {

        LogUtils.d(TAG,"onOwnHeadClickListener");
        intent.setClass(activity,UserDetailActivity.class);
        activity.startActivityForResult(intent, flag);
    }

    @Override
    public void onSendMessageClickListener(Activity activity, Intent intent) {
        LogUtils.d(TAG,"onSendMessageClickListener");
        intent.setClass(activity,ChatActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void onZoneOwnClickListener(Activity activity, Intent intent,int flag) {
        LogUtils.d(TAG,"onZoneOwnClickListener");
        intent.setClass(activity,ZoneOwnActivity.class);
        activity.startActivityForResult(intent,flag);
    }

    @Override
    public void onZoneOwnClickListener(Fragment fragment, Intent intent, int flag) {
        LogUtils.d(TAG,"onZoneOwnClickListener");
        intent.setClass(fragment.getContext(), ZoneOwnActivity.class);
        fragment.startActivityForResult(intent, flag);
    }

    @Override
    public void startPrivateChat(Activity activity, Bundle bundle) {
        LogUtils.d(TAG,"startPrivateChat");
        Intent intent = new Intent(activity,ChatActivity.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    @Override
    public void startMultChat(Activity activity, Bundle bundle) {
        LogUtils.d(TAG,"startMultChat");
        Intent intent = new Intent(activity,GroupChatActivity.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    @Override
    public void onGroupListClickListener(Activity activity) {
        Intent intent = new Intent(activity,GroupListActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void onZoneMsgClickListener(Fragment fragment, Intent intent,int flag) {
        LogUtils.d(TAG,"onZoneMsgClickListener");
        intent.setClass(fragment.getContext(),ZoneMsgActivity.class);
        fragment.startActivityForResult(intent,flag);
    }

    @Override
    public void onZoneMsgDetailClickListener(Activity activity,Intent intent,int flag) {
        LogUtils.d(TAG,"onZoneMsgDetailClickListener");
        intent.setClass(activity,ZoneMsgDetailActivity.class);
        activity.startActivityForResult(intent, flag);
    }

    @Override
    public void onZonePublishClickListener(Fragment fragment, Intent intent,int flag) {
        LogUtils.d(TAG,"onZonePublishClickListener");
        intent.setClass(fragment.getContext(),ZonePublishActivity.class);
        fragment.startActivityForResult(intent, flag);
    }

    @Override
    public void onIMOrgClickListener(Activity activity, Intent intent, int flag) {
        LogUtils.d(TAG,"onIMOrgClickListener");
        intent.setClass(activity,OrgActivity.class);
        activity.startActivityForResult(intent, flag);
    }

    @Override
    public ComponentName getMainComponentName(Context context) {
        return new ComponentName(context.getPackageName(),
                MainActivity.class.getName());
    }

}
