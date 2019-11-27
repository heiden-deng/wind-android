package com.windchat.client.bean.event;

import android.os.Bundle;

/**
 * Created by yichao on 2018/1/20.
 */

public class LoginEvent extends BusEvent {
    public LoginEvent(int action, Bundle bundle) {
        super(action, bundle);
    }
}
