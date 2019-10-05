package com.akaxin.client.api;

import com.akaxin.client.Configs;
import com.akaxin.proto.core.PhoneProto;
import com.akaxin.proto.platform.ApiPhoneApplyTokenProto;
import com.akaxin.proto.platform.ApiPhoneLoginProto;
import com.akaxin.proto.platform.ApiPhoneVerifyCodeProto;
import com.windchat.im.socket.TransportPackageForResponse;

/**
 * Created by Mr.kk on 2018/6/14.
 * This Project was client-android
 */

public class ApiClientForPhone {
    private static String TAG = ApiClientForPhone.class.getSimpleName();
    private String logTag = "ApiClientForPhone";
    private ApiClient client = null;


    private static final String API_PHONE_VERIFY_CODE = "api.phone.verifyCode";
    private static final String API_PHONE_LOGIN = "api.phone.login";
    private static final String API_PHONE_APPLY_TOKEN = "api.phone.applyToken";

    public ApiClientForPhone(ApiClient client) {
        this.client = client;
        this.logTag = "ApiClient." + this.getClass().getName();
    }

    /**
     * 获取平台验证码
     *
     * @param phone
     * @param vcType
     * @param countryCode
     * @return
     * @throws Exception
     */
    public ApiPhoneVerifyCodeProto.ApiPhoneVerifyCodeResponse getVerifyCode(String phone, int vcType, String countryCode) throws Exception {
        ApiPhoneVerifyCodeProto.ApiPhoneVerifyCodeRequest request = ApiPhoneVerifyCodeProto.ApiPhoneVerifyCodeRequest.newBuilder()
                .setPhoneId(phone)
                .setVcType(vcType)
                .setCountryCode(countryCode)
                .build();

        TransportPackageForResponse response = this.client.sendRequest(API_PHONE_VERIFY_CODE, request);
        return ApiPhoneVerifyCodeProto.ApiPhoneVerifyCodeResponse.parseFrom(response.data.getData());
    }

    /**
     * 根据手机登录平台
     *
     * @param phone
     * @param verifyCode
     * @return
     * @throws Exception
     */
    public ApiPhoneLoginProto.ApiPhoneLoginResponse loginPlatformByPhone(String phone, String verifyCode) throws Exception {
        ApiPhoneLoginProto.ApiPhoneLoginRequest request = ApiPhoneLoginProto.ApiPhoneLoginRequest.newBuilder()
                .setPhoneVerifyCode(verifyCode)
                .setPhoneId(phone)
                .setVcType(PhoneProto.VCType.PHONE_LOGIN_VALUE)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_PHONE_LOGIN, request);
        return ApiPhoneLoginProto.ApiPhoneLoginResponse.parseFrom(response.data.getData());
    }

    /**
     * 获取平台token
     *
     * @param siteAddress
     * @return
     * @throws Exception
     */
    public ApiPhoneApplyTokenProto.ApiPhoneApplyTokenResponse getPlatformToken(String siteAddress) throws Exception {
        ApiPhoneApplyTokenProto.ApiPhoneApplyTokenRequest request = ApiPhoneApplyTokenProto.ApiPhoneApplyTokenRequest.newBuilder()
                .setGlobalUserId(Configs.getGlobalUserId())
                .setSiteAddress(siteAddress)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_PHONE_APPLY_TOKEN, request);
        return ApiPhoneApplyTokenProto.ApiPhoneApplyTokenResponse.parseFrom(response.data.getData());
    }
}
