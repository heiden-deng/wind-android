package com.windchat.im.message;

import com.windchat.proto.core.CoreProto;

import java.util.Arrays;


public class Message {

    public static long timeMsgInterval = 1000 * 60 * 3;

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

    public static final String MSG_PROCESSS_RATE = "msg_process_rate";

    protected String msgId;
    protected long msgPointer;
    protected String siteUserId = "";
    protected String siteToId;

    protected String chatSessionId = "";
    protected ChatType chatType;

    protected String content = "";
    protected int msgType = CoreProto.MsgType.UNRECOGNIZED.getNumber();//消息类型，保持proto中一致
    protected String toDeviceId = "";
    protected long sendMsgTime;//消息到达服务端时间
    protected long msgTime;//消息到达服务端时间
    protected int msgStatus;
    protected boolean isSecret = false;
    protected String msgTsk = "";
    protected String img;
    protected String userName = "";
    protected String toDevicePubk = "";

    protected byte[] secretData = new byte[1];

    public Message() {
    }

    public long getMsgPointer() {
        return msgPointer;
    }

    public void setMsgPointer(long msgPointer) {
        this.msgPointer = msgPointer;
    }

    public int getMsgType() {
        return msgType;
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

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public String getSiteToId() {
        return siteToId;
    }

    public void setSiteToId(String siteToId) {
        this.siteToId = siteToId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "msgId='" + msgId + '\'' +
                ", msgPointer=" + msgPointer +
                ", siteUserId='" + siteUserId + '\'' +
                ", chatSessionId='" + chatSessionId + '\'' +
                ", content='" + content + '\'' +
                ", msgType=" + msgType +
                ", toDeviceId='" + toDeviceId + '\'' +
                ", sendMsgTime=" + sendMsgTime +
                ", msgTime=" + msgTime +
                ", msgStatus=" + msgStatus +
                ", isSecret=" + isSecret +
                ", msgTsk='" + msgTsk + '\'' +
                ", img='" + img + '\'' +
                ", userName='" + userName + '\'' +
                ", toDevicePubk='" + toDevicePubk + '\'' +
                ", secretData=" + Arrays.toString(secretData) +
                '}';
    }


    public enum ChatType {
        NOTIFICATION,
        MSG_U2,
        MSG_GROUP
    }

}

