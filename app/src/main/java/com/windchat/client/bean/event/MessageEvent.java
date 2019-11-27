package com.windchat.client.bean.event;

import android.os.Bundle;

/**
 * Created by yichao on 2017/10/19.
 */

public class MessageEvent extends BusEvent{

    public MessageEvent(int action, Bundle bundle) {
        super(action, bundle);
    }
}
