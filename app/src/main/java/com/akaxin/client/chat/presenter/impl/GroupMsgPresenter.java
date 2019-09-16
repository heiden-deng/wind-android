package com.akaxin.client.chat.presenter.impl;

import android.os.Bundle;

import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.AudioInfo;
import com.akaxin.client.bean.ChatSession;
import com.akaxin.client.bean.ImageInfo;
//import com.akaxin.client.bean.Message;
import com.akaxin.client.bean.Message;
import com.akaxin.client.bean.Site;
import com.akaxin.client.chat.MessageAdapter;
import com.akaxin.client.chat.presenter.IGroupMsgPresenter;
import com.akaxin.client.chat.view.IGroupMsgView;
import com.akaxin.client.db.ZalyDbContentHelper;
import com.akaxin.client.db.dao.SiteMessageDao;
import com.akaxin.client.plugin.task.GetMsgPluginListTask;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.util.MsgUtils;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.file.UploadFileUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.CoreProto;
import com.akaxin.proto.core.FileProto;
import com.akaxin.proto.core.GroupProto;
import com.orhanobut.logger.Logger;
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

public class GroupMsgPresenter implements IGroupMsgPresenter {

    private static final String TAG = GroupMsgPresenter.class.getSimpleName();
    private static final int GROUP_MSG_PAGE_SIZE = 20;

    private IGroupMsgView iView;

    private boolean isSecretMode = false;//是否为绝密模式
    private String groupId;//群组Id
    private String chatSessionId;//群组Id
    private MessageAdapter msgAdapter;
    private GroupProto.SimpleGroupProfile groupProfile;
    public Site currentSite;

    @Override
    public void bindView(IGroupMsgView messageView) {
        this.iView = messageView;
    }

