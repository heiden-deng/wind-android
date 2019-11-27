package com.windchat.client.register.presenter;

import com.windchat.client.bean.Site;

public interface ILoginSitePresenter {

    void loadCurrentSites(boolean needUnreadNum);

    void tryLogin(String siteAddress);

    void loginOrRegisterSite(Site site);
}
