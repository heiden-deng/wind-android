package com.windchat.im.message;

import com.windchat.proto.core.CoreProto;

import org.json.JSONObject;

public class GroupAudioMessage extends Message {

    private int msgType = CoreProto.MsgType.GROUP_VOICE_VALUE;

    private String groupId;

    private String audioId;
    private long audioTime;
    private String audioFilePath;

    public GroupAudioMessage() {
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

    public String getAudioId() {
        return audioId;
    }

    public void setAudioId(String audioId) {
        this.audioId = audioId;
    }

    public long getAudioTime() {
        return audioTime;
    }

    public void setAudioTime(long audioTime) {
        this.audioTime = audioTime;
    }

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public static String toJSON(GroupAudioMessage info) {
        String jsonStr = null;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("audioId", info.getAudioId());
            jsonObject.put("audioTime", info.getAudioTime());
            jsonObject.put("audioFilePath", info.getAudioFilePath());
            jsonStr = jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonStr;
    }

    public static GroupAudioMessage parseJSON(String jsonStr) {
        try {
            GroupAudioMessage u2AudioMessage = new GroupAudioMessage();
            JSONObject jsonObject = new JSONObject(jsonStr);
            u2AudioMessage.setAudioId(jsonObject.optString("audioId"));
            u2AudioMessage.setAudioTime(jsonObject.optLong("audioTime"));
            u2AudioMessage.setAudioFilePath(jsonObject.optString("audioFilePath"));
            return u2AudioMessage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
