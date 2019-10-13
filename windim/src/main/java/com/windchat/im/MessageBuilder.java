package com.windchat.im;

import com.google.protobuf.ByteString;
import com.windchat.im.message.Notification;
import com.windchat.im.message.U2AudioMessage;
import com.windchat.im.message.U2ImageMessage;
import com.windchat.im.message.Message;
import com.windchat.proto.core.CoreProto;
import com.windchat.proto.server.ImCtsMessageProto;


public class MessageBuilder {

    public static ImCtsMessageProto.ImCtsMessageRequest buildMessageRequest(Message message) {
        ImCtsMessageProto.ImCtsMessageRequest request = null;

        if (Message.ChatType.MSG_U2 == message.getChatType()) {
            //单人消息
            request = buildU2MessageRequest(message);
        } else if (Message.ChatType.MSG_GROUP == message.getChatType()) {
            //群消息消息
            request = buildGroupMessageRequest(message);
        } else if (Message.ChatType.NOTIFICATION == message.getChatType()) {
            // 消息推送通知
            request = buildNotificationRequest(message);
        }
        return request;
    }


    private static ImCtsMessageProto.ImCtsMessageRequest buildU2MessageRequest(Message message) {

        ImCtsMessageProto.ImCtsMessageRequest request = null;

        switch (message.getMsgType()) {
            case CoreProto.MsgType.TEXT_VALUE:
                CoreProto.MsgText msgText = CoreProto.MsgText.newBuilder()
                        .setMsgId(message.getMsgId())
                        .setSiteUserId(message.getSiteUserId())
                        .setSiteFriendId(message.getSiteToId())
                        .setText(ByteString.copyFrom(message.getContent().getBytes()))
                        .setTime(message.getMsgTime())
                        .build();
                request = ImCtsMessageProto.ImCtsMessageRequest.newBuilder()
                        .setType(CoreProto.MsgType.TEXT)
                        .setText(msgText)
                        .build();
                break;
            case CoreProto.MsgType.VOICE_VALUE:
                U2AudioMessage u2AudioMessage = U2AudioMessage.parseJSON(message.getContent());
                CoreProto.MsgVoice msgVoice = CoreProto.MsgVoice.newBuilder()
                        .setMsgId(message.getMsgId())
                        .setSiteUserId(message.getSiteUserId())
                        .setSiteFriendId(message.getSiteToId())
                        .setVoiceId(u2AudioMessage.getAudioId())
                        .setTime(message.getMsgTime())
                        .build();
                request = ImCtsMessageProto.ImCtsMessageRequest.newBuilder()
                        .setType(CoreProto.MsgType.VOICE)
                        .setVoice(msgVoice)
                        .build();
                break;
            case CoreProto.MsgType.IMAGE_VALUE:
                U2ImageMessage u2ImageMessage = U2ImageMessage.parseJSON(message.getContent());
                CoreProto.MsgImage msgImage = CoreProto.MsgImage.newBuilder()
                        .setMsgId(message.getMsgId())
                        .setSiteUserId(message.getSiteUserId())
                        .setSiteFriendId(message.getSiteToId())
                        .setImageId(u2ImageMessage.getFileId())
                        .setTime(message.getMsgTime())
                        .build();
                request = ImCtsMessageProto.ImCtsMessageRequest.newBuilder()
                        .setType(CoreProto.MsgType.IMAGE)
                        .setImage(msgImage)
                        .build();
                break;
            default:
                request = null;
        }

        return request;
    }


    private static ImCtsMessageProto.ImCtsMessageRequest buildGroupMessageRequest(Message message) {

        ImCtsMessageProto.ImCtsMessageRequest request = null;
        /** 群组消息 **/

        switch (message.getMsgType()) {
            case CoreProto.MsgType.GROUP_TEXT_VALUE:
                CoreProto.GroupText groupText = CoreProto.GroupText.newBuilder()
                        .setMsgId(message.getMsgId())
                        .setSiteUserId(message.getSiteUserId())
                        .setSiteGroupId(message.getSiteToId())
                        .setText(ByteString.copyFrom(message.getContent().getBytes()))
                        .setTime(message.getMsgTime())
                        .build();
                request = ImCtsMessageProto.ImCtsMessageRequest.newBuilder()
                        .setType(CoreProto.MsgType.GROUP_TEXT)
                        .setGroupText(groupText)
                        .build();
                break;
            case CoreProto.MsgType.GROUP_VOICE_VALUE:
                U2AudioMessage u2AudioMessage = U2AudioMessage.parseJSON(message.getContent());
                CoreProto.GroupVoice groupVoice = CoreProto.GroupVoice.newBuilder()
                        .setMsgId(message.getMsgId())
                        .setSiteUserId(message.getSiteUserId())
                        .setSiteGroupId(message.getSiteToId())
                        .setVoiceId(u2AudioMessage.getAudioId())
                        .setTime(message.getMsgTime())
                        .build();
                request = ImCtsMessageProto.ImCtsMessageRequest.newBuilder()
                        .setType(CoreProto.MsgType.GROUP_VOICE)
                        .setGroupVoice(groupVoice)
                        .build();
                break;
            case CoreProto.MsgType.GROUP_IMAGE_VALUE:
                U2ImageMessage u2ImageMessage = U2ImageMessage.parseJSON(message.getContent());
                CoreProto.GroupImage groupImage = CoreProto.GroupImage.newBuilder()
                        .setMsgId(message.getMsgId())
                        .setSiteUserId(message.getSiteUserId())
                        .setSiteGroupId(message.getSiteToId())
                        .setImageId(u2ImageMessage.getFileId())
                        .setTime(message.getMsgTime())
                        .build();
                request = ImCtsMessageProto.ImCtsMessageRequest.newBuilder()
                        .setType(CoreProto.MsgType.GROUP_IMAGE)
                        .setGroupImage(groupImage)
                        .build();
                break;
            default:
                request = null;
        }

        return request;

    }

    private static ImCtsMessageProto.ImCtsMessageRequest buildNotificationRequest(Message message) {
        ImCtsMessageProto.ImCtsMessageRequest request = null;
        Notification notification = new Notification();

        return request;
    }
}
