package com.akaxin.client.api;

import com.akaxin.client.socket.TransportPackageForResponse;
import com.akaxin.proto.core.GroupProto;
import com.akaxin.proto.site.ApiGroupAddMemberProto;
import com.akaxin.proto.site.ApiGroupApplyTokenProto;
import com.akaxin.proto.site.ApiGroupCreateProto;
import com.akaxin.proto.site.ApiGroupDeleteProto;
import com.akaxin.proto.site.ApiGroupJoinByTokenProto;
import com.akaxin.proto.site.ApiGroupListProto;
import com.akaxin.proto.site.ApiGroupMembersProto;
import com.akaxin.proto.site.ApiGroupNonMembersProto;
import com.akaxin.proto.site.ApiGroupProfileProto;
import com.akaxin.proto.site.ApiGroupQuitProto;
import com.akaxin.proto.site.ApiGroupRemoveMemberProto;
import com.akaxin.proto.site.ApiGroupSettingProto;
import com.akaxin.proto.site.ApiGroupUpdateProfileProto;
import com.akaxin.proto.site.ApiGroupUpdateSettingProto;

import java.util.List;

/**
 * Created by Mr.kk on 2018/6/14.
 * This Project was client-android
 */

public class ApiClientForGroup {
    private ApiClient client = null;
    private String logTag = "ApiClientForGroup";
    private static final String API_GROUP_APPLY_TOKEN = "api.group.applyToken";
    private static final String API_GROUP_JOIN_BY_TOKEN = "api.group.joinByToken";
    private static final String API_GROUP_LIST = "api.group.list";
    private static final String API_GROUP_PROFILE = "api.group.profile";
    private static final String API_GROUP_UPDATE_PROFILE = "api.group.updateProfile";
    private static final String API_GROUP_SETTING = "api.group.setting";
    private static final String API_GROUP_UPDATE_SETTING = "api.group.updateSetting";
    private static final String API_GROUP_ADD_MEMBER = "api.group.addMember";
    private static final String API_GROUP_NON_MEMBERS = "api.group.nonMembers";
    private static final String API_GROUP_MEMBERS = "api.group.members";
    private static final String API_GROUP_DELETE_MEMBER = "api.group.deleteMember";
    private static final String API_GROUP_CREATE = "api.group.create";
    private static final String API_GROUP_DELETE = "api.group.delete";
    private static final String API_GROUP_QUIT = "api.group.quit";

    public ApiClientForGroup(ApiClient client) {
        this.client = client;
        this.logTag = "ApiClient." + this.getClass().getName();
    }

    /**
     * 根据群组token来加入群
     *
     * @param siteGroupId
     * @param token
     * @return
     * @throws Exception
     */
    public ApiGroupJoinByTokenProto.ApiGroupJoinByTokenResponse joinGroupByToken(String siteGroupId, String token) throws Exception {
        ApiGroupJoinByTokenProto.ApiGroupJoinByTokenRequest request = ApiGroupJoinByTokenProto.ApiGroupJoinByTokenRequest.newBuilder()
                .setSiteGroupId(siteGroupId).setToken(token).build();
        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_JOIN_BY_TOKEN, request);
        return ApiGroupJoinByTokenProto.ApiGroupJoinByTokenResponse.parseFrom(response.data.getData());
    }


    /**
     * 获取群组的token生成二维码
     *
     * @param siteGroupId
     * @return
     * @throws Exception
     */
    public ApiGroupApplyTokenProto.ApiGroupApplyTokenResponse getGroupApplyToken(String siteGroupId) throws Exception {
        ApiGroupApplyTokenProto.ApiGroupApplyTokenRequest request = ApiGroupApplyTokenProto.ApiGroupApplyTokenRequest
                .newBuilder()
                .setSiteGroupId(siteGroupId)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_APPLY_TOKEN, request);
        return ApiGroupApplyTokenProto.ApiGroupApplyTokenResponse.parseFrom(response.data.getData());
    }

    /**
     * 获取群组列表
     *
     * @return
     * @throws Exception
     */
    public ApiGroupListProto.ApiGroupListResponse getGroupFromSite(String siteUserId) throws Exception {
        ApiGroupListProto.ApiGroupListRequest request = ApiGroupListProto.ApiGroupListRequest.newBuilder()
                .setSiteUserId(siteUserId)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_LIST, request);
        return ApiGroupListProto.ApiGroupListResponse.parseFrom(response.data.getData());
    }

    /**
     * 获取群资料
     *
     * @param groupId
     * @return
     * @throws Exception
     */
    public ApiGroupProfileProto.ApiGroupProfileResponse getGroupProfile(String groupId) throws Exception {
        ApiGroupProfileProto.ApiGroupProfileRequest request = ApiGroupProfileProto.ApiGroupProfileRequest.newBuilder()
                .setGroupId(groupId)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_PROFILE, request);
        return ApiGroupProfileProto.ApiGroupProfileResponse.parseFrom(response.data.getData());
    }

    /**
     * 更新群资料
     *
     * @param groupId
     * @param groupName
     * @return
     * @throws Exception
     */
    public ApiGroupUpdateProfileProto.ApiGroupUpdateProfileResponse updateGroupProfile(String groupId, String groupName) throws Exception {
        GroupProto.GroupProfile profile = GroupProto.GroupProfile.newBuilder().setId(groupId).setName(groupName).build();
        ApiGroupUpdateProfileProto.ApiGroupUpdateProfileRequest request = ApiGroupUpdateProfileProto.ApiGroupUpdateProfileRequest.newBuilder()
                .setProfile(profile).build();
        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_UPDATE_PROFILE, request);
        return ApiGroupUpdateProfileProto.ApiGroupUpdateProfileResponse.parseFrom(response.data.getData());
    }

    /**
     * 查询群设置
     *
     * @param groupId
     * @return
     * @throws Exception
     */
    public ApiGroupSettingProto.ApiGroupSettingResponse getGroupSetting(String groupId) throws Exception {
        ApiGroupSettingProto.ApiGroupSettingRequest request = ApiGroupSettingProto.ApiGroupSettingRequest.newBuilder()
                .setGroupId(groupId).build();
        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_SETTING, request);
        return ApiGroupSettingProto.ApiGroupSettingResponse.parseFrom(response.data.getData());
    }

    /**
     * 更新群设置
     *
     * @param groupId
     * @param mute
     * @return
     * @throws Exception
     */
    public ApiGroupUpdateSettingProto.ApiGroupUpdateSettingResponse updateGroupSetting(String groupId, boolean mute) throws Exception {
        ApiGroupUpdateSettingProto.ApiGroupUpdateSettingRequest request = ApiGroupUpdateSettingProto.ApiGroupUpdateSettingRequest.newBuilder()
                .setGroupId(groupId).setMessageMute(mute).build();
        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_UPDATE_SETTING, request);
        return ApiGroupUpdateSettingProto.ApiGroupUpdateSettingResponse.parseFrom(response.data.getData());
    }

    /**
     * 群主设置是否允许邀请群成员
     *
     * @param groupId
     * @param inviteGroupChatBanned
     * @return
     * @throws Exception
     */
    public ApiGroupUpdateProfileProto.ApiGroupUpdateProfileResponse setInviteGroupChat(String groupId, boolean inviteGroupChatBanned) throws Exception {
        GroupProto.GroupProfile profile = GroupProto.GroupProfile.newBuilder().setId(groupId).build();
        ApiGroupUpdateProfileProto.ApiGroupUpdateProfileRequest request = ApiGroupUpdateProfileProto.ApiGroupUpdateProfileRequest.newBuilder()
                .setProfile(profile).setCloseInviteGroupChat(inviteGroupChatBanned).build();

        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_UPDATE_PROFILE, request);
        return ApiGroupUpdateProfileProto.ApiGroupUpdateProfileResponse.parseFrom(response.data.getData());
    }

    /**
     * 添加群成员
     *
     * @param groupId
     * @param friends
     * @return
     * @throws Exception
     */
    public ApiGroupAddMemberProto.ApiGroupAddMemberResponse addFriendGroup(String groupId, List<String> friends) throws Exception {
        ApiGroupAddMemberProto.ApiGroupAddMemberRequest request = ApiGroupAddMemberProto.ApiGroupAddMemberRequest.newBuilder()
                .setGroupId(groupId)
                .addAllUserList(friends)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_ADD_MEMBER, request);
        return ApiGroupAddMemberProto.ApiGroupAddMemberResponse.parseFrom(response.data.getData());
    }

    /**
     * 获取不在群组中的好友列表
     *
     * @param groupId
     * @return
     * @throws Exception
     */
    public ApiGroupNonMembersProto.ApiGroupNonMembersResponse getFriendNonMemberList(String groupId) throws Exception {
        ApiGroupNonMembersProto.ApiGroupNonMembersRequest request = ApiGroupNonMembersProto.ApiGroupNonMembersRequest
                .newBuilder().setGroupId(groupId).build();
        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_NON_MEMBERS, request);
        return ApiGroupNonMembersProto.ApiGroupNonMembersResponse.parseFrom(response.data.getData());
    }

    /**
     * 获取群成员
     *
     * @param groupId
     * @return
     * @throws Exception
     */
    public ApiGroupMembersProto.ApiGroupMembersResponse getGroupMembers(String groupId) throws Exception {
        ApiGroupMembersProto.ApiGroupMembersRequest request = ApiGroupMembersProto.ApiGroupMembersRequest.newBuilder()
                .setGroupId(groupId)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_MEMBERS, request);
        return ApiGroupMembersProto.ApiGroupMembersResponse.parseFrom(response.data.getData());
    }

    /**
     * 删除群成员
     *
     * @param groupId
     * @param members
     * @return
     * @throws Exception
     */
    public ApiGroupRemoveMemberProto.ApiGroupRemoveMemberResponse deleteGroupMembers(String groupId, List<String> members) throws Exception {
        ApiGroupRemoveMemberProto.ApiGroupRemoveMemberRequest request = ApiGroupRemoveMemberProto.ApiGroupRemoveMemberRequest.newBuilder()
                .setGroupId(groupId)
                .addAllSiteUserId(members)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_DELETE_MEMBER, request);
        return ApiGroupRemoveMemberProto.ApiGroupRemoveMemberResponse.parseFrom(response.data.getData());
    }

    /**
     * 创建群组
     *
     * @param groupName
     * @param friends
     * @return
     * @throws Exception
     */
    public ApiGroupCreateProto.ApiGroupCreateResponse createGroup(String groupName, List<String> friends) throws Exception {
        ApiGroupCreateProto.ApiGroupCreateRequest request = ApiGroupCreateProto.ApiGroupCreateRequest.newBuilder()
                .setGroupName(groupName)
                .addAllSiteUserIds(friends)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_CREATE, request);
        return ApiGroupCreateProto.ApiGroupCreateResponse.parseFrom(response.data.getData());
    }

    /**
     * 删除群组
     *
     * @param groupId
     * @return
     * @throws Exception
     */
    public ApiGroupDeleteProto.ApiGroupDeleteResponse deleteGroup(String groupId) throws Exception {
        ApiGroupDeleteProto.ApiGroupDeleteRequest request = ApiGroupDeleteProto.ApiGroupDeleteRequest.newBuilder()
                .setGroupId(groupId)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_DELETE, request);
        return ApiGroupDeleteProto.ApiGroupDeleteResponse.parseFrom(response.data.getData());
    }


    public ApiGroupQuitProto.ApiGroupQuitResponse exitGroupFromSite(String groupId) throws Exception {
        ApiGroupQuitProto.ApiGroupQuitRequest request = ApiGroupQuitProto.ApiGroupQuitRequest.newBuilder()
                .setGroupId(groupId)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_GROUP_QUIT, request);
        return ApiGroupQuitProto.ApiGroupQuitResponse.parseFrom(response.data.getData());
    }
}
