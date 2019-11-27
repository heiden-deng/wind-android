package com.windchat.client.util.log;

import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogAdapter;

/**
 * Created by yichao on 2017/10/14.
 */

public class WuerLogAdapter implements LogAdapter {

    private final FormatStrategy formatStrategy;

    public WuerLogAdapter() {
        this.formatStrategy = WuerFormatStrategy.newBuilder().build();
    }

    @Override
    public boolean isLoggable(int priority, String tag) {
        return true;
    }

    @Override
    public void log(int priority, String tag, String message) {
        formatStrategy.log(priority, tag, message);
    }
}
