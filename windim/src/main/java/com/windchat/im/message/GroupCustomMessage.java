package com.windchat.im.message;

import android.provider.BaseColumns;

import com.windchat.proto.core.CoreProto;

/**
 * 自定义消息类型
 */
public class GroupCustomMessage extends Message {


    private int msgType = CoreProto.MsgType.GROUP_WEB_VALUE;
    private String groupId;

    public GroupCustomMessage() {
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

    public String toString() {
        return "";
    }

}

