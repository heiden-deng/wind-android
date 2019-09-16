package com.akaxin.client.api;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.ServerConfig;
import com.akaxin.client.util.ClientTypeHepler;
import com.akaxin.client.util.DeviceUtils;
import com.akaxin.proto.core.PhoneProto;
import com.akaxin.proto.platform.ApiPlatformLoginProto;
import com.akaxin.proto.platform.ApiPlatformLogoutProto;
import com.akaxin.proto.platform.ApiPlatformTopSecretProto;
import com.akaxin.proto.platform.ApiPushAuthProto;
import com.akaxin.proto.platform.ApiUserRealNameProto;
import com.akaxin.proto.site.ApiPlatformRegisterByPhoneProto;
import com.windchat.im.socket.TransportPackageForResponse;

/**
 * Created by Mr.kk on 2018/6/13.
 * This Project was client-android
 */

public class ApiClientForPlatform {
    private static String TAG = ApiClientForPlatform.class.getSimpleName();
    private String logTag = "ApiClientForPlatform";
    private ApiClient client = null;


    private static final String API_PLATFORM_REGISTER_BY_PHONE = "api.platform.registerByPhone";
    private static final String API_PLATFORM_LOGIN = "api.platform.login";
    private static final String API_PLATFORM_TOP_SECRET = "api.platform.topSecret";
    private static final String API_PLATFORM_LOGOUT = "api.platform.logout";
    private static final String API_PUSH_AUTH = "api.push.auth";
    private static final String API_USER_REAL_NAME = "api.user.realName";

    public ApiClientForPlatform(ApiClient client) {
        this.client = client;
        this.logTag = "ApiClient." + this.getClass().getName();
    }


    /**
     * 获取平台站点
     *
     * @return
     */
    public static Site getPlatformSite() {
        Site platformSite = new Site();
        platformSite.setSiteHost(ServerConfig.PLATFORM_ADDRESS);
        platformSite.setSitePort(ServerConfig.PLATFORM_PROT);
        return platformSite;
    }


    /**
     * 手机号注册平台
     *
     * @param phone
     * @param userPrivKeyPem
     * @param userPubKeyPem
     * @param verifyCode
     * @return
     * @throws Exception
     */
    public ApiPlatformRegisterByPhoneProto.ApiPlatformRegisterByPhoneResponse registerPlatform(String phone, String userPrivKeyPem, String userPubKeyPem, String verifyCode, int VcType) throws Exception {
        ApiPlatformRegisterByPhoneProto.ApiPlatformRegisterByPhoneRequest request = ApiPlatformRegisterByPhoneProto.ApiPlatformRegisterByPhoneRequest.newBuilder()
                .setPhoneId(phone)
                .setUserIdPrik(userPrivKeyPem)
                .setUserIdPubk(userPubKeyPem)
                .setPushToken(ClientTypeHepler.getPushToken())
                .setPhoneVerifyCode(verifyCode)
                .setVcType(VcType)
                .setCountryCode(ServerConfig.CHINA_COUNTRY_CODE)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_PLATFORM_REGISTER_BY_PHONE, request);
        return ApiPlatformRegisterByPhoneProto.ApiPlatformRegisterByPhoneResponse.parseFrom(response.data.getData());
        /**
         * 匿名登实名
         */
    }


    /**
     * Platform logout
     *
     * @return
     * @throws Exception
     */
    public ApiPlatformLogoutProto.ApiPlatformLogoutResponse platformLogout() throws Exception {
        ApiPlatformLogoutProto.ApiPlatformLogoutRequest request = ApiPlatformLogoutProto.ApiPlatformLogoutRequest.newBuilder()
                .setDeviceIdPubk(ZalyApplication.getCfgSP().getKey(Configs.DEVICE_PUB_KEY))
                .setUserIdPubk(ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY)).build();
        TransportPackageForResponse response = this.client.sendRequest(API_PLATFORM_LOGOUT, request);
        return ApiPlatformLogoutProto.ApiPlatformLogoutResponse.parseFrom(response.data.getData());
    }

    /**
     * 登录平台
     *
     * @param userSignBase64
     * @param deviceSignBase64
     * @return
     * @throws Exception
     */
    public ApiPlatformLoginProto.ApiPlatformLoginResponse loginPlatform(String userSignBase64, String deviceSignBase64) throws Exception {
        ApiPlatformLoginProto.ApiPlatformLoginRequest request = ApiPlatformLoginProto.ApiPlatformLoginRequest.newBuilder()
                .setUserDeviceIdPubk(ZalyApplication.getCfgSP().getKey(Configs.DEVICE_PUB_KEY))
                .setUserIdSignBase64(userSignBase64)
                .setUserIdPubk(ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY))
                .setUserDeviceIdSignBase64(deviceSignBase64)
                .setUserDeviceName(DeviceUtils.getDeviceName())
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_PLATFORM_LOGIN, request);
        return ApiPlatformLoginProto.ApiPlatformLoginResponse.parseFrom(response.data.getData());
    }

    /**
     * topSecret
     *
     * @return
     * @throws Exception
     */
    public ApiPlatformTopSecretProto.ApiPlatformTopSecretResponse getTopSecret(Site site) throws Exception {
        ApiPlatformTopSecretProto.ApiPlatformTopSecretRequest request = ApiPlatformTopSecretProto.ApiPlatformTopSecretRequest.newBuilder()
                .setSiteHost(site.getSiteHost())
                .setSitePort(site.getSitePort() + "")
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_PLATFORM_TOP_SECRET, request);
        return ApiPlatformTopSecretProto.ApiPlatformTopSecretResponse.parseFrom(response.data.getData());
    }


    public ApiPushAuthProto.ApiPushAuthResponse pushAuth(Site site, String userToken) throws Exception {
        ApiPushAuthProto.ApiPushAuthRequest request = ApiPushAuthProto.ApiPushAuthRequest.newBuilder()
                .setSiteAddress(site.getSiteHost())
                .setSitePort(site.getSitePort() + "")
                .setUserToken(userToken)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_PUSH_AUTH, request);
        return ApiPushAuthProto.ApiPushAuthResponse.parseFrom(response.data.getData());
    }

    /**
     * 实名认证
     *
     * @param phone
     * @param verifyCode
     * @return
     * @throws Exception
     */
    public ApiUserRealNameProto.ApiUserRealNameResponse verifyIdentity(String phone, String verifyCode) throws Exception {
        ApiUserRealNameProto.ApiUserRealNameRequest request = ApiUserRealNameProto.ApiUserRealNameRequest.newBuilder()
                .setPhoneId(phone)
                .setPhoneVerifyCode(verifyCode)
                .setUserIdPrik(ZalyApplication.getCfgSP().getKey(Configs.USER_PRI_KEY))
                .setUserIdPubk(ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY))
                .setCountryCode(ServerConfig.CHINA_COUNTRY_CODE)
                .setVcType(PhoneProto.VCType.PHONE_REALNAME_VALUE)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_USER_REAL_NAME, request);
        return ApiUserRealNameProto.ApiUserRealNameResponse.parseFrom(response.data.getData());

    }

}
