package com.windchat.client.api;

import com.windchat.client.bean.Site;

import com.windchat.client.bean.event.AppEvent;
import com.windchat.client.db.bean.UserGroupBean;
import com.windchat.client.db.dao.SiteGroupProfileDao;
import com.windchat.client.util.NetUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.site.ApiGroupProfileProto;
import com.akaxin.proto.site.ApiGroupSettingProto;
import com.windchat.im.socket.SiteAddress;

import org.greenrobot.eventbus.EventBus;

/**
 * <pre>
 * 替代旧的获取用户资料的方式：
 *  1.直接从网络获取
 *  2.直接从数据库获取
 *  3.先从数据库获取，若无->从网络获取->填充数据库
 *
 *  SiteGroupAPI，使用3。
 * </pre>
 * <p>
 * Created by anguoyue on 21/03/2018.
 */

public class SiteGroupAPI {
    private static final String TAG = SiteGroupAPI.class.getSimpleName();
    private static Site currentSite;

    private SiteGroupAPI(Site site) {
        currentSite = site;
    }

    public static SiteGroupAPI getInstance(Site site) {
        return new SiteGroupAPI(site);
    }

    /**
     * 获取simple-group-profile
     *
     * @param address
     * @param siteGroupid
     * @return
     * @throws Exception
     */
    public UserGroupBean getGroupBeanProfile(SiteAddress address, String siteGroupid) throws Exception {
        //1.同步数据库获取
        UserGroupBean simpleGroupBean = SiteGroupProfileDao.getInstance(address).queryGroupBeanByGroupId(siteGroupid);

        if (simpleGroupBean != null && simpleGroupBean.getGroupId() != null) {
            return simpleGroupBean;
        }
        boolean isNet = NetUtils.getNetInfo();
        if (isNet) {
            //2.异步同步网络，并会写数据库
            ZalyTaskExecutor.executeTask(TAG, new ReactGroupProfileFromNetToDBTask(address, siteGroupid));
        }

        return null;
    }


    //异步task，groupprofile入库
    private class ReactGroupProfileFromNetToDBTask extends ZalyTaskExecutor.Task<Void, Void, UserGroupBean> {

        private SiteAddress address;
        private String siteGroupId;

        public ReactGroupProfileFromNetToDBTask(SiteAddress address, String siteGroupId) {
            this.address = address;
            this.siteGroupId = siteGroupId;
            ZalyLogUtils.getInstance().info(TAG, "group id is " + siteGroupId);
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
        }

        @Override
        protected UserGroupBean executeTask(Void... voids) throws Exception {
            //query group profile from net
            ApiGroupProfileProto.ApiGroupProfileResponse profileResponse = ApiClient.getInstance(currentSite).getGroupApi().getGroupProfile(siteGroupId);
            if (profileResponse != null && profileResponse.getProfile() != null) {

                UserGroupBean bean = new UserGroupBean();
                bean.setGroupId(profileResponse.getProfile().getId());
                bean.setGroupName(profileResponse.getProfile().getName());
                bean.setGroupImage(profileResponse.getProfile().getIcon());
                bean.setCloseInviteGroupChat(profileResponse.getCloseInviteGroupChat());
                bean.setAsGroupMember(true);

                boolean closeInviteGroupChat = profileResponse.getCloseInviteGroupChat();
                int numGroupMembers = profileResponse.getGroupMemberCount();
                bean.setCloseInviteGroupChat(closeInviteGroupChat);
                bean.setGroupCountMember(numGroupMembers);
                if (profileResponse.getOwner() != null) {
                    bean.setGroupOwnerId(profileResponse.getOwner().getSiteUserId());
                    bean.setGroupOwnerName(profileResponse.getOwner().getUserName());
                    bean.setGroupOwnerIcon(profileResponse.getOwner().getUserPhoto());
                }

                try {
                    ApiGroupSettingProto.ApiGroupSettingResponse settingResponse = ApiClient.getInstance(currentSite).getGroupApi().getGroupSetting(siteGroupId);
                    if (settingResponse != null) {
                        bean.setMute(settingResponse.getMessageMute());
                    }
                } catch (Exception e) {
                    bean.setMute(false);
                    ZalyLogUtils.getInstance().exceptionError(e);
                }

                bean.setLatestTime(System.currentTimeMillis());
                SiteGroupProfileDao.getInstance(address).insertSiteGroupProfile(bean);
                return bean;
            }

            return null;
        }

        @Override
        protected void onTaskSuccess(UserGroupBean bean) {
            EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_UPDATE_SESSION_LIST, null));
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            ZalyLogUtils.getInstance().exceptionError(zalyAPIException);
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
        }

    }

}
