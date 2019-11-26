package com.windchat.im;

import android.util.Base64;

import com.windchat.im.message.GroupAudioMessage;
import com.windchat.im.message.GroupCustomMessage;
import com.windchat.im.message.GroupImageMessage;
import com.windchat.im.message.GroupNoticeMessage;
import com.windchat.im.message.GroupTextMessage;
import com.windchat.im.message.Notification;
import com.windchat.im.message.U2AudioMessage;
import com.windchat.im.message.U2CustomMessage;
import com.windchat.im.message.U2ImageMessage;
import com.windchat.im.message.Message;
import com.windchat.im.bean.Site;
import com.windchat.im.message.U2NoticeMessage;
import com.windchat.im.message.U2TextMessage;
import com.windchat.im.socket.IMessageHandler;
import com.windchat.im.socket.TransportPackage;
import com.windchat.logger.WindLogger;
import com.windchat.proto.client.ImStcMessageProto;
import com.windchat.proto.client.ImStcNoticeProto;
import com.windchat.proto.core.CoreProto;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yichao on 2017/10/18.
 * <p>
 * Zaly消息同步器，使用psh, sync同步机制
 * <p>
 * <p>
 * 处理所有服务端主动推送给客户端的消息
 */
public class IMMessageReceiveHandler implements IMessageHandler {
    public static final String TAG = "IMMessageReceiveHandler";

    private IMClient imClient;
    private Site site;

    private volatile boolean isReceiveMsgFinish = false;

    private static final long syncTimeOut = 2 * 1000;
    private long lastSyncFinishTime = 0l;

    private long pointer = 0l;
    private HashMap<String, Long> groupPointers = new HashMap<>();

    private IMessageReceiver messageReceiver;

    public IMMessageReceiveHandler(IMClient client) {
        this.imClient = client;
        this.messageReceiver = client.getMessageReceiver();
        this.site = client.getSite();
    }

    @Override
    public boolean handle(TransportPackage packet) throws Exception {
        String action = packet.action;

        WindLogger.getInstance().debug(IMMessageReceiveHandler.TAG, "im.receive " + action);

        switch (action) {
            case IMConst.Action.Pong:
                this.imClient.keepAlivedWorker.recvPong();
                break;
            case IMConst.Action.PSN:
                long nowTime = System.currentTimeMillis();
                if (!isReceiveMsgFinish && (nowTime - lastSyncFinishTime < syncTimeOut)) {
                    return false;
                }
                this.imClient.syncMessage();
                break;
            case IMConst.Action.Notice:
                receiveMessageNotice(packet.data.toByteArray());
                break;
            case IMConst.Action.ReceiveMsgFromSite:
                receiveMessage(packet.data.toByteArray());
                break;
            case IMConst.Action.MsgFinish:
                isReceiveMsgFinish = true;
                lastSyncFinishTime = System.currentTimeMillis();
                this.imClient.syncFinish(pointer, groupPointers);
                break;
        }
        return false;
    }

    /**
     * 处理notice IM消息
     *
     * @param data
     */
    private void receiveMessageNotice(byte[] data) {
        try {
            CoreProto.TransportPackageData packageData = CoreProto.TransportPackageData.parseFrom(data);
            ImStcNoticeProto.ImStcNoticeRequest request = ImStcNoticeProto.ImStcNoticeRequest.parseFrom(packageData.getData());

            messageReceiver.handleNoticeMessage(this.site, request);
        } catch (Exception e) {
            messageReceiver.handleException(this.site, e);
        }
    }


