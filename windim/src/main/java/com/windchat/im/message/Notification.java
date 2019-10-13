package com.windchat.im.message;

import com.windchat.proto.core.CoreProto;

import java.util.Arrays;

public class Notification extends Message {

    private int msgType = CoreProto.MsgType.NOTICE_VALUE;

    public Notification() {
        this.chatType = ChatType.NOTIFICATION;
    }

    public int getMsgType() {
        return msgType;
    }

    @Override
    public String toString() {
        return "U2TextMessage{" +
                "msgType=" + msgType +
                ", msgId='" + msgId + '\'' +
                ", msgPointer=" + msgPointer +
                ", siteUserId='" + siteUserId + '\'' +
                ", groupId='" + siteToId + '\'' +
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

