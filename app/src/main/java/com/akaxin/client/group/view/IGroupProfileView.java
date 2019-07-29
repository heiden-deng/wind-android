package com.akaxin.client.group.view;

import com.akaxin.proto.core.GroupProto;
import com.akaxin.proto.core.UserProto;

/**
 * Created by alexfan on 2018/4/2.
 */

public interface IGroupProfileView {

    void onGroupIdError();
    void onGroupNotExist();

    void onStartDeleteGroup();
    void onDeleteGroupSucceed();
    void onDeleteGroupFail();

    void onStartQuitGroup();
    void onQuitGroupSucceed();
    void onQuitGroupFail();

    void onStartGetGroupProfile();
    void onGetGroupProfileSucceed(GroupProto.GroupProfile groupProfile,
                                  UserProto.UserProfile ownerProfile,
                                  int myPower,
                                  boolean inviteGroupChatBanned,
                                  int numGroupMembers);
    void onGetGroupProfileFail();

    void onStartGetGroupSetting();
    void onGetGroupSettingSucceed(boolean mute);
    void onGetGroupSettingFail();

    void onStartCloseInvite();
    void onCloseInviteSucceed();
    void onCloseInviteFail();

    void onStartSetGroupSetting();
    void onSetGroupSettingSucceed();
    void onSetGroupSettingFail(boolean mute);

    void onStartChangeGroupName();
    void onChangeGroupNameSucceed(String name);
    void onChangeGroupNameFail();
}
