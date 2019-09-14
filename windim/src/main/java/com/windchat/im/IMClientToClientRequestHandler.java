package com.windchat.im;

import android.content.Intent;
import android.util.Base64;

import com.windchat.im.bean.AudioInfo;
import com.windchat.im.bean.ImageInfo;
import com.windchat.im.bean.Message;
import com.windchat.im.bean.Site;
import com.windchat.im.socket.IMessageHandler;
import com.windchat.im.socket.SiteAddress;
import com.windchat.im.socket.TransportPackage;
import com.windchat.logger.ZalyLogUtils;
import com.windchat.proto.client.ImStcMessageProto;
import com.windchat.proto.client.ImStcNoticeProto;
import com.windchat.proto.core.CoreProto;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by yichao on 2017/10/18.
 * <p>
 * Zaly消息同步器，使用psh, sync同步机制
 * <p>
 * <p>
 * 处理所有服务端主动推送给客户端的消息
 */
public class IMClientToClientRequestHandler implements IMessageHandler {

    public static final String TAG = "ZalyMsgSynchronizer";
    private IMClient imClient;
    private Site site;

    private volatile boolean isReceiveMsgFinish = false;

    private static final long syncTimeOut = 2 * 1000;
    private long lastSyncFinishTime = 0l;
    private static boolean isSendSyncMsg = true;

    private long pointer = 0l;
    private HashMap<String, Long> groupPointers = new HashMap<>();

    public IMClientToClientRequestHandler(IMClient client) {
        this.imClient = client;
    }

    @Override
    public boolean matchReceive(TransportPackage packet) throws Exception {
        String action = packet.action;

        ZalyLogUtils.getInstance().debug(
                IMClientToClientRequestHandler.TAG,
                "im.recv " + action
        );
        switch (action) {
            case ZalyIM.Action.PSN:
                long nowTime = System.currentTimeMillis();
                /////没有执行到recevice msg finish , psn 和上次finish时间小于2秒不执行
                if (!isReceiveMsgFinish && (nowTime - lastSyncFinishTime < syncTimeOut)) {
                    return false;
                }
                this.imClient.syncMessage();


                break;
            case ZalyIM.Action.MsgFinish:
                isReceiveMsgFinish = true;
                lastSyncFinishTime = System.currentTimeMillis();
                this.imClient.syncFinish(pointer, groupPointers);
                break;

            case ZalyIM.Action.Notice:
                dealNoticeAction(packet.data.toByteArray());
                break;
            case ZalyIM.Action.ReceiveMsgFrmSite:
                dealReceiveMsg(packet.data.toByteArray());
                break;
            case ZalyIM.Action.Pong:
                this.imClient.keepAlivedWorker.recvPong();
                break;

        }
        return false;
    }

