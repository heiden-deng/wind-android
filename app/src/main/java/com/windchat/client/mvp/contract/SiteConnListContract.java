package com.windchat.client.mvp.contract;

import android.content.Context;

import com.windchat.client.bean.Site;
import com.windchat.client.mvp.BasePresenter;
import com.windchat.client.mvp.BaseView;

import java.util.List;

/**
 * Created by Mr.kk on 2018/7/2.
 * This Project was client-android
 */

public class SiteConnListContract {
    public interface View extends BaseView {
        void onTaskStart(String content);

        void onTaskFinish();

        void onGetSiteConfigSuccess(Site site);

        void onTaskError();

        void onLoginSiteError();

        void onLoginSiteNeedRegister(Site site);

        void onLoginSiteSuccess(Site site);

        void onConnAndLoginSuccess(Site site);

        void onSwitchSiteSuccess(Site currentSite);

        void onGetSitesSuccess(List<Site> sites);
    }

    public interface Presenter extends BasePresenter<View> {
        void getSiteConfig(Context mContext, String siteAddress, final Site currentSite);

        void getPlatformToken(Site site);

        void loginSite(String userSignBase64, String deviceSignBase64, String userToken, Site site);

        void addSiteAndChangeIdentity(Site site);

        void switchSite(Site site, Site currentSite);

        void loadCurrentSites(boolean needUnreadNum);
    }
}
