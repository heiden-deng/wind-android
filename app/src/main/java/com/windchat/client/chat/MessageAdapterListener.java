package com.windchat.client.chat;

import com.windchat.client.bean.Message;

/**
 * Created by yichao on 2017/10/10.
 */

public interface MessageAdapterListener {
    void onMessageClick(int type, Message message);
}