    @Override
    public void loadInitData(String groupId, Site currentSite) {
        this.groupId = groupId;
        this.chatSessionId = groupId;
        this.currentSite = currentSite;
        msgAdapter = new MessageAdapter(iView.getContext(), chatSessionId, MessageAdapter.IS_GROUP_MESSAGE, currentSite);
        iView.setMsgRvAdapter(msgAdapter);
        ZalyTaskExecutor.executeUserTask(TAG, new LoadInitMsgFromDB());
        ZalyTaskExecutor.executeUserTask(TAG, new GetMsgPluginListTask(iView, GetMsgPluginListTask.GROUP_MSG_PLUGIN, chatSessionId, currentSite));
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
    public void loadMoreHistoryMsg() {
        if (iView == null) {
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new LoadHistoryMsgFromDB(msgAdapter.getLastMsgId()));
    }

    @Override
    public void onSecretMode(boolean open) {

    }

    @Override
    public void cleanUnreadNum() {
        SitePresenter.getInstance().cleanUnreadNum(currentSite, chatSessionId);
    }

    @Override
    public void sendTextMessage(Message message) {
        //发送者
        message.setSiteUserId(currentSite.getSiteUserId());
        //群ID，这里为接受者
        message.setGroupId(groupId);
        message.setMsgId(MsgUtils.getCurMsgId(MsgUtils.MSG_TYPE_GROUP, currentSite));
        message.setMsgTime(System.currentTimeMillis());
        message.setSecret(isSecretMode);
        message.setMsgType(CoreProto.MsgType.GROUP_TEXT_VALUE);
        boolean isNet = NetUtils.getNetInfo();
        if (!isNet) {
            message.setMsgStatus(Message.STATUS_SEND_FAILED);
        } else {
            message.setMsgStatus(Message.STATUS_SENDING);
        }
        message.setChatSessionId(chatSessionId);
        ZalyTaskExecutor.executeUserTask(TAG, new SendMessageTask(SendMessageTask.INSERT_MODE, message));
        iView.onStartSendingMessage(message);
    }

    @Override
    public void resendTextMessage(Message message) {
        ZalyTaskExecutor.executeUserTask(TAG, new SendMessageTask(SendMessageTask.UPDATE_MODE, message));
        iView.refreshMsgStatus(message);
    }

    @Override
    public void sendAudioMessage(final long audioTime, final String audioFilePath) {
        final Message message = new Message();
        message.setSiteUserId(currentSite.getSiteUserId());
        message.setGroupId(groupId);
        message.setChatSessionId(chatSessionId);
        message.setMsgId(MsgUtils.getCurMsgId(MsgUtils.MSG_TYPE_GROUP, currentSite));
        message.setMsgType(CoreProto.MsgType.GROUP_VOICE_VALUE);
        message.setSecret(isSecretMode);
        boolean isNet = NetUtils.getNetInfo();
        if (!isNet) {
            message.setMsgStatus(Message.STATUS_SEND_FAILED);
        } else {
            message.setMsgStatus(Message.STATUS_SENDING);
        }
        message.setMsgTime(System.currentTimeMillis());
        final AudioInfo audioInfo = new AudioInfo();
        audioInfo.setAudioFilePath(audioFilePath);
        audioInfo.setAudioTime(audioTime);
        // 为了在未上传时快速展示在界面中
        message.setMsgTime(new Date().getTime());
        message.setContent(AudioInfo.toJSON(audioInfo));

        ZalyTaskExecutor.executeUserTask(TAG, new SendAudioMessageTask(SendAudioMessageTask.INSERT_MODE, message));

        UploadFileUtils.uploadMsgFile(audioFilePath, new UploadFileUtils.UploadFileListener() {
            @Override
            public void onUploadSuccess(String fileId) {
                audioInfo.setAudioId(fileId);
                message.setContent(AudioInfo.toJSON(audioInfo));
                message.setMsgTime(System.currentTimeMillis());
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
        }, FileProto.FileType.MESSAGE_VOICE, message, currentSite);
        iView.onStartSendingMessage(message);
    }


    @Override
    public void resendAudioMessage(Message msg) {
        final Message message = msg;
        final AudioInfo audioInfo = AudioInfo.parseJSON(msg.getContent());

        String audioFilePath = audioInfo.getAudioFilePath();

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
                ZalyLogUtils.getInstance().info(TAG, " process num is " + processNum);
                sendDBContent(message, processNum);
            }

        }, FileProto.FileType.MESSAGE_VOICE, message, currentSite);
        iView.refreshMsgStatus(message);
    }

    @Override
    public void sendImgMessage(String imgPath) {
        if (StringUtils.isEmpty(imgPath)) {
            return;
        }
        File file = new File(imgPath);
        final long length = file.length();
        if (length <= 0) {
            Toaster.showInvalidate("图片异常，请稍候再试");
            return;
        }

        final ImageInfo imageInfo = new ImageInfo();
        imageInfo.setFilePath(imgPath);
        imageInfo.setFileLength(length);
        imageInfo.setStatus(ImageInfo.STATUS_UPLOADING);

        final Message message = new Message();

        message.setContent(ImageInfo.toJSON(imageInfo));
        message.setSiteUserId(currentSite.getSiteUserId());
        message.setGroupId(groupId);
        message.setMsgId(MsgUtils.getCurMsgId(MsgUtils.MSG_TYPE_GROUP, currentSite));
        message.setMsgTime(new Date().getTime());
        message.setSecret(isSecretMode);
        message.setMsgType(CoreProto.MsgType.GROUP_IMAGE_VALUE);//群图片消息
        boolean isNet = NetUtils.getNetInfo();
        if (!isNet) {
            message.setMsgStatus(Message.STATUS_SEND_FAILED);
        } else {
            message.setMsgStatus(Message.STATUS_SENDING);
        }
        message.setChatSessionId(chatSessionId);
        message.setSiteFriendId(currentSite.getSiteUserId());

        ZalyTaskExecutor.executeUserTask(TAG, new UpdateImgMsgDBTask(UpdateImgMsgDBTask.INSERT_MODE, message));

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
            public void onProcessRate(int processNum) {
                ZalyLogUtils.getInstance().info(TAG, " process num is " + processNum);
                sendDBContent(message, processNum);
            }

            @Override
            public void onUploadFail(Exception e) {
                ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            }
        }, FileProto.FileType.MESSAGE_IMAGE, message, currentSite);
        iView.onStartSendingMessage(message);
    }

    public void sendDBContent(Message message, int processNum) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ZalyDbContentHelper.IMG_PROCESS_MSG_INFO, message);
        bundle.putInt(ZalyDbContentHelper.IMG_PROCESS_NUM, processNum);
        ZalyDbContentHelper.executeAction(ZalyDbContentHelper.Action.MSG_IMG_PROCESS, bundle);
    }

    /**
     * 删除群组的某条消息
     *
     * @param msgId
     */
    @Override
    public void deleteGroupMsg(String msgId) {
        SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getSiteAddress())).deleteGroupMsgById(msgId);
    }

    @Override
    public void resendMessage(Message msg) {
        switch (msg.getMsgType()) {
            case CoreProto.MsgType.GROUP_TEXT_VALUE:
                resendTextMessage(msg);
                break;
            case CoreProto.MsgType.GROUP_IMAGE_VALUE:
                resendImgMessage(msg);
                break;
            case CoreProto.MsgType.GROUP_VOICE_VALUE:
                resendAudioMessage(msg);
                break;
        }
    }

    @Override
    public void resendImgMessage(Message msg) {
        final Message message = msg;
        final ImageInfo imageInfo = ImageInfo.parseJSON(msg.getContent());

        String imgPath = imageInfo.getFilePath();

        UploadFileUtils.uploadMsgFile(imgPath, new UploadFileUtils.UploadFileListener() {
            @Override
            public void onUploadSuccess(String fileId) {
                imageInfo.setFileId(fileId);
                message.setContent(ImageInfo.toJSON(imageInfo));
                ZalyTaskExecutor.executeUserTask(TAG, new UpdateImgMsgDBTask(UpdateImgMsgDBTask.UPDATE_MODE, message));
                sendMsgByIMManger(message);
            }

            @Override
            public void onUploadFail(Exception e) {
                ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            }

            @Override
            public void onProcessRate(int processNum) {
                ZalyLogUtils.getInstance().info(TAG, " process num is " + processNum);
                sendDBContent(message, processNum);
            }

        }, FileProto.FileType.MESSAGE_IMAGE, message, currentSite);

    }

    @Override
    public void updateMsgStatus(String msgId, int status) {
        Logger.w(TAG, Thread.currentThread().getName());
        if (iView == null || msgAdapter == null || StringUtils.isEmpty(msgId)) {
            return;
        }
        List<Message> messageList = msgAdapter.getMessages();
        for (int index = 0; index < messageList.size(); index++) {
            if (messageList.get(index).getMsgId().equals(msgId)) {
                messageList.get(index).setMsgStatus(status);
                msgAdapter.notifyItemChanged(index);
                return;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshMsgStatus(String msgId) {
        Logger.w(TAG, Thread.currentThread().getName());
        if (iView == null || msgAdapter == null || StringUtils.isEmpty(msgId)) {
            return;
        }
        List<Message> messageList = msgAdapter.getMessages();
        int index = 0;
        for (; index < messageList.size(); index++) {
            if (msgId.equals(messageList.get(index).getMsgId())) {
                msgAdapter.notifyItemChanged(index);
                return;
            }
        }
    }

    @Override
    public void onDestroy() {
        ZalyTaskExecutor.cancleAllTasksByTag(TAG);
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
        }

        @Override
        protected Message executeTask(Void... voids) throws Exception {
            Long _id = null;
            if (mode == INSERT_MODE) {
                //先入库
                SiteAddress siteAddress = new SiteAddress(currentSite.getSiteAddress());
                _id = SiteMessageDao.getInstance(siteAddress).insertGroupMsg(message);
            }
            if (mode == UPDATE_MODE) {
                _id = message.get_id();
            }

            if (_id == null) {
                throw new Exception("消息发送失败");
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
            super.onTaskError(e);
            Logger.e(e);
            Toaster.showInvalidate("发送失败");
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
            SiteAddress siteAddress = new SiteAddress(currentSite.getSiteAddress());
            if (mode == INSERT_MODE) {
                //先入库
                _id = SiteMessageDao.getInstance(siteAddress).insertGroupMsg(message);
            }
            if (mode == UPDATE_MODE) {
                _id = message.get_id();
                SiteMessageDao.getInstance(siteAddress).updateGroupMsgContent(message.get_id(), message.getContent());
                sendMsgByIMManger(message);
            }

            if (_id == null) {
                throw new Exception("消息发送失败");
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
            super.onTaskError(e);
            Logger.e(e);
            Toaster.showInvalidate("发送失败");
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
                String curSiteIdentity = currentSite.getSiteIdentity();
                IMClient.getInstance(new SiteAddress(curSiteIdentity)).sendMessage(message);
            } catch (Exception e) {
                ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            }
        }
    }

    class LoadInitMsgFromDB extends ZalyTaskExecutor.Task<Void, Void, List<Message>> {

        @Override
        protected List<Message> executeTask(Void... voids) throws Exception {
            SiteAddress address = ZalyApplication.getSiteAddressObj(currentSite.getSiteAddress());
            return SiteMessageDao.getInstance(address).queryGroupNewMsg(-1, groupId, GROUP_MSG_PAGE_SIZE);
        }

        @Override
        protected void onTaskSuccess(List<Message> messages) {
            super.onTaskSuccess(messages);
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
        }
    }

    class LoadNewMsgFromDB extends ZalyTaskExecutor.Task<Void, Void, List<Message>> {

        private long _id;

        public LoadNewMsgFromDB(long _id) {
            this._id = _id;
        }

        @Override
        protected List<Message> executeTask(Void... voids) throws Exception {
            return SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getSiteAddress())).queryGroupNewMsg(_id, groupId, -1);
        }

        @Override
        protected void onTaskSuccess(List<Message> messages) {
            super.onTaskSuccess(messages);
            if (msgAdapter != null) {
                msgAdapter.addNewMsgItems(messages);
                iView.onReceiveNewMessage();
            }
            syncMessageStatus(messages);
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            Logger.e(e);
        }
    }

    @Override
    public void syncMessageStatus(List<Message> messages) {
        try {
            List<String> msgIds = new ArrayList<>();
            for (int i = 0; i < messages.size(); i++) {
                Message msg = messages.get(i);

                /////自己发送的
                boolean isOutTime = (System.currentTimeMillis() - msg.getSendMsgTime()) > Message.SYNC_MSG_STATUS_EXPIRE_TIME;
                if (msg.getMsgType() == 1 && msg.getSiteUserId().equals(currentSite.getSiteUserId()) && (!isOutTime)) {
                    msgIds.add(i, msg.getMsgId());
                }
            }
            if (msgIds.size() > 0) {
                String curSiteIdentity = currentSite.getSiteIdentity();
                IMClient.getInstance(new SiteAddress(curSiteIdentity)).syncMessageStatus(msgIds, CoreProto.MsgType.GROUP_TEXT_VALUE);
                return;
            }
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }
    }

    class LoadHistoryMsgFromDB extends ZalyTaskExecutor.Task<Void, Void, List<Message>> {

        private long _id;

        public LoadHistoryMsgFromDB(long _id) {
            this._id = _id;
        }

        @Override
        protected List<Message> executeTask(Void... voids) throws Exception {

            //////得到群组历史消息记录
            return SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getSiteAddress())).queryGroupHistoryMsg(_id, groupId, GROUP_MSG_PAGE_SIZE);
        }

        @Override
        protected void onTaskSuccess(List<Message> messages) {
            super.onTaskSuccess(messages);
            if (msgAdapter != null) {
                msgAdapter.addHistoryItems(messages);
            }
            syncMessageStatus(messages);
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
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
            SiteAddress siteAddress = new SiteAddress(currentSite.getSiteAddress());

            if (mode == INSERT_MODE) {
                long _id = SiteMessageDao.getInstance(siteAddress).insertGroupMsg(message);
                message.set_id(_id);
            }
            if (mode == UPDATE_MODE) {
                ////TODO DBChange 写入群消息
                SiteMessageDao.getInstance(siteAddress).updateGroupMsgContent(message.get_id(), message.getContent());
            }
            return message;
        }

        @Override
        protected void onTaskSuccess(Message message) {
            super.onTaskSuccess(message);
            if (mode == INSERT_MODE) iView.onSendMessageFinish(message);
        }
    }

    public void setGroupProfile(GroupProto.SimpleGroupProfile groupProfile) {
        this.groupProfile = groupProfile;
        if (groupProfile != null) {
            ZalyTaskExecutor.executeUserTask(TAG, new AddNewSessionTask());
        }
    }

    /**
     * 数据库中新增会话，由于数据库约束为
     * UNIQUE(opposite_id) ON CONFLICT IGNORE
     * 所以无需考虑多次插入的问题
     */
    class AddNewSessionTask extends ZalyTaskExecutor.Task<Void, Void, Boolean> {

        @Override
        protected Boolean executeTask(Void... voids) throws Exception {
            ChatSession chatSession = new ChatSession();
            chatSession.setChatSessionId(groupId);
            chatSession.setLastMsgId("-1");
            chatSession.setDesc("暂无消息");
            chatSession.setStatus(ChatSession.STATUS_NEW_SESSION);
            chatSession.setType(ChatSession.TYPE_GROUP_SESSION);
            chatSession.setLatestTime(new Date().getTime());
            if (groupProfile != null) {
                chatSession.setIcon("");
                String userName = groupProfile.getGroupName();
                if (StringUtils.isEmpty(userName)) {
                    userName = groupProfile.getGroupName();
                }
                chatSession.setTitle(userName);
            }
            ////会话写入session
            return SitePresenter.getInstance().insertChatSession(currentSite.getSiteAddress(), chatSession);
        }

        @Override
        protected void onTaskError(Exception e) {
            Logger.e(e);
            Logger.w(TAG, "insert new session error");
        }
    }

    public boolean isSecretMode() {
        return isSecretMode;
    }

    public String getGroupId() {
        return groupId;
    }

    @Override
    public GroupProto.SimpleGroupProfile getGroupProfile() {
        return groupProfile;
    }
}
