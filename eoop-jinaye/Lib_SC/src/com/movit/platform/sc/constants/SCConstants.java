package com.movit.platform.sc.constants;

import com.movit.platform.common.constants.CommConstants;

/**
 * Created by Louanna.Lu on 2015/11/10.
 */
public class SCConstants {

    private final static String URL_SC = "http://" + CommConstants.URL_API + CommConstants.HOST_PORT + "/eoop-sc/sc/rest/";

    public final static String GET_ZONE_LIST_DATA = URL_SC + "getdata";

    public final static String PUBLISH_ZONE_SAY = URL_SC + "say";

    public final static String GET_ZONE_SAY = URL_SC + "getsay";

    public final static String GET_MINE_SAY_COUNT = URL_SC + "mysaycount";

    public final static String GET_MINE_SAY_LIST = URL_SC + "mysaylist";

    public final static String DELETE_MINE_SAY = URL_SC + "saydel";

    public final static String ZONE_NICE = URL_SC + "nice";

    public final static String ZONE_COMMENT = URL_SC + "comment";

    public final static String DELETE_COMMENT = URL_SC + "commentdel";

    public final static String ZONE_NEW_MESSAGE = URL_SC + "havenew";

    public final static String ZONE_MESSAGE = URL_SC + "messages";

    public final static String ZONE_MESSAGE_COUNT = URL_SC + "messagecount";

    public final static String ZONE_MESSAGE_DELETE = URL_SC + "messagedel";

    public static final String RECORD_ACCESS_PERSON_INFO = URL_SC + "recordAccessPersonInfo";
}
