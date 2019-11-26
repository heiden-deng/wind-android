package com.akaxin.client.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.akaxin.proto.core.CoreProto;

/**
 * Created by yichao on 2017/10/10.
 */

public class Message extends com.windchat.im.message.Message implements Parcelable {

    public static final int MESSAGE_SEND = 100;                     // 发送的消息
    public static final int MESSAGE_RECEIVE = 200;                  // 接收的消息

    public static final int STATUS_SEND_FAILED_NOT_IN_GROUP = -2;   // 用户非群成员，群消息发送失败
    public static final int STATUS_SEND_FAILED_NOT_FRIEND = -1;     // 用户非好友关系，二人消息发送失败
    public static final int STATUS_SEND_FAILED = 0;                 // 其他情况发送失败
    public static final int STATUS_SENDING = 1;                     // 发送中
    public static final int STATUS_SEND_SUCCESS = 2;                // 发送成功
    public static final int STATUS_RECEIVE_NONE = 3;                // 收到的消息无状态
    public static final int STATUS_RECEIVE_UNREAD = 5;              // 收到的消息未读状态
    public static final int STATUS_RECEIVE_READ = 6;                // 收到的消息已读

    public static final long SYNC_MSG_STATUS_EXPIRE_TIME = 24 * 60 * 60 * 1000; ////有效期24小时

    private long _id;
    private String msgId;
    private long msgPointer;
    private String siteUserId = "";
    private String siteFriendId = "";
    private String chatSessionId = "";
    private String content = "";
    private int msgType = CoreProto.MsgType.UNRECOGNIZED.getNumber();//消息类型，保持proto中一致
    private String toDeviceId = "";
    private long sendMsgTime;//消息到达服务端时间
    private long msgTime;//消息到达服务端时间
    private int msgStatus;
    private boolean isSecret = false;
    private String msgTsk = "";
    private String img;
    private String userName = "";
    private String toDevicePubk = "";

    private int msgWidth = 0;
    private int msgHeight = 0;
    private String hrefUrl = "";


    //群组字段
    private String groupId;

    private byte[] secretData = new byte[1];

    public Message() {
    }

    public long getMsgPointer() {
        return msgPointer;
    }

    public void setMsgPointer(long msgPointer) {
        this.msgPointer = msgPointer;
    }

    public String getHrefUrl() {
        return hrefUrl;
    }

    public void setHrefUrl(String hrefUrl) {
        this.hrefUrl = hrefUrl;
    }


    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public boolean isSecret() {
        return isSecret;
    }

    public void setSecret(boolean secret) {
        isSecret = secret;
    }

    public String getChatSessionId() {
        return chatSessionId;
    }

    public String getToDevicePubk() {
        return toDevicePubk;
    }

    public void setToDevicePubk(String devicePubk) {
        toDevicePubk = devicePubk;
    }

    public void setChatSessionId(String chatSessionId) {
        this.chatSessionId = chatSessionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getSiteUserId() {
        return siteUserId;
    }

    public void setSiteUserId(String siteUserId) {
        this.siteUserId = siteUserId;
    }

    public String getGroupId() {
        return siteFriendId;
    }

    public void setGroupId(String groupId) {
        this.siteFriendId = groupId;
    }

    public long getSendMsgTime() {
        return sendMsgTime;
    }

    public void setSendMsgTime(long sendMsgTime) {
        this.sendMsgTime = sendMsgTime;
    }

    public long getMsgTime() {
        return msgTime;
    }

    public void setMsgTime(long msgTime) {
        this.msgTime = msgTime;
    }

    public int getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(int msgStatus) {
        this.msgStatus = msgStatus;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getMsgTsk() {
        return msgTsk;
    }

    public void setMsgTsk(String msgTsk) {
        this.msgTsk = msgTsk;
    }

    public String getToDeviceId() {
        return toDeviceId;
    }

    public void setToDeviceId(String toDeviceId) {
        this.toDeviceId = toDeviceId;
    }

    public byte[] getSecretData() {
        return secretData;
    }

    public void setSecretData(byte[] secretData) {
        this.secretData = secretData;
    }

    public void setMsgWidth(int width) {
        this.msgWidth = width;
    }

    public int getMsgWidth() {
        return this.msgWidth;
    }

    public void setMsgHeight(int height) {
        this.msgHeight = height;
    }

    public int getMsgHeight() {
        return this.msgHeight;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    protected Message(Parcel in) {
        msgId = in.readString();
        siteUserId = in.readString();
        siteFriendId = in.readString();
        content = in.readString();
        msgTime = in.readLong();
        msgType = in.readInt();
        msgStatus = in.readInt();
        isSecret = in.readByte() != 0;
        msgTsk = in.readString();
        img = in.readString();
        groupId = in.readString();
        toDeviceId = in.readString();
        chatSessionId = in.readString();
        secretData = new byte[in.readInt()];
        in.readByteArray(secretData);
        msgWidth = in.readInt();
        msgHeight = in.readInt();
        hrefUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msgId);
        dest.writeString(siteUserId);
        dest.writeString(siteFriendId);
        dest.writeString(content);
        dest.writeLong(msgTime);
        dest.writeInt(msgType);
        dest.writeInt(msgStatus);
        dest.writeByte((byte) (isSecret ? 1 : 0));
        dest.writeString(msgTsk);
        dest.writeString(img);
        dest.writeString(groupId);
        dest.writeString(toDeviceId);
        dest.writeString(chatSessionId);
        dest.writeInt(secretData.length);
        dest.writeByteArray(secretData);
        dest.writeInt(msgWidth);
        dest.writeInt(msgHeight);
        dest.writeString(hrefUrl);
    }

    public static class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "message_table";
        public static final String COLUMN_NAME_CONTENT = "content";
    }

    public static class Action {
        public static final String MSG_INFO = "msg_info";

        public static final int UPDATE_MSG_STATUS = 1;
        public static final int MSG_RECEIVE = 2;
        public static final int SHOW_SECRET_DEVICE = 3;
        public static final int U2_DELETE_MSG = 4;
        public static final int U2_RESEND_MSG = 5;
        public static final int IMG_PROGRESS = 9;

        public static final int GROUP_DELETE_MSG = 7;
        public static final int GROUP_RESEND_MSG = 8;
    }

    public static final String KEY_DEVICE_ID = "key_device_id";
    public static final String KEY_SITE_USER_ID = "key_site_user_id";

    public static Message copyMessage(com.windchat.im.message.Message msg) {

        Message message = new Message();
        if (msg != null) {
            message.setMsgId(msg.getMsgId());
            message.setSiteUserId(msg.getSiteUserId());
            message.setContent(msg.getContent());
            message.setMsgTime(msg.getMsgTime());
            message.setMsgType(msg.getMsgType());
            message.setMsgStatus(msg.getMsgStatus());
            message.setSecret(msg.isSecret());
            message.setMsgTsk(msg.getMsgTsk());
            message.setChatSessionId(msg.getChatSessionId());
            message.setImg(msg.getImg());
            message.setToDeviceId(msg.getToDeviceId());
            message.setSiteToId(msg.getSiteToId());
        }
        return message;
    }
}

