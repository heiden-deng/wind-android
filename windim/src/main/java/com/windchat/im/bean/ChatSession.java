package com.windchat.im.bean;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * 用户聊天界面会话BEAN
 */
public class ChatSession implements Parcelable {
    public static final int STATUS_NEW_SESSION = 1;//新增会话
    public static final int STATUS_HAS_MSG = 2;    //已经存在的会话

    public static final int TYPE_FRIEND_SESSION = 1;    //群组二人会话
    public static final int TYPE_GROUP_SESSION = 2;     //群组会话

    private String _id;
    private String chatSessionId;   //用户ID或者GroupID
    private String title = "";
    private String icon = "";
    private String latestMsg = "";
    private long latestTime;
    private String lastMsgId = "";   //最后一条消息的id
    private int type;
    private int status;
    private int unreadNum;
    private String sessionGoto = ""; //会话跳转
    private String groupId = "";
    private String desc = "";//【绝密消息】,[图片消息】
    private boolean openTS;
    private String editText = "";
    private boolean mute;

    public ChatSession() {
    }

    protected ChatSession(Parcel in) {
        icon = in.readString();
        title = in.readString();
        latestMsg = in.readString();
        latestTime = in.readLong();
        lastMsgId = in.readString();
    }

    public static final Creator<ChatSession> CREATOR = new Creator<ChatSession>() {
        @Override
        public ChatSession createFromParcel(Parcel in) {
            return new ChatSession(in);
        }

        @Override
        public ChatSession[] newArray(int size) {
            return new ChatSession[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        icon = in.readString();
        title = in.readString();
        latestMsg = in.readString();
        latestTime = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(icon);
        dest.writeString(title);
        dest.writeString(latestMsg);
        dest.writeLong(latestTime);
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getChatSessionId() {
        return chatSessionId;
    }

    public void setChatSessionId(String chatSessionId) {
        this.chatSessionId = chatSessionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLatestMsg() {
        return latestMsg;
    }

    public void setLatestMsg(String latestMsg) {
        this.latestMsg = latestMsg;
    }

    public long getLatestTime() {
        return latestTime;
    }

    public void setLatestTime(long latestTime) {
        this.latestTime = latestTime;
    }

    public String getLastMsgId() {
        return lastMsgId;
    }

    public void setLastMsgId(String lastMsgId) {
        this.lastMsgId = lastMsgId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getUnreadNum() {
        return unreadNum;
    }

    public void setUnreadNum(int unreadNum) {
        this.unreadNum = unreadNum;
    }

    public String getSessionGoto() {
        return sessionGoto;
    }

    public void setSessionGoto(String sessionGoto) {
        this.sessionGoto = sessionGoto;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }

    public boolean isOpenTS() {
        return openTS;
    }

    public void setOpenTS(boolean openTS) {
        this.openTS = openTS;
    }

    public String getEditText() {
        return editText;
    }

    public void setEditText(String editText) {
        this.editText = editText;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    @Override
    public String toString() {
        return "";
    }
}
