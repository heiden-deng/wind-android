package com.akaxin.client.api;

import com.akaxin.client.ZalyApplication;
import com.akaxin.proto.site.ApiDeviceBoundListProto;
import com.akaxin.proto.site.ApiDeviceListProto;
import com.akaxin.proto.site.ApiDeviceProfileProto;
import com.windchat.im.socket.TransportPackageForResponse;

/**
 * Created by Mr.kk on 2018/6/14.
 * This Project was client-android
 */

public class ApiClientForDevice {
    private ApiClient client = null;
    private String logTag = "";

    private static final String API_DEVICE_LIST = "api.device.list";
    private static final String API_DEVICE_PROFILE = "api.device.profile";
    private static final String API_DEVICE_BOUND_LIST = "api.device.boundList";


    public ApiClientForDevice(ApiClient client) {
        this.client = client;
        this.logTag = "ApiClient."+this.getClass().getName();
    }

    /**
     * 获取对方的设备列表
     * @param friendSiteUserId
     * @return
     * @throws Exception
     */
    public ApiDeviceListProto.DeviceListInfoResponse getDeviceListInfo(String friendSiteUserId) throws Exception {
        ApiDeviceListProto.DeviceListInfoRequest request = ApiDeviceListProto.DeviceListInfoRequest.newBuilder()
                .setSiteUserId(friendSiteUserId)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_DEVICE_LIST, request);
        return ApiDeviceListProto.DeviceListInfoResponse.parseFrom(response.data.getData());
    }

    /**
     * 获取设备详情
     * @param deviceId
     * @return
     * @throws Exception
     */
    public ApiDeviceProfileProto.ApiDeviceProfileResponse getDeviceDetail(String deviceId) throws Exception {
        ApiDeviceProfileProto.ApiDeviceProfileRequest request = ApiDeviceProfileProto.ApiDeviceProfileRequest.newBuilder()
                .setDeviceId(deviceId)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_DEVICE_PROFILE, request);
        return ApiDeviceProfileProto.ApiDeviceProfileResponse.parseFrom(response.data.getData());
    }

    /**
     * 获取当前设备绑定列表
     * @return
     */
    public ApiDeviceBoundListProto.ApiDeviceBoundListResponse getBindDeviceList(String siteUserId) throws Exception {
        ApiDeviceBoundListProto.ApiDeviceBoundListRequest request = ApiDeviceBoundListProto.ApiDeviceBoundListRequest.newBuilder()
                .setSiteUserId(siteUserId)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_DEVICE_BOUND_LIST, request);
        return ApiDeviceBoundListProto.ApiDeviceBoundListResponse.parseFrom(response.data.getData());
    }
}
