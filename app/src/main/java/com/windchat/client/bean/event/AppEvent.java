package com.windchat.client.bean.event;

import android.os.Bundle;

/**
 * Created by Mr.kk on 2018/6/27.
 * This Project was client-android
 */

public class AppEvent extends BusEvent {
    public static final int LOGIN_PLATFORM_SUCCESS = 1;
    public static final int ERROR_SESSION = 2;
    public static final int SET_SITE_LOGIN_ID = 3;
    public static final int ACTION_UPDATE_SESSION_LIST = 4;
    public static final int ACTION_UPDATE_PLUGIN_LIST = 5;
    public static final int ACTION_UPDATE_MAIN_SESSION_TAB_BUBBLE = 6;
    public static final String KEY_CURRENT_SITE_TOTAL_UNREAD_NUM = "key_current_site_total_unread_num";

    public static final int ACTION_NEW_FRIEND = 7;
    public static final int ACTION_RELOAD = 8;
    public static final int ACTION_SWITCH_SITE = 9;

    public static final int ACTION_TO_TOP_APP_WEB = 10;
    public static final int NO_PLUGIN = 11;

    public AppEvent(int action, Bundle bundle) {
        super(action, bundle);
    }
}
