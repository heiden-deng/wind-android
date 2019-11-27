package com.windchat.client.chat.presenter.impl;

import android.os.Bundle;
import android.util.Base64;

import com.windchat.client.ZalyApplication;
import com.windchat.client.api.ApiClient;
import com.windchat.client.api.ZalyAPIException;
import com.windchat.client.bean.AudioInfo;
import com.windchat.client.bean.ChatSession;
import com.windchat.client.bean.ImageInfo;
import com.windchat.client.bean.Message;
import com.windchat.client.bean.Site;
import com.windchat.client.chat.MessageAdapter;
import com.windchat.client.chat.presenter.IMessagePresenter;
import com.windchat.client.chat.view.IMessageView;
import com.windchat.client.db.ZalyDbContentHelper;
import com.windchat.client.db.dao.SiteMessageDao;
import com.windchat.client.plugin.task.GetMsgPluginListTask;
import com.windchat.client.site.presenter.impl.SitePresenter;
import com.windchat.client.util.MsgUtils;
import com.windchat.client.util.NetUtils;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.file.ImageRoateUtil;
import com.windchat.client.util.file.UploadFileUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.security.RSAUtils;
import com.windchat.client.util.task.ZalyTaskExecutor;
import com.windchat.client.util.toast.Toaster;
import com.akaxin.proto.core.CoreProto;
import com.akaxin.proto.core.DeviceProto;
import com.akaxin.proto.core.FileProto;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.site.ApiDeviceProfileProto;
import com.akaxin.proto.site.ApiSecretChatApplyU2Proto;
import com.windchat.im.IMClient;
import com.windchat.im.socket.SiteAddress;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yichao on 2017/10/20.
 */

public class U2MessagePresenter implements IMessagePresenter {

    private static final String TAG = "U2MessagePresenter";
    private IMessageView iView;

    // 是否为绝密模式
    private boolean isSecretMode = false;
    // 对方的设备信息
    private DeviceProto.SimpleDeviceProfile friendDeviceInfo;
    private String friendSiteUserId;
    private UserProto.SimpleUserProfile friendSimpleProfile;
    private MessageAdapter msgAdapter;
    private String chatSessionId;
    private String resendDevicePubk;
    private Boolean isLastedSecretMode = false;
    public Site currentSite;

    private static final int U2_MSG_PAGE_SIZE = 20;

    @Override
    public void bindView(IMessageView messageView) {
        this.iView = messageView;
    }

    @Override
    public void loadInitData(String friendSiteUserId, Site currentSite) {
        this.currentSite = currentSite;
        this.friendSiteUserId = friendSiteUserId;
        this.chatSessionId = friendSiteUserId;

        msgAdapter = new MessageAdapter(iView.getContext(), chatSessionId, MessageAdapter.IS_U2_MESSAGE, currentSite);
        iView.setMsgRvAdapter(msgAdapter);
        ZalyTaskExecutor.executeUserTask(TAG, new LoadInitMsgFromDB());
        ZalyTaskExecutor.executeUserTask(TAG, new GetMsgPluginListTask(iView, GetMsgPluginListTask.U2_MSG_PLUGIN, chatSessionId, currentSite));
    }

    @Override
    public void loadNewMsgFromDB() {
        if (iView == null || msgAdapter == null) {
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new LoadNewMsgFromDB(msgAdapter.getNewMsgId()));
    }

    @Override
    public void loadNewMsgFromRecevieDB() {
        if (iView == null || msgAdapter == null) {
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new LoadNewMsgFromDB(msgAdapter.getNewMsgIdByReceive()));
    }

    @Override
    public void loadMoreMsg() {
        if (iView == null) {
            return;
        }

        ZalyTaskExecutor.executeUserTask(TAG, new LoadHistoryMsgFromDB(msgAdapter.getLastMsgId()));
    }

    @Override
    public void LoadIsOpenTsFromChatSession() {
        if (iView == null) {
            return;
        }

        ZalyTaskExecutor.executeUserTask(TAG, new LoadIsOpenTsFromChatSession());
    }


    /**
     * 加载指定条数的消息用于加载未读消息，未读消息数 - 初始加载消息数 + 5（多加载几条用于展示）
     *
     * @param msgNum
     */
    @Override
    public void loadMoreMsg(int msgNum) {
        if (iView == null) {
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new LoadHistoryMsgFromDB(msgAdapter.getLastMsgId(), msgNum - U2_MSG_PAGE_SIZE + 5, true));
    }

    @Override
    public void switchSecretMode(boolean open) {
        ////断网状态直接提示用户，稍候再试
        if (!isLastedSecretMode) {
            boolean isNet = NetUtils.getNetInfo();
            if (!isNet && open) {
                setTopSecretOff();
                Toaster.showInvalidate("请稍候再试");
                return;
            }
            if (open) {
                ZalyTaskExecutor.executeUserTask(TAG, new ApplySecretTask(friendSiteUserId));
            } else {
                setTopSecretOff();
            }
        }
    }

