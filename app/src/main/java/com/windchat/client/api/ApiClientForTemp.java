package com.windchat.client.api;

import com.google.protobuf.ByteString;
import com.windchat.im.socket.TransportPackageForResponse;
import com.windchat.proto.platform.ApiTempDownloadProto;

/**
 * Created by Mr.kk on 2018/6/14.
 * This Project was client-android
 */

public class ApiClientForTemp {
    private ApiClient client = null;
    private String logTag = "ApiClientForTemp";


    private static final String API_TEMP_UPLOAD = "api.temp.upload";
    private static final String API_TEMP_DOWNLOAD = "api.temp.download";


    public ApiClientForTemp(ApiClient client) {
        this.client = client;
        this.logTag = "ApiClient." + this.getClass().getName();
    }

    /**
     * 从临时申请存储空间下载内容
     *
     * @param spaceKey
     * @return
     * @throws Exception
     */
    public ApiTempDownloadProto.ApiTempDownloadResponse getTempSpaceContent(String spaceKey) throws Exception {
        ApiTempDownloadProto.ApiTempDownloadRequest request = ApiTempDownloadProto.ApiTempDownloadRequest.newBuilder()
                .setName(spaceKey)
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_TEMP_DOWNLOAD, request);
        return ApiTempDownloadProto.ApiTempDownloadResponse.parseFrom(response.data.getData());
    }
}
