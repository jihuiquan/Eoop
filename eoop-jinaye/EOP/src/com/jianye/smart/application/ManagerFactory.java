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

package com.jianye.smart.application;

import android.content.Context;

import com.movit.platform.common.api.IGroupManager;
import com.movit.platform.common.api.IUserManager;
import com.movit.platform.common.api.IZoneManager;
import com.movit.platform.common.application.IManagerFactory;
import com.movit.platform.im.manager.GroupManager;
import com.movit.platform.sc.module.zone.manager.UserManager;
import com.movit.platform.sc.module.zone.manager.ZoneManager;

/**
 * Created by Louanna.Lu on 2015/11/5.
 */
public class ManagerFactory implements IManagerFactory {

    private Context context;
    private IZoneManager zoneManager;
    private IGroupManager groupManager;
    private IUserManager userManager;

    public ManagerFactory(Context context) {
        this.context = context;
    }

    @Override
    public IZoneManager getZoneManager() {

        if(null!=zoneManager){
            return zoneManager;
        }else{
            zoneManager = new ZoneManager(context);
            return zoneManager;
        }

    }

    @Override
    public IGroupManager getGroupManager() {
        if(null!=groupManager){
            return groupManager;
        }else{
            groupManager =GroupManager.getInstance(context);
            return groupManager;
        }
    }

    @Override
    public IUserManager getUserManager() {
        if(null!=userManager){
            return userManager;
        }else{
            userManager = UserManager.getInstance(context);
            return userManager;
        }
    }
}
