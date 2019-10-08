package com.windchat.im.message;

import android.provider.BaseColumns;

import com.windchat.proto.core.CoreProto;

import java.util.Arrays;

public class GroupMapMessage extends Message {

    private int msgType = CoreProto.MsgType.GROUP_MAP_VALUE;

    //群组字段
    private String groupId;

    public GroupMapMessage() {
        this.chatType = ChatType.MSG_GROUP;
    }


    @Override
    public int getMsgType() {
        return msgType;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "GroupMapMessage{" +
                "msgType=" + msgType +
                ", msgId='" + msgId + '\'' +
                ", groupId='" + groupId + '\'' +
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
                ", groupId='" + groupId + '\'' +
                ", secretData=" + Arrays.toString(secretData) +
                '}';
    }
}

