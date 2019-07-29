package com.akaxin.client.bean.event;

import android.os.Bundle;

/**
 * Created by yichao on 2017/12/19.
 */

public class SiteEvent extends BusEvent {

    public static final int SWITCH_SITE_KEY = 1;
    public static final int SWITCH_SITE_KEY_PERSONAL = 2;
    public static final int UPDATE_SITE_INFO = 3;
    public static final int NEW_SITE_KEY = 4;


    public SiteEvent(int action, Bundle bundle) {
        super(action, bundle);
    }
}
