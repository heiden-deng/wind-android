package com.windchat.im.message;

import android.provider.BaseColumns;

import com.windchat.proto.core.CoreProto;

public class U2MapMessage extends Message {

    private int msgType = CoreProto.MsgType.U2_MAP_VALUE;
    private String siteFriendId;

    public U2MapMessage() {
        this.chatType = ChatType.MSG_U2;
    }

    @Override
    public int getMsgType() {
        return msgType;
    }

    public String getSiteFriendId() {
        return siteFriendId;
    }

    public void setSiteFriendId(String siteFriendId) {
        this.siteFriendId = siteFriendId;
    }

    public static U2MapMessage copyMessage(U2MapMessage msg) {
        U2MapMessage message = new U2MapMessage();
        if (msg != null) {
            message.setMsgId(msg.getMsgId());
            message.setSiteUserId(msg.getSiteUserId());
            message.setContent(msg.getContent());
            message.setMsgTime(msg.getMsgTime());
            message.msgType = msg.getMsgType();
            message.setMsgStatus(msg.getMsgStatus());
            message.setSecret(msg.isSecret());
            message.setMsgTsk(msg.getMsgTsk());
            message.setChatSessionId(msg.getChatSessionId());
            message.setImg(msg.getImg());
            message.setToDeviceId(msg.getToDeviceId());
        }
        return message;
    }

    public String toString() {
        return "";
    }

}

