package com.windchat.im.bean;

import org.json.JSONObject;

/**
 * Created by yichao on 2017/11/6.
 */

public class AudioInfo {

    public static final int NONE_DOWNLOAD = -1;
    public static final int DOWNLOAD_FAIL = -2;

    private String audioId;
    private long audioTime;
    private String audioFilePath;

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

    public static String toJSON(AudioInfo info) {
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

    public static AudioInfo parseJSON(String jsonStr) {
        try {
            AudioInfo audioInfo = new AudioInfo();
            JSONObject jsonObject = new JSONObject(jsonStr);
            audioInfo.setAudioId(jsonObject.optString("audioId"));
            audioInfo.setAudioTime(jsonObject.optLong("audioTime"));
            audioInfo.setAudioFilePath(jsonObject.optString("audioFilePath"));
            return audioInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
