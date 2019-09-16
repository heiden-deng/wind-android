package com.akaxin.client.site.presenter.impl;

import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.ChatSession;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bean.User;
import com.akaxin.client.db.dao.AkxCommonDao;
import com.akaxin.client.db.dao.SiteChatSessionDao;
import com.akaxin.client.db.dao.SiteGroupProfileDao;
import com.akaxin.client.db.dao.SiteMessageDao;
import com.akaxin.client.db.dao.SiteUserProfileDao;
import com.akaxin.client.db.helper.AkxDBManager;
import com.akaxin.client.site.presenter.ISitePresenter;
import com.akaxin.client.site.task.AddSiteTask;
import com.akaxin.client.site.task.ApiUserProfileTask;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.orhanobut.logger.Logger;
import com.windchat.im.socket.SiteAddress;

import java.util.Date;
import java.util.List;

import static com.akaxin.client.bean.Site.STATUS_SITE_ONLINE;


/**
 * Created by zhangjun on 2018/3/7.
 */

public class SitePresenter implements ISitePresenter {
    public static final String TAG = SitePresenter.class.getSimpleName();

    private SitePresenter() {

    }

    public static SitePresenter getInstance() {
        return SitePresenter.SingletonHolder.instance;
    }

    static class SingletonHolder {
        private static SitePresenter instance = new SitePresenter();
    }


    /**
     * 查询数据库中站点信息
     *
     * @param host
     * @param port
     * @return
     */
    @Override
    public Site getSiteByHostAndPort(String host, String port) {
        return AkxCommonDao.getInstance().querySiteByHostAndPort(host, port);
    }


    /**
     * 插入用户的身份
     */
    @Override
    public Long insertUserIdentity(User user) {
        try {
            checkCommonUserIdentityTable();
            return AkxCommonDao.getInstance().insertUserIdentity(user);
        } catch (Exception e) {
            Logger.e(e);
        }
        return null;
    }

    /**
     * 插入用户的身份
     */
    @Override
    public int delUserIdentity() {
        try {
            checkCommonUserIdentityTable();
            return AkxCommonDao.getInstance().delUserIdentity();
        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
        }
        return User.DEL_USER_FAILED;
    }


