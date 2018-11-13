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

package com.movit.platform.im.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.framework.helper.MFHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.R;
import com.movit.platform.im.manager.GroupManager;
import com.movit.platform.im.manager.MessageManager;
import com.movit.platform.im.manager.XmppManager;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.packet.Presence;

/**
 * Created by Louanna.Lu on 2015/10/16.
 */
public class XmppHelper {

    private Context context;
    private CommonHelper loginHelper;
    private ServiceHelper serviceHelper;

    public XmppHelper(Context context) {
        super();
        this.context = context;
        loginHelper = new CommonHelper(context);
        serviceHelper = new ServiceHelper(context);
    }

    // 登录xmpp
    public void loginXMPP() {
        Log.d("chatRecords", "loginXMPP: ");
        new Thread(new Runnable() {

            @Override
            public void run() {
                LoginInfo loginConfig = loginHelper.getLoginConfig();
                String username = loginConfig.getmUserInfo().getEmpAdname();
                String password = loginConfig.getmUserInfo().getOpenFireToken();

                if (StringUtils.empty(password)) {
                    password = "1";
                }
                try {
                    XmppManager manager = XmppManager.getInstance();
                    // 初始化xmpp配置
                    manager.initialize(CommConstants.URL_XMPP, CommConstants.PORT,context);
                    AbstractXMPPConnection connection = manager.getConnection();

                    //Log.d("XmppHelper", "run: start");
                    connection.login(username.toLowerCase().trim(), password + "," + MFHelper.getDeviceId(context)); // 登录
                    connection.sendStanza(new Presence(Presence.Type.available));
                    //Log.d("XmppHelper", "run: end");

                    String xmpp = connection.getXMPPServiceDomain().toString();
                    MessageManager.SUFFIX = "@" + xmpp;
                    MessageManager.GROUP_AT = "@conference." + xmpp;
                    handler.sendEmptyMessage(3);
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(2);
                }
            }
        }).start();
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:// 失败
                    Intent intent = new Intent(
                            CommConstants.SIMPLE_LOGIN_ACTION);
                    intent.putExtra("body", context.getString(R.string.im_connect_failed));
                    intent.putExtra("type", CommConstants.TYPE_JUST_TIPS);
                    intent.setPackage(context.getPackageName());
                    context.sendBroadcast(intent);

                    Intent intent3 = new Intent(CommConstants.ACTION_XMPP_LOGIN);
                    intent3.putExtra("type", CommConstants.TYPE_JUST_TIPS);
                    intent3.setPackage(context.getPackageName());
                    context.sendBroadcast(intent3);

                    break;
                case 3:// 初始化各项服务

                    Intent intent2 = new Intent(CommConstants.ACTION_XMPP_LOGIN);
                    intent2.putExtra("type", CommConstants.TYPE_XMPP_LOGIN_SUCCESS);
                    intent2.setPackage(context.getPackageName());
                    context.sendBroadcast(intent2);

                    //每次成功登录XMPP后，均需重新updatDevice，以保证设备信息及时更新，push消息推送正确
                    //每次成功登录XMPP后，均需主动调用getGroupList
                    GroupManager.getInstance(context).getGroupList();
                    //每次成功登录XMPP后，均需重新获取聊天记录
//                    IMManager.getContactList(context,null);
                    // 每次成功登录XMPP后，均需启动XMPP服务
                    serviceHelper.startService();

                    break;
                default:
                    break;
            }
        }
    };

}