    @Override
    public void cleanUnreadNum() {
        SitePresenter.getInstance().cleanUnreadNum(currentSite, chatSessionId);
    }

    @Override
    public void sendTextMessage(String content) {
        Message u2Message = new Message();
        u2Message.setContent(content);
        u2Message.setSiteUserId(currentSite.getSiteUserId());
        u2Message.setSiteToId(friendSiteUserId);
        u2Message.setChatSessionId(chatSessionId);
        u2Message.setMsgId(MsgUtils.getCurMsgId(MsgUtils.MSG_TYPE_U2, currentSite));
        long time = System.currentTimeMillis();
        u2Message.setMsgTime(time);
        u2Message.setSendMsgTime(time);
        u2Message.setSecret(isSecretMode);
        boolean isNet = NetUtils.getNetInfo();
        if (!isNet) {
            u2Message.setMsgStatus(Message.STATUS_SEND_FAILED);
        } else {
            u2Message.setMsgStatus(Message.STATUS_SENDING);
        }

        if (isSecretMode) {
            u2Message.setMsgType(CoreProto.MsgType.SECRET_TEXT_VALUE);
            u2Message.setToDeviceId(friendDeviceInfo.getDeviceId());
            String toDevicePubk64 = Base64.encodeToString(friendDeviceInfo.getUserDevicePubk().getBytes(), Base64.NO_WRAP);
            u2Message.setToDevicePubk(toDevicePubk64);
            ZalyTaskExecutor.executeUserTask(TAG, new SendSecretTask(SendSecretTask.INSERT_MODE, u2Message, friendDeviceInfo.getUserDevicePubk()));
        } else {
            u2Message.setMsgType(CoreProto.MsgType.TEXT_VALUE);
            ZalyTaskExecutor.executeUserTask(TAG, new SendMessageTask(SendMessageTask.INSERT_MODE, u2Message));
        }
        iView.onStartSendingMessage(u2Message);
    }

    @Override
    public void resendMessage(Message msg) {
        switch (msg.getMsgType()) {
            case CoreProto.MsgType.TEXT_VALUE:
            case CoreProto.MsgType.SECRET_TEXT_VALUE:
                resendTextMessage(msg);
                break;
            case CoreProto.MsgType.IMAGE_VALUE:
            case CoreProto.MsgType.SECRET_IMAGE_VALUE:
                resendImgMessage(msg);
                break;
            case CoreProto.MsgType.VOICE_VALUE:
            case CoreProto.MsgType.SECRET_VOICE_VALUE:
                resendAudioMessage(msg);
                break;
        }
    }

    @Override
    public void resendTextMessage(Message message) {
        boolean isNet = NetUtils.getNetInfo();
        if (!isNet) {
            return;
        }
        if (message.isSecret()) {
            String toDevicePubk = new String(Base64.decode(message.getToDevicePubk(), Base64.NO_WRAP));
            ZalyTaskExecutor.executeUserTask(TAG, new SendSecretTask(SendSecretTask.UPDATE_MODE, message, toDevicePubk));
        } else {
            ZalyTaskExecutor.executeUserTask(TAG, new SendMessageTask(SendMessageTask.UPDATE_MODE, message));
        }
    }

