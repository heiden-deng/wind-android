package com.windchat.client.bean.event;

import android.os.Bundle;

/**
 * Created by yichao on 2017/10/26.
 */

public class GroupEvent extends BusEvent{

    public static final int ACTION_DEL_GROUP = 1;
    public static final int ACTION_QUIT_GROUP = 2;

    public static String KEY_GROUP_ID = "key_group_id";

    public GroupEvent(int action, Bundle bundle) {
        super(action, bundle);
    }
}
