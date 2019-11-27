package com.windchat.client.mvp.contract;

import com.windchat.client.bean.ChatSession;
import com.windchat.client.bean.Site;
import com.windchat.client.mvp.BasePresenter;
import com.windchat.client.mvp.BaseView;

import java.util.List;

/**
 * Created by Mr.kk on 2018/7/4.
 * This Project was client-android
 */

public class SessionContract {

    public interface View extends BaseView {

        void onTaskStart(String content);

        void onTaskFinish();

        void onLoadChatSessionSuccess(List<ChatSession> chatSessions);

        void onLoadChatSessionError();
    }

    public interface Presenter extends BasePresenter<SessionContract.View> {
        void loadChatSession(Site currentSite);
    }
}
