package com.akaxin.client.im.files;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.akaxin.client.api.ApiClient;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.proto.core.CoreProto;
import com.akaxin.proto.core.FileProto;
import com.akaxin.proto.site.ApiFileUploadProto;
import com.google.protobuf.ByteString;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.Site;
import com.akaxin.client.util.data.StringUtils;
import com.windchat.im.socket.ConnectionConfig;
import com.windchat.im.socket.TransportPackageForResponse;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yichao on 2017/10/17.
 */

public class IMFileUtils {

    public static final String TAG = "IMFileUtils";
    public static final String ACTION_DOWNLOAD = "api.file.download";
    public static final String ACTION_UPLOAD = "api.file.upload";


    public static byte[] resizeImageByWidth(byte[] data, int limitWidth) {
        try {
            Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
            //获取图片资源的高

            int imageHeight = image.getHeight();
            //获取图片资源的宽

            int imageWidth  = image.getWidth();

            if (imageWidth > limitWidth) {
                imageHeight = imageHeight * limitWidth / imageWidth;
                imageWidth = limitWidth;
            }

            Bitmap newImage = Bitmap.createScaledBitmap(image, imageWidth, imageHeight, false);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            newImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        } catch (Exception e) {
            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
        }
        return data;
    }

    public static ApiFileUploadProto.ApiFileUploadResponse uploadImg(Site site, byte[] data, FileProto.FileType fileType) {
        return uploadFile(data, fileType, ConnectionConfig.getConnectionCfg(site));
    }

    /**
     * 上传图片，区分站点
     *
     * @param data
     * @param fileType
     * @param site
     * @return
     */
    public static ApiFileUploadProto.ApiFileUploadResponse uploadFile(byte[] data, FileProto.FileType fileType, Site site) {
        return uploadFile(data, fileType, ConnectionConfig.getConnectionCfg(site));
    }

    /**
     * 上传图片
     *
     * @param data
     */
    public static ApiFileUploadProto.ApiFileUploadResponse uploadFile(byte[] data, FileProto.FileType fileType, int width, int height, int length, Site site) {
        return ApiClient.getInstance(ConnectionConfig.getConnectionCfg(site)).getFileApi().uploadFile(data, fileType, width, height, length);
    }

    /**
     * 上传图片
     *
     * 这一个我是什么时候改的？
     *
     * @param data
     */
    public static ApiFileUploadProto.ApiFileUploadResponse uploadFile(byte[] data, FileProto.FileType fileType,  ConnectionConfig connectionConfig) {
        Map<Integer, String> header = new HashMap<>();

        if (!StringUtils.isEmpty(connectionConfig.getSessionId())) {
            header.put(CoreProto.HeaderKey.CLIENT_SOCKET_SITE_SESSION_ID_VALUE, connectionConfig.getSessionId());
        }

        FileProto.File file = FileProto.File.newBuilder()
                .setFileContent(ByteString.copyFrom(data))
                .setFileType(fileType)
                .build();
        FileProto.FileDesc fileDesc = FileProto.FileDesc.newBuilder().build();

        try {
            ApiFileUploadProto.ApiFileUploadRequest request = ApiFileUploadProto.ApiFileUploadRequest.newBuilder()
                    .setFile(file)
                    .setFileDesc(fileDesc)
                    .build();

            TransportPackageForResponse tResponse = ApiClient.getInstance(connectionConfig).sendRequest(ACTION_UPLOAD, request);
            ApiFileUploadProto.ApiFileUploadResponse response = ApiFileUploadProto.ApiFileUploadResponse.parseFrom( tResponse.data.getData() );
            return response;
        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
        }
        return null;
    }

}
