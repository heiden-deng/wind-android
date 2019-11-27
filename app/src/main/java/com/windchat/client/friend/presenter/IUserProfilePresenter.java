package com.windchat.client.friend.presenter;

import com.windchat.client.db.bean.UserFriendBean;
import com.akaxin.proto.core.UserProto;

import java.util.List;

/**
 * Created by zhangjun on 2018/3/8.
 */

public interface IUserProfilePresenter {

    /**
     * 查询是否为好友
     * @param siteUserId
     * @return
     */
    UserProto.SimpleUserProfile queryFriendBySiteUserId(String siteUserId);

    /**
     * 删除好友
     * @param siteUserId
     * @return
     */
    boolean deleteFriendBySiteUserId(String siteUserId);

    /**
     * 设置二人静音与否
     * @param siteUserId
     * @param isMute
     * @return
     */
    boolean setFriendIsMuteBySiteUserId(String siteUserId, Boolean isMute);

    /**
     * 查询所有的好友
     * @param siteUserId
     * @return
     */
    List<UserProto.SimpleUserProfile> getFriendList(String siteUserId);

    /**
     * 插入群里陌生朋友信息
     * @param userProfile
     */
    void insertStrangerFriend(UserProto.UserProfile userProfile);

    /**
     * 写入好友
     */
    Long insertSiteUserProfile(UserProto.UserProfile userProfile);

    /**
     * 更新用户名字，以及用户头像,好友关系
     * @param userFriendBean
     * @return
     */
    Long updateSiteUserProfile(UserFriendBean userFriendBean);

    /**
     * 返回friendBean
     * @param siteUserId
     * @return
     */
    UserFriendBean queryFriendBeanBySiteUserId(String siteUserId);

    Long updateSiteUserProfile(UserFriendBean userFriendBean, String siteAddress);

    Long updateRemarkName(String remarkName, String siteUserId);

    UserProto.SimpleUserProfile queryFriend(String siteUserId);
}
