package com.akaxin.client.api;

import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.proto.core.FileProto;
import com.akaxin.proto.site.ApiFileDownloadProto;
import com.akaxin.proto.site.ApiFileUploadProto;
import com.google.protobuf.ByteString;
import com.orhanobut.logger.Logger;
import com.windchat.im.socket.TransportPackageForResponse;

/**
 * Created by sssl on 08/06/2018.
 */

public class ApiClientForFile {

    public static final String ACTION_DOWNLOAD = "api.file.download";
    public static final String ACTION_UPLOAD = "api.file.upload";


    private ApiClient client = null;
    private String logTag = "";

    public ApiClientForFile(ApiClient client) {
        this.client = client;
        this.logTag = "ApiClient." + this.getClass().getName();
    }

    public ApiFileDownloadProto.ApiFileDownloadResponse downloadFile(String fileId, FileProto.FileType fileType) {
        try {
            ApiFileDownloadProto.ApiFileDownloadRequest request = ApiFileDownloadProto.ApiFileDownloadRequest.newBuilder()
                    .setFileId(fileId)
                    .build();
            TransportPackageForResponse tResponse = this.client.sendRequest(ACTION_DOWNLOAD, request);
            ApiFileDownloadProto.ApiFileDownloadResponse response = ApiFileDownloadProto.ApiFileDownloadResponse.parseFrom(tResponse.data.getData());
            return response;
        } catch (Exception e) {
            Logger.e(e);
            return null;
        }
    }


    // 这里有一个设计上的大BUG
    // 上传接口，是用来上传文件的，不应该接收width、height这类针对图片的处理逻辑
    // 先copy过来了，兼容老代码，Android攻城狮要把这个逻辑剥离到上层去
    public ApiFileUploadProto.ApiFileUploadResponse uploadFile(byte[] data, FileProto.FileType fileType, int width, int height, int length) {
        FileProto.FileDesc fileDesc = FileProto.FileDesc.newBuilder().build();
        FileProto.File file = FileProto.File.newBuilder()
                .setFileContent(ByteString.copyFrom(data))
                .setFileType(fileType)
                .build();

        if (fileType == FileProto.FileType.MESSAGE_IMAGE) {
            fileDesc = FileProto.FileDesc.newBuilder()
                    .setWidth(width)
                    .setHeight(height)
                    .build();
        } else if (fileType == FileProto.FileType.MESSAGE_VOICE) {
            fileDesc = FileProto.FileDesc.newBuilder()
                    .setLength(length)
                    .build();
        }

        ApiFileUploadProto.ApiFileUploadRequest request = ApiFileUploadProto.ApiFileUploadRequest.newBuilder()
                .setFile(file)
                .setFileDesc(fileDesc)
                .build();

        ApiFileUploadProto.ApiFileUploadResponse response = null;
        try {
            TransportPackageForResponse tResponse = this.client.sendRequest(ACTION_UPLOAD, request);
            response = ApiFileUploadProto.ApiFileUploadResponse.parseFrom(tResponse.data.getData());
        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(this.logTag, e.getMessage());
        }

        return response;
    }
}
