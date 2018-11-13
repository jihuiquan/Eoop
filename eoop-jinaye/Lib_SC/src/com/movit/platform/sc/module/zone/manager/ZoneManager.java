package com.movit.platform.sc.module.zone.manager;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.movit.platform.common.api.IZoneManager;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.sc.constants.SCConstants;
import com.movit.platform.sc.module.zone.constant.ZoneConstants;
import com.movit.platform.sc.utils.ZoneConvert;
import com.movit.platform.sc.module.zone.activity.ZonePublishActivity;
import com.movit.platform.sc.entities.Zone;
import com.movit.platform.sc.entities.ZoneMessage;

public class ZoneManager implements IZoneManager {
    private Context context;
    private SharedPreUtils spUtil;

    public ZoneManager(Context context) {
        super();
        this.context = context;
        spUtil = new SharedPreUtils(context);
    }

    /**
     * @param refreshTime 刷新时间
     * @param tCreateTime 第一条时间
     * @param bCreateTime 最后一条时间
     * @param isAfter     默认1 0为时间之前消息
     * @param type        默认 all 消息类型：工作 1\生活 0
     * @param isSecret    默认 0 1隐私消息
     * @param handler
     */
    @Override
    public void getZoneListData(final String officeId, final String refreshTime,
        final String tCreateTime, final String bCreateTime,
        final String isAfter, final String type, final String isSecret,
        final Handler handler) {
        new Thread(new Runnable() {
            // http:/172.18.50.120:8080/CZ/rest/getdata?userId=005b78a48ad64d638fef26d414fda190&Token=R%2FpunGPWvd5CVvfvsG0N%2BVXJRqWlNLCVBR0%2FJPFjEsjzREsySvepgqIuBuEyMe2ZezMVYDZU%2FAE3Hq2X8xl3EyNxESI2a%2FcIGhgQ4gyR0gt48VPJ06a1NAfk%2FIJfnFd5PFRCDu5eBjE%3D
            // &iPageRowCount=15

            @Override
            public void run() {
                try {
                    String url = SCConstants.GET_ZONE_LIST_DATA +"?userId="
                        + spUtil.getString(CommConstants.USERID)
                        + "&Token="
                        + URLEncoder.encode(
                        spUtil.getString(CommConstants.TOKEN), "UTF-8");
                    if (!"".equals(refreshTime)) {
                        url += "&refreshTime="
                            + URLEncoder.encode(refreshTime, "UTF-8");
                    }
                    if (!"".equals(tCreateTime)) {
                        url += "&tCreateTime="
                            + URLEncoder.encode(tCreateTime, "UTF-8");
                    }
                    if (!"".equals(bCreateTime)) {
                        url += "&bCreateTime="
                            + URLEncoder.encode(bCreateTime, "UTF-8");
                    }
                    if (!"".equals(isAfter)) {
                        url += "&isAfter=" + isAfter;
                    }
                    if (!"".equals(type)) {
                        url += "&type=" + type;
                    }
                    if (!"".equals(isSecret)) {
                        url += "&isSecret=" + isSecret;
                    }

                    url += "&officeId=" + officeId;

                    String result = HttpClientUtils.postWithoutEncrypt(url
                        + "&iPageRowCount=15");

                    ArrayList<String> delList = ZoneConvert.getZoneListDataDel(
                        result);
                    ArrayList<Zone> newList = ZoneConvert.getZoneListDataNew(
                        result, context);
                    ArrayList<Zone> oldList = ZoneConvert.getZoneListDataOld(
                        result, context);
                    ArrayList<Zone> topList = ZoneConvert.getZoneListDataTop(
                        result, context);

                    JSONObject jsonObject = new JSONObject(result);
                    int code = jsonObject.getInt("code");
                    if (code == 0) {
                        JSONObject list = jsonObject.getJSONObject("item");
                        if (list.has("refreshTime")) {
                            SharedPreUtils spUtil = new SharedPreUtils(
                                context);
                            String refreshTime = list.getString("refreshTime");
                            spUtil.setString("refreshTime", refreshTime);
                        }
                    }

                    Message message = new Message();
                    Bundle data = new Bundle();
                    data.putStringArrayList("delList", delList);
                    data.putSerializable("newList", newList);
                    data.putSerializable("oldList", oldList);
                    data.putSerializable("topList", topList);
                    message.setData(data);

                    if ("1".equals(isAfter)) {
                        message.what = ZoneConstants.ZONE_MORE_RESULT;
                        handler.sendMessage(message);
                    } else {
                        if (!newList.isEmpty()) {
                            String dCreateTime = newList.get(0)
                                .getdCreateTime();
                            spUtil.setString("dCreateTime", dCreateTime);
                        }
                        message.what = ZoneConstants.ZONE_LIST_RESULT;
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.obtainMessage(ZoneConstants.ZONE_ERROR_RESULT,
                        "消息获取失败！").sendToTarget();
                }
            }
        }).start();
    }

    /**
     * @param content      文本
     * @param type         说说类型 work 1,life 0
     * @param isSecret     是否隐私 1是 0否
     * @param sImages      图片URL
     * @param sAtGroup
     * @param sAtPerson
     * @param sMessageList
     * @param handler
     * @ 组ID
     * @ 人ID
     * @ 发送人ID
     */
    @Override
    public void say(final String content, final String type,
        final String isSecret, final String sImages, final String sAtGroup,
        final String sAtPerson, final String sMessageList,
        final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // 是否隐私 1是 0否,说说类型 work 1,life 0
                    String sssString = "userId="
                        + spUtil.getString(CommConstants.USERID)
                        + "&type="
                        + type
                        + "&isSecret="
                        + isSecret
                        + "&sContent="
                        + URLEncoder.encode(content, "UTF-8")
                        + "&sImages="
                        + sImages
                        + "&sAtGroup="
                        + sAtGroup
                        + "&sAtPerson="
                        + sAtPerson
                        + "&sMessageList="
                        + sMessageList
                        + "&Token="
                        + URLEncoder.encode(
                        spUtil.getString(CommConstants.TOKEN), "UTF-8");
                    String responseStr = HttpClientUtils.postZone(
                        SCConstants.PUBLISH_ZONE_SAY, sssString,
                        Charset.forName("UTF-8"));
                    handler.obtainMessage(ZonePublishActivity.ZONE_SAY_RESULT,
                        responseStr).sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * @param cSayId   说说id
     * @param touserId 目标用户uid
     * @param handler
     */
    @Override
    public void nice(final String cSayId, final String touserId,
        final String undo, final int postion, final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String sssString = "userId="
                        + spUtil.getString(CommConstants.USERID)
                        + "&cSayId="
                        + cSayId
                        + "&touserId="
                        + touserId
                        + "&Token="
                        + URLEncoder.encode(
                        spUtil.getString(CommConstants.TOKEN), "UTF-8");
                    int isNice = 0;
                    if (StringUtils.notEmpty(undo)) {
                        sssString += "&undo=1";
                        isNice = 1;
                    }
                    String responseStr = HttpClientUtils.postZone(
                        SCConstants.ZONE_NICE, sssString,
                        Charset.forName("UTF-8"));

                    handler.obtainMessage(ZoneConstants.ZONE_NICE_RESULT,
                        postion, isNice, responseStr).sendToTarget();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * @param cSayId    说说id
     * @param userId    用户Id
     * @param touserId  发送对象用户Id
     * @param sContent  文本
     * @param cParentId 评论父节点ID
     * @param cRootId   评论根节点ID
     * @param postion
     * @param handler
     */
    @Override
    public void comment(final String cSayId, final String userId,
        final String touserId, final String sContent,
        final String cParentId, final String cRootId, final int postion,
        final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String sssString = "userId="
                        + spUtil.getString(CommConstants.USERID)
                        + "&cSayId="
                        + cSayId
                        + "&touserId="
                        + touserId
                        + "&sContent="
                        + URLEncoder.encode(sContent, "UTF-8")
                        + "&cParentId="
                        + cParentId
                        + "&cRootId="
                        + cRootId
                        + "&Token="
                        + URLEncoder.encode(
                        spUtil.getString(CommConstants.TOKEN), "UTF-8");
                    String responseStr = HttpClientUtils.postZone(
                        SCConstants.ZONE_COMMENT, sssString,
                        Charset.forName("UTF-8"));
                    handler.obtainMessage(ZoneConstants.ZONE_COMMENT_RESULT,
                        postion, 0, responseStr).sendToTarget();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * @param handler
     */
    @Override
    public void messages(final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String sssString = "?userId="
                        + spUtil.getString(CommConstants.USERID)
                        + "&Token="
                        + URLEncoder.encode(
                        spUtil.getString(CommConstants.TOKEN), "UTF-8");
                    String url = SCConstants.ZONE_MESSAGE + sssString;
                    String result = HttpClientUtils.postWithoutEncrypt(url);
                    ArrayList<ZoneMessage> messages = ZoneConvert
                        .getZoneMessageData(result);
                    if (messages == null) {
                        handler.obtainMessage(
                            ZoneConstants.ZONE_ERROR_RESULT, "消息获取失败！")
                            .sendToTarget();
                    } else {
                        handler.obtainMessage(
                            ZoneConstants.ZONE_LIST_RESULT, messages)
                            .sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.obtainMessage(ZoneConstants.ZONE_ERROR_RESULT,
                        "消息获取失败！").sendToTarget();
                }
            }
        }).start();
    }

    @Override
    public void messagecount(final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String sssString = "?userId="
                        + spUtil.getString(CommConstants.USERID)
                        + "&Token="
                        + URLEncoder.encode(
                        spUtil.getString(CommConstants.TOKEN), "UTF-8");
                    String url = SCConstants.ZONE_MESSAGE_COUNT + sssString;
                    String result = HttpClientUtils.postWithoutEncrypt(url);
                    handler.obtainMessage(
                        ZoneConstants.ZONE_MESSAGE_COUNT_RESULT, result)
                        .sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void messagedel(final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String sssString = "userId="
                        + spUtil.getString(CommConstants.USERID)
                        + "&Token="
                        + URLEncoder.encode(
                        spUtil.getString(CommConstants.TOKEN), "UTF-8");
                    String result = HttpClientUtils.postZone(SCConstants.ZONE_MESSAGE_DELETE, sssString, Charset.forName("UTF-8"));
                    handler.obtainMessage(ZoneConstants.ZONE_MSG_DEL_RESULT,
                        result).sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 获取说说的详情
     *
     * @param cSayId
     * @param handler
     */
    @Override
    public void getSay(final String cSayId, final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String sssString = "?userId="
                        + spUtil.getString(CommConstants.USERID)
                        + "&cSayId="
                        + cSayId
                        + "&Token="
                        + URLEncoder.encode(
                        spUtil.getString(CommConstants.TOKEN), "UTF-8");
                    String url = SCConstants.GET_ZONE_SAY + sssString;
                    String result = HttpClientUtils.postWithoutEncrypt(url);
                    ArrayList<Zone> zoneList = ZoneConvert.getZoneDetailData(
                        result, context);
                    if (zoneList == null) {
                        handler.obtainMessage(
                            ZoneConstants.ZONE_ERROR_NO_SAY)
                            .sendToTarget();
                    } else {
                        handler.obtainMessage(
                            ZoneConstants.ZONE_GET_RESULT, zoneList)
                            .sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.obtainMessage(ZoneConstants.ZONE_ERROR_NO_SAY)
                        .sendToTarget();
                }
            }
        }).start();
    }

