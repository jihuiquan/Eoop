package com.movit.platform.common.constants;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommConstants {
    public static String LogTag = "futureland";

    public static int loginXmppTime = 0;

    //是否登录EOP服务器
    public static boolean IS_LOGIN_EOP_SERVER;
    public static String URL_EOP_ADMIN;
    public static String URL_EOP_API;
    public static String URL_EOP_NEWS;
    public static String URL_EOP_IM;

    public static final String TYPE_JUST_TIPS = "1";//给出提示信息,不强制退出APP
    public static final String TYPE_LOGINOUT = "2";//强制退出APP
    public static final String TYPE_XMPP_LOGIN_SUCCESS = "3";//xmpp登陆成功

    /**************
     * Common
     ****************/
    public static LoginInfo loginConfig;
    public static List<UserInfo> allUserInfos;
    public static List<OrganizationTree> allOrgunits;
    //标记获取完毕，在tab页中需要先判断是否获取完毕，获取完毕再显示页面内容，否则loading
    public static boolean GET_ATTENTION_FINISH = false;

    public static String URL_STUDIO;
    public static String URL_DOWN;
    public static String URL_EOP_DMS;
    public static String URL_DOWN_FILE;
    public static String BASE_URL;

    public static String URL_DOWNLOAD;

    public static String SD_DOWNLOAD;

    public static final String AVATAR = "avatar";
    public static final String EMPADNAME = "empAdname";
    public static final String ACTION_GROUP_MEMBERS_CHANGES = "action.group.members.changes";
    public static final String ACTION_GROUP_DISPALYNAME_CHANGES = "action.group.displayname.changes";
    public static final String ACTION_GROUP_DISSOLVE_CHANGES = "action.group.dissolve.changes";


    //TODO anna
    public static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 5000;
    public static final int CAMERA_REQUEST_CODE = 5001;
    public static final int RECORD_AUDIO_CODE = 5002;

    /**************Common****************/

    /**************
     * IM
     ****************/
    public static Map<String, Integer> mFaceMap = new LinkedHashMap<String, Integer>();
    public static Map<String, Integer> mFaceGifMap = new LinkedHashMap<String, Integer>();
    public static int NUM = 20;// 每页20个表情,还有最后一个删除button
    public static final int NUM_PAGE = 7;// 总共有多少页

    /**************
     * IM
     ****************/

    public static String SD_CARD;
    public static String SD_CARD_MYPHOTOS;
    public static String SD_CARD_IMPICTURES;
    public static String SD_CARD_IM;

    // 注：小米2s只能放在根目录下，其他手机没关系
    public static final String IMAGE_FILE_LOCATION = "file:///sdcard/temp.jpg";// temp
    // file
    public static String HOST_PATH = "/im/";
    public static String SD_CARD_IM_VIDEO;//视频

    public static String SD_DOCUMENT;
    public static String SD_DATA;
    public static String SD_DATA_PIC;
    public static String SD_DATA_AUDIO;
    public static String SD_DATA_VIDEO;
    public static String SD_DATA_FILE;


    public static int PORT = 5222;
    public static String URL_XMPP = "";

    public static String URL;

    public static String URL_API = "";
    public static String HOST_PORT = "";
    public static String HOST_CMS_PORT = "";
    public static String HOST_TYPE = "";
    public static boolean ORG_TREE = false;

    public static String URL_UPLOAD;
    public static String DOWNLOAD_URL;
//    public static String APP_DOWNLOAD_URL;

    public static String REGIST_URL;
    public static String TIMESHEET_URL;
    public static String url_company;
    public static String url_suggestion;
    public static String url_attendance;
    public static String url_advice_type;

    public static String URL_BDODOWNLOAD;

    public static String URL_INTERNALEA = "";
    public static String URL_PUNCHCARD = "";
    public static String URL_GET_PUNSH_RECORD = "";

    public static String URL_MDM;
    public static String URL_BANNER_PICTURE;
    public static String UPLOAD_BPM_PIC;
    public static String URL_SCHEDULE_TASK;
    public static String JHXT_IP;
    public static String JHXT_PORT;

    public static String URL_MING_YUAN_COUNT;//明源流程审批未读数
    public static String URL_EKP_COUNT;//EKP流程审批未读数
    public static String URL_JING_YOU_COUNT;//竟优流程审批未读数
    public static String URL_BID_OPENING_DONOT_COUNT;//移动开标待办数

    public static String URL_WORK_TABLE;
    public static String URL_WORK_EMAIL;
    public static String URL_ATTENDANCE;

    public static void initHost(Context context) {

        SD_CARD = Environment
                .getExternalStorageDirectory().getAbsolutePath();

        SD_CARD_MYPHOTOS = SD_CARD + "/建业相册/";

        SharedPreUtils spUtil = new SharedPreUtils(context);
        String ip = spUtil.getString("ip");
        if (StringUtils.notEmpty(ip)) {
            URL_API = ip;
            HOST_PORT = spUtil.getString("port");
            HOST_CMS_PORT = spUtil.getString("cmsPort");
        }

        String xmppIp = spUtil.getString("xmppIp");
        if (StringUtils.notEmpty(xmppIp)) {
            URL_XMPP = xmppIp;
        }

        String versionName = "";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(),
                    0);
            versionName = info.versionName;

            JHXT_IP = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getString("JHXT_IP");
            JHXT_PORT = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getString("JHXT_PORT");

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        URL_BDODOWNLOAD = "http://eoopapppub.movitechcn.com:8080/eoopapp/ios/v4.0.2/test/";

        UPLOAD_BPM_PIC = JHXT_IP + JHXT_PORT + "/WebService/PlanMobileService.asmx/SaveAttach";

        URL_SCHEDULE_TASK = JHXT_IP + JHXT_PORT + "/mobile/main.html?UserId=%1$s&Key=%2$s";

//		DOWNLOAD_URL = "http://eoopapppub.movitechcn.com:8080/eoopapp/android/v"
//				+ versionName + "/" + HOST_TYPE + "/ANDVersion.json";
        //http://appdownload.xincheng.com:9098/app/xcgzt/version/ios.json
        DOWNLOAD_URL = "https://gzt.jianye.com.cn:20799/app/echat/version/android.json";
        //APP_DOWNLOAD_URL = "http://gzt.jianye.com.cn:80/eoop/wwwPhone/bowserHref/eop.html";//UAT
        //APP_DOWNLOAD_URL = "http://gzt.jianye.com.cn:80/eoop/wwwPhone/bowserHref/eop.html";
        //APP_DOWNLOAD_URL = "http://download.eop.movitech.cn"+"/?type=" + HOST_TYPE + "&version=v" + versionName;
        HOST_PATH = "/im" + HOST_TYPE + "/";
		/*---配置文件路径--*/
        SD_CARD_IM = SD_CARD + HOST_PATH;
        SD_CARD_IMPICTURES = SD_CARD_IM + "EoopPictures/";
        SD_CARD_IM_VIDEO = SD_CARD_IM + "EoopVideos/";
        SD_DOWNLOAD = SD_CARD_IM + "download/";
        SD_DOCUMENT = SD_CARD_IM + "document/";
        SD_DATA = SD_CARD_IM + "data/";
        SD_DATA_PIC = SD_CARD_IM + "data/pic/";
        SD_DATA_AUDIO = SD_CARD_IM + "data/audio/";
        SD_DATA_VIDEO = SD_CARD_IM + "data/video/";
        SD_DATA_FILE = SD_CARD_IM + "data/file/";

		/*---配置接口地址--*/
        BASE_URL = "http://" + URL_API;
        URL_DOWNLOAD = "http://" + URL_API + "/app-download";
        URL = "http://" + URL_API + HOST_PORT + "/eoop-api/rest/";

        URL_UPLOAD = "http://" + URL_API + HOST_CMS_PORT
                + "/eoop-cms/file/upload";
        URL_DOWN = "http://" + URL_API + HOST_CMS_PORT + "/cmsContent/";

        URL_EOP_IM = "http://" + URL_API + HOST_PORT+"/eop-im/";

        TIMESHEET_URL = "http://" + URL_API + HOST_PORT
                + "/eoop-ts/timesheet/index?";

        URL_EOP_API = "http://" + URL_API + HOST_PORT + "/eoop-api/";
        URL_EOP_NEWS = "http://" + URL_API + HOST_PORT + "/eoop-news/";
        URL_EOP_ADMIN = "http://" + URL_API + HOST_PORT + "/eoop-admin/";

        URL_STUDIO = URL_EOP_API + "r/sys/rest/";

        REGIST_URL = URL_EOP_API + "a/sys/user/register";

        url_company = URL_EOP_API + "r/product?companyId=000000000000000000000000000000000000";
        url_suggestion = URL_EOP_API + "r/advice/saveAdvice";
        url_attendance = URL_EOP_API + "r/eattendance/";
        url_advice_type = URL_EOP_API + "r/advice/getAdviceType";

        URL_MDM = URL_EOP_API + "r/sys/rest/unbindDevice/";

        URL_BANNER_PICTURE = URL_EOP_API + "r/sys/app/picture/getBanners";

        URL_MING_YUAN_COUNT = URL_EOP_API + "r/unread/getMingYuanCount?userName=";
        URL_EKP_COUNT = URL_EOP_API + "r/unread/getERPCount?userName=";
        URL_JING_YOU_COUNT = URL_EOP_API + "r/unread/getJingYouCount?userName=";
        URL_BID_OPENING_DONOT_COUNT = URL_EOP_API + "r/unread/getBidOpeningDonotCount?userName=";

        URL_INTERNALEA = URL_EOP_API + "r/sys/punchcard/GetAllPoint";
        URL_PUNCHCARD = URL_EOP_API + "r/sys/punchcard";
        URL_GET_PUNSH_RECORD = URL_EOP_API + "r/sys/punchcard/GetPunshRecord";

        /** 打卡后的运营图片接口**/
        URL_ATTENDANCE = URL_EOP_API + "r/sys/appmgtrest/queryAttendancePath";

        URL_WORK_TABLE = URL_EOP_API + "r/sys/sso/login";

        URL_WORK_EMAIL = URL_EOP_API + "rest/sys/email/getEmailUrl";

        URL_EOP_DMS = "http://" + URL_API + HOST_PORT + "/eoop-dms/";
        URL_DOWN_FILE = URL_EOP_DMS + "services/download/document";

        URL_EDIT_NAME =  URL+ "im/group/edit_name";
        URL_UPDATE_USER_INFO = URL_STUDIO + "updateUserInfo";

        URL_IM_ADD_MEMBERS =  URL+ "im/group/add_members";
        URL_IM_BOWOUT =  URL+ "im/group/bowout";
        URL_IM_DISSOLVE =  URL+ "im/group/dissolve";
        URL_IM_GROUP_LIST =  URL+ "im/group/list";
        URL_IM_CREATE =  URL+ "im/group/create";
        URL_IM_GROUP =  URL+ "im/group/group";
        URL_IM_DEL_MEMBERS =  URL+ "im/group/del_members";
    }

    public static String roomServerName = "@conference.";

    public static String PHONEBRAND = "";
    public static String PHONEVERSION = "";

    public static String productName = "";
    public static String companyName = "";
    public static String companyLogo = "";

    public static boolean isLogin = false;
    public static boolean isExit = false;
    public static boolean isServiceRunning = false;
    public static boolean isGestureOK = false;
    public static boolean isCome = false;

    public static final String KEY_GROUP_TYPE = "key_group_type";
    public static final int CHAT_TYPE_SINGLE = 0;
    public static final int CHAT_TYPE_GROUP = 1;
    public static final int CHAT_TYPE_PUBLIC = 2;
    public static final int CHAT_TYPE_SYSTEM = 3;

    public static final String MARK_UNREAD_IDS = "unReadIds";
    public static final String MARK_READ_IDS = "readIds";

    //TODO anna: 0管理员创建群组 1部门群组 2任务群组 3个人群组 4匿名群组
    public static final int CHAT_TYPE_GROUP_PERSON = 3;
    public static final int CHAT_TYPE_GROUP_ANS = 4;

    public static final int MSG_READ = 1;
    public static final int MSG_UNREAD = 0;

    public static final int MSG_SEND = 1;
    public static final int MSG_RECEIVE = 0;

    //新增MSG_TYPE时，注意不要与下面已定义的重复
    public static final String KEY_MSG_TYPE = "MSG_TYPE";
    public static final String MSG_TYPE_TEXT = "T";
    public static final String MSG_TYPE_AUDIO = "A";
    public static final String MSG_TYPE_VIDEO = "V";//视频
    public static final String MSG_TYPE_PIC = "P";
    public static final String MSG_TYPE_ADMIN = "Z";
    public static final String MSG_TYPE_METTING = "M";
    public static final String MSG_TYPE_FILE_1 = "F1";//手机文件
    public static final String MSG_TYPE_FILE_2 = "F2";//文档管理文件
    public static final String MSG_TYPE_LOCATION = "L";//位置

    public static final String MSG_TYPE_KICK = "K";
    public static final String MSG_TYPE_INVITE = "I";
    public static final String MSG_TYPE_MEMBERS_CHANGE = "MC";
    public static final String MSG_TYPE_DISSOLVE = "S";

    public static final int MSG_SEND_FAIL = 0;
    public static final int MSG_SEND_SUCCESS = 1;
    public static final int MSG_SEND_PROGRESS = 2;
    public static final int MSG_SEND_RESEND = 3;

    public static final String PIC_TEXT = "[图片]";
    public static final String VOICE_TEXT = "[语音]";
    public static final String MEETING_TEXT = "[会议]";
    public static final String VIDEO_TEXT = "[视频]";
    public static final String FILE_TEXT = "[文件]";
    public static final String LOCATION_TEXT = "[位置]";
    public static final String ACTION_ORGUNITION_DONE = "action.orgunition.done";


    public static final String ACTION_MY_KICKED = "action.my.kicked";
    public static final String ACTION_MY_INVITE = "action.my.invite";
    public static final String ACTION_CONTACT_LIST = "action.contact.list";
    public static final String ACTION_HISTORY_MESSAGE_LIST = "action.history.message.list";
    public static final String ACTION_SESSION_MESSAGE_LIST = "action.session.message.list";

    public static final String SET_REDPOINT_ACTION = "new.message.redpoint";

    public static final String ACTION_SET_REDPOINT = "action.set.redpoint";
    public static final String ACTION_NEW_MESSAGE = "action.new.message";
    public static final String ACTION_GROUP_LIST_RESPONSE = "action.group.list.response";//群组列表接口返回

    public static final String MSG_UPDATE_SEND_STATUS_ACTION = "message.update.send.status.receive";

    public static final String NEW_MESSAGE_ACTION = "new.message.receive";
    public static final String MSG_UPDATE_UNREAD_ACTION = "message.update.unread.receive";
    public static final String SIMPLE_LOGIN_ACTION = "simple.login.receive";


    public static final String ACTION_XMPP_LOGIN = "action.xmpp.login";

    public static final String GROUP_ADMIN = "group-admin";
    public static String DEVICE_ID = "";
    public static boolean IS_RUNNING = false;
    public static String IGNORE_CHECK_VERSION_CODE = "ignore_check_version_code";

    /**
     * 服务器的配置
     */
    public static final String LOGIN_SET = "movitech_login_set";// 登录设置
    public static final String USERNAME = "username";// 账户
    public static final String PASSWORD = "password";// 密码
    public static final String IS_AUTOLOGIN = "isAutoLogin";// 是否自动登录
    public static final String IS_REMEMBER = "isRemember";// 是否记住账户密码
    public static final String IS_FIRSTSTART = "isFirstStart";// 是否首次启动
    public static final String IS_SHOW_TOUR = "isshowtour";// 是否首次启动
    public static final String ORIGINAL_VERSION = "originalversion";// 是否首次启动
    public static final String USERID = "userId";
    public static final String CALL_COUNT = "callCount";
    public static final String IS_LEADER = "isLeader";

    public static final String EMPCNAME = "empCname";
    public static final String EMPID = "empId";
    public static final String GENDER = "gender";
    public static final String TOKEN = "openFireToken";

    /**
     * 登录提示
     */
    public static final int LOGIN_SECCESS = 0;// 成功
    public static final int LOGIN_ERROR_ACCOUNT_PASS = 3;// 账号或者密码错
    public static final int SERVER_UNAVAILABLE = 4;// 无法连接到服务器
    public static final int LOGIN_ERROR = 5;// 连接失败
    public static final int LOGIN_FAILE_REASON = 6;// 自定义连接错误提

    public static String URL_EDIT_NAME;//修改群名片
    public static String URL_UPDATE_USER_INFO;//更新用户信息

    public static String URL_IM_ADD_MEMBERS;//添加Group成员
    public static String URL_IM_BOWOUT;//退出Group
    public static String URL_IM_DISSOLVE;//解散Group
    public static String URL_IM_GROUP_LIST;//Group List
    public static String URL_IM_CREATE;// 创建Group
    public static String URL_IM_GROUP;// Group message
    public static String URL_IM_DEL_MEMBERS;// 删除成员
    public static String URL_CHECK_TOKEN;//检查token是否过期

}
