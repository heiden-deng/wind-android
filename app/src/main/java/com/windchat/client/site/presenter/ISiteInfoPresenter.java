package com.windchat.client.site.presenter;

import android.net.Uri;

import com.windchat.client.bean.Site;

/**
 * Created by alexfan on 2018/4/13.
 */

public interface ISiteInfoPresenter {
    Site getSite();
    void setSite(Site site);

    void getPlatformSiteSetting();
    void updateSiteMute(boolean mute);
    void connectSite();
    void disconnectSite();
    void delSite();


    void updateUserImage(Uri uri);
    void updateUsername(String username);
    void updateSiteLoginId(String siteLoginId);
}
