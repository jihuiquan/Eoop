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

package com.movit.platform.common.application;

import android.app.Application;
import com.movit.platform.common.utils.ConfigUtils;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.helper.MFSPHelper;
import java.util.concurrent.TimeUnit;

/**
 * Created by Louanna.Lu on 2015/10/30.
 */
public class BaseApplication extends Application {

    private UIController uiController;
    private IManagerFactory managerFactory;
    public static final String SKINTYPE = "skinType";

    protected static Application myApplication;

    public static String TOP_COLOR = " ";
    public static String Token = "";

    @Override
    public void onCreate() {
        super.onCreate();
        MFSPHelper.initialize(getApplicationContext());
        //初始化 IM & SC 配置信息
        ConfigUtils.initConfigInfo(this);

        OkHttpUtils.getInstance().debug("OkHttpUtils").setConnectTimeout(100000, TimeUnit.MILLISECONDS);
        //使用https，但是默认信任全部证书
        OkHttpUtils.getInstance().setCertificates();
    }

    public UIController getUIController() {
        return uiController;
    }

    public void setUIController(UIController uiController) {
        this.uiController = uiController;
    }

    public IManagerFactory getManagerFactory() {
        return managerFactory;
    }

    public void setManagerFactory(IManagerFactory managerFactory) {
        this.managerFactory = managerFactory;
    }

    public synchronized static Application getInstance() {
        return myApplication;
    }

}
