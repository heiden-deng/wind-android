package com.windchat.client.site.presenter.impl;

import com.windchat.client.bean.User;
import com.windchat.client.site.presenter.IPlatformPresenter;

/**
 * Created by zhangjun on 2018/3/12.
 */

public class PlatformPresenter implements IPlatformPresenter {

    public final String TAG = "PlatformPresenter";


    public static PlatformPresenter getInstance() {
        return PlatformPresenter.SingletonHolder.instance;
    }

    static class SingletonHolder {
        private static PlatformPresenter instance = new PlatformPresenter();
    }

    @Override
    public String getPlatformSessionId() {
        User user = SitePresenter.getInstance().getUserIdentity();
        if (user != null) {
            return user.getPlatformSessionId();
        }
        return null;
    }

}