    /**
     * @param data
     */
    private void receiveMessage(byte[] data) {

        try {
            CoreProto.TransportPackageData packageData = CoreProto.TransportPackageData.parseFrom(data);

            ImStcMessageProto.ImStcMessageRequest request = ImStcMessageProto.ImStcMessageRequest.parseFrom(packageData.getData());
            List<ImStcMessageProto.MsgWithPointer> msgWithPointers = request.getListList();

            ArrayList<Message> messages = new ArrayList<>();
            //接收消息是否完成，完成时需要发送synchFinish数据包
            isReceiveMsgFinish = false;

            //获取用户ID
            String curSiteUserId = this.site.getSiteUserId();

            //消息指针 服务端需要
            for (ImStcMessageProto.MsgWithPointer withPointer : msgWithPointers) {
                WindLogger.getInstance().info(TAG, withPointer.toString());

                Message message = null;
                try {
                    // 在最后更新this.pointer
                    switch (withPointer.getType().getNumber()) {
                        case CoreProto.MsgType.MSG_STATUS_VALUE:    //消息状态
                            String msgId = withPointer.getStatus().getMsgId();
                            if (StringUtils.isEmpty(msgId)) {
                                return;
                            }
                            Long serverMsgTime = withPointer.getStatus().getMsgServerTime();
                            if (serverMsgTime == null) {
                                serverMsgTime = System.currentTimeMillis();
                            }

                            int messageStatus = withPointer.getStatus().getMsgStatus();
                            MsgStatus msgStatus = MsgStatus.parseFrom(messageStatus);
                            messageReceiver.handleMessageStatus(this.site, msgId, serverMsgTime, msgStatus);
                            return;

                        case CoreProto.MsgType.NOTICE_VALUE:
                            Notification notification = new Notification();
                            // TODO
                            break;
                        case CoreProto.MsgType.U2_NOTICE_VALUE:
                            CoreProto.U2MsgNotice u2MsgNotice = withPointer.getU2MsgNotice();

                            U2NoticeMessage u2Notice = new U2NoticeMessage();
                            u2Notice.setMsgId(u2MsgNotice.getMsgId());
                            u2Notice.setSiteUserId(u2MsgNotice.getSiteUserId());
                            u2Notice.setSiteToId(u2MsgNotice.getSiteFriendId());
                            u2Notice.setChatSessionId(u2MsgNotice.getSiteUserId());
                            u2Notice.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);
                            if (u2MsgNotice.getSiteUserId().equals(curSiteUserId)) {
                                u2Notice.setChatSessionId(u2MsgNotice.getSiteFriendId());
                            }
                            u2Notice.setContent(u2MsgNotice.getText().toStringUtf8());
                            u2Notice.setMsgTime(u2MsgNotice.getTime());

                            message = u2Notice;
                            messages.add(u2Notice);
                            break;
                        case CoreProto.MsgType.TEXT_VALUE:
                            //二人文本
                            CoreProto.MsgText msgText = withPointer.getText();

                            U2TextMessage u2TextMessage = new U2TextMessage();
                            u2TextMessage.setMsgId(msgText.getMsgId());
                            u2TextMessage.setContent(msgText.getText().toStringUtf8());
                            u2TextMessage.setMsgTime(msgText.getTime()); //消息到达服务器的时间
                            u2TextMessage.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);
                            ////chat_session_id 是对方的id,或者群组id
                            u2TextMessage.setChatSessionId(msgText.getSiteFriendId());

                            if (msgText.getSiteFriendId().equals(curSiteUserId)) {
                                u2TextMessage.setChatSessionId(msgText.getSiteUserId());
                            }
                            u2TextMessage.setSiteUserId(msgText.getSiteUserId());
                            u2TextMessage.setSiteToId(msgText.getSiteFriendId());
                            message = u2TextMessage;
                            messages.add(u2TextMessage);
                            break;
                        case CoreProto.MsgType.VOICE_VALUE:
                            CoreProto.MsgVoice voice = withPointer.getVoice();
                            U2AudioMessage u2AudioMessage = new U2AudioMessage();
                            u2AudioMessage.setAudioId(voice.getVoiceId());
                            u2AudioMessage.setAudioTime(U2AudioMessage.NONE_DOWNLOAD);
                            u2AudioMessage.setAudioFilePath("");

                            U2AudioMessage u2VoiceMsg = new U2AudioMessage();
                            u2VoiceMsg.setMsgId(voice.getMsgId());
                            u2VoiceMsg.setContent(U2AudioMessage.toJSON(u2AudioMessage));
                            u2VoiceMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);

                            ////chat_session_id 是对方的id,或者群组id
                            u2VoiceMsg.setChatSessionId(voice.getSiteFriendId());
                            if (voice.getSiteFriendId().equals(curSiteUserId)) {
                                u2VoiceMsg.setChatSessionId(voice.getSiteUserId());
                            }
                            u2VoiceMsg.setSiteUserId(voice.getSiteUserId());
                            u2VoiceMsg.setSiteToId(voice.getSiteFriendId());
                            u2VoiceMsg.setMsgTime(voice.getTime());

                            message = u2VoiceMsg;
                            messages.add(u2VoiceMsg);
                            break;
                        case CoreProto.MsgType.IMAGE_VALUE:
                            CoreProto.MsgImage image = withPointer.getImage();
                            U2ImageMessage u2ImageMessage = new U2ImageMessage();
                            u2ImageMessage.setFileId(image.getImageId());
                            u2ImageMessage.setFileLength(-1);
                            u2ImageMessage.setFilePath("");
                            u2ImageMessage.setStatus(U2ImageMessage.STATUS_RECEIVE_NO_DOWNLOAD);

                            U2ImageMessage u2ImgMsg = new U2ImageMessage();
                            u2ImgMsg.setMsgId(image.getMsgId());
                            u2ImgMsg.setContent(U2ImageMessage.toJSON(u2ImageMessage));
                            u2ImgMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);

                            ////chat_session_id 是对方的id,或者群组id
                            u2ImgMsg.setChatSessionId(image.getSiteFriendId());

                            if (image.getSiteFriendId().equals(curSiteUserId)) {
                                u2ImgMsg.setChatSessionId(image.getSiteUserId());
                            }
                            u2ImgMsg.setSiteUserId(image.getSiteUserId());
                            u2ImgMsg.setSiteToId(image.getSiteFriendId());
                            u2ImgMsg.setMsgTime(image.getTime());

                            message = u2ImgMsg;
                            messages.add(u2ImgMsg);
                            break;
                        case CoreProto.MsgType.SECRET_TEXT_VALUE:
                        case CoreProto.MsgType.SECRET_IMAGE_VALUE:
                        case CoreProto.MsgType.SECRET_VOICE_VALUE:
                            break;
                        case CoreProto.MsgType.U2_WEB_NOTICE_VALUE:
                            break;
                        case CoreProto.MsgType.U2_WEB_VALUE:
                            CoreProto.U2Web u2Web = withPointer.getU2Web();
                            U2CustomMessage u2CustomMsg = new U2CustomMessage();
                            u2CustomMsg.setMsgId(u2Web.getMsgId());
                            u2CustomMsg.setChatSessionId(u2Web.getSiteFriendId());
                            u2CustomMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);

