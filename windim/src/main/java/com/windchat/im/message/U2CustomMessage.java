package com.windchat.im.message;

import android.provider.BaseColumns;

import com.windchat.proto.core.CoreProto;

/**
 * 自定义消息类型
 */
public class U2CustomMessage extends Message {

    private int msgType = CoreProto.MsgType.U2_WEB_VALUE;

    public U2CustomMessage() {
        this.chatType = ChatType.MSG_U2;
    }

    @Override
    public int getMsgType() {
        return msgType;
    }

}

