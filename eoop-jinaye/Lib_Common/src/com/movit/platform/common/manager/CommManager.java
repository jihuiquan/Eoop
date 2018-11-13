package com.movit.platform.common.manager;

import android.content.Context;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.movit.platform.framework.utils.SharedPreUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/16.
 */
public class CommManager {

    public static void postDeviceType(final String deviceId, final Context context) {
        new Thread() {
            public void run() {
                String deviceType = "2";
                JSONObject object = new JSONObject();
                try {
                    SharedPreUtils spUtil = new SharedPreUtils(context);
                    String userId = spUtil.getString(CommConstants.USERID);
                    object.put("userId", userId);
                    object.put("deviceType", deviceType);
                    object.put("device", deviceId);
                    object.put("mobilemodel", CommConstants.PHONEBRAND);
                    object.put("mobileversion", CommConstants.PHONEVERSION);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HttpClientUtils.post(CommConstants.URL
                                + "updateDevice", object.toString(),
                        Charset.forName("UTF-8"));
            }

        }.start();
    }

    //获取关注的人和被关注的人的信息
    public static void getAttentionData(final Context mContext) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreUtils spUtil = new SharedPreUtils(mContext);
                JSONObject useridJson = new JSONObject();
                try {
                    useridJson.put("userid", spUtil.getString(CommConstants.USERID));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String result = HttpClientUtils.post(
                        CommConstants.URL_STUDIO + "getAttentions", useridJson.toString() ,Charset.forName("UTF-8"));
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (null != jsonObject && jsonObject.has("objValue")
                            && !jsonObject.isNull("objValue")) {
                        JSONObject objValue = jsonObject.getJSONObject("objValue");

                        ArrayList<String> toBeAttentionPO = new ArrayList<String>();
                        if (null != objValue && objValue.has("toBeAttentionPO")
                                && !objValue.isNull("toBeAttentionPO")) {
                            JSONArray array = objValue.getJSONArray("toBeAttentionPO");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                String userid = "";
                                if (object.has("userid")) {
                                    userid = object.getString("userid");
                                    if (!toBeAttentionPO.contains(userid)) {
                                        toBeAttentionPO.add(userid);
                                    }
                                }
                            }
                        }

                        ArrayList<String> attentionPO = new ArrayList<String>();
                        if (objValue.has("attentionPO") && !objValue.isNull("attentionPO")) {
                            JSONArray array = objValue.getJSONArray("attentionPO");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                String attentionid = "";
                                if (object.has("attentionid")) {
                                    attentionid = object.getString("attentionid");
                                    if (!attentionPO.contains(attentionid)) {
                                        attentionPO.add(attentionid);
                                    }
                                }
                            }
                        }
                        CommConstants.loginConfig.getmUserInfo().setAttentionPO(attentionPO);
                        CommConstants.loginConfig.getmUserInfo().setToBeAttentionPO(toBeAttentionPO);
                    }

                    //标记获取完毕，在tab页中需要先判断是否获取完毕，获取完毕再显示页面内容，否则loading
                    CommConstants.GET_ATTENTION_FINISH = true;

                } catch (JSONException e) {
                    e.printStackTrace();
                    CommConstants.GET_ATTENTION_FINISH = true;
                }
            }
        }).start();
    }


}
