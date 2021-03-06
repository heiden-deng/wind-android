package com.windchat.im.message;

import com.windchat.proto.core.CoreProto;

import java.util.Arrays;

public class U2TextMessage extends Message {

    private int msgType = CoreProto.MsgType.TEXT_VALUE;

    public U2TextMessage() {
        this.chatType = ChatType.MSG_U2;
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

