package com.windchat.im.bean;


import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;


public class ImageInfo {

    public static final int STATUS_UPLOADING = 1;
    public static final int STATUS_SEND = 2;

    public static final int STATUS_RECEIVE_NO_DOWNLOAD = 3;
    public static final int STATUS_RECEIVE_DOWNLOAD = 4;
    public static final int STATUS_RECEIVE_DOWNLOAD_FAIL = 5;

    private String fileId;
    private long fileLength;//bytes num
    private String filePath;
    private int status;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static String toJSON(ImageInfo info) {
        if (info == null) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fileId", info.getFileId());
            jsonObject.put("fileLength", info.getFileLength());
            jsonObject.put("filePath", info.getFilePath());
            jsonObject.put("status", info.getStatus());
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ImageInfo parseJSON(String content) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(content);
            ImageInfo imageInfo = new ImageInfo();
            imageInfo.setFileId(jsonObject.optString("fileId"));
            imageInfo.setFileLength(jsonObject.optLong("fileLength"));
            imageInfo.setFilePath(jsonObject.optString("filePath"));
            imageInfo.setStatus(jsonObject.optInt("status"));
            return imageInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
