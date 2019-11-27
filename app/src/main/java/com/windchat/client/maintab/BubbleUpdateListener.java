package com.windchat.client.maintab;

/**
 * Created by alexfan on 2018/4/4.
 */

public interface BubbleUpdateListener {

    void onSessionBubbleChange(int unreadNum);
    void onContactBubbleChange(boolean unread);
}
