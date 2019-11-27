package com.windchat.client.bean.event;

import android.os.Bundle;

/**
 * Created by yichao on 2017/10/30.
 */

public class BusEvent {
    protected int action;
    protected Bundle data;

    public BusEvent(int action, Bundle bundle) {
        this.action = action;
        this.data = bundle;
    }

    public int getAction() {
        return action;
    }

    public Bundle getData() {
        return data;
    }
}
