package com.akaxin.client.group.presenter.impl;

import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Group;
import com.akaxin.client.bean.Site;
import com.akaxin.client.db.bean.UserGroupBean;
import com.akaxin.client.db.dao.SiteGroupProfileDao;
import com.akaxin.client.group.presenter.IGroupProfilePresenter;
import com.akaxin.client.group.view.IGroupProfileView;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.SPUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.core.GroupProto;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.site.ApiGroupDeleteProto;
import com.akaxin.proto.site.ApiGroupProfileProto;
import com.akaxin.proto.site.ApiGroupQuitProto;
import com.akaxin.proto.site.ApiGroupSettingProto;
import com.akaxin.proto.site.ApiGroupUpdateProfileProto;
import com.akaxin.proto.site.ApiGroupUpdateSettingProto;
import com.orhanobut.logger.Logger;

/**
 * Created by alexfan on 2018/4/2.
 */

public class GroupProfilePresenter implements IGroupProfilePresenter {

    private String TAG = getClass().getSimpleName();

    /* 三种当前用户在该群的身份: 群主, 普通成员, 非成员. */
    public static final int POWER_OWNER = 1;
    public static final int POWER_ORDINARY_MEMBER = 2;
    public static final int POWER_NON_MEMBER = 3;

    public static final String ERROR_GROUP_DELETED = "error.group.deleted";

    private IGroupProfileView iView;

    private String groupId;
    private GroupProto.GroupProfile groupProfile;
    private UserProto.UserProfile ownerProfile;

    /* 当前用户在本群的身份(群主, 普通成员, 非成员) */
    private int myPower;
    private boolean messageMute;
    private boolean closeInviteGroupChat;
    private Site currentSite;

    public GroupProfilePresenter(IGroupProfileView iView, Site site) {
        super();
        this.iView = iView;
        this.currentSite = site;
    }

    public boolean isGroupMember() {
        return myPower != POWER_NON_MEMBER;
    }

    @Override
    public void setGroupId(String groupId) {
        if (StringUtils.isEmpty(groupId)) {
            iView.onGroupIdError();
            return;
        }
        this.groupId = groupId;
    }
    @Override
    public String getGroupId() {
        return this.groupId;
    }

    @Override
    public String getOwnerId() {
        return ownerProfile.getSiteUserId();
    }

    @Override
    public void setMyPowerOnStart(boolean isGroupMember) {
        this.myPower = isGroupMember ? POWER_ORDINARY_MEMBER : POWER_NON_MEMBER;
    }

    @Override
    public int getMyPower() {
        return myPower;
    }

    @Override
    public boolean isOwner() {
        return myPower == POWER_OWNER;
    }

    @Override
    public void getGroupProfile() {
        ZalyTaskExecutor.executeUserTask(TAG, new GetGroupProfileTask());
    }

    @Override
    public void getGroupSetting() {
        boolean isNet = NetUtils.getNetInfo();

        if (isGroupMember() && isNet)
            ZalyTaskExecutor.executeUserTask(TAG, new GetGroupSettingTask());
    }

    @Override
    public void deleteGroup() {
        ZalyTaskExecutor.executeUserTask(TAG, new DeleteGroupTask());
    }

    @Override
    public void quitGroup() {
        ZalyTaskExecutor.executeUserTask(TAG, new QuitGroupTask());
    }

    @Override
    public void setGroupMessageMute(boolean messageMute) {
        ZalyTaskExecutor.executeUserTask(TAG, new SetGroupSettingTask(messageMute));
    }

    @Override
    public void setCloseInviteGroupChat(boolean closeInviteGroupChat) {
        this.closeInviteGroupChat = closeInviteGroupChat;
        ZalyTaskExecutor.executeUserTask(TAG, new SetCloseInviteGroupChatTask(closeInviteGroupChat));
    }

    @Override
    public void setGroupName(String name) {
        ZalyTaskExecutor.executeUserTask(TAG, new SetGroupNameTask(name));
    }

    class DeleteGroupTask extends ZalyTaskExecutor.Task<Void, Void, ApiGroupDeleteProto.ApiGroupDeleteResponse> {

        @Override
        protected void onPreTask() {
            super.onPreTask();
            iView.onStartDeleteGroup();
        }

        @Override
        protected ApiGroupDeleteProto.ApiGroupDeleteResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getGroupApi().deleteGroup(groupId);
        }

        @Override
        protected void onTaskSuccess(ApiGroupDeleteProto.ApiGroupDeleteResponse apiGroupDeleteResponse) {
            super.onTaskSuccess(apiGroupDeleteResponse);
            iView.onDeleteGroupSucceed();
        }

