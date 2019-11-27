package com.windchat.client.mvp.presenter;

import com.windchat.client.bean.ChatSession;
import com.windchat.client.bean.Site;
import com.windchat.client.mvp.BasePresenterImpl;
import com.windchat.client.mvp.contract.SessionContract;
import com.windchat.client.site.presenter.impl.SitePresenter;
import com.windchat.client.util.task.ZalyTaskExecutor;

import java.util.List;

/**
 * Created by Mr.kk on 2018/7/4.
 * This Project was client-android
 */

public class SessionPresenter extends BasePresenterImpl<SessionContract.View> implements SessionContract.Presenter {
    @Override
    public void loadChatSession(final Site currentSite) {
        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, List<ChatSession>>() {
            @Override
            protected List<ChatSession> executeTask(Void... voids) throws Exception {
                return SitePresenter.getInstance().getChatSessionList(currentSite.getHostAndPort());
            }

            @Override
            protected void onTaskSuccess(List<ChatSession> chatSessions) {
                super.onTaskSuccess(chatSessions);
                mView.onLoadChatSessionSuccess(chatSessions);
            }

            @Override
            protected void onTaskError(Exception e) {
                super.onTaskError(e);
                mView.onLoadChatSessionError();
            }

        });
    }
}
