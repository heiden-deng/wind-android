package com.akaxin.client.site.task;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.db.bean.UserFriendBean;
import com.akaxin.client.friend.presenter.impl.UserProfilePresenter;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.site.ApiUserProfileProto;

/**
 * Created by zhangjun on 2018/3/15.
 */

/**
 * 获取用户信息
 */
public class ApiUserProfileTask extends ZalyTaskExecutor.Task<Void, Void, ApiUserProfileProto.ApiUserProfileResponse> {
    public final String TAG = ApiUserProfileTask.class.getSimpleName();

    private String siteUserId;
    private String siteAddress;
    private Site site;

    public ApiUserProfileTask(Site site) {
        this.site = site;
        this.siteUserId = site.getSiteUserId();
        this.siteAddress = site.getHostAndPort();
    }

    @Override
    protected ApiUserProfileProto.ApiUserProfileResponse executeTask(Void... voids) throws Exception {
        ApiUserProfileProto.ApiUserProfileResponse response = ApiClient.getInstance(site).getUserApi().getProfile(siteUserId);
        UserProto.UserProfile profile = response.getUserProfile();
        if (response != null) {
            //TODO DBChange 更新站点用户的信息, 同时更新当前好友profile，里面的个人用户信息
            site.setSiteUserImage(profile.getUserPhoto());
            site.setSiteLoginId(profile.getSiteLoginId());
            site.setSiteUserName(profile.getUserName());
            SitePresenter.getInstance().updateSiteUserInfo(site);

            UserFriendBean userFriendBean = new UserFriendBean();
            userFriendBean.setSiteUserId(profile.getSiteUserId());
            userFriendBean.setUserName(profile.getUserName());
            userFriendBean.setUserImage(profile.getUserPhoto());
            userFriendBean.setUserIdPubk(ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY));
            userFriendBean.setSiteLoginId(profile.getSiteLoginId());
            userFriendBean.setSiteNickName(profile.getNickName());
            UserProfilePresenter.getInstance(site).updateSiteUserProfile(userFriendBean, siteAddress);
        }
        return response;
    }

    @Override
    protected void onTaskError(Exception e) {
        ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
        super.platformLoginByError(e);
        ZalyLogUtils.getInstance().exceptionError(e);

    }

    @Override
    public void onAPIError(ZalyAPIException zalyAPIException) {
        ZalyLogUtils.getInstance().errorToInfo(TAG, zalyAPIException.getMessage());
        super.platformLoginByApiError(zalyAPIException);
        ZalyLogUtils.getInstance().exceptionError(zalyAPIException);
    }

    @Override
    protected void onTaskSuccess(ApiUserProfileProto.ApiUserProfileResponse response) {
        super.onTaskSuccess(response);
        ZalyApplication.setCurProfile(response.getUserProfile());
    }
}