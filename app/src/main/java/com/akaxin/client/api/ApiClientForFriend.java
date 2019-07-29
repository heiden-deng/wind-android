package com.akaxin.client.api;

import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.Site;
import com.akaxin.client.socket.TransportPackageForResponse;
import com.akaxin.proto.site.ApiFriendApplyListProto;
import com.akaxin.proto.site.ApiFriendApplyProto;
import com.akaxin.proto.site.ApiFriendApplyResultProto;
import com.akaxin.proto.site.ApiFriendDeleteProto;
import com.akaxin.proto.site.ApiFriendListProto;
import com.akaxin.proto.site.ApiFriendProfileProto;
import com.akaxin.proto.site.ApiFriendRemarkProto;
import com.akaxin.proto.site.ApiFriendSettingProto;
import com.akaxin.proto.site.ApiFriendUpdateSettingProto;

/**
 * Created by Mr.kk on 2018/6/14.
 * This Project was client-android
 */

public class ApiClientForFriend {

    private ApiClient client = null;
    private String logTag = "ApiClientForFriend";


    private static final String API_FRIEND_LIST = "api.friend.list";
    private static final String API_FRIEND_PROFILE = "api.friend.profile";
    private static final String API_FRIEND_APPLY = "api.friend.apply";
    private static final String API_FRIEND_APPLY_LIST = "api.friend.applyList";
    private static final String API_FRIEND_APPLY_RESULT = "api.friend.applyResult";
    private static final String API_FRIEND_DELETE = "api.friend.delete";
    private static final String API_FRIEND_UPDATE_SETTING = "api.friend.updateSetting";
    private static final String API_FRIEND_SETTING = "api.friend.setting";
    private static final String API_USER_UPDATE_REMARK_NAME = "api.friend.remark";

    public ApiClientForFriend(ApiClient client) {
        this.client = client;
        this.logTag = "ApiClient." + this.getClass().getName();
    }


    /**
     * 获取好友列表
     *
     * @return
     * @throws Exception
     */
    public ApiFriendListProto.ApiFriendListResponse getSiteFriend(String siteUserId) throws Exception {
        ApiFriendListProto.ApiFriendListRequest request = ApiFriendListProto.ApiFriendListRequest.newBuilder()
                .setSiteUserId(siteUserId)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_FRIEND_LIST, request);
        return ApiFriendListProto.ApiFriendListResponse.parseFrom(response.data.getData());
    }

    /**
     * 查找用户
     *
     * @param siteUserId
     * @return
     * @throws Exception
     */
    public ApiFriendProfileProto.ApiFriendProfileResponse findUser(String siteUserId) throws Exception {
        ApiFriendProfileProto.ApiFriendProfileRequest request = ApiFriendProfileProto.ApiFriendProfileRequest.newBuilder()
                .setSiteUserId(siteUserId)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_FRIEND_PROFILE, request);
        return ApiFriendProfileProto.ApiFriendProfileResponse.parseFrom(response.data.getData());
    }

    /**
     * 请求添加好友
     *
     * @param friendSiteUserId
     * @param reason
     * @return
     * @throws Exception
     */
    public ApiFriendApplyProto.ApiFriendApplyResponse applyFriend(String friendSiteUserId, String reason) throws Exception {
        ApiFriendApplyProto.ApiFriendApplyRequest request = ApiFriendApplyProto.ApiFriendApplyRequest.newBuilder()
                .setSiteFriendId(friendSiteUserId)
                .setApplyReason(reason)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_FRIEND_APPLY, request);
        return ApiFriendApplyProto.ApiFriendApplyResponse.parseFrom(response.data.getData());

    }

    /**
     * 获取好友申请列表
     *
     * @return
     * @throws Exception
     */
    public ApiFriendApplyListProto.ApiFriendApplyListResponse getApplyList() throws Exception {
        ApiFriendApplyListProto.ApiFriendApplyListRequest request = ApiFriendApplyListProto.ApiFriendApplyListRequest.newBuilder().build();
        TransportPackageForResponse response = this.client.sendRequest(API_FRIEND_APPLY_LIST, request);
        return ApiFriendApplyListProto.ApiFriendApplyListResponse.parseFrom(response.data.getData());
    }

    /**
     * 同意好友申请
     *
     * @param friendSiteUserId
     * @return
     * @throws Exception
     */
    public ApiFriendApplyResultProto.ApiFriendApplyResultResponse agreeApplyFriend(String friendSiteUserId) throws Exception {
        ApiFriendApplyResultProto.ApiFriendApplyResultRequest request = ApiFriendApplyResultProto.ApiFriendApplyResultRequest.newBuilder()
                .setSiteFriendId(friendSiteUserId)
                .setApplyResult(true)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_FRIEND_APPLY_RESULT, request);
        return ApiFriendApplyResultProto.ApiFriendApplyResultResponse.parseFrom(response.data.getData());
    }

    /**
     * 删除好友
     *
     * @param siteUserId
     * @return
     * @throws Exception
     */
    public ApiFriendDeleteProto.ApiFriendDeleteResponse deleteFriend(String siteUserId) throws Exception {
        ApiFriendDeleteProto.ApiFriendDeleteRequest request = ApiFriendDeleteProto.ApiFriendDeleteRequest.newBuilder()
                .setSiteFriendId(siteUserId)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_FRIEND_DELETE, request);
        return ApiFriendDeleteProto.ApiFriendDeleteResponse.parseFrom(response.data.getData());
    }

    /**
     * 更新好友设置
     *
     * @param siteUserId
     * @param mute
     * @return
     * @throws Exception
     */
    public ApiFriendUpdateSettingProto.ApiFriendUpdateSettingResponse updateFriendSetting(String siteUserId, boolean mute) throws Exception {
        ApiFriendUpdateSettingProto.ApiFriendUpdateSettingRequest request = ApiFriendUpdateSettingProto.ApiFriendUpdateSettingRequest.newBuilder()
                .setSiteFriendId(siteUserId).setMessageMute(mute).build();
        TransportPackageForResponse response = this.client.sendRequest(API_FRIEND_UPDATE_SETTING, request);
        return ApiFriendUpdateSettingProto.ApiFriendUpdateSettingResponse.parseFrom(response.data.getData());

    }

    /**
     * 查询好友设置
     *
     * @param siteUserId
     * @return
     * @throws Exception
     */
    public ApiFriendSettingProto.ApiFriendSettingResponse getFriendSetting(String siteUserId) throws Exception {
        ApiFriendSettingProto.ApiFriendSettingRequest request = ApiFriendSettingProto.ApiFriendSettingRequest.newBuilder()
                .setSiteFriendId(siteUserId).build();
        TransportPackageForResponse response = this.client.sendRequest(API_FRIEND_SETTING, request);
        return ApiFriendSettingProto.ApiFriendSettingResponse.parseFrom(response.data.getData());
    }

    /**
     * 修改用户信息
     * @param siteFriendId
     * @param remarkName
     * @return
     * @throws Exception
     */
    public ApiFriendRemarkProto.ApiFriendRemarkResponse updateRemarkName(String siteFriendId, String remarkName) throws Exception {
        ApiFriendRemarkProto.ApiFriendRemarkRequest request = ApiFriendRemarkProto.ApiFriendRemarkRequest.newBuilder()
                .setSiteFriendId(siteFriendId)
                .setAliasName(remarkName)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_USER_UPDATE_REMARK_NAME, request);
        return ApiFriendRemarkProto.ApiFriendRemarkResponse.parseFrom(response.data.getData());
    }

}
