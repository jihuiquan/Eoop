package com.jianye.smart.module.workbench.manager;

import android.content.Context;
import android.os.Handler;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.HttpClientUtils;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.jianye.smart.module.workbench.constants.Constants;
import com.jianye.smart.module.workbench.model.WorkTable;

import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.List;

public class WorkTableManage {

    private Context mContext;
    private SharedPreUtils spUtil;
    private Handler mHandler;

    public static String TASK_MANAGE = "taskManage";
    public static String FUTURELAND_DIARY = "futureland_diary";
    public static String FUTURELAND_MANAGE = "futureland_manage";
    public static String MOBILEWORKFLOW = "com.mysoft.mobileworkflow";
    public static String MINGYUAN = "mingyuan";
    public static String FUTURELAND_APPROVAL = "futureland_approval";
    public static String LIVE = "com.ifca.moa.czxc.live";
    public static String PROCUREMENT = "procurement";
    public static String SCHEDULE_TASK = "schedule-task";
    public static String BUSINESS_REPORT = "futureland_businessReport";
    public static String DIARY_REPORT = "futureland_dailyReport";

    public WorkTableManage(Context context) {
        super();
        this.mContext = context;
        spUtil = new SharedPreUtils(context);
    }

    public void getAllUnreadNumber(final Handler handler, List<WorkTable> worktables) {
        this.mHandler = handler;
        if (null == worktables || worktables.isEmpty()) {
            return;
        }
        final String name = spUtil.getString(CommConstants.EMPADNAME);
        for (final WorkTable worktable : worktables) {
            if (StringUtils.notEmpty(worktable.getUnreadUrl())) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String result = HttpClientUtils.get(worktable.getUnreadUrl());
                            JSONObject jsonObject = new JSONObject(result);
                            int unread = jsonObject.optInt("objValue");
                            handler.obtainMessage(Constants.GET_UNREAD_TASK_ALL, worktable.getAndroid_access_url()
                                    + "---" + unread).sendToTarget();
                            //						getTaskUnreadNum(handler);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        }
    }

    public void getPersonalModules(final Handler handler) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    JSONObject object = new JSONObject();
                    object.put("userId", spUtil.getString(CommConstants.USERID));
                    String json = HttpClientUtils
                            .post(Constants.GET_PERSONAL_MOUDLES, object.toString(), Charset.forName("UTF-8"));
                    handler.obtainMessage(
                            Constants.PERSONALMODULES_RESULT, json)
                            .sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(Constants.MODULE_ERROR);
                }
            }
        }).start();
    }

    public void updateModules(final Handler handler, final String modules) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String json = HttpClientUtils
                            .post(Constants.UPDATE_PERSONAL_MOUDLES
                                            + "?userId="
                                            + spUtil.getString(CommConstants.USERID)
                                            + "&moduleIds=" + modules, "",
                                    Charset.forName("UTF-8"));
                    handler.obtainMessage(Constants.UPDATEMODULES_RESULT,
                            json).sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(Constants.MODULE_ERROR);
                }
            }
        }).start();
    }
}
