package com.akaxin.client.register.presenter;

import com.akaxin.client.bean.Site;
import com.akaxin.proto.site.ApiSiteRegisterProto;

public interface ILoginSitePresenter {

    void loadCurrentSites(boolean needUnreadNum);

    void tryLogin(String siteAddress);

    void loginOrRegisterSite(Site site);
}
