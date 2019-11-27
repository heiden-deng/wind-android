package com.windchat.client.site.view;

import com.windchat.client.bean.Site;

/**
 * Created by alexfan on 2018/4/13.
 */

public interface ISiteInfoView {

    void updateNotificationSwitch(boolean mute);

    void onUpdateUserProfileStart();
    void onUpdateUserProfileSuccess();
    void onUpdateUserProfileError();

    void onGetPlatformSiteSettingStart();
    void onGetPlatformSiteSettingSuccess(boolean mute);
    void onGetPlatformSiteSettingError();

    void onUpdateSiteSettingStart();
    void onUpdateSiteSettingSuccess();
    void onUpdateSiteSettingError(boolean originalMuteStatus);

    void onDisconnectStart();
    void onDisconnectSuccess();
    void onDisconnectError();

    void onConnectStart();
    void onConnectSuccess();
    void onConnectError();

    void onDelSiteStart();
    void onDelSiteSuccessAtCurrentSite(Site deleted, Site toSite);
    void onDelSiteSuccessAtAnotherSite(Site deleted);
    void onDelSiteError();
}