    /**
     * 处理notice IM消息
     *
     * @param data
     */
    private void dealNoticeAction(byte[] data) {

        try {
            CoreProto.TransportPackageData packageData = CoreProto.TransportPackageData.parseFrom(data);
            ImStcNoticeProto.ImStcNoticeRequest request = ImStcNoticeProto.ImStcNoticeRequest.parseFrom(packageData.getData());

//            Intent intent = new Intent(ZalyIM.IM_NOTICE_ACTION);
//            intent.setPackage(PackageSign.getPackage());
//            intent.putExtra(ZalyIM.KEY_NOTICE_SITE_IDENTITY, this.imClient.imConnection.getConnSiteIdentity());
//            intent.putExtra(ZalyIM.KEY_NOTICE_TYPE, request.getTypeValue());
//            ZalyApplication.getContext().sendBroadcast(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 处理从site收到的请求，故需要解析request
     * ZalyIM.KEY_NOTICE_TYPE
     *
     * @param data
     */
    private void dealReceiveMsg(byte[] data) {
        try {

            CoreProto.TransportPackageData packageData = CoreProto.TransportPackageData.parseFrom(data);

            ImStcMessageProto.ImStcMessageRequest request = ImStcMessageProto.ImStcMessageRequest.parseFrom(packageData.getData());
            List<ImStcMessageProto.MsgWithPointer> msgWithPointers = request.getListList();

            ArrayList<Message> messages = new ArrayList<>();
            //接收消息是否完成，完成时需要发送synchFinish数据包
            isReceiveMsgFinish = false;

            //获取站点信息
            String siteIdentity = this.imClient.imConnection.getConnSiteIdentity();
            String siteAddress = this.imClient.imConnection.getSiteAddress();
            Site tmpSite = new Site(this.imClient.address.getHost(), this.imClient.address.getPort());
            String curSiteUserId = tmpSite.getSiteUserId();

            //消息指针 服务端需要
            for (ImStcMessageProto.MsgWithPointer withPointer : msgWithPointers) {
                ZalyLogUtils.getInstance().info(TAG, withPointer.toString());
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

                            int messageStatus = Message.STATUS_SENDING;
                            /*
                             * 根据返回的消息状态选择入库的消息状态
                             * msg_status: 1 发送成功
                             * msg_status: 0 默认状态
                             * msg_status: -1 用户非好友关系，二人消息发送失败
                             * msg_status: -2 用户非群成员，群消息发送失败
                             */
                            switch (withPointer.getStatus().getMsgStatus()) {
                                case -2:
                                    messageStatus = Message.STATUS_SEND_FAILED_NOT_IN_GROUP;
                                    break;
                                case -1:
                                    messageStatus = Message.STATUS_SEND_FAILED_NOT_FRIEND;
                                    break;
                                case 1:
                                    messageStatus = Message.STATUS_SEND_SUCCESS;
                                    break;
                            }

//                            int updateStatusFlag = SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(siteAddress)).updateU2MsgStatusForSend(msgId, serverMsgTime, messageStatus);
//                            // 如果不是单人消息则去群组表中更新数据, 回写时间
//                            if (updateStatusFlag == 0) {
////                                if (SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(siteAddress)).updateGroupMsgStatusForSend(msgId, serverMsgTime, messageStatus) == 0) {
////                                }
//                            }

//                            //通知UI进程
//                            Bundle bundle = new Bundle();
//                            bundle.putString(ZalyDbContentHelper.KEY_MSG_ID, msgId);
//                            bundle.putString(ZalyDbContentHelper.KEY_SITE_IDENTITY, siteIdentity);
//                            bundle.putString(ZalyDbContentHelper.KEY_CUR_SITE_USER_ID, curSiteUserId);
//                            bundle.putInt(ZalyDbContentHelper.KEY_MSG_STATUS, messageStatus);
//                            ZalyDbContentHelper.executeAction(ZalyDbContentHelper.Action.MSG_STATUS, bundle);
                            return;
                        case CoreProto.MsgType.TEXT_VALUE:
                            //二人文本
                            CoreProto.MsgText msgText = withPointer.getText();
                            Message message = new Message();
                            message.setMsgId(msgText.getMsgId());
                            message.setContent(msgText.getText().toStringUtf8());
                            message.setMsgTime(msgText.getTime()); //消息到达服务器的时间
                            message.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);
                            ////chat_session_id 是对方的id,或者群组id
                            message.setChatSessionId(msgText.getSiteFriendId());

                            if (msgText.getSiteFriendId().equals(curSiteUserId)) {
                                message.setChatSessionId(msgText.getSiteUserId());
                            }
                            message.setSiteUserId(msgText.getSiteUserId());
                            message.setSiteFriendId(msgText.getSiteFriendId());
                            message.setMsgType(CoreProto.MsgType.TEXT_VALUE);
                            messages.add(message);

                            break;
                        case CoreProto.MsgType.SECRET_TEXT_VALUE:
                            //二人绝密文本
                            CoreProto.MsgSecretText secretText = withPointer.getSecretText();
                            Message secretMsg = new Message();
                            secretMsg.setMsgId(secretText.getMsgId());
                            String content = Base64.encodeToString(secretText.getText().toByteArray(), Base64.NO_WRAP);
                            secretMsg.setContent(content);

                            secretMsg.setMsgTime(secretText.getTime());
                            secretMsg.setMsgTsk(secretText.getBase64TsKey());
                            secretMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);

                            secretMsg.setSecret(true);
                            secretMsg.setChatSessionId(secretText.getSiteFriendId());
                            secretMsg.setToDeviceId(secretText.getToDeviceId());
                            if (secretText.getSiteFriendId().equals(curSiteUserId)) {
                                secretMsg.setChatSessionId(secretText.getSiteUserId());
                            }
                            secretMsg.setSiteUserId(secretText.getSiteUserId());
                            secretMsg.setSiteFriendId(secretText.getSiteFriendId());
                            secretMsg.setMsgType(CoreProto.MsgType.SECRET_TEXT_VALUE);
                            messages.add(secretMsg);

                            break;
                        case CoreProto.MsgType.VOICE_VALUE:
                            CoreProto.MsgVoice voice = withPointer.getVoice();
                            AudioInfo audioInfo = new AudioInfo();
                            audioInfo.setAudioId(voice.getVoiceId());
                            audioInfo.setAudioTime(AudioInfo.NONE_DOWNLOAD);
                            audioInfo.setAudioFilePath("");
                            Message voiceMsg = new Message();
                            voiceMsg.setMsgId(voice.getMsgId());
                            voiceMsg.setContent(AudioInfo.toJSON(audioInfo));
                            voiceMsg.setMsgTime(voice.getTime());
                            voiceMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);

                            ////chat_session_id 是对方的id,或者群组id
                            voiceMsg.setChatSessionId(voice.getSiteFriendId());
                            if (voice.getSiteFriendId().equals(curSiteUserId)) {
                                voiceMsg.setChatSessionId(voice.getSiteUserId());
                            }
                            voiceMsg.setSiteUserId(voice.getSiteUserId());
                            voiceMsg.setSiteFriendId(voice.getSiteFriendId());
                            voiceMsg.setMsgType(CoreProto.MsgType.VOICE_VALUE);
                            messages.add(voiceMsg);
                            break;
                        case CoreProto.MsgType.IMAGE_VALUE:
                            CoreProto.MsgImage image = withPointer.getImage();
                            ImageInfo imageInfo = new ImageInfo();
                            imageInfo.setFileId(image.getImageId());
                            imageInfo.setFileLength(-1);
                            imageInfo.setFilePath("");
                            imageInfo.setStatus(ImageInfo.STATUS_RECEIVE_NO_DOWNLOAD);
                            Message imgMsg = new Message();
                            imgMsg.setMsgId(image.getMsgId());
                            imgMsg.setContent(ImageInfo.toJSON(imageInfo));
                            imgMsg.setMsgTime(image.getTime());
                            imgMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);

                            ////chat_session_id 是对方的id,或者群组id
                            imgMsg.setChatSessionId(image.getSiteFriendId());

                            if (image.getSiteFriendId().equals(curSiteUserId)) {
                                imgMsg.setChatSessionId(image.getSiteUserId());
                            }
                            imgMsg.setSiteUserId(image.getSiteUserId());
                            imgMsg.setSiteFriendId(image.getSiteFriendId());
                            imgMsg.setMsgType(CoreProto.MsgType.IMAGE_VALUE);
                            messages.add(imgMsg);
                            break;
                        case CoreProto.MsgType.SECRET_IMAGE_VALUE:
                            CoreProto.MsgSecretImage secretImage = withPointer.getSecretImage();
                            ImageInfo secretImageInfo = new ImageInfo();
                            secretImageInfo.setFileId(secretImage.getImageId());
                            secretImageInfo.setFileLength(-1);
                            secretImageInfo.setFilePath("");
                            secretImageInfo.setStatus(ImageInfo.STATUS_RECEIVE_NO_DOWNLOAD);
                            Message secretImgMsg = new Message();
                            secretImgMsg.setMsgId(secretImage.getMsgId());
                            secretImgMsg.setContent(ImageInfo.toJSON(secretImageInfo));
                            secretImgMsg.setMsgTime(secretImage.getTime());
                            secretImgMsg.setMsgTsk(secretImage.getBase64TsKey());
                            secretImgMsg.setSecret(true);
                            secretImgMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);

