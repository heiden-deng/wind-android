package com.windchat.client.site.presenter;

import com.windchat.client.bean.ChatSession;
import com.windchat.client.bean.Site;
import com.windchat.client.bean.User;
import com.windchat.im.socket.SiteAddress;

import java.util.List;

/**
 * Created by zhangjun on 2018/3/7.
 */

public interface ISitePresenter {

    public Site getSiteByHostAndPort(String host, String port);

    /**
     * 写入用户身份
     */
    Long insertUserIdentity(User user);

    /**
     * 注册站点
     */
    boolean insertSiteAndUserIdentity(Site site, String globalUserId, String generateIndentityType);

    /**
     * 写入站点
     */
    boolean insertSite(Site site, String siteUserId, String siteUserSessionId);

    /**
     * 删除站点表
     */
    void dropSiteBaseTable(SiteAddress siteAddress);


    /**
     * 检查通用表
     */
    void checkCommonBaseTable();

    /**
     * 删除
     */
    void dropCommonBaseTable();

    /**
     * 更新platformSessionId
     */
    void updateUserPlatformSessionId(String siteUserId, String userSessionId);

    /**
     * 查询所有的站点， 不带红点
     */
    List<Site> getAllSiteLists();

    /**
     * 查询所有的站点, 是否带红点展示
     */
    List<Site> getAllSiteLists(Boolean isNeesNureadNum);

    /**
     * 更新站点表的当前用户信息 akx_site_table
     */
    void updateSiteUserInfo(Site site);

    /**
     * 检查指定站点的siteAddress
     *
     * @param siteAddress
     */
    void checkSiteBaseTable(String siteAddress);

    /**
     * 得到当前会话
     *
     * @return
     */
    List<ChatSession> getChatSessionList(String siteAddress);

    /**
     * 写入会话session
     *
     * @return
     */
    boolean insertChatSession(String siteAddress, ChatSession chatSession);

    void deleteCommonDB();

    void deleteSiteDB(SiteAddress siteAddress);

    /**
     * clean chat session unreadNum
     */
    Long cleanUnreadNum(Site site, String chatSessionId);

    /**
     * 更新站点的session
     *
     * @param siteUserId
     * @param userSessionId
     * @return
     */
    int updateUserSiteSessionId(String siteUserId, String userSessionId);

    /**
     * 更新站点静音状态
     *
     * @param siteHost
     * @param sitePort
     * @param isMute
     * @return
     */
    boolean updateSiteMute(String siteHost, String sitePort, boolean isMute);

    /**
     * 查找站点用户
     *
     * @param address
     * @return
     */
    Site getSiteUser(String address);

    /**
     * 获取用户的身份列表
     *
     * @return
     */
    User getUserIdentity();

    /**
     * 修改站点的链接状态
     *
     * @param connStatus
     * @param siteHost
     * @param sitePort
     * @return
     */
    int updateSiteConnStatus(int connStatus, String siteHost, String sitePort);

    /**
     * 删除站点信息
     *
     * @param host
     * @param port
     */
    void delSiteInfo(String host, String port);

    int delUserIdentity();

    void deleteU2MsgByChatSessionId(Site site, String chatSessionId);

    void deleteGroupMsgByChatSessionId(Site site, String chatSessionId);

    void updateSiteInfo(Site site);

    void updateGlobalUserId(String globalUerId);

    boolean getTSByChatSessionId(Site site, String chatSessionId);

    int updateTSByChatSessionId(Site site, boolean isOpenTS, String chatSessionId);

    long getChatSessionByChatSessionId(String siteAddress, String chatSessionId);
}
