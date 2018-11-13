package com.movit.platform.im.manager;

import android.content.Context;

import com.alibaba.fastjson.JSONArray;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.okhttp.utils.AesUtils;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.core.okhttp.callback.StringCallback2;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.db.IMDBFactory;
import com.movit.platform.im.utils.JSONConvert;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by Administrator on 2015/12/22.
 */
public class IMManager {

    public interface CallBack{
        public void refreshUI(List<MessageBean> contactList);
    }

    //获取聊天记录列表
    @SuppressWarnings("unchecked")
    public static void getContactList(final Context context, final CallBack callBack){
        String url = CommConstants.URL_EOP_IM + "im/getContactList?userName=" + MFSPHelper.getString(CommConstants.USERNAME).toLowerCase();
        HttpManager.getJsonWithToken(url, new StringCallback2() {
            @Override
            public void onError(Call call, Exception e) {
                if(null!=callBack){
                    callBack.refreshUI(null);
                }
            }

            @Override
            public void onResponse(String response) throws JSONException {
                if (StringUtils.notEmpty(response)) {
                    List<MessageBean> contactList = null;
                    try {
                        String jsonStr = new JSONObject(response).getString("objValue");
                        if (StringUtils.notEmpty(jsonStr)) {
                            Map<String, Object> responseMap = JSONConvert.json2MessageBean(jsonStr, context);

                            IMConstants.contactListDatas.clear();
                            IMConstants.contactListDatas = (ArrayList<MessageBean>) responseMap.get("messageBean");
                            contactList = JSONArray.parseArray(jsonStr, MessageBean.class);
//                            Intent contactIntent = new Intent(CommConstants.ACTION_CONTACT_LIST);
//                            contactIntent.setPackage(context.getPackageName());
//                            context.sendBroadcast(contactIntent);

                            //通知更新聊天未读数
//                            Intent intent = new Intent(CommConstants.ACTION_SET_REDPOINT);
//                            intent.setPackage(context.getPackageName());
//                            context.sendBroadcast(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        if(null!=callBack){
                            callBack.refreshUI(contactList);
                        }
                    }
                }
            }
        });
    }

    public static MessageBean enterPrivateSession(Context context,String sessionObjId ,
                                                  Message.Type chatType,boolean hasNewMes){
        try {
            MessageBean messageBean = null;
            if(chatType.equals(Message.Type.chat)){
                messageBean = IMDBFactory.getInstance(context).getRecordsManager()
                        .getStartTimeAndEndTime(sessionObjId, MFSPHelper.getString(CommConstants.EMPADNAME));
            }else {
                ArrayList<MessageBean> messageBeans = IMDBFactory.getInstance(context).getRecordsManager()
                        .getRecordsByRoomId(sessionObjId);
                if(!messageBeans.isEmpty()){
                    messageBean = messageBeans.get(messageBeans.size()-1);
                }
            }


            String curUser = CommConstants.loginConfig.getmUserInfo().getEmpAdname();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "recordSession");
            jsonObject.put("from", curUser);
            jsonObject.put("to", sessionObjId);
            if(StringUtils.notEmpty(messageBean) && hasNewMes){
                jsonObject.put("msgId", messageBean.getMsgId());
                jsonObject.put("timestamp", messageBean.getTimestamp());
            }
            EntityBareJid jid = JidCreate.entityBareFrom("admin" + MessageManager.SUFFIX);
            EntityBareJid fromJid = JidCreate.entityBareFrom(curUser + MessageManager.SUFFIX);
            Message message = new Message(jid, chatType);
            message.setFrom(fromJid);
            message.setBody(jsonObject.toString());

            if(chatType.equals(Message.Type.chat)){
                ChatManager manager = ChatManager.getInstanceFor(XmppManager.getInstance()
                        .getConnection());
                Chat chat = manager.chatWith(jid);
                chat.send(message);
            }else {
                MultiUserChat muc = MultiUserChatManager.getInstanceFor(XmppManager.getInstance().getConnection())
                        .getMultiUserChat(jid);
                muc.sendMessage(message);
            }


            return messageBean;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //离开单聊会话
    @Deprecated
    public static void leavePrivateSession(String session) {
//        try {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("type", "leaveSessionDialog");
//            jsonObject.put("toJID", session.toLowerCase());
//            ChatManager manager = ChatManager.getInstanceFor(XmppManager.getInstance()
//                    .getConnection());
//            EntityBareJid jid = JidCreate.entityBareFrom("admin" + MessageManager.SUFFIX);
//            Chat chat = manager.chatWith(jid);
//            chat.send(jsonObject.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    //离开群聊会话
    @Deprecated
    public static void leaveGroupSession(String session) {
//        try {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("type", "leaveRoomSessionDialog");
//            jsonObject.put("roomID", session.toLowerCase());
//            ChatManager manager = ChatManager.getInstanceFor(XmppManager.getInstance()
//                    .getConnection());
//            EntityBareJid jid = JidCreate.entityBareFrom("admin" + MessageManager.SUFFIX);
//            Chat chat = manager.chatWith(jid);
//            chat.send(jsonObject.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
