package com.akaxin.client.api;

import com.akaxin.client.bean.Site;
import com.akaxin.client.socket.TransportPackageForResponse;
import com.akaxin.proto.platform.ApiSettingDeleteUserTokenProto;
import com.akaxin.proto.platform.ApiSettingSiteMuteProto;
import com.akaxin.proto.platform.ApiSettingUpdateSiteMuteProto;

/**
 * Created by Mr.kk on 2018/6/14.
 * This Project was client-android
 */

public class ApiClientForSetting {
    private ApiClient client = null;
    private String logTag = "ApiClientForFriend";


    private static final String API_SETTING_SITE_MUTE = "api.setting.siteMute";
    private static final String API_SETTING_UPDATE_SITE_MUTE = "api.setting.updateSiteMute";
    private static final String API_SETTING_DELETE_USER_TOKEN = "api.setting.deleteUserToken";

    public ApiClientForSetting(ApiClient client) {
        this.client = client;
        this.logTag = "ApiClient." + this.getClass().getName();
    }

    /**
     * 平台查询站点设置
     *
     * @param site
     * @return
     * @throws Exception
     */
    public ApiSettingSiteMuteProto.ApiSettingSiteMuteResponse getSiteSetting(Site site) throws Exception {
        ApiSettingSiteMuteProto.ApiSettingSiteMuteRequest request = ApiSettingSiteMuteProto.ApiSettingSiteMuteRequest.newBuilder()
                .setSiteHost(site.getSiteHost())
                .setSitePort(Integer.parseInt(site.getSitePort()))
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_SETTING_SITE_MUTE, request);
        return ApiSettingSiteMuteProto.ApiSettingSiteMuteResponse.parseFrom(response.data.getData());
    }

    /**
     * 平台更新站点设置
     *
     * @param site
     * @param mute
     * @return
     * @throws Exception
     */
    public ApiSettingUpdateSiteMuteProto.ApiSettingUpdateSiteMuteResponse updateSiteSetting(Site site, boolean mute) throws Exception {
        ApiSettingUpdateSiteMuteProto.ApiSettingUpdateSiteMuteRequest request
                = ApiSettingUpdateSiteMuteProto.ApiSettingUpdateSiteMuteRequest.newBuilder()
                .setSiteHost(site.getSiteHost())
                .setSitePort(Integer.parseInt(site.getSitePort()))
                .setMute(mute).build();
        TransportPackageForResponse response = this.client.sendRequest(API_SETTING_UPDATE_SITE_MUTE, request);
        return ApiSettingUpdateSiteMuteProto.ApiSettingUpdateSiteMuteResponse.parseFrom(response.data.getData());
    }

    /**
     * 从平台删除user token
     * @param site
     * @return
     * @throws Exception
     */
    public ApiSettingDeleteUserTokenProto.ApiSettingDeleteUserTokenResponse deleteUserToken(Site site) throws Exception {
        ApiSettingDeleteUserTokenProto.ApiSettingDeleteUserTokenRequest request = ApiSettingDeleteUserTokenProto.ApiSettingDeleteUserTokenRequest.newBuilder()
                .setSiteHost(site.getSiteHost())
                .setSitePort(Integer.valueOf(site.getSitePort()))
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_SETTING_DELETE_USER_TOKEN, request);
        return ApiSettingDeleteUserTokenProto.ApiSettingDeleteUserTokenResponse.parseFrom(response.data.getData());
    }

}
