package com.windchat.im;

import com.google.protobuf.ByteString;
import com.windchat.im.bean.AudioInfo;
import com.windchat.im.bean.ImageInfo;
import com.windchat.im.bean.Message;
import com.windchat.proto.core.CoreProto;
import com.windchat.proto.server.ImCtsMessageProto;

import org.apache.commons.lang3.StringUtils;


public class MessageBuilder {

    public static ImCtsMessageProto.ImCtsMessageRequest buildMessageRequest(Message message) {

        ImCtsMessageProto.ImCtsMessageRequest request;

        if (StringUtils.isEmpty(message.getGroupId())) {//单人消息
            if (message.isSecret()) {//单人绝密消息
                switch (message.getMsgType()) {
                    case CoreProto.MsgType.SECRET_TEXT_VALUE:
                        //创建消息对象
                        CoreProto.MsgSecretText secretText = CoreProto.MsgSecretText.newBuilder()
                                .setMsgId(message.getMsgId())
                                .setSiteUserId(message.getSiteUserId())
                                .setSiteFriendId(message.getSiteFriendId())
                                .setText(ByteString.copyFrom(message.getSecretData()))
                                .setTime(message.getMsgTime())
                                .setBase64TsKey(message.getMsgTsk())
                                .setToDeviceId(message.getToDeviceId())
                                .build();
                        //生成请求
                        request = ImCtsMessageProto.ImCtsMessageRequest.newBuilder()
                                .setType(CoreProto.MsgType.SECRET_TEXT)
                                .setSecretText(secretText)
                                .build();
                        break;

                    case CoreProto.MsgType.SECRET_VOICE_VALUE:
                        AudioInfo audioInfo = AudioInfo.parseJSON(message.getContent());
                        CoreProto.MsgSecretVoice secretVoice = CoreProto.MsgSecretVoice.newBuilder()
                                .setMsgId(message.getMsgId())
                                .setSiteUserId(message.getSiteUserId())
                                .setSiteFriendId(message.getSiteFriendId())
                                .setVoiceId(audioInfo.getAudioId())
                                .setBase64TsKey(message.getMsgTsk())
                                .setTime(message.getMsgTime())
                                .setToDeviceId(message.getToDeviceId())
                                .build();
                        request = ImCtsMessageProto.ImCtsMessageRequest.newBuilder()
                                .setType(CoreProto.MsgType.SECRET_VOICE)
                                .setSecretVoice(secretVoice)
                                .build();
                        break;

                    case CoreProto.MsgType.SECRET_IMAGE_VALUE:
                        ImageInfo imageInfo = ImageInfo.parseJSON(message.getContent());
                        CoreProto.MsgSecretImage secretImage = CoreProto.MsgSecretImage.newBuilder()
                                .setMsgId(message.getMsgId())
                                .setSiteUserId(message.getSiteUserId())
                                .setSiteFriendId(message.getSiteFriendId())
                                .setImageId(imageInfo.getFileId())
                                .setTime(message.getMsgTime())
                                .setBase64TsKey(message.getMsgTsk())
                                .setToDeviceId(message.getToDeviceId())
                                .build();
                        request = ImCtsMessageProto.ImCtsMessageRequest.newBuilder()
                                .setType(CoreProto.MsgType.SECRET_IMAGE)
                                .setSecretImage(secretImage)
                                .build();
                        break;

                    default:
                        request = null;
                }
            } else {
                switch (message.getMsgType()) {
                    case CoreProto.MsgType.TEXT_VALUE:
                        CoreProto.MsgText msgText = CoreProto.MsgText.newBuilder()
                                .setMsgId(message.getMsgId())
                                .setSiteUserId(message.getSiteUserId())
                                .setSiteFriendId(message.getSiteFriendId())
                                .setText(ByteString.copyFrom(message.getContent().getBytes()))
                                .setTime(message.getMsgTime())
                                .build();
                        request = ImCtsMessageProto.ImCtsMessageRequest.newBuilder()
                                .setType(CoreProto.MsgType.TEXT)
                                .setText(msgText)
                                .build();
                        break;
                    case CoreProto.MsgType.VOICE_VALUE:
                        AudioInfo audioInfo = AudioInfo.parseJSON(message.getContent());
                        CoreProto.MsgVoice msgVoice = CoreProto.MsgVoice.newBuilder()
                                .setMsgId(message.getMsgId())
                                .setSiteUserId(message.getSiteUserId())
                                .setSiteFriendId(message.getSiteFriendId())
                                .setVoiceId(audioInfo.getAudioId())
                                .setTime(message.getMsgTime())
                                .build();
                        request = ImCtsMessageProto.ImCtsMessageRequest.newBuilder()
                                .setType(CoreProto.MsgType.VOICE)
                                .setVoice(msgVoice)
                                .build();
                        break;
                    case CoreProto.MsgType.IMAGE_VALUE:
                        ImageInfo imageInfo = ImageInfo.parseJSON(message.getContent());
                        CoreProto.MsgImage msgImage = CoreProto.MsgImage.newBuilder()
                                .setMsgId(message.getMsgId())
                                .setSiteUserId(message.getSiteUserId())
                                .setSiteFriendId(message.getSiteFriendId())
                                .setImageId(imageInfo.getFileId())
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
            }

        } else {

            /** 群组消息 **/
            if (message.isSecret()) {//群组绝密消息（暂时不支持）
                //创建消息对象
                CoreProto.MsgSecretText secretText = CoreProto.MsgSecretText.newBuilder()
                        .setMsgId(message.getMsgId())
                        .setSiteUserId(message.getSiteUserId())
                        .setSiteFriendId(message.getSiteFriendId())
                        .setText(ByteString.copyFrom(message.getSecretData()))
                        .setTime(message.getMsgTime())
                        .setBase64TsKey(message.getMsgTsk())
                        .setToDeviceId("")
                        .build();
                //生成请求
                request = ImCtsMessageProto.ImCtsMessageRequest.newBuilder()
                        .setType(CoreProto.MsgType.SECRET_TEXT)
                        .setSecretText(secretText)
                        .build();
            } else {//群组正常消息
                switch (message.getMsgType()) {
                    case CoreProto.MsgType.GROUP_TEXT_VALUE:
                        CoreProto.GroupText groupText = CoreProto.GroupText.newBuilder()
                                .setMsgId(message.getMsgId())
                                .setSiteUserId(message.getSiteUserId())
                                .setSiteGroupId(message.getGroupId())
                                .setText(ByteString.copyFrom(message.getContent().getBytes()))
                                .setTime(message.getMsgTime())
                                .build();
                        request = ImCtsMessageProto.ImCtsMessageRequest.newBuilder()
                                .setType(CoreProto.MsgType.GROUP_TEXT)
                                .setGroupText(groupText)
                                .build();
                        break;
                    case CoreProto.MsgType.GROUP_VOICE_VALUE:
                        AudioInfo audioInfo = AudioInfo.parseJSON(message.getContent());
                        CoreProto.GroupVoice groupVoice = CoreProto.GroupVoice.newBuilder()
                                .setMsgId(message.getMsgId())
                                .setSiteUserId(message.getSiteUserId())
                                .setSiteGroupId(message.getGroupId())
                                .setVoiceId(audioInfo.getAudioId())
                                .setTime(message.getMsgTime())
                                .build();
                        request = ImCtsMessageProto.ImCtsMessageRequest.newBuilder()
                                .setType(CoreProto.MsgType.GROUP_VOICE)
                                .setGroupVoice(groupVoice)
                                .build();
                        break;
                    case CoreProto.MsgType.GROUP_IMAGE_VALUE:
                        ImageInfo imageInfo = ImageInfo.parseJSON(message.getContent());
                        CoreProto.GroupImage groupImage = CoreProto.GroupImage.newBuilder()
                                .setMsgId(message.getMsgId())
                                .setSiteUserId(message.getSiteUserId())
                                .setSiteGroupId(message.getGroupId())
                                .setImageId(imageInfo.getFileId())
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
            }
        }
        return request;
    }
}
