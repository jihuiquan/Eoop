package com.movit.platform.common.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import com.androidquery.util.AQUtility;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.SharedPreUtils;
import com.movit.platform.framework.utils.StringUtils;

import java.io.File;

/**
 * Created by Louanna.Lu on 2015/10/10.
 */
public class ConfigUtils {

    public static void initConfigInfo(Context context){

        String xmppIp = "";
        String ip = "";
        String port = "";
        String cmsPort = "";
        String type = "";
        try {
            SharedPreUtils spUtil = new SharedPreUtils(context);
            ip = spUtil.getString("ip");
            if (StringUtils.empty(ip)) {
                ip = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                        PackageManager.GET_META_DATA).metaData
                        .getString("CHANNEL_IP");
                port = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                        PackageManager.GET_META_DATA).metaData
                        .getString("CHANNEL_PORT");
                cmsPort = context.getPackageManager().getApplicationInfo(
                        context.getPackageName(), PackageManager.GET_META_DATA).metaData
                        .getString("CHANNEL_CMS_PORT");

                spUtil.setString("ip", ip);
                spUtil.setString("port", port);
                spUtil.setString("cmsPort", cmsPort);
            }

            xmppIp = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA).metaData
                    .getString("CHANNEL_XMPP_IP");

            spUtil.setString("xmppIp", xmppIp);

            type = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA).metaData
                    .getString("CHANNEL_TYPE");

            CommConstants.HOST_TYPE = type;
            CommConstants.ORG_TREE = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA).metaData
                    .getBoolean("CHANNEL_ORG_TREE");
        } catch (Exception e) {
            e.printStackTrace();
        }

        CommConstants.initHost(context);
        File cacheDir = new File(CommConstants.SD_DATA, "pic");
        AQUtility.setCacheDir(cacheDir);
        AQUtility.setDebug(true);
    }
}
