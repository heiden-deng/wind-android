package com.windchat.im.message;


import com.windchat.proto.core.CoreProto;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;


public class GroupImageMessage extends Message {

    private int msgType = CoreProto.MsgType.GROUP_IMAGE_VALUE;

    private String groupId;

    private String fileId;
    private long fileLength;//bytes num
    private String filePath;
    private int status;

    public GroupImageMessage() {
        this.chatType = ChatType.MSG_GROUP;
    }


    public int getMsgType() {
        return msgType;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    public static String toJSON(GroupImageMessage info) {
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

    public static GroupImageMessage parseJSON(String content) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(content);
            GroupImageMessage u2ImageMessage = new GroupImageMessage();
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
