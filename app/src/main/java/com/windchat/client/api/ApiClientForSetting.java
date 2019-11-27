package com.windchat.client.api;

import com.windchat.client.bean.Site;
import com.windchat.im.socket.TransportPackageForResponse;

/**
 * Created by Mr.kk on 2018/6/14.
 * This Project was client-android
 */

public class ApiClientForSetting {
    private ApiClient client = null;
    private String logTag = "ApiClientForFriend";


    private static final String API_SETTING_SITE_MUTE = "api.setting.siteMute";
    private static final String API_SETTING_UPDATE_SITE_MUTE = "api.setting.updateSiteMute";
    private static final String API_SETTING_DELETE_USER_TOKEN = "api.setting.deleteUserToken";

    public ApiClientForSetting(ApiClient client) {
        this.client = client;
        this.logTag = "ApiClient." + this.getClass().getName();
    }

}
