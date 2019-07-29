package com.akaxin.client.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.akaxin.client.util.GsonUtils;

/**
 * Created by yichao on 2017/10/10.
 */

public class Session implements Parcelable {

    public static final int STATUS_NEW_SESSION = 1;
    public static final int STATUS_HAS_MSG = 2;

    public static final int TYPE_FRIEND_SESSION = 1;
    public static final int TYPE_GROUP_SESSION = 2;

    private String _id;
    private String image = "";
    private String title = "";
    private String desc;
    private long time;

    private String oppositeId;
    private int sessionType;
    private int sessionStatus;
    private String lastMsgId;
    private int unReadNum;
    private Boolean openTsChat;

    private  String chatSessionId;

    public Session() {
    }

    protected Session(Parcel in) {
        image = in.readString();
        title = in.readString();
        desc = in.readString();
        time = in.readLong();
    }

    public static final Creator<Session> CREATOR = new Creator<Session>() {
        @Override
        public Session createFromParcel(Parcel in) {
            return new Session(in);
        }

        @Override
        public Session[] newArray(int size) {
            return new Session[size];
        }
    };

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getLastMsgId() {
        return lastMsgId;
    }

    public void setLastMsgId(String lastMsgId) {
        this.lastMsgId = lastMsgId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        image = in.readString();
        title = in.readString();
        desc = in.readString();
        time = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(image);
        dest.writeString(title);
        dest.writeString(desc);
        dest.writeLong(time);
    }

    public String getOppositeId() {
        return oppositeId;
    }
    public String getChatSessionId() {
        return chatSessionId;
    }
    public void setChatSessionId(String chatSessionId) {
        this.chatSessionId = chatSessionId;
    }

    public String getOpenTsChat() {
        return chatSessionId;
    }
    public void setOpenTsChat(Boolean openTsChat) {
        this.openTsChat = openTsChat;
    }


    public void setOppositeId(String oppositeId) {
        this.oppositeId = oppositeId;
    }

    public int getSessionType() {
        return sessionType;
    }

    public void setSessionType(int sessionType) {
        this.sessionType = sessionType;
    }

    public int getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(int sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public int getUnReadNum() {
        return unReadNum;
    }

    public void setUnReadNum(int unReadNum) {
        this.unReadNum = unReadNum;
    }

    public String toString() {
        return GsonUtils.toJson(this);
    }
}