                            if (u2Web.getSiteFriendId().equals(curSiteUserId)) {
                                u2CustomMsg.setChatSessionId(u2Web.getSiteUserId());
                                u2CustomMsg.setMsgStatus(Message.STATUS_SEND_SUCCESS);
                            }
                            u2CustomMsg.setSiteUserId(u2Web.getSiteUserId());
                            u2CustomMsg.setSiteToId(u2Web.getSiteFriendId());
                            u2CustomMsg.setContent(u2Web.getWebCode());
                            u2CustomMsg.setMsgTime(u2Web.getTime());

                            message = u2CustomMsg;
                            messages.add(u2CustomMsg);
                            break;

                        case CoreProto.MsgType.GROUP_NOTICE_VALUE:
                            CoreProto.GroupMsgNotice groupMsgNotice = withPointer.getGroupMsgNotice();

                            GroupNoticeMessage groupNotice = new GroupNoticeMessage();
                            groupNotice.setMsgId(groupMsgNotice.getMsgId());
                            groupNotice.setChatSessionId(groupMsgNotice.getSiteGroupId());
                            groupNotice.setSiteUserId(groupMsgNotice.getSiteUserId());
                            groupNotice.setGroupId(groupMsgNotice.getSiteGroupId());
                            groupNotice.setContent(groupMsgNotice.getText().toStringUtf8());
                            groupNotice.setMsgTime(groupMsgNotice.getTime());
                            groupNotice.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);

                            message = groupNotice;
                            messages.add(groupNotice);
                            break;
                        case CoreProto.MsgType.GROUP_TEXT_VALUE:
                            CoreProto.GroupText groupText = withPointer.getGroupText();

                            GroupTextMessage groupTextMsg = new GroupTextMessage();
                            groupTextMsg.setMsgId(groupText.getMsgId());
                            groupTextMsg.setSiteUserId(groupText.getSiteUserId());
                            groupTextMsg.setChatSessionId(groupText.getSiteGroupId());
                            groupTextMsg.setGroupId(groupText.getSiteGroupId());
                            groupTextMsg.setContent(groupText.getText().toStringUtf8());
                            groupTextMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);
                            groupTextMsg.setMsgTime(groupText.getTime());

                            message = groupTextMsg;
                            messages.add(groupTextMsg);
                            break;
                        case CoreProto.MsgType.GROUP_VOICE_VALUE:
                            CoreProto.GroupVoice groupVoice = withPointer.getGroupVoice();
                            U2AudioMessage groupU2AudioMessage = new U2AudioMessage();
                            groupU2AudioMessage.setAudioId(groupVoice.getVoiceId());
                            groupU2AudioMessage.setAudioTime(U2AudioMessage.NONE_DOWNLOAD);
                            groupU2AudioMessage.setAudioFilePath("");

                            GroupAudioMessage groupVoiceMsg = new GroupAudioMessage();
                            groupVoiceMsg.setMsgId(groupVoice.getMsgId());
                            groupVoiceMsg.setSiteUserId(groupVoice.getSiteUserId());
                            groupVoiceMsg.setGroupId(groupVoice.getSiteGroupId());
                            groupVoiceMsg.setChatSessionId(groupVoice.getSiteGroupId());
                            groupVoiceMsg.setContent(U2AudioMessage.toJSON(groupU2AudioMessage));
                            groupVoiceMsg.setMsgTime(groupVoice.getTime());
                            groupVoiceMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);

                            message = groupVoiceMsg;
                            messages.add(groupVoiceMsg);
                            break;
                        case CoreProto.MsgType.GROUP_IMAGE_VALUE:
                            CoreProto.GroupImage groupImage = withPointer.getGroupImage();
                            U2ImageMessage groupU2ImageMessage = new U2ImageMessage();
                            groupU2ImageMessage.setFileId(groupImage.getImageId());
                            groupU2ImageMessage.setFileLength(-1);
                            groupU2ImageMessage.setFilePath("");
                            groupU2ImageMessage.setStatus(U2ImageMessage.STATUS_RECEIVE_NO_DOWNLOAD);

                            GroupImageMessage groupImgMsg = new GroupImageMessage();
                            groupImgMsg.setMsgId(groupImage.getMsgId());
                            groupImgMsg.setSiteUserId(groupImage.getSiteUserId());
                            groupImgMsg.setGroupId(groupImage.getSiteGroupId());
                            groupImgMsg.setChatSessionId(groupImage.getSiteGroupId());
                            groupImgMsg.setContent(U2ImageMessage.toJSON(groupU2ImageMessage));
                            groupImgMsg.setMsgTime(groupImage.getTime());
                            groupImgMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);

                            message = groupImgMsg;
                            messages.add(groupImgMsg);
                            break;
                        case CoreProto.MsgType.GROUP_WEB_NOTICE_VALUE:
                            break;
                        case CoreProto.MsgType.GROUP_WEB_VALUE:
                            CoreProto.GroupWeb groupWeb = withPointer.getGroupWeb();
                            GroupCustomMessage groupWebMsg = new GroupCustomMessage();
                            groupWebMsg.setMsgId(groupWeb.getMsgId());
                            groupWebMsg.setSiteUserId(groupWeb.getSiteUserId());
                            groupWebMsg.setGroupId(groupWeb.getSiteGroupId());
                            groupWebMsg.setChatSessionId(groupWeb.getSiteGroupId());
                            groupWebMsg.setContent(groupWeb.getWebCode());
                            groupWebMsg.setMsgTime(groupWeb.getTime());
                            if (groupWeb.getSiteUserId().equals(curSiteUserId)) {
                                groupWebMsg.setMsgStatus(Message.STATUS_SEND_SUCCESS);
                            } else {
                                groupWebMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);
                            }
