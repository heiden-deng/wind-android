package com.windchat.client.group.presenter;

/**
 * Created by alexfan on 2018/4/2.
 */

public interface IGroupProfilePresenter {

    void setGroupId(String groupId);
    String getGroupId();
    String getOwnerId();
    void setMyPowerOnStart(boolean isGroupMember);
    int getMyPower();
    boolean isOwner();
    void getGroupProfile();
    void getGroupSetting();

    void quitGroup();
    void deleteGroup();
    void setGroupMessageMute(boolean messageMute);
    void setCloseInviteGroupChat(boolean closeInviteGroupChat);
    void setGroupName(String name);
}