    @Override
    public void mysaycount(final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String sssString = "?userId="
                        + spUtil.getString(CommConstants.USERID)
                        + "&Token="
                        + URLEncoder.encode(
                        spUtil.getString(CommConstants.TOKEN), "UTF-8");
                    String url = SCConstants.GET_MINE_SAY_COUNT + sssString;
                    String result = HttpClientUtils.postWithoutEncrypt(url);
                    handler.obtainMessage(
                        ZoneConstants.ZONE_MY_SAY_COUNT_RESULT, result)
                        .sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void havenew(final String officeId,final String dCreateTime, final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String sssString = "?userId="
                        + spUtil.getString(CommConstants.USERID)
                        + "&dCreateTime="
                        + URLEncoder.encode(dCreateTime, "UTF-8")
                        +"&officeId="
                        +officeId
                        + "&Token="
                        + URLEncoder.encode(
                        spUtil.getString(CommConstants.TOKEN), "UTF-8");
                    String url = SCConstants.ZONE_NEW_MESSAGE + sssString;
                    String result = HttpClientUtils.postWithoutEncrypt(url);
                    handler.obtainMessage(
                        ZoneConstants.ZONE_NEW_SAY_COUNT_RESULT, result)
                        .sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * @param userId
     * @param refreshTime 刷新时间
     * @param tCreateTime 第一条时间
     * @param bCreateTime 最后一条时间
     * @param isAfter     默认1 0为时间之前消息
     * @param type        默认 all 消息类型：工作 1\生活 0
     * @param isSecret    默认 0 1隐私消息
     * @param handler
     */
    @Override
    public void getPersonalZoneList(final String userId,
        final String refreshTime, final String tCreateTime,
        final String bCreateTime, final String isAfter, final String type,
        final String isSecret, final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String url = SCConstants.GET_MINE_SAY_LIST
                        + "?userId="
                        + userId
                        + "&Token="
                        + URLEncoder.encode(
                        spUtil.getString(CommConstants.TOKEN), "UTF-8");
                    if (!"".equals(refreshTime)) {
                        url += "&refreshTime="
                            + URLEncoder.encode(refreshTime, "UTF-8");
                    }
                    if (!"".equals(tCreateTime)) {
                        url += "&tCreateTime="
                            + URLEncoder.encode(tCreateTime, "UTF-8");
                    }
                    if (!"".equals(bCreateTime)) {
                        url += "&bCreateTime="
                            + URLEncoder.encode(bCreateTime, "UTF-8");
                    }
                    if (!"".equals(isAfter)) {
                        url += "&isAfter=" + isAfter;
                    }
                    if (!"".equals(type)) {
                        url += "&type=" + type;
                    }
                    if (!"".equals(isSecret)) {
                        url += "&isSecret=" + isSecret;
                    }
                    String result = HttpClientUtils.postWithoutEncrypt(url
                        + "&iPageRowCount=15");
                    ArrayList<String> delList = ZoneConvert.getZoneListDataDel(
                        result);
                    ArrayList<Zone> newList = ZoneConvert.getZoneListDataNew(
                        result, context);
                    ArrayList<Zone> oldList = ZoneConvert.getZoneListDataOld(
                        result, context);
                    ArrayList<Zone> topList = ZoneConvert.getZoneListDataTop(
                        result, context);

                    Message message = new Message();
                    Bundle data = new Bundle();
                    data.putStringArrayList("delList", delList);
                    data.putSerializable("newList", newList);
                    data.putSerializable("oldList", oldList);
                    data.putSerializable("topList", topList);
                    message.setData(data);

                    if ("1".equals(isAfter)) {
                        message.what = ZoneConstants.ZONE_MORE_RESULT;
                        handler.sendMessage(message);
                    } else {
                        message.what = ZoneConstants.ZONE_LIST_RESULT;
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.obtainMessage(ZoneConstants.ZONE_ERROR_RESULT,
                        "消息获取失败！").sendToTarget();
                }
            }
        }).start();
    }

    @Override
    public void saydel(final String sayId, final int postion,
        final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String sssString = "userId="
                        + spUtil.getString(CommConstants.USERID)
                        + "&cSayId="
                        + sayId
                        + "&Token="
                        + URLEncoder.encode(
                        spUtil.getString(CommConstants.TOKEN), "UTF-8");
                    String result = HttpClientUtils.postZone(SCConstants.DELETE_MINE_SAY, sssString, Charset.forName("UTF-8"));
                    handler.obtainMessage(ZoneConstants.ZONE_SAY_DEL_RESULT,
                        postion, 0, result).sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void commentdel(final String commentId, final int postion,
        final int delCommentLine, final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String sssString = "userId="
                        + spUtil.getString(CommConstants.USERID)
                        + "&commentId="
                        + commentId
                        + "&Token="
                        + URLEncoder.encode(
                        spUtil.getString(CommConstants.TOKEN), "UTF-8");
                    String result = HttpClientUtils.postZone(SCConstants.DELETE_COMMENT, sssString, Charset.forName("UTF-8"));
                    handler.obtainMessage(
                        ZoneConstants.ZONE_COMMENT_DEL_RESULT, postion,
                        delCommentLine, result).sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.obtainMessage(ZoneConstants.ZONE_ERROR_RESULT,
                        "删除失败!").sendToTarget();
                }
            }
        }).start();
    }

}