    @Override
    public boolean insertSiteAndUserIdentity(Site site, String globalUserId, String generateIndentityType) {

        try {
            User user = new User();
            user.setGlobalUserId(globalUserId);
            user.setIdentityName(site.getSiteUserName());
            user.setIdentitySource(generateIndentityType);
            AkxCommonDao.getInstance().insertUserIdentity(user);

            ZalyTaskExecutor.executeUserTask(TAG, new ApiUserProfileTask(site));

            return true;
        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean insertSite(Site site, String siteUserId, String siteUserSessionId) {
        try {
            site.setSiteUserId(siteUserId);
            site.setLastLoginTime(new Date().getTime());
            site.setSiteStatus(STATUS_SITE_ONLINE);
            site.setSiteSessionId(siteUserSessionId);
            site.setSiteUserImage("");
            ZalyTaskExecutor.executeUserTask(TAG, new AddSiteTask(site));
            return true;
        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            return false;
        }
    }


    @Override
    public void checkCommonBaseTable() {
        AkxDBManager.getCommonDBHelper(ZalyApplication.getContext()).checkBaseTable();
    }

    private void checkCommonSiteTable() {
        AkxDBManager.getCommonDBHelper(ZalyApplication.getContext()).checkBaseTable();
    }


    private void checkCommonUserIdentityTable() {
        AkxDBManager.getCommonDBHelper(ZalyApplication.getContext()).checkBaseTable();
    }

    @Override
    public void checkSiteBaseTable(String siteAddress) {
        AkxDBManager.getSiteDBHelper(ZalyApplication.getContext(), ZalyApplication.getSiteAddressObj(siteAddress)).checkSiteTable();
    }

    @Override
    public void dropCommonBaseTable() {
        AkxDBManager.getCommonDBHelper(ZalyApplication.getContext()).dropBaseTable();
    }

    @Override
    public void dropSiteBaseTable(SiteAddress siteAddress) {
        AkxDBManager.getSiteDBHelper(ZalyApplication.getContext(), siteAddress).dropSiteTable();
    }

    @Override
    public void updateUserPlatformSessionId(String siteUserId, String userSessionId) {
        AkxCommonDao.getInstance().updateUserPlatformSessionId(siteUserId, userSessionId);
    }

    @Override
    public int updateUserSiteSessionId(String siteUserId, String userSessionId) {
        return AkxCommonDao.getInstance().updateUserSiteSessionId(siteUserId, userSessionId);
    }

    @Override
    public List<Site> getAllSiteLists() {
        return AkxCommonDao.getInstance().queryAllSite(false);
    }

    @Override
    public List<Site> getAllSiteLists(Boolean needUnreadNum) {
        return AkxCommonDao.getInstance().queryAllSite(needUnreadNum);
    }

    @Override
    public void updateSiteUserInfo(Site site) {
        AkxCommonDao.getInstance().updateSiteUserInfo(site.getSiteHost(), site.getSitePort() + "", site.getSiteUserName(), site.getSiteUserImage(), site.getSiteLoginId());
    }

    @Override
    public void updateSiteInfo(Site site) {
        AkxCommonDao.getInstance().updateSiteInfo(site);
    }

    @Override
    public List<ChatSession> getChatSessionList(String siteAddress) {
        SiteAddress address = ZalyApplication.getSiteAddressObj(siteAddress);
        return SiteChatSessionDao.getInstance(address).queryChatSessions();
    }

    @Override
    public boolean insertChatSession(String siteAddress, ChatSession chatSession) {
        SiteAddress address = ZalyApplication.getSiteAddressObj(siteAddress);
        SiteChatSessionDao.getInstance(address).insertChatSession(chatSession);
        return false;
    }

    @Override
    public long getChatSessionByChatSessionId(String siteAddress, String chatSessionId) {
        SiteAddress address = ZalyApplication.getSiteAddressObj(siteAddress);
        return SiteChatSessionDao.getInstance(address).queryChatSessionByChatSessionId(chatSessionId);
    }

    @Override
    public void deleteCommonDB() {
        AkxCommonDao.getInstance().removeDaoObject();
        AkxDBManager.deleteCommonDb(ZalyApplication.getContext());
    }

    @Override
    public void deleteSiteDB(SiteAddress siteAddress) {
        SiteChatSessionDao.getInstance(siteAddress).removeChatSessionDaoMap(siteAddress);
        SiteMessageDao.getInstance(siteAddress).removeMessageDaoMap(siteAddress);
        SiteGroupProfileDao.getInstance(siteAddress).removeGroupProfileDaoMap(siteAddress);
        SiteUserProfileDao.getInstance(siteAddress).removeUserProfileDaoMap(siteAddress);
        AkxDBManager.deleteSiteInnerDB(ZalyApplication.getContext(), siteAddress);
    }

    @Override
    public Long cleanUnreadNum(Site site, String chatSessionId) {
        SiteAddress siteAddress = new SiteAddress(site.getSiteAddress());
        return SiteChatSessionDao.getInstance(siteAddress).cleanUnreadNum(chatSessionId);
    }

    @Override
    public boolean updateSiteMute(String siteHost, String sitePort, boolean isMute) {
        return AkxCommonDao.getInstance().updadeSiteMute(siteHost, sitePort, isMute);
    }

    @Override
    public Site getSiteUser(String address) {
        SiteAddress siteAddress = ZalyApplication.getSiteAddressObj(address);
        return AkxCommonDao.getInstance().querySiteInfo(siteAddress);
    }

    @Override
    public User getUserIdentity() {
        return AkxCommonDao.getInstance().getUserIdentity();
    }

    @Override
    public int updateSiteConnStatus(int connStatus, String siteHost, String sitePort) {
        return AkxCommonDao.getInstance().updateSiteConnectionStatus(connStatus, siteHost, sitePort);
    }

    @Override
    public void delSiteInfo(String siteHost, String sitePort) {
        AkxCommonDao.getInstance().delSiteInfo(siteHost, sitePort);
    }

    /**
     * 删除二人的消息
     *
     * @param chatSessionId
     */
    @Override
    public void deleteU2MsgByChatSessionId(Site site, String chatSessionId) {
        SiteAddress siteAddress = new SiteAddress(site.getSiteAddress());
        SiteMessageDao.getInstance(siteAddress).deleteU2MsgByChatSessionId(chatSessionId);
    }


    /**
     * 删除群组的消息
     *
     * @param chatSessionId
     */
    @Override
    public void deleteGroupMsgByChatSessionId(Site site, String chatSessionId) {
        SiteAddress siteAddress = new SiteAddress(site.getSiteAddress());
        SiteMessageDao.getInstance(siteAddress).deleteGroupMsgByChatSessionId(chatSessionId);
    }

    /**
     * 更新当前会话的绝密状态
     *
     * @param site
     * @param isOpenTS
     * @param chatSessionId
     * @return
     */
    @Override
    public int updateTSByChatSessionId(Site site, boolean isOpenTS, String chatSessionId) {
        SiteAddress siteAddress = new SiteAddress(site.getSiteAddress());
        return SiteChatSessionDao.getInstance(siteAddress).updateTSByChatSessionId(isOpenTS, chatSessionId);
    }

    /**
     * 获取当前会话的绝密状态
     *
     * @param site
     * @param chatSessionId
     * @return
     */

    @Override
    public boolean getTSByChatSessionId(Site site, String chatSessionId) {
        SiteAddress siteAddress = new SiteAddress(site.getSiteAddress());
        Long flag = SiteChatSessionDao.getInstance(siteAddress).getTSByChatSessionId(chatSessionId);
        return flag != null && flag > 0 ? true : false;
    }

    @Override
    public void updateGlobalUserId(String globalUerId) {
        AkxCommonDao.getInstance().updateGlobalUserId(globalUerId);
    }
}
