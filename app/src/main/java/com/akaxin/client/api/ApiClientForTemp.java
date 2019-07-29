package com.akaxin.client.api;

import com.akaxin.client.socket.TransportPackageForResponse;
import com.akaxin.proto.platform.ApiTempDownloadProto;
import com.akaxin.proto.platform.ApiTempUploadProto;
import com.google.protobuf.ByteString;

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
     * 在服务器上申请临时存储空间
     * @param spaceKey
     * @param SHA1UserPubKey
     * @return
     * @throws Exception
     */
    public ApiTempUploadProto.ApiTempUploadResponse applyTempSpace(String spaceKey,String SHA1UserPubKey) throws Exception {
        ApiTempUploadProto.ApiTempUploadRequest request = ApiTempUploadProto.ApiTempUploadRequest.newBuilder()
                .setName(spaceKey)
                .setContent(ByteString.copyFromUtf8(SHA1UserPubKey))
                .build();
        TransportPackageForResponse response = this.client.sendRequest(API_TEMP_UPLOAD, request);
        return ApiTempUploadProto.ApiTempUploadResponse.parseFrom(response.data.getData());

    }

    /**
     * 从临时申请存储空间下载内容
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
