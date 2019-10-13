package com.windchat.im.message;


import com.windchat.proto.core.CoreProto;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;


public class U2ImageMessage extends Message {


    private int msgType = CoreProto.MsgType.IMAGE_VALUE;

    public static final int STATUS_UPLOADING = 1;
    public static final int STATUS_SEND = 2;

    public static final int STATUS_RECEIVE_NO_DOWNLOAD = 3;
    public static final int STATUS_RECEIVE_DOWNLOAD = 4;
    public static final int STATUS_RECEIVE_DOWNLOAD_FAIL = 5;

    private String fileId;
    private long fileLength;//bytes num
    private String filePath;
    private int status;

    public U2ImageMessage() {
        this.chatType = ChatType.MSG_U2;
    }

    @Override
    public int getMsgType() {
        return msgType;
    }

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

    public static String toJSON(U2ImageMessage info) {
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

    public static U2ImageMessage parseJSON(String content) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(content);
            U2ImageMessage u2ImageMessage = new U2ImageMessage();
            u2ImageMessage.setFileId(jsonObject.optString("fileId"));
            u2ImageMessage.setFileLength(jsonObject.optLong("fileLength"));
            u2ImageMessage.setFilePath(jsonObject.optString("filePath"));
            u2ImageMessage.setStatus(jsonObject.optInt("status"));
            return u2ImageMessage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
