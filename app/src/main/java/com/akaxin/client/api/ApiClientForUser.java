package com.akaxin.client.api;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.constant.ServerConfig;
import com.akaxin.proto.core.PhoneProto;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.platform.ApiUserPhoneProto;
import com.akaxin.proto.platform.ApiUserPushTokenProto;
import com.akaxin.proto.platform.ApiUserRealNameProto;
import com.akaxin.proto.site.ApiSecretChatApplyU2Proto;
import com.akaxin.proto.site.ApiUserMuteProto;
import com.akaxin.proto.site.ApiUserProfileProto;
import com.akaxin.proto.site.ApiUserSearchProto;
import com.akaxin.proto.site.ApiUserUpdateMuteProto;
import com.akaxin.proto.site.ApiUserUpdateProfileProto;
import com.windchat.im.socket.TransportPackageForResponse;

/**
 * Created by sssl on 08/06/2018.
 */

public class ApiClientForUser {

    private static final String ACTION_API_SECRET_CHAT_APPLY_U2 = "api.secretChat.applyU2";
    private static final String API_USER_UPDATE_PROFILE = "api.user.updateProfile";
    private static final String API_USER_PHONE = "api.user.phone";
    private static final String API_USER_PROFILE = "api.user.profile";
    private static final String API_USER_MUTE = "api.user.mute";
    private static final String API_USER_UPDATE_MUTE = "api.user.updateMute";
    private static final String API_USER_PUSH_TOKEN = "api.user.pushToken";
    private static final String API_USER_SEARCH = "api.user.search";

    private ApiClient client = null;

    public ApiClientForUser(ApiClient client) {
        this.client = client;
    }


    public ApiSecretChatApplyU2Proto.ApiSecretChatApplyU2Response applySecretChat(String siteFriendId) throws Exception {
        ApiSecretChatApplyU2Proto.ApiSecretChatApplyU2Request request =
                ApiSecretChatApplyU2Proto.ApiSecretChatApplyU2Request.newBuilder()
                        .setSiteFriendId(siteFriendId)
                        .build();

        TransportPackageForResponse response = this.client.sendRequest(ApiClientForUser.ACTION_API_SECRET_CHAT_APPLY_U2, request);

        return ApiSecretChatApplyU2Proto.ApiSecretChatApplyU2Response.parseFrom(response.data.getData());
    }

    /**
     * 修改用户信息
     *
     * @param userProfileDetails
     * @return
     * @throws Exception
     */
    public ApiUserUpdateProfileProto.ApiUserUpdateProfileResponse updateProfile(UserProto.UserProfile userProfileDetails) throws Exception {
        ApiUserUpdateProfileProto.ApiUserUpdateProfileRequest request = ApiUserUpdateProfileProto.ApiUserUpdateProfileRequest.newBuilder()
                .setUserProfile(userProfileDetails)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_USER_UPDATE_PROFILE, request);
        return ApiUserUpdateProfileProto.ApiUserUpdateProfileResponse.parseFrom(response.data.getData());
    }

    /**
     * 获取用户信息
     *
     * @param siteUserId
     * @return
     * @throws Exception
     */
    public ApiUserProfileProto.ApiUserProfileResponse getProfile(String siteUserId) throws Exception {
        ApiUserProfileProto.ApiUserProfileRequest request = ApiUserProfileProto.ApiUserProfileRequest.newBuilder()
                .setSiteUserId(siteUserId)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_USER_PROFILE, request);
        return ApiUserProfileProto.ApiUserProfileResponse.parseFrom(response.data.getData());
    }

    /**
     * 获取站点是否消息免打扰
     *
     * @return
     * @throws Exception
     */
    public ApiUserMuteProto.ApiUserMuteResponse getSiteMute() throws Exception {
        ApiUserMuteProto.ApiUserMuteRequest request = ApiUserMuteProto.ApiUserMuteRequest.newBuilder().build();
        TransportPackageForResponse response = this.client.sendRequest(API_USER_MUTE, request);
        return ApiUserMuteProto.ApiUserMuteResponse.parseFrom(response.data.getData());
    }

    /**
     * 更新站点免打扰状态
     *
     * @param mute
     * @return
     * @throws Exception
     */
    public ApiUserUpdateMuteProto.ApiUserUpdateMuteResponse updateSiteMute(boolean mute) throws Exception {
        ApiUserUpdateMuteProto.ApiUserUpdateMuteRequest request = ApiUserUpdateMuteProto.ApiUserUpdateMuteRequest.newBuilder().setMute(mute).build();
        TransportPackageForResponse response = this.client.sendRequest(API_USER_UPDATE_MUTE, request);
        return ApiUserUpdateMuteProto.ApiUserUpdateMuteResponse.parseFrom(response.data.getData());
    }

    /**
     * User token
     * @param request
     * @return
     * @throws Exception
     */
    public ApiUserPushTokenProto.ApiUserPushTokenResponse pushUserToken(ApiUserPushTokenProto.ApiUserPushTokenRequest request) throws Exception {
        TransportPackageForResponse response = this.client.sendRequest(API_USER_PUSH_TOKEN, request);
        return ApiUserPushTokenProto.ApiUserPushTokenResponse.parseFrom(response.data.getData());
    }

    /**
     * 获取手机号
     * @return
     * @throws Exception
     */
    public ApiUserPhoneProto.ApiUserPhoneResponse getUserPhone() throws Exception {
        ApiUserPhoneProto.ApiUserPhoneRequest request = ApiUserPhoneProto.ApiUserPhoneRequest.newBuilder().build();
        TransportPackageForResponse response = this.client.sendRequest(API_USER_PHONE, request);
        return ApiUserPhoneProto.ApiUserPhoneResponse.parseFrom(response.data.getData());
    }

    /**
     * 查询好友
     * @param searchValue 查找的value
     * @return
     * @throws Exception
     */
    public ApiUserSearchProto.ApiUserSearchResponse searchUserProfile(String searchValue) throws Exception {
        ApiUserSearchProto.ApiUserSearchRequest request = ApiUserSearchProto.ApiUserSearchRequest.newBuilder().setId(searchValue).build();
        TransportPackageForResponse response = this.client.sendRequest(API_USER_SEARCH, request);
        return ApiUserSearchProto.ApiUserSearchResponse.parseFrom(response.data.getData());
    }
}
