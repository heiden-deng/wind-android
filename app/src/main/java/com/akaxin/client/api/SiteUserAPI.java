package com.akaxin.client.api;

import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bean.event.AppEvent;
import com.akaxin.client.db.bean.UserFriendBean;
import com.akaxin.client.db.dao.SiteUserProfileDao;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.site.ApiFriendProfileProto;
import com.akaxin.proto.site.ApiFriendSettingProto;
import com.windchat.im.socket.SiteAddress;

import org.greenrobot.eventbus.EventBus;

/**
 * <pre>
 * 替代旧的获取用户资料的方式：
 *  1.直接从网络获取
 *  2.直接从数据库获取
 *  3.先从数据库获取，若无->从网络获取->填充数据库
 *  4.先从数据库中获取，同时从网络获取->填充数据库
 *
 *  SiteUserAPI，使用3,4
 * </pre>
 */

public class SiteUserAPI {
    private static final String TAG = SiteUserAPI.class.getSimpleName();

    private SiteUserAPI() {

    }

    public static SiteUserAPI getInstance() {
        return SingletonHolder.instance;
    }

    static class SingletonHolder {
        private static SiteUserAPI instance = new SiteUserAPI();
    }


    /**
     * 获取用户的simpleUserProfile
     *
     * @param site
     * @param siteUserId
     * @return
     */
    public UserProto.SimpleUserProfile getSimpleUserProfile(final Site site, final String siteUserId) throws Exception {
        //query from db
        final SiteAddress address = site.getSiteAddress();
        UserProto.SimpleUserProfile simpleUserProfile = SiteUserProfileDao.getInstance(address).queryFriend(siteUserId);
        if (simpleUserProfile != null && StringUtils.isNotEmpty(simpleUserProfile.getSiteUserId())) {
            return simpleUserProfile;
        }
        ZalyLogUtils.getInstance().info(TAG, " getSimpleUserProfile site ==" + site.toString());
        ZalyLogUtils.getInstance().info(TAG, " getSimpleUserProfile  site address ==" + address);
        ZalyLogUtils.getInstance().info(TAG, " getSimpleUserProfile site siteUserId ==" + siteUserId);

        boolean isNet = NetUtils.getNetInfo();
        if (isNet) {
            //2.从网络获取，回写数据库
            ZalyTaskExecutor.executeTask(TAG, new ReactUserProfileFromNetToDBTask(site, address, siteUserId));
        }
        return null;
    }

    //获取用户自身的profile
    public UserFriendBean getFriendProfile(Site site, SiteAddress address, String siteUserId) throws Exception {
        //1.从库里获取信息
        UserFriendBean bean = SiteUserProfileDao.getInstance(address).queryUserById(siteUserId);
        if (bean != null && StringUtils.isNotEmpty(bean.getSiteUserId())) {
            return bean;
        }
        boolean isNet = NetUtils.getNetInfo();
        if (isNet) {
            //2.从网络获取，回写数据库
            ZalyLogUtils.getInstance().info(TAG, " getFriendProfile site ==" + site.toString());
            ZalyLogUtils.getInstance().info(TAG, " getFriendProfile  site address ==" + address);
            ZalyLogUtils.getInstance().info(TAG, " getFriendProfile site siteUserId ==" + siteUserId);

            ZalyTaskExecutor.executeTask(TAG, new ReactUserProfileFromNetToDBTask(site, address, siteUserId));
        }
        return null;
    }

    //异步task，userProfile入库
    private class ReactUserProfileFromNetToDBTask extends ZalyTaskExecutor.Task<Void, Void, UserFriendBean> {

        private SiteAddress address;
        private String siteUserId;
        private Site site;

        public ReactUserProfileFromNetToDBTask(Site site, SiteAddress address, String siteUserId) {
            this.address = address;
            this.siteUserId = siteUserId;
            this.site = site;
            ZalyLogUtils.getInstance().info(TAG, " ReactUserProfileFromNetToDBTask site ==" + site.toString());
            ZalyLogUtils.getInstance().info(TAG, " ReactUserProfileFromNetToDBTask  site address ==" + address);
            ZalyLogUtils.getInstance().info(TAG, " ReactUserProfileFromNetToDBTask site siteUserId ==" + siteUserId);

        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
        }

        @Override
        protected UserFriendBean executeTask(Void... voids) throws Exception {
            //query from site-server
            //查网络信息
            ApiFriendProfileProto.ApiFriendProfileResponse profileRes = ApiClient.getInstance(site).getFriendApi().findUser(siteUserId);
            if (profileRes != null && profileRes.getProfile() != null) {

                //save data to db for next time to query
                UserFriendBean bean = new UserFriendBean();
                bean.setSiteUserId(siteUserId);
                bean.setUserName(profileRes.getProfile().getUserName());
                bean.setUserImage(profileRes.getProfile().getUserPhoto());
                bean.setUserIdPubk(profileRes.getUserIdPubk());
                bean.setRelation(profileRes.getRelationValue());
                bean.setSiteLoginId(profileRes.getProfile().getSiteLoginId());

                ZalyLogUtils.getInstance().info(TAG, " site login id is " + profileRes.getProfile().getSiteLoginId());
                try {
                    ApiFriendSettingProto.ApiFriendSettingResponse settingRes = ApiClient.getInstance(site).getFriendApi().getFriendSetting(siteUserId);
                    if (settingRes != null) {
                        bean.setMute(settingRes.getMessageMute());
                    }

                } catch (Exception e) {
                    bean.setMute(false);
                    ZalyLogUtils.getInstance().exceptionError(e);
                }
                //add data to db
                SiteUserProfileDao.getInstance(address).insertSiteUserProfile(bean);
                return bean;
            }

            return null;
        }

        @Override
        protected void onTaskSuccess(UserFriendBean bean) {
            EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_UPDATE_SESSION_LIST, null));
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);

            super.platformLoginByError(e);
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            ZalyLogUtils.getInstance().exceptionError(zalyAPIException);
            super.platformLoginByApiError(zalyAPIException);
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
        }

    }
}