                            ////chat_session_id 是对方的id,或者群组id
                            secretImgMsg.setChatSessionId(secretImage.getSiteFriendId());
                            secretImgMsg.setToDeviceId(secretImage.getToDeviceId());
                            if (secretImage.getSiteFriendId().equals(curSiteUserId)) {
                                secretImgMsg.setChatSessionId(secretImage.getSiteUserId());

                            }
                            secretImgMsg.setSiteUserId(secretImage.getSiteUserId());
                            secretImgMsg.setSiteFriendId(secretImage.getSiteFriendId());
                            secretImgMsg.setMsgType(CoreProto.MsgType.SECRET_IMAGE_VALUE);
                            messages.add(secretImgMsg);
                            break;

                        case CoreProto.MsgType.SECRET_VOICE_VALUE:
                            CoreProto.MsgSecretVoice secretVoice = withPointer.getSecretVoice();
                            AudioInfo secretAudioInfo = new AudioInfo();
                            secretAudioInfo.setAudioId(secretVoice.getVoiceId());
                            secretAudioInfo.setAudioTime(AudioInfo.NONE_DOWNLOAD);
                            secretAudioInfo.setAudioFilePath("");
                            Message secretVoiceMsg = new Message();
                            secretVoiceMsg.setMsgId(secretVoice.getMsgId());
                            secretVoiceMsg.setContent(AudioInfo.toJSON(secretAudioInfo));
                            secretVoiceMsg.setMsgTime(secretVoice.getTime());
                            secretVoiceMsg.setMsgTsk(secretVoice.getBase64TsKey());
                            secretVoiceMsg.setSecret(true);
                            secretVoiceMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);

                            ////chat_session_id 是对方的id,或者群组id
                            secretVoiceMsg.setChatSessionId(secretVoice.getSiteFriendId());
                            secretVoiceMsg.setToDeviceId(secretVoice.getToDeviceId());

                            if (secretVoice.getSiteFriendId().equals(curSiteUserId)) {
                                secretVoiceMsg.setChatSessionId(secretVoice.getSiteUserId());
                            }
                            secretVoiceMsg.setSiteUserId(secretVoice.getSiteUserId());
                            secretVoiceMsg.setSiteFriendId(secretVoice.getSiteFriendId());
                            secretVoiceMsg.setMsgType(CoreProto.MsgType.SECRET_VOICE_VALUE);
                            messages.add(secretVoiceMsg);
                            break;

                        case CoreProto.MsgType.GROUP_TEXT_VALUE:
                            CoreProto.GroupText groupText = withPointer.getGroupText();
                            Message groupMsg = new Message();
                            groupMsg.setMsgId(groupText.getMsgId());
                            groupMsg.setSiteUserId(groupText.getSiteUserId());
                            groupMsg.setChatSessionId(groupText.getSiteGroupId());
                            groupMsg.setGroupId(groupText.getSiteGroupId());
                            groupMsg.setContent(groupText.getText().toStringUtf8());
                            groupMsg.setMsgTime(groupText.getTime());
                            groupMsg.setMsgStatus(Message.STATUS_RECEIVE_NONE);
                            groupMsg.setMsgType(CoreProto.MsgType.GROUP_TEXT_VALUE);
                            groupMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);
                            messages.add(groupMsg);
                            break;

                        case CoreProto.MsgType.GROUP_VOICE_VALUE:
                            CoreProto.GroupVoice groupVoice = withPointer.getGroupVoice();
                            AudioInfo groupAudioInfo = new AudioInfo();
                            groupAudioInfo.setAudioId(groupVoice.getVoiceId());
                            groupAudioInfo.setAudioTime(AudioInfo.NONE_DOWNLOAD);
                            groupAudioInfo.setAudioFilePath("");
                            Message groupVoiceMsg = new Message();
                            groupVoiceMsg.setMsgId(groupVoice.getMsgId());
                            groupVoiceMsg.setSiteUserId(groupVoice.getSiteUserId());
                            groupVoiceMsg.setGroupId(groupVoice.getSiteGroupId());
                            groupVoiceMsg.setChatSessionId(groupVoice.getSiteGroupId());
                            groupVoiceMsg.setContent(AudioInfo.toJSON(groupAudioInfo));
                            groupVoiceMsg.setMsgTime(groupVoice.getTime());
                            groupVoiceMsg.setMsgStatus(Message.STATUS_RECEIVE_NONE);
                            groupVoiceMsg.setMsgType(CoreProto.MsgType.GROUP_VOICE_VALUE);
                            groupVoiceMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);
                            messages.add(groupVoiceMsg);
                            break;

                        case CoreProto.MsgType.GROUP_IMAGE_VALUE:
                            CoreProto.GroupImage groupImage = withPointer.getGroupImage();
                            ImageInfo groupImageInfo = new ImageInfo();
                            groupImageInfo.setFileId(groupImage.getImageId());
                            groupImageInfo.setFileLength(-1);
                            groupImageInfo.setFilePath("");
                            groupImageInfo.setStatus(ImageInfo.STATUS_RECEIVE_NO_DOWNLOAD);
                            Message groupImgMsg = new Message();
                            groupImgMsg.setMsgId(groupImage.getMsgId());
                            groupImgMsg.setSiteUserId(groupImage.getSiteUserId());
                            groupImgMsg.setGroupId(groupImage.getSiteGroupId());
                            groupImgMsg.setChatSessionId(groupImage.getSiteGroupId());
                            groupImgMsg.setContent(ImageInfo.toJSON(groupImageInfo));
                            groupImgMsg.setMsgTime(groupImage.getTime());
                            groupImgMsg.setMsgStatus(Message.STATUS_RECEIVE_NONE);
                            groupImgMsg.setMsgType(CoreProto.MsgType.GROUP_IMAGE_VALUE);
                            groupImgMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);
                            messages.add(groupImgMsg);
                            break;

                        case CoreProto.MsgType.GROUP_NOTICE_VALUE:
                            CoreProto.GroupMsgNotice groupMsgNotice = withPointer.getGroupMsgNotice();
                            Message groupNotice = new Message();
                            groupNotice.setChatSessionId(groupMsgNotice.getSiteGroupId());
                            groupNotice.setMsgId(groupMsgNotice.getMsgId());
                            groupNotice.setSiteUserId(groupMsgNotice.getSiteUserId());
                            groupNotice.setGroupId(groupMsgNotice.getSiteGroupId());
                            groupNotice.setContent(groupMsgNotice.getText().toStringUtf8());
                            groupNotice.setMsgTime(groupMsgNotice.getTime());
                            groupNotice.setMsgType(CoreProto.MsgType.GROUP_NOTICE_VALUE);
                            groupNotice.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);
                            messages.add(groupNotice);
                            break;

                        case CoreProto.MsgType.U2_NOTICE_VALUE:
                            CoreProto.U2MsgNotice u2MsgNotice = withPointer.getU2MsgNotice();
                            Message u2Notice = new Message();
                            u2Notice.setMsgId(u2MsgNotice.getMsgId());
                            u2Notice.setMsgTime(u2MsgNotice.getTime());
                            u2Notice.setSiteUserId(u2MsgNotice.getSiteUserId());
                            u2Notice.setSiteFriendId(u2MsgNotice.getSiteFriendId());
                            // 说明：ChatSessionId = 发送者的siteUserId
                            // 当发送者为当前用户自身，则ChatSessionId=接受者
                            u2Notice.setChatSessionId(u2MsgNotice.getSiteUserId());
                            u2Notice.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);
                            if (u2MsgNotice.getSiteUserId().equals(curSiteUserId)) {
                                u2Notice.setChatSessionId(u2MsgNotice.getSiteFriendId());
                            }
                            u2Notice.setContent(u2MsgNotice.getText().toStringUtf8());
                            u2Notice.setMsgType(CoreProto.MsgType.U2_NOTICE_VALUE);
                            messages.add(u2Notice);
                            break;
                        case CoreProto.MsgType.GROUP_WEB_VALUE:
                            CoreProto.GroupWeb groupWeb = withPointer.getGroupWeb();
                            Message groupWebMsg = new Message();
                            groupWebMsg.setMsgId(groupWeb.getMsgId());
                            groupWebMsg.setSiteUserId(groupWeb.getSiteUserId());
                            groupWebMsg.setGroupId(groupWeb.getSiteGroupId());
                            groupWebMsg.setChatSessionId(groupWeb.getSiteGroupId());
                            groupWebMsg.setContent(groupWeb.getWebCode());
                            groupWebMsg.setMsgTime(groupWeb.getTime());
                            groupWebMsg.setMsgType(CoreProto.MsgType.GROUP_WEB_VALUE);
                            if (groupWeb.getSiteUserId().equals(curSiteUserId)) {
                                groupWebMsg.setMsgStatus(Message.STATUS_SEND_SUCCESS);
                            } else {
                                groupWebMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);
                            }
                            groupWebMsg.setMsgHeight(groupWeb.getHeight());
                            groupWebMsg.setMsgWidth(groupWeb.getWidth());
                            groupWebMsg.setHrefUrl(groupWeb.getHrefUrl());
                            messages.add(groupWebMsg);
                            break;

                        case CoreProto.MsgType.GROUP_WEB_NOTICE_VALUE:
                            CoreProto.GroupWebNotice groupWebNotice = withPointer.getGroupWebNotice();
                            Message groupWebMsgNotice = new Message();
                            groupWebMsgNotice.setMsgId(groupWebNotice.getMsgId());
                            groupWebMsgNotice.setSiteUserId(groupWebNotice.getSiteUserId());
                            groupWebMsgNotice.setGroupId(groupWebNotice.getSiteGroupId());
                            groupWebMsgNotice.setChatSessionId(groupWebNotice.getSiteGroupId());
                            groupWebMsgNotice.setContent(groupWebNotice.getWebCode());
                            groupWebMsgNotice.setMsgTime(groupWebNotice.getTime());
                            groupWebMsgNotice.setMsgType(CoreProto.MsgType.GROUP_WEB_NOTICE_VALUE);
                            groupWebMsgNotice.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);
                            groupWebMsgNotice.setHrefUrl(groupWebNotice.getHrefUrl());
                            groupWebMsgNotice.setMsgHeight(groupWebNotice.getHeight());
                            if (groupWebNotice.getSiteUserId().equals(curSiteUserId)) {
                                groupWebMsgNotice.setMsgStatus(Message.STATUS_SEND_SUCCESS);
                            } else {
                                groupWebMsgNotice.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);
                            }
                            messages.add(groupWebMsgNotice);
                            break;

                        case CoreProto.MsgType.U2_WEB_NOTICE_VALUE:
                            CoreProto.U2WebNotice u2WebNotice = withPointer.getU2WebNotice();
                            Message u2WebNoticeMsg = new Message();
                            u2WebNoticeMsg.setMsgId(u2WebNotice.getMsgId());
                            u2WebNoticeMsg.setChatSessionId(u2WebNotice.getSiteFriendId());
                            u2WebNoticeMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);

                            if (u2WebNotice.getSiteFriendId().equals(curSiteUserId)) {
                                u2WebNoticeMsg.setChatSessionId(u2WebNotice.getSiteUserId());
                                u2WebNoticeMsg.setMsgStatus(Message.STATUS_SEND_SUCCESS);
                            }
                            u2WebNoticeMsg.setSiteUserId(u2WebNotice.getSiteUserId());
                            u2WebNoticeMsg.setSiteFriendId(u2WebNotice.getSiteFriendId());
                            u2WebNoticeMsg.setContent(u2WebNotice.getWebCode());
                            u2WebNoticeMsg.setMsgTime(u2WebNotice.getTime());
                            u2WebNoticeMsg.setMsgType(CoreProto.MsgType.U2_WEB_NOTICE_VALUE);
                            u2WebNoticeMsg.setHrefUrl(u2WebNotice.getHrefUrl());
                            u2WebNoticeMsg.setMsgHeight(u2WebNotice.getHeight());
                            messages.add(u2WebNoticeMsg);
                            break;
                        case CoreProto.MsgType.U2_WEB_VALUE:
                            CoreProto.U2Web u2Web = withPointer.getU2Web();
                            Message u2WebMsg = new Message();
                            u2WebMsg.setMsgId(u2Web.getMsgId());
                            u2WebMsg.setChatSessionId(u2Web.getSiteFriendId());
                            u2WebMsg.setMsgStatus(Message.STATUS_RECEIVE_UNREAD);

                            if (u2Web.getSiteFriendId().equals(curSiteUserId)) {
                                u2WebMsg.setChatSessionId(u2Web.getSiteUserId());
                                u2WebMsg.setMsgStatus(Message.STATUS_SEND_SUCCESS);
                            }
                            u2WebMsg.setSiteUserId(u2Web.getSiteUserId());
                            u2WebMsg.setSiteFriendId(u2Web.getSiteFriendId());
                            u2WebMsg.setContent(u2Web.getWebCode());
                            u2WebMsg.setMsgTime(u2Web.getTime());
                            u2WebMsg.setMsgType(CoreProto.MsgType.U2_WEB_VALUE);

                            u2WebMsg.setHrefUrl(u2Web.getHrefUrl());
                            u2WebMsg.setMsgWidth(u2Web.getWidth());
                            u2WebMsg.setMsgHeight(u2Web.getHeight());
                            messages.add(u2WebMsg);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (Message message : messages) {
                    String tmpGroupId = message.getGroupId();
                    if (tmpGroupId == null || tmpGroupId.isEmpty()) {
                        this.pointer = withPointer.getPointer();
                    } else {
                        this.groupPointers.put(tmpGroupId, withPointer.getPointer());
                    }
                }
            }

            if (messages != null && messages.size() > 0) {

                ZalyLogUtils.getInstance().info(TAG, "batch inserting...");
                // 插入数据库
                SiteAddress siteAddressObj = new SiteAddress(siteAddress);

                ArrayList<Message> u2Messages = new ArrayList<>();
                ArrayList<Message> groupMessages = new ArrayList<>();
                for (Message message : messages) {
                    if (StringUtils.isEmpty(message.getGroupId()))
                        u2Messages.add(message);
                    else groupMessages.add(message);
                }
                if (u2Messages.size() > 0) {
//                    SiteMessageDao.getInstance(siteAddressObj).batchInsertU2Messages(u2Messages);
//                    ZalyLogUtils.getInstance().info(TAG, "inserting U2 messages: " + u2Messages);
                }
                if (groupMessages.size() > 0) {
//                    SiteMessageDao.getInstance(siteAddressObj).batchInsertGroupMessages(groupMessages);
//                    ZalyLogUtils.getInstance().info(TAG, "inserting Group messages: " + groupMessages);
                }
            }

            // 通知UI进程
//            Bundle bundle = new Bundle();
//            bundle.putString(ZalyDbContentHelper.KEY_SITE_IDENTITY, siteIdentity);
//            bundle.putString(ZalyDbContentHelper.KEY_CUR_SITE_USER_ID, curSiteUserId);
//            bundle.putParcelableArrayList(ZalyDbContentHelper.KEY_MSG_RECEIVE_LIST, messages);
//            bundle.putBoolean(ZalyDbContentHelper.KEY_MSG_RECEIVE_FINISH, isReceiveMsgFinish);
//            ZalyDbContentHelper.executeAction(ZalyDbContentHelper.Action.MSG_RECEIVE, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