        @Override
        protected void onTaskError(Exception e) {
            iView.onDeleteGroupFail();
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            iView.onDeleteGroupFail();
            ZalyLogUtils.getInstance().apiError(TAG, zalyAPIException);
        }
    }

    class QuitGroupTask extends ZalyTaskExecutor.Task<Void, Void, ApiGroupQuitProto.ApiGroupQuitResponse> {

        @Override
        protected void onPreTask() {
            super.onPreTask();
            iView.onStartQuitGroup();
        }

        @Override
        protected ApiGroupQuitProto.ApiGroupQuitResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getGroupApi().exitGroupFromSite(groupId);
        }

        @Override
        protected void onTaskSuccess(ApiGroupQuitProto.ApiGroupQuitResponse apiGroupQuitResponse) {
            super.onTaskSuccess(apiGroupQuitResponse);
            iView.onQuitGroupSucceed();
        }

        @Override
        protected void onTaskError(Exception e) {
            iView.onQuitGroupFail();
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            iView.onQuitGroupFail();
            ZalyLogUtils.getInstance().apiError(TAG, zalyAPIException);
        }
    }

    class GetGroupProfileTask extends ZalyTaskExecutor.Task<Void, Void, ApiGroupProfileProto.ApiGroupProfileResponse> {

        @Override
        protected void onPreTask() {
            super.onPreTask();
            iView.onStartGetGroupProfile();
        }

        @Override
        protected void onCacheTask() {
            UserGroupBean userGroupBean = GroupPresenter.getInstance(currentSite).getGroupBean(groupId);

            if (userGroupBean == null) {
                boolean isNet = NetUtils.getNetInfo();
                if (!isNet) {
                    iView.onGetGroupProfileFail();
                }
                return;
            }
            groupProfile = GroupProto.GroupProfile.newBuilder()
                    .setId(userGroupBean.getGroupId())
                    .setName(userGroupBean.getGroupName())
                    .build();

            UserProto.UserProfile ownerProfile = UserProto.UserProfile.newBuilder()
                    .setSiteUserId(userGroupBean.getGroupOwnerId())
                    .setUserName(userGroupBean.getGroupOwnerName())
                    .setUserPhoto(userGroupBean.getGroupOwnerIcon())
                    .build();

            String groupOwnerSiteUserId = userGroupBean.getGroupOwnerId();
            if (StringUtils.isNotEmpty(groupOwnerSiteUserId))
                if (groupOwnerSiteUserId.equals(currentSite.getSiteUserId()))
                    myPower = POWER_OWNER;


            closeInviteGroupChat = userGroupBean.isCloseInviteGroupChat();
            int numGroupMembers = userGroupBean.getGroupCountMember();

            iView.onGetGroupProfileSucceed(groupProfile, ownerProfile, myPower, closeInviteGroupChat, numGroupMembers);
            iView.onGetGroupSettingSucceed(userGroupBean.isMute());
        }

        @Override
        protected ApiGroupProfileProto.ApiGroupProfileResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getGroupApi().getGroupProfile(groupId);
        }

        @Override
        protected void onTaskSuccess(ApiGroupProfileProto.ApiGroupProfileResponse response) {
            if (response == null) {
                iView.onGetGroupProfileFail();
                return;
            }
            super.onTaskSuccess(response);

            groupProfile = response.getProfile();
            ownerProfile = response.getOwner();
            String groupOwnerSiteUserId = ownerProfile.getSiteUserId();
            if (StringUtils.isNotEmpty(groupOwnerSiteUserId))
                if (groupOwnerSiteUserId.equals(currentSite.getSiteUserId()))
                    myPower = POWER_OWNER;
            closeInviteGroupChat = response.getCloseInviteGroupChat();
            int numGroupMembers = response.getGroupMemberCount();
            iView.onGetGroupProfileSucceed(groupProfile, ownerProfile, myPower, closeInviteGroupChat, numGroupMembers);

            UserGroupBean userGroupBean = new UserGroupBean();
            userGroupBean.setGroupName(groupProfile.getName());
            userGroupBean.setCloseInviteGroupChat(closeInviteGroupChat);
            userGroupBean.setGroupImage(groupProfile.getIcon());
            userGroupBean.setGroupId(groupProfile.getId());
            userGroupBean.setGroupOwnerId(ownerProfile.getSiteUserId());
            userGroupBean.setGroupOwnerName(ownerProfile.getUserName());
            userGroupBean.setGroupOwnerIcon(ownerProfile.getUserPhoto());
            userGroupBean.setGroupCountMember(numGroupMembers);
            userGroupBean.setAsGroupMember(Group.isGroupMember);

            GroupPresenter.getInstance(currentSite).insertGroupProfile(userGroupBean);
            //TODO 更新数据库 群组信息 Group message
            GroupPresenter.getInstance(currentSite).updateGroupOwnerProfile(groupProfile.getId(), ownerProfile, numGroupMembers, closeInviteGroupChat);
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            iView.onGetGroupProfileFail();
            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            ZalyLogUtils.getInstance().info(TAG, zalyAPIException.getMessage());

            if (zalyAPIException.getErrorInfoCode().equals(ERROR_GROUP_DELETED)) {
                iView.onGroupNotExist();
            } else {
                super.onAPIError(zalyAPIException);
                iView.onGetGroupProfileFail();
            }
        }

    }

    class GetGroupSettingTask extends ZalyTaskExecutor.Task<Void, Void, ApiGroupSettingProto.ApiGroupSettingResponse> {
        @Override
        protected ApiGroupSettingProto.ApiGroupSettingResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getGroupApi().getGroupSetting(groupId);
        }

        @Override
        protected void onPreTask() {
            iView.onStartGetGroupSetting();
        }

        @Override
        protected void onTaskSuccess(ApiGroupSettingProto.ApiGroupSettingResponse apiGroupSettingResponse) {
            super.onTaskSuccess(apiGroupSettingResponse);
            messageMute = apiGroupSettingResponse.getMessageMute();
            iView.onGetGroupSettingSucceed(messageMute);
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            iView.onGetGroupSettingFail();
        }
    }

    class SetCloseInviteGroupChatTask extends ZalyTaskExecutor.Task<Void, Void, ApiGroupUpdateProfileProto.ApiGroupUpdateProfileResponse> {

        boolean inviteGroupChatBanned;

        public SetCloseInviteGroupChatTask(boolean inviteGroupChatBanned) {
            this.inviteGroupChatBanned = inviteGroupChatBanned;
        }

        @Override
        protected ApiGroupUpdateProfileProto.ApiGroupUpdateProfileResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getGroupApi().setInviteGroupChat(groupId,inviteGroupChatBanned);
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            iView.onStartCloseInvite();
        }

        @Override
        protected void onTaskSuccess(ApiGroupUpdateProfileProto.ApiGroupUpdateProfileResponse apiGroupUpdateProfileResponse) {
            super.onTaskSuccess(apiGroupUpdateProfileResponse);
            iView.onCloseInviteSucceed();
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            iView.onCloseInviteFail();
            Logger.e(e);
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            super.onAPIError(zalyAPIException);
            ZalyLogUtils.getInstance().apiError(this.TAG, zalyAPIException);
            iView.onCloseInviteFail();
        }
    }

    class SetGroupSettingTask extends ZalyTaskExecutor.Task<Void, Void, ApiGroupUpdateSettingProto.ApiGroupUpdateSettingResponse> {

        boolean mute;

        public SetGroupSettingTask(boolean mute) {
            this.mute = mute;
        }

        @Override
        protected ApiGroupUpdateSettingProto.ApiGroupUpdateSettingResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getGroupApi().updateGroupSetting(groupId,mute);
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            iView.onStartSetGroupSetting();
        }

        @Override
        protected void onTaskSuccess(ApiGroupUpdateSettingProto.ApiGroupUpdateSettingResponse response) {
            super.onTaskSuccess(response);
            iView.onSetGroupSettingSucceed();
            /////更新数据库静音状态
            GroupPresenter.getInstance(currentSite).updateGroupMute(groupId, mute);
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            iView.onSetGroupSettingFail(!mute);
        }
    }

    class SetGroupNameTask extends ZalyTaskExecutor.Task<Void, Void, ApiGroupUpdateProfileProto.ApiGroupUpdateProfileResponse> {

        String groupName;

        public SetGroupNameTask(String groupName) {
            this.groupName = groupName;
        }

        @Override
        protected ApiGroupUpdateProfileProto.ApiGroupUpdateProfileResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getGroupApi().updateGroupProfile(groupId, groupName);
        }


        @Override
        protected void onPreTask() {
            super.onPreTask();
            iView.onStartChangeGroupName();
        }

        @Override
        protected void onTaskSuccess(ApiGroupUpdateProfileProto.ApiGroupUpdateProfileResponse apiGroupUpdateProfileResponse) {
            super.onTaskSuccess(apiGroupUpdateProfileResponse);
            SiteGroupProfileDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort())).updateSiteGroupProfile(groupId, groupName);
            iView.onChangeGroupNameSucceed(groupName);
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            iView.onChangeGroupNameFail();
        }
    }
}
