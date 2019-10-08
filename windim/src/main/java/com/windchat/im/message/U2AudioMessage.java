package com.windchat.im.message;

import com.windchat.proto.core.CoreProto;

import org.json.JSONObject;

public class U2AudioMessage extends Message {


    private int msgType = CoreProto.MsgType.VOICE_VALUE;

    public static final int NONE_DOWNLOAD = -1;
    public static final int DOWNLOAD_FAIL = -2;

    private String siteFriendId;

    private String audioId;
    private long audioTime;
    private String audioFilePath;

    public U2AudioMessage() {
        this.chatType = ChatType.MSG_U2;
    }

    @Override
    public int getMsgType() {
        return msgType;
    }

    public String getSiteFriendId() {
        return siteFriendId;
    }

    public void setSiteFriendId(String siteFriendId) {
        this.siteFriendId = siteFriendId;
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

    public static String toJSON(U2AudioMessage info) {
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

    public static U2AudioMessage parseJSON(String jsonStr) {
        try {
            U2AudioMessage u2AudioMessage = new U2AudioMessage();
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
