package com.akaxin.client.mvp.contract;

import com.akaxin.client.bean.ChatSession;
import com.akaxin.client.bean.Site;
import com.akaxin.client.mvp.BasePresenter;
import com.akaxin.client.mvp.BaseView;

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