    @Override
    public void sendAudioMessage(final long audioTime, final String audioFilePath) {
        final Message message = new Message();
        message.setSiteUserId(currentSite.getSiteUserId());
        message.setGroupId(friendSiteUserId);
        message.setChatSessionId(chatSessionId);
        message.setMsgId(MsgUtils.getCurMsgId(MsgUtils.MSG_TYPE_U2, currentSite));
        message.setSecret(isSecretMode);
        final AudioInfo audioInfo = new AudioInfo();
        audioInfo.setAudioFilePath(audioFilePath);
        audioInfo.setAudioTime(audioTime);
        // 为了在未上传时快速展示在界面中
        long time = System.currentTimeMillis();
        message.setMsgTime(time);
        message.setSendMsgTime(time);
        message.setContent(AudioInfo.toJSON(audioInfo));
        boolean isNet = NetUtils.getNetInfo();
        if (!isNet) {
            message.setMsgStatus(Message.STATUS_SEND_FAILED);
        } else {
            message.setMsgStatus(Message.STATUS_SENDING);
        }
        message.setMsgType(CoreProto.MsgType.VOICE_VALUE);

        if (isSecretMode) {
            message.setMsgType(CoreProto.MsgType.SECRET_VOICE_VALUE);
            String toDevicePubk64 = Base64.encodeToString(friendDeviceInfo.getUserDevicePubk().getBytes(), Base64.NO_WRAP);
            message.setToDevicePubk(toDevicePubk64);
            message.setToDeviceId(friendDeviceInfo.getDeviceId());
        }

        ZalyTaskExecutor.executeUserTask(TAG, new SendAudioMessageTask(SendAudioMessageTask.INSERT_MODE, message));

        if (isSecretMode) {

            UploadFileUtils.uploadMsgSecretFile(audioFilePath, new UploadFileUtils.UploadSecretFileListener() {
                @Override
                public void encryptFileSuccess(String tsk64) {
                    message.setMsgTsk(tsk64);
                }

                @Override
                public void onUploadSuccess(String fileId) {
                    audioInfo.setAudioId(fileId);
                    message.setContent(AudioInfo.toJSON(audioInfo));
                    message.setToDeviceId(friendDeviceInfo.getDeviceId());
                    ZalyTaskExecutor.executeUserTask(TAG, new SendAudioMessageTask(SendAudioMessageTask.UPDATE_MODE, message));
                }

                @Override
                public void onUploadFail(Exception e) {
                    ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
                }

                @Override
                public void onProcessRate(int processNum) {
                    sendDBContent(message, processNum);
                }
            }, FileProto.FileType.MESSAGE_VOICE, friendDeviceInfo.getUserDevicePubk(), message, currentSite);
        } else {
            UploadFileUtils.uploadMsgFile(audioFilePath, new UploadFileUtils.UploadFileListener() {
                @Override
                public void onUploadSuccess(String fileId) {
                    audioInfo.setAudioId(fileId);
                    message.setContent(AudioInfo.toJSON(audioInfo));
                    message.setMsgTime(System.currentTimeMillis());
                    ZalyTaskExecutor.executeUserTask(TAG, new SendAudioMessageTask(SendAudioMessageTask.UPDATE_MODE, message));
                }

                @Override
                public void onProcessRate(int processNum) {
                    sendDBContent(message, processNum);
                }

                @Override
                public void onUploadFail(Exception e) {
                    ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());

                }
            }, FileProto.FileType.MESSAGE_VOICE, message, currentSite);
        }
        iView.onStartSendingMessage(message);
    }

    public void sendDBContent(Message message, int processNum) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ZalyDbContentHelper.IMG_PROCESS_MSG_INFO, message);
        bundle.putInt(ZalyDbContentHelper.IMG_PROCESS_NUM, processNum);
        ZalyDbContentHelper.executeAction(ZalyDbContentHelper.Action.MSG_IMG_PROCESS, bundle);
    }

    /**
     * 删除二人的某条消息
     *
     * @param msgId
     */
    @Override
    public void deleteU2Msg(String msgId) {
        SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort())).deleteU2MsgById(msgId);
    }

    /**
     * 删除二人的消息
     *
     * @param chatSessionId
     */
    @Override
    public void deleteU2MsgByChatSessionId(String chatSessionId) {
        SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort())).deleteU2MsgByChatSessionId(chatSessionId);
    }

    @Override
    public void resendAudioMessage(Message msg) {
        final Message message = msg;
        final AudioInfo audioInfo = AudioInfo.parseJSON(msg.getContent());
        String audioFilePath = audioInfo.getAudioFilePath();
        if (msg.isSecret()) {
            String toDevicePubk = new String(Base64.decode(msg.getToDevicePubk(), Base64.NO_WRAP));

            UploadFileUtils.uploadMsgSecretFile(audioFilePath, new UploadFileUtils.UploadSecretFileListener() {
                @Override
                public void encryptFileSuccess(String tsk64) {
                    message.setMsgTsk(tsk64);
                }

                @Override
                public void onUploadSuccess(String fileId) {
                    audioInfo.setAudioId(fileId);
                    message.setContent(AudioInfo.toJSON(audioInfo));
                    ZalyTaskExecutor.executeUserTask(TAG, new SendAudioMessageTask(SendMessageTask.UPDATE_MODE, message));
                }

                @Override
                public void onUploadFail(Exception e) {
                    ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
                }

                @Override
                public void onProcessRate(int processNum) {
                    sendDBContent(message, processNum);
                }

            }, FileProto.FileType.MESSAGE_VOICE, toDevicePubk, message, currentSite);
        } else {
            UploadFileUtils.uploadMsgFile(audioFilePath, new UploadFileUtils.UploadFileListener() {
                @Override
                public void onUploadSuccess(String fileId) {
                    audioInfo.setAudioId(fileId);
                    message.setContent(AudioInfo.toJSON(audioInfo));
                    ZalyTaskExecutor.executeUserTask(TAG, new SendAudioMessageTask(SendMessageTask.UPDATE_MODE, message));
                }

                @Override
                public void onUploadFail(Exception e) {
                    ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
                }

                @Override
                public void onProcessRate(int processNum) {
                    sendDBContent(message, processNum);
                }
            }, FileProto.FileType.MESSAGE_VOICE, message, currentSite);
        }
        iView.refreshMsgStatus(message);
    }


    @Override
    public void sendImgMessage(final String imgPath) {


        ZalyLogUtils.getInstance().info(TAG, "doRotateImageAndSave === start");

        ImageRoateUtil.doRotateImageAndSave(imgPath);

        ZalyLogUtils.getInstance().info(TAG, "doRotateImageAndSave === end");


        if (StringUtils.isEmpty(imgPath)) {
            return;
        }
        File file = new File(imgPath);
        final long length = file.length();
        if (length <= 0) {
            iView.onSentImageInvalidate();
            return;
        }

        final ImageInfo imageInfo = new ImageInfo();
        imageInfo.setFilePath(imgPath);
        imageInfo.setFileLength(length);
        imageInfo.setStatus(ImageInfo.STATUS_UPLOADING);

        final Message message = new Message();
        message.setContent(ImageInfo.toJSON(imageInfo));
        message.setSiteUserId(currentSite.getSiteUserId());
        message.setGroupId(friendSiteUserId);
        message.setChatSessionId(chatSessionId);
        message.setMsgId(MsgUtils.getCurMsgId(MsgUtils.MSG_TYPE_U2, currentSite));
        long time = System.currentTimeMillis();
        message.setMsgTime(time);
        message.setSendMsgTime(time);
        message.setSecret(isSecretMode);

        if (isSecretMode) {
            message.setMsgType(CoreProto.MsgType.SECRET_IMAGE_VALUE);
        } else {
            message.setMsgType(CoreProto.MsgType.IMAGE_VALUE);
        }
        boolean isNet = NetUtils.getNetInfo();
        if (!isNet) {
            message.setMsgStatus(Message.STATUS_SEND_FAILED);
        } else {
            message.setMsgStatus(Message.STATUS_SENDING);
        }

        if (friendDeviceInfo != null) {
            message.setToDeviceId(friendDeviceInfo.getDeviceId());
            String toDevicePubk64 = Base64.encodeToString(friendDeviceInfo.getUserDevicePubk().getBytes(), Base64.NO_WRAP);
            message.setToDevicePubk(toDevicePubk64);
        }

        ZalyTaskExecutor.executeUserTask(TAG, new UpdateImgMsgDBTask(UpdateImgMsgDBTask.INSERT_MODE, message));


        if (isSecretMode) {
            UploadFileUtils.uploadMsgSecretFile(imgPath, new UploadFileUtils.UploadSecretFileListener() {
                @Override
                public void encryptFileSuccess(String tsk64) {
                    message.setMsgTsk(tsk64);
                }

                @Override
                public void onUploadSuccess(String fileId) {
                    imageInfo.setFileId(fileId);
                    imageInfo.setStatus(ImageInfo.STATUS_SEND);
                    message.setContent(ImageInfo.toJSON(imageInfo));
                    ZalyTaskExecutor.executeUserTask(TAG, new UpdateImgMsgDBTask(UpdateImgMsgDBTask.UPDATE_MODE, message));
                    sendMsgByIMManger(message);
                }

                @Override
                public void onUploadFail(Exception e) {
                    message.setMsgStatus(Message.STATUS_SEND_FAILED);
                    iView.onSendingMessageError(message);
                }

                @Override
                public void onProcessRate(int processNum) {
                    sendDBContent(message, processNum);
                }
            }, FileProto.FileType.MESSAGE_IMAGE, friendDeviceInfo.getUserDevicePubk(), message, currentSite);
        } else {
            UploadFileUtils.uploadMsgFile(imgPath, new UploadFileUtils.UploadFileListener() {
                @Override
                public void onUploadSuccess(String fileId) {
                    imageInfo.setFileId(fileId);
                    imageInfo.setStatus(ImageInfo.STATUS_SEND);
                    message.setContent(ImageInfo.toJSON(imageInfo));
                    ZalyTaskExecutor.executeUserTask(TAG, new UpdateImgMsgDBTask(UpdateImgMsgDBTask.UPDATE_MODE, message));
                    try {
                        sendMsgByIMManger(message);
                    } catch (Exception e) {
                        ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
                    }
                }

                @Override
                public void onUploadFail(Exception e) {
                    message.setMsgStatus(Message.STATUS_SEND_FAILED);
                    iView.onSendingMessageError(message);
                }

                @Override
                public void onProcessRate(int processNum) {
//                    WindLogger.getInstance().info(TAG, " process num is " + processNum);
                    sendDBContent(message, processNum);
                }

            }, FileProto.FileType.MESSAGE_IMAGE, message, currentSite);
        }
        iView.onStartSendingMessage(message);
    }

    @Override
    public void resendImgMessage(Message msg) {

        final Message message = msg;
        final ImageInfo imageInfo = ImageInfo.parseJSON(msg.getContent());

        String imgPath = imageInfo.getFilePath();

        if (msg.isSecret()) {
            String toDevicePubk = new String(Base64.decode(msg.getToDevicePubk(), Base64.NO_WRAP));

            UploadFileUtils.uploadMsgSecretFile(imgPath, new UploadFileUtils.UploadSecretFileListener() {
                @Override
                public void encryptFileSuccess(String tsk64) {
                    message.setMsgTsk(tsk64);
                }

                @Override
                public void onUploadSuccess(String fileId) {
                    imageInfo.setFileId(fileId);
                    imageInfo.setStatus(ImageInfo.STATUS_SEND);
                    message.setContent(ImageInfo.toJSON(imageInfo));

                    ZalyTaskExecutor.executeUserTask(TAG, new UpdateImgMsgDBTask(UpdateImgMsgDBTask.UPDATE_MODE, message));
                    sendMsgByIMManger(message);
                }

                @Override
                public void onUploadFail(Exception e) {
                    message.setMsgStatus(Message.STATUS_SEND_FAILED);
                    iView.onSendingMessageError(message);
                }

                @Override
                public void onProcessRate(int processNum) {
                    sendDBContent(message, processNum);
                }

            }, FileProto.FileType.MESSAGE_IMAGE, toDevicePubk, message, currentSite);
        } else {
            UploadFileUtils.uploadMsgFile(imgPath, new UploadFileUtils.UploadFileListener() {
                @Override
                public void onUploadSuccess(String fileId) {
                    imageInfo.setFileId(fileId);
                    imageInfo.setStatus(ImageInfo.STATUS_SEND);
                    message.setContent(ImageInfo.toJSON(imageInfo));

                    ZalyTaskExecutor.executeUserTask(TAG, new UpdateImgMsgDBTask(UpdateImgMsgDBTask.UPDATE_MODE, message));
                    sendMsgByIMManger(message);
                }

                @Override
                public void onUploadFail(Exception e) {
                    message.setMsgStatus(Message.STATUS_SEND_FAILED);
                    iView.onSendingMessageError(message);
                }

                @Override
                public void onProcessRate(int processNum) {
                    sendDBContent(message, processNum);
                }
            }, FileProto.FileType.MESSAGE_IMAGE, message, currentSite);
        }
        iView.refreshMsgStatus(message);
    }

    @Override
    public void updateMsgStatus(String msgId, int status) {

        if (iView == null || msgAdapter == null || StringUtils.isEmpty(msgId)) {
            return;
        }
        List<Message> messageList = msgAdapter.getMessages();
        int index = 0;
        for (; index < messageList.size(); index++) {
            if (msgId.equals(messageList.get(index).getMsgId())) {
                messageList.get(index).setMsgStatus(status);
                msgAdapter.notifyItemChanged(index);
                return;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshMsgStatus(String msgId) {
        if (iView == null || msgAdapter == null || StringUtils.isEmpty(msgId)) {
            return;
        }
        List<Message> messageList = msgAdapter.getMessages();
        int index = 0;
        for (; index < messageList.size(); index++) {
            if (msgId.equals(messageList.get(index).getMsgId())) {
                msgAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    @Override
    public String getFriendDeviceName() {
        return friendDeviceInfo.getDeviceName();
    }

    @Override
    public boolean isSecretMode() {
        return isSecretMode;
    }

    @Override
    public void setFriendDevPubKey64Str(DeviceProto.SimpleDeviceProfile deviceInfo) {
        this.friendDeviceInfo = deviceInfo;
    }

    @Override
    public void getSecretDeviceInfo(String deviceId) {
        if (!StringUtils.isEmpty(deviceId)) {
            ZalyTaskExecutor.executeUserTask(TAG, new GetSecretDeviceInfo(deviceId));
        }
    }

    @Override
    public void onDestroy() {
        ZalyTaskExecutor.cancleAllTasksByTag(TAG);
    }


    class GetSecretDeviceInfo extends ZalyTaskExecutor.Task<Void, Void, ApiDeviceProfileProto.ApiDeviceProfileResponse> {

        private String deviceId;


        public GetSecretDeviceInfo(String deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            if (iView != null) {
                iView.showProgress();
            }
        }

        @Override
        protected ApiDeviceProfileProto.ApiDeviceProfileResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getDeviceApi().getDeviceDetail(deviceId);
        }

        @Override
        protected void onTaskSuccess(ApiDeviceProfileProto.ApiDeviceProfileResponse response) {
            super.onTaskSuccess(response);
            if (iView != null) {
                iView.showSecretDeviceInfoDialog(response.getDeviceProfile().getDeviceName());
            }
            resendDevicePubk = response.getDeviceProfile().getUserDevicePubk();
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            if (iView != null) {
                iView.hideProgress();
            }
        }
    }

    class UpdateImgMsgDBTask extends ZalyTaskExecutor.Task<Void, Void, Message> {

        public static final int INSERT_MODE = 1;
        public static final int UPDATE_MODE = 2;

        private int mode = INSERT_MODE;
        private Message message;

        public UpdateImgMsgDBTask(int mode, Message message) {
            this.mode = mode;
            this.message = message;
        }

        @Override
        protected Message executeTask(Void... voids) throws Exception {
            SiteAddress siteAddress = ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort());

            if (mode == INSERT_MODE) {
                long _id = SiteMessageDao.getInstance(siteAddress).insertU2Message(message);
                message.set_id(_id);
            }
            if (mode == UPDATE_MODE) {
                SiteMessageDao.getInstance(siteAddress).updateU2MsgContent(message.get_id(), message.getContent(), message.getMsgTsk());
            }
            return message;
        }

        @Override
        protected void onTaskSuccess(Message message) {
            super.onTaskSuccess(message);
            if (mode == INSERT_MODE) iView.onSendMessageFinish(message);
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            if (mode == INSERT_MODE) iView.onSendingMessageError(message);
        }

        @Override
        protected void onTaskError(Exception e) {
            iView.onSendingMessageError(message);
        }
    }

    /**
     * 发送普通消息任务
     */
    class SendMessageTask extends ZalyTaskExecutor.SendMsgTask<Void, Void, Message> {

        private Message message;
        public static final int INSERT_MODE = 1;
        public static final int UPDATE_MODE = 2;

        private int mode = INSERT_MODE;

        public SendMessageTask(int mode, Message message) {
            this.mode = mode;
            this.message = message;
            this.message.setChatType(com.windchat.im.message.Message.ChatType.MSG_U2);
        }

        @Override
        protected Message executeTask(Void... voids) throws Exception {
            Long _id = null;
            if (mode == INSERT_MODE) {
                ////修改信息写入2人消息表
                SiteAddress siteAddress = ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort());
                _id = SiteMessageDao.getInstance(siteAddress).insertU2Message(message);

            }
            if (mode == UPDATE_MODE) {
                _id = message.get_id();
            }

            if (_id == null) {
                throw new Exception("message sending error");
            } else {
                sendMsgByIMManger(message);
                message.set_id(_id);
                return message;
            }

        }

        @Override
        protected void onTaskSuccess(Message message) {
            super.onTaskSuccess(message);
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
            super.onTaskError(e);
            iView.onSendingMessageError(message);
        }
    }

    /**
     * 发送普通消息任务
     */
    class SendAudioMessageTask extends ZalyTaskExecutor.SendMsgTask<Void, Void, Message> {

        private Message message;
        public static final int INSERT_MODE = 1;
        public static final int UPDATE_MODE = 2;

        private int mode = INSERT_MODE;

        public SendAudioMessageTask(int mode, Message message) {
            this.mode = mode;
            this.message = message;
        }

        @Override
        protected Message executeTask(Void... voids) throws Exception {
            Long _id = null;
            SiteAddress siteAddress = ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort());

            if (mode == INSERT_MODE) {
                ////修改信息写入2人消息表
                _id = SiteMessageDao.getInstance(siteAddress).insertU2Message(message);

            }
            if (mode == UPDATE_MODE) {
                _id = message.get_id();
                SiteMessageDao.getInstance(siteAddress).updateU2MsgContent(message.get_id(), message.getContent(), message.getMsgTsk());
                sendMsgByIMManger(message);
                message.set_id(_id);
            }

            if (_id == null) {
                throw new Exception("message sending error");
            } else {
                message.set_id(_id);
                return message;
            }

        }

        @Override
        protected void onTaskSuccess(Message message) {
            super.onTaskSuccess(message);
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
            super.onTaskError(e);
            iView.onSendingMessageError(message);
        }
    }

    /**
     * 使用imManger发送消息
     *
     * @param message
     */
    protected void sendMsgByIMManger(Message message) {
        boolean isNet = NetUtils.getNetInfo();
        if (isNet) {
            try {
                IMClient.getInstance(currentSite).sendMessage(message);
            } catch (Exception e) {
                ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            }
        }
    }

    /**
     * 发送绝密消息任务
     */
    class SendSecretTask extends ZalyTaskExecutor.SendMsgTask<Void, Void, Message> {

        private Message targetMessage;
        private Message rawMessage;
        private String pubKeyStr;
        private boolean isNet;
        public static final int INSERT_MODE = 1;
        public static final int UPDATE_MODE = 2;

        private int mode = INSERT_MODE;

        public SendSecretTask(int mode, Message message, String pubKeyStr) {
            this.mode = mode;
            this.rawMessage = message;
            this.pubKeyStr = pubKeyStr;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            targetMessage = Message.copyMessage(rawMessage);
        }

        @Override
        protected Message executeTask(Void... voids) throws Exception {
            RSAUtils.getInstance().encryptMsg(targetMessage, pubKeyStr);
            Long _id = null;
            ////修改信息写入2人消息表
            if (mode == INSERT_MODE) {
                SiteAddress siteAddress = currentSite.getSiteAddress();
                _id = SiteMessageDao.getInstance(siteAddress).insertU2Message(rawMessage);
            }
            if (mode == UPDATE_MODE) {
                _id = rawMessage.get_id();
            }
            if (_id == null) {
                throw new Exception("message sending error");
            } else {
                /////TODO 有网络链接才回发送消息
                sendMsgByIMManger(targetMessage);
                targetMessage.set_id(_id);
                rawMessage.set_id(_id);
                return rawMessage;
            }
        }

        @Override
        protected void onTaskSuccess(Message message) {
            super.onTaskSuccess(message);
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            iView.onSendingMessageError(rawMessage);
        }
    }

    /**
     * 申请绝密聊天
     */
    class ApplySecretTask extends ZalyTaskExecutor.Task<Void, Void, ApiSecretChatApplyU2Proto.ApiSecretChatApplyU2Response> {

        private String friendSiteUserId;

        public ApplySecretTask(String friendSiteUserId) {
            this.friendSiteUserId = friendSiteUserId;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            iView.showProgress("正在获取对方加密公钥");
        }

        @Override
        protected ApiSecretChatApplyU2Proto.ApiSecretChatApplyU2Response executeTask(Void... voids) throws Exception {
            //要求用户察觉加密设置，感觉不优雅
            //Thread.sleep(1000);

            return ApiClient.getInstance(currentSite).getUserApi().applySecretChat(friendSiteUserId);
        }

        @Override
        protected void onTaskSuccess(ApiSecretChatApplyU2Proto.ApiSecretChatApplyU2Response applySecretChatResponse) {
            super.onTaskSuccess(applySecretChatResponse);
            if (iView == null) {
                return;
            }

            friendDeviceInfo = applySecretChatResponse.getDeviceProfile();
            if (StringUtils.isEmpty(friendDeviceInfo.getDeviceId()) ||
                    StringUtils.isEmpty(friendDeviceInfo.getUserDevicePubk())) {
                SitePresenter.getInstance().updateTSByChatSessionId(currentSite, false, friendSiteUserId);
                setTopSecretOff();
                return;
            }
            // 只有走到了这一步, isSecretMode 才真正设置为 true.
            isSecretMode = true;
            SitePresenter.getInstance().updateTSByChatSessionId(currentSite, true, friendSiteUserId);
            iView.onTopSecretOn(friendDeviceInfo.getDeviceName());
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            if (zalyAPIException != null) {
                String errorInfo = zalyAPIException.getErrorInfoStr();
                if (StringUtils.isEmpty(errorInfo)) {
                    errorInfo = "请求失败，请稍候再试";
                }
                Toaster.showInvalidate(errorInfo);
                if (iView == null) {
                    return;
                }
                setTopSecretOff();
                ZalyLogUtils.getInstance().info(TAG, zalyAPIException.getMessage());
            }
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            if (iView == null) {
                return;
            }
            setTopSecretOff();
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            if (iView == null) {
                return;
            }
            iView.hideProgress();
        }
    }

    public void setTopSecretOff() {
        isSecretMode = false;
        SitePresenter.getInstance().updateTSByChatSessionId(currentSite, false, friendSiteUserId);
        iView.onTopSecretOff();
    }


    class LoadInitMsgFromDB extends ZalyTaskExecutor.Task<Void, Void, List<Message>> {

        @Override
        protected List<Message> executeTask(Void... voids) throws Exception {
            return SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort())).queryU2NewMsg(-1, U2_MSG_PAGE_SIZE, chatSessionId);
        }

        @Override
        protected void onTaskSuccess(List<Message> messages) {
            super.onTaskSuccess(messages);

            //清理当前的session 对话未读数量
            SitePresenter.getInstance().cleanUnreadNum(currentSite, chatSessionId);
            if (messages.size() == 0) {
                return;
            }
            if (msgAdapter != null) {
                msgAdapter.addNewMsgItems(messages);
            }
            syncMessageStatus(messages);
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
        }
    }

    @Override
    public void syncMessageStatus(List<Message> messages) {
        try {
            List<String> msgIds = new ArrayList<>();
            for (int i = 0; i < messages.size(); i++) {
                Message msg = messages.get(i);
                /////自己发送的, 24小时有效期
                boolean isOutTime = (System.currentTimeMillis() - msg.getSendMsgTime()) > Message.SYNC_MSG_STATUS_EXPIRE_TIME;
                if (msg.getMsgType() == 1 && msg.getSiteUserId().equals(currentSite.getSiteUserId()) && (!isOutTime)) {
                    msgIds.add(i, msg.getMsgId());
                }
            }
            if (msgIds.size() > 0) {
                ZalyLogUtils.getInstance().info(TAG, "shaoye --  sync msg status == " + msgIds.toString());
                IMClient.getInstance(currentSite).syncMessageStatus(msgIds, CoreProto.MsgType.TEXT_VALUE);
                return;
            }
            ZalyLogUtils.getInstance().info(TAG, "shaoye -- no sync msg status");
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }
    }

    class LoadNewMsgFromDB extends ZalyTaskExecutor.Task<Void, Void, List<Message>> {

        private long _id;

        public LoadNewMsgFromDB(long _id) {
            this._id = _id;
        }

        @Override
        protected List<Message> executeTask(Void... voids) throws Exception {
            return SiteMessageDao.getInstance(currentSite.getSiteAddress()).queryU2NewMsg(_id, -1, chatSessionId);
        }

        @Override
        protected void onTaskSuccess(List<Message> messages) {
            super.onTaskSuccess(messages);
            syncMessageStatus(messages);

            if (msgAdapter != null) {
                msgAdapter.addNewMsgItems(messages);
                iView.onNewMessagesReceived();
            }
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
        }
    }

    class LoadHistoryMsgFromDB extends ZalyTaskExecutor.Task<Void, Void, List<Message>> {

        private long _id;
        private int count = 20;
        private boolean isLoadUnreadMsg = false;

        public LoadHistoryMsgFromDB(long _id) {
            this._id = _id;
        }

        public LoadHistoryMsgFromDB(long _id, int count, boolean isLoadUnreadMsg) {
            this._id = _id;
            this.count = count;
            this.isLoadUnreadMsg = isLoadUnreadMsg;
        }

        @Override
        protected List<Message> executeTask(Void... voids) throws Exception {
            SiteAddress siteAddress = currentSite.getSiteAddress();
            return SiteMessageDao.getInstance(siteAddress).queryU2HistoryMsg(_id, count, friendSiteUserId);
        }

        @Override
        protected void onTaskSuccess(List<Message> messages) {
            super.onTaskSuccess(messages);
            syncMessageStatus(messages);

            if (msgAdapter != null) {
                msgAdapter.addHistoryItems(messages);
            }
            if (iView != null && isLoadUnreadMsg) {
                iView.scrollUplMsgList();
            }
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
        }
    }


    class LoadIsOpenTsFromChatSession extends ZalyTaskExecutor.Task<Void, Void, Boolean> {

        @Override
        protected Boolean executeTask(Void... voids) throws Exception {
            Boolean isOpenTs = SitePresenter.getInstance().getTSByChatSessionId(currentSite, friendSiteUserId);
            ZalyLogUtils.getInstance().info(TAG, " is open ts == " + isOpenTs);
            if (isOpenTs) {
                isLastedSecretMode = true;
                try {
                    ApiSecretChatApplyU2Proto.ApiSecretChatApplyU2Response applySecretChatResponse
                            = ApiClient.getInstance(currentSite).getUserApi().applySecretChat(friendSiteUserId);

                    friendDeviceInfo = applySecretChatResponse.getDeviceProfile();
                    if (StringUtils.isEmpty(friendDeviceInfo.getDeviceId()) ||
                            StringUtils.isEmpty(friendDeviceInfo.getUserDevicePubk())) {
                        setTopSecretOff();
                        return false;
                    }
                    isSecretMode = true;
                    iView.onTopSecretOn(friendDeviceInfo.getDeviceName());
                } catch (Exception e) {
                    ZalyLogUtils.getInstance().exceptionError(e);
                    setTopSecretOff();
                }
                return true;
            }
            isLastedSecretMode = false;
            return true;
        }

        @Override
        protected void onTaskError(Exception e) {
            isLastedSecretMode = false;
            setTopSecretOff();
            ZalyLogUtils.getInstance().exceptionError(e);
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            isLastedSecretMode = false;
            setTopSecretOff();
            ZalyLogUtils.getInstance().exceptionError(zalyAPIException);
        }

        @Override
        protected void onTaskFinish() {
            isLastedSecretMode = false;
        }
    }

    /**
     * 增加新的消息会话
     */
    class AddNewSessionTask extends ZalyTaskExecutor.Task<Void, Void, Boolean> {

        @Override
        protected Boolean executeTask(Void... voids) throws Exception {
            ChatSession chatSession = new ChatSession();
            chatSession.setChatSessionId(friendSiteUserId);
            chatSession.setLastMsgId("-1");
            chatSession.setDesc("暂无消息");
            chatSession.setStatus(ChatSession.STATUS_NEW_SESSION);
            chatSession.setType(ChatSession.TYPE_FRIEND_SESSION);
            chatSession.setLatestTime(new Date().getTime());

            if (friendSimpleProfile != null) {
                chatSession.setIcon("");
                String userName = friendSimpleProfile.getUserName();
                if (StringUtils.isEmpty(userName)) {
                    userName = friendSimpleProfile.getSiteUserId();
                }
                chatSession.setTitle(userName);
            }
            return SitePresenter.getInstance().insertChatSession(currentSite.getHostAndPort(), chatSession);
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, " error msg is " + e.getMessage());
        }
    }

    public void setFriendSimpleProfile(UserProto.SimpleUserProfile friendSimpleProfile) {
        this.friendSimpleProfile = friendSimpleProfile;
        if (friendSimpleProfile != null) {
            ZalyTaskExecutor.executeUserTask(TAG, new AddNewSessionTask());
        }
    }

}
