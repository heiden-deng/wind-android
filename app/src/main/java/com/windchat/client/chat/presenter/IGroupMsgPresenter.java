package com.windchat.client.chat.presenter;

import com.windchat.client.bean.Site;
import com.akaxin.proto.core.GroupProto;
import com.windchat.client.bean.Message;
import com.windchat.client.chat.view.IGroupMsgView;

import java.util.List;

/**
 * Created by yichao on 2017/10/20.
 */

public interface IGroupMsgPresenter {

    /**
     * 绑定view
     *
     * @param messageView
     */
    void bindView(IGroupMsgView messageView);

    /**
     * 初始加载数据
     */
    void loadInitData(String groupId, Site site);

    /**
     * 加载最新消息
     */
    void loadNewMsgFromDB();

    /**
     * 加载历史消息
     */
    void loadMoreHistoryMsg();

    /**
     * 开关绝密模式
     *
     * @param open
     */
    void onSecretMode(boolean open);

    /**
     * 发送语音消息
     *
     * @param audioTime
     * @param audioFilePath
     */
    void sendAudioMessage(long audioTime, String audioFilePath);


    void sendImgMessage(String path);

    /**
     * 发送消息（包括绝密）
     *
     * @param message
     */
    void sendTextMessage(Message message);

    /**
     * 更新指定消息的发送状态
     *
     * @param msgId
     * @param status
     */
    void updateMsgStatus(String msgId, int status);

    void refreshMsgStatus(String msgId);

    /**
     * 当前是否为绝密模式
     *
     * @return
     */
    boolean isSecretMode();

    String getGroupId();

    GroupProto.SimpleGroupProfile getGroupProfile();

    void setGroupProfile(GroupProto.SimpleGroupProfile groupProfile);

    void onDestroy();
    /**
     * 清空当前会话未读消息
     */
    void cleanUnreadNum();

    void resendTextMessage(Message msg);

    void resendImgMessage(Message msg);

    void resendAudioMessage(Message msg);

    void deleteGroupMsg(String msgId);

    void resendMessage(Message msg);

    void syncMessageStatus(List<Message> messages);

    void loadNewMsgFromRecevieDB();
}
