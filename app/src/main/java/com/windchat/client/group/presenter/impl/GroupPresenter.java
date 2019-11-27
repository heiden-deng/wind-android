package com.windchat.client.group.presenter.impl;

import com.windchat.client.ZalyApplication;
import com.windchat.client.api.SiteGroupAPI;
import com.windchat.client.bean.Site;
import com.windchat.client.db.bean.UserGroupBean;
import com.windchat.client.db.dao.SiteGroupProfileDao;
import com.windchat.client.group.presenter.IGroupPresenter;
import com.windchat.client.util.log.ZalyLogUtils;
import com.akaxin.proto.core.GroupProto;
import com.akaxin.proto.core.UserProto;
import com.windchat.im.socket.SiteAddress;

import java.util.List;

/**
 * Created by zhangjun on 2018/3/8.
 */

public class GroupPresenter implements IGroupPresenter {
    private static final String TAG = GroupPresenter.class.getSimpleName();
    private Site currentSite;

    private GroupPresenter(Site site) {
        this.currentSite = site;
    }

    public static GroupPresenter getInstance(Site site) {
        return new GroupPresenter(site);
    }

    @Override
    public void batchInsertGroup(List<GroupProto.SimpleGroupProfile> simpleGroupProfiles) {
//        SiteAddress siteAddress = getCurrentSite();
//        SiteGroupProfileDao.getInstance(siteAddress).insertGroupsWithClear(simpleGroupProfiles);
    }

    public SiteAddress getCurrentSite() {
        return ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort());
    }


    @Override
    public void updateGroupOwnerProfile(String siteGroupId, UserProto.UserProfile ownerProfile, int numGroupMembers, boolean closeInviteGroupChat) {
        SiteAddress siteAddress = getCurrentSite();
        SiteGroupProfileDao.getInstance(siteAddress).updateSiteGroupProfile(siteGroupId, ownerProfile, numGroupMembers, closeInviteGroupChat);
    }

    @Override
    public UserGroupBean getGroupBeanByGroupId(String siteGroupId, Site site) {
        try {
            SiteAddress siteAddressObj = ZalyApplication.getSiteAddressObj(site.getHostAndPort());

            return SiteGroupAPI.getInstance(site).getGroupBeanProfile(siteAddressObj, siteGroupId);
        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, " site Group id is " + siteGroupId + " msg is :" + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean updateGroupMute(String siteGroupId, boolean isMute){
        SiteAddress siteAddress = getCurrentSite();
        return SiteGroupProfileDao.getInstance(siteAddress).updateGroupMute(siteGroupId, isMute);
    }

    @Override
    public UserGroupBean getGroupBean(String siteGroupid) {
        SiteAddress siteAddress = getCurrentSite();
        return SiteGroupProfileDao.getInstance(siteAddress).queryGroupBeanByGroupId(siteGroupid);
    }

    @Override
    public Long insertGroupProfile(UserGroupBean userGroupBean){
        SiteAddress siteAddress = getCurrentSite();
        return SiteGroupProfileDao.getInstance(siteAddress).insertSiteGroupProfile(userGroupBean);
    }

    @Override
    public Long createGroupSimpleProfile(UserGroupBean bean){
        SiteAddress siteAddress = getCurrentSite();
        return SiteGroupProfileDao.getInstance(siteAddress).createSiteSimpleGroupProfile(bean);
    }

}
