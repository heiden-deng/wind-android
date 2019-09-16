package com.akaxin.client.group.presenter;

import com.akaxin.client.bean.Site;
import com.akaxin.client.db.bean.UserGroupBean;
import com.akaxin.proto.core.GroupProto;
import com.akaxin.proto.core.UserProto;

import java.util.List;

/**
 * Created by zhangjun on 2018/3/8.
 */

public interface IGroupPresenter {

    /**
     * 批量插入群组信息
     */
    void batchInsertGroup(List<GroupProto.SimpleGroupProfile> simpleGroupProfiles);


    /**
     * 更新群组管理员
     * @param siteGroupId
     * @param ownerProfile
     */
    void updateGroupOwnerProfile(String siteGroupId, UserProto.UserProfile ownerProfile, int numGroupMembers, boolean closeInviteGroupChat);
    /**
     * 查询群首页
     * @param siteGroupId
     * @param site
     * @return
     */
    UserGroupBean getGroupBeanByGroupId(String siteGroupId, Site site);

    /**
     * 更新站点状态
     * @param siteGroupId
     * @param isMute
     * @return
     */
    boolean updateGroupMute(String siteGroupId, boolean isMute);

    /**
     * 获取本地指定群组的profile
     * @param siteGroupid
     * @return
     */
    UserGroupBean getGroupBean(String siteGroupid);

    Long insertGroupProfile(UserGroupBean userGroupBean);

    /**
     * 只有本地创建群的会写入
     * @param bean
     * @return
     */
    Long createGroupSimpleProfile(UserGroupBean bean);
}
