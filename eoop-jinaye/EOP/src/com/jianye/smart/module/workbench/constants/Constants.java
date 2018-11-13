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

package com.jianye.smart.module.workbench.constants;

import com.movit.platform.common.constants.CommConstants;

/**
 * Created by Louanna.Lu on 2015/11/3.
 */
public class Constants {

    public final static String RECEIVER_UNREADNUM = "unreadnum_receiver";

    public final static int PERSONALMODULES_RESULT = 12;
    public final static int MODULE_ERROR = 13;
    public final static int UPDATEMODULES_RESULT = 14;
    public final static int GET_TASK_UNREAD_NUM_RESULT = 15;
    public final static int GET_UNREAD_MING_YUAN_RESULT = 16;
    public final static int GET_UNREAD_EKP_RESULT = 17;
    public final static int GET_UNREAD_JING_YOU_RESULT = 18;
    public final static int GET_UNREAD_BID_OPENING_RESULT = 19;
    public final static int GET_UNREAD_DIARY = 20;
    public final static int GET_UNREAD_SCHEDULE_TASK = 21;
    public final static int GET_UNREAD_DIARY_REPORT = 22;
    public final static int GET_UNREAD_TASK_MANAGE = 23;
    public final static int GET_UNREAD_TASK_ALL = 24;

    private final static String URL_WORK_TABLE = CommConstants.URL_EOP_API+"r/sys/appmgtrest/";

    public final static String GET_TASK_UNREAD_NUM = "http://task.900950.com:9082/task/home/getECAccount";

    public final static String GET_PERSONAL_MOUDLES = URL_WORK_TABLE + "getpersonalmodules";
    public final static String UPDATE_PERSONAL_MOUDLES = URL_WORK_TABLE + "updatepersonmodules";

    public final static String URL_OFFICE_HR = CommConstants.URL_EOP_ADMIN+"a/oa/hr/myInformation?";
    public final static String URL_OFFICE_SHOP = CommConstants.URL_EOP_API+ "a/shop/mobile/center?";
    public final static String URL_OFFICE_ATTENDANCE = CommConstants.URL_EOP_ADMIN+"a/oa/hr/myAttendance?";

    public final static String URL_OFFICE_NEWS = CommConstants.URL_EOP_NEWS + "eoop/news?";

    public final static String  URL_OFFICE_TASK = "http://" + CommConstants.URL_API + CommConstants.HOST_PORT + "/eoop-task?";


    //应用状态:1.available,2.unavailable,3.offline
    public final static String STATUS_AVAILABLE = "1";
    public final static String STATUS_UNAVAILABLE = "2";
    public final static String STATUS_OFFLINE = "3";

    //应用类型:1.internal html5,2.html5,3.3,4.thirdparty_app
    public final static String TYPE_INTERNAL_HTML5 = "1";
    public final static String TYPE_WEB_HTML5 = "2";
    public final static String TYPE_NATIVE_APP = "3";
    public final static String TYPE_THIRDPARTY_APP = "4";

    //应用是否展示 1.show 2.hide
    public final static String DISPLAY_SHOW = "1";
    public final static String DISPLAY_HIDE = "2";
}
