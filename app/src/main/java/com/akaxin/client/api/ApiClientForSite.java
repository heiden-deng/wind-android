package com.akaxin.client.api;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.Site;
import com.akaxin.client.util.DeviceUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.proto.site.ApiSiteConfigProto;
import com.akaxin.proto.site.ApiSiteLoginProto;
import com.akaxin.proto.site.ApiSiteRegisterProto;
import com.windchat.im.socket.TransportPackageForResponse;

/**
 * Created by sssl on 12/06/2018.
 */


// 这个是从Connection里copy备份出来的，auth失败的时候，应该去更新session，但是之前的那份实现，直接耦合在connection离了
public class ApiClientForSite {


    private static final String API_SITE_CONFIG = "api.site.config";
    private static final String API_SITE_REGISTER = "api.site.register";
    private static final String API_SITE_LOGIN = "api.site.login";

    private ApiClient client = null;
    private String logTag = "ApiClientForSite";

    public ApiClientForSite(ApiClient client) {
        this.client = client;
        this.logTag = "ApiClient." + this.getClass().getName();
    }

    /**
     * 注册至“站点”
     *
     * @param request
     * @return
     * @throws Exception
     */
    public ApiSiteRegisterProto.ApiSiteRegisterResponse registerSiteByConfig(ApiSiteRegisterProto.ApiSiteRegisterRequest request) throws Exception {
        TransportPackageForResponse response = this.client.sendRequest(API_SITE_REGISTER, request);
        return ApiSiteRegisterProto.ApiSiteRegisterResponse.parseFrom(response.data.getData());
    }

    /**
     * 登录
     *
     * @param userSignBase64
     * @param deviceSignBase64
     * @param userToken
     * @param phoneToken
     * @return
     * @throws Exception
     */
    public ApiSiteLoginProto.ApiSiteLoginResponse loginSite(String userSignBase64, String deviceSignBase64, String userToken, String phoneToken) throws Exception {
        ApiSiteLoginProto.ApiSiteLoginRequest request = ApiSiteLoginProto.ApiSiteLoginRequest.newBuilder()
                .setUserIdPubk(ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY))
                .setUserIdSignBase64(userSignBase64)
                .setUserDeviceIdPubk(ZalyApplication.getCfgSP().getKey(Configs.DEVICE_PUB_KEY))
                .setUserDeviceIdSignBase64(deviceSignBase64)
                .setUserDeviceName(DeviceUtils.getDeviceName())
                .setUserToken(userToken)
                .setPhoneToken(phoneToken)
                .build();

        TransportPackageForResponse response = this.client.sendRequest(API_SITE_LOGIN, request);
        return ApiSiteLoginProto.ApiSiteLoginResponse.parseFrom(response.data.getData());
    }

    /**
     * 获取当前站点的信息
     *
     * @return
     * @throws Exception
     */
    public ApiSiteConfigProto.ApiSiteConfigResponse getSiteInfo() throws Exception {
        ApiSiteConfigProto.ApiSiteConfigRequest request = ApiSiteConfigProto.ApiSiteConfigRequest.newBuilder()
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_SITE_CONFIG, request);
        return ApiSiteConfigProto.ApiSiteConfigResponse.parseFrom(response.data.getData());
    }




}
