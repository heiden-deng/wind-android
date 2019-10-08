package com.windchat.im.message;

import com.windchat.proto.core.CoreProto;

import java.util.Arrays;

public class U2NoticeMessage extends Message {

    private int msgType = CoreProto.MsgType.NOTICE_VALUE;

    protected String siteFriendId;

    public U2NoticeMessage() {
        this.chatType = ChatType.MSG_U2;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getSiteFriendId() {
        return siteFriendId;
    }

    public void setSiteFriendId(String siteFriendId) {
        this.siteFriendId = siteFriendId;
    }

    @Override
    public String toString() {
        return "U2TextMessage{" +
                "msgType=" + msgType +
                ", msgId='" + msgId + '\'' +
                ", msgPointer=" + msgPointer +
                ", siteUserId='" + siteUserId + '\'' +
                ", groupId='" + siteFriendId + '\'' +
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
}