//                            groupWebMsg.setMsgHeight(groupWeb.getHeight());
//                            groupWebMsg.setMsgWidth(groupWeb.getWidth());
//                            groupWebMsg.setHrefUrl(groupWeb.getHrefUrl());

                            message = groupWebMsg;
                            messages.add(groupWebMsg);
                            break;

                    }
                } catch (Exception e) {
                    messageReceiver.handleException(this.site, e);
                }

                if (message != null) {
                    Message.ChatType chatType = message.getChatType();
                    if (Message.ChatType.MSG_GROUP == chatType) {
                        this.groupPointers.put(message.getChatSessionId(), withPointer.getPointer());
                    } else {
                        // get max pointers
                        this.pointer = withPointer.getPointer();
                    }
                }
            }

            if (messages != null && messages.size() > 0) {

                WindLogger.getInstance().info(TAG, "batch inserting...");

                List<Notification> notifications = null;
                List<Message> u2Messages = null;
                List<Message> groupMessages = null;

                for (Message message : messages) {
                    switch (message.getChatType()) {
                        // 系统通知
                        case NOTIFICATION:
                            if (notifications == null) {
                                notifications = new ArrayList<>();
                            }
                            notifications.add((Notification) message);
                            break;
                        case MSG_U2:
                            if (u2Messages == null) {
                                u2Messages = new ArrayList<>();
                            }
                            u2Messages.add(message);
                            break;
                        case MSG_GROUP:
                            if (groupMessages == null) {
                                groupMessages = new ArrayList<>();
                            }
                            groupMessages.add(message);
                            break;
                    }
                }
                if (notifications != null && notifications.size() > 0) {
                    messageReceiver.handleNotification(this.site, notifications);
                }
                if (u2Messages != null && u2Messages.size() > 0) {
                    messageReceiver.handleU2Message(this.site, u2Messages);
                }
                if (groupMessages != null && groupMessages.size() > 0) {
                    messageReceiver.handleGroupMessage(this.site, groupMessages);
                }
            }

        } catch (Exception e) {
            messageReceiver.handleException(this.site, e);
        }
    }
}
