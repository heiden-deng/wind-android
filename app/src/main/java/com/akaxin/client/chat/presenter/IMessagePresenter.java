package com.akaxin.client.chat.presenter;

import com.akaxin.client.bean.Site;
import com.akaxin.proto.core.DeviceProto;
import com.akaxin.proto.core.UserProto;
import com.akaxin.client.bean.Message;
import com.akaxin.client.chat.view.IMessageView;

import java.util.List;

/**
 * Created by yichao on 2017/10/20.
 */

public interface IMessagePresenter {

    /**
     * 绑定view
     *
     * @param messageView
     */
    void bindView(IMessageView messageView);

    /**
     * 初始加载数据
     */
    void loadInitData(String friendSiteUserId, Site site);

    /**
     * 加载最新消息
     */
    void loadNewMsgFromDB();

    /**
     * 加载历史消息
     */
    void loadMoreMsg();

    /**
     * 加载历史消息，指定条数
     *
     * @param msgNum
     */
    void loadMoreMsg(int msgNum);

    /**
     * 开关绝密模式
     *
     * @param open
     */
    void switchSecretMode(boolean open);

    /**
     * 根据目前的是否开启绝密模式的状态, 发送文字消息.
     *
     * @param content 来自输入框的字符串内容.
     */
    void sendTextMessage(String content);

    /**
     * 发送语音消息
     *
     * @param audioTime
     * @param audioFilePath
     */
    void sendAudioMessage(long audioTime, String audioFilePath);

    /**
     * 发送图片
     *
     * @param imgPath
     */
    void sendImgMessage(String imgPath);

    /**
     * 更新指定消息的发送状态
     *
     * @param msgId
     * @param status
     */
    void updateMsgStatus(String msgId, int status);

    void refreshMsgStatus(String msgId);

    String getFriendDeviceName();

    /**
     * 设置加密信息的对方设备公钥
     *
     * @param deviceInfo
     */
    void setFriendDevPubKey64Str(DeviceProto.SimpleDeviceProfile deviceInfo);

    void getSecretDeviceInfo(String deviceId);

    /**
     * 当前是否为绝密模式
     *
     * @return
     */
    boolean isSecretMode();

    void setFriendSimpleProfile(UserProto.SimpleUserProfile simpleProfile);

    void onDestroy();

    /**
     * 清空当前会话未读消息
     */
    void cleanUnreadNum();

    void resendTextMessage(Message msg);

    void resendImgMessage(Message msg);

    void resendAudioMessage(Message msg);

    void deleteU2Msg(String msgId);

    void resendMessage(Message msg);

    void deleteU2MsgByChatSessionId(String chatSessionId);

    void syncMessageStatus(List<Message> messages);

    void LoadIsOpenTsFromChatSession();

    void loadNewMsgFromRecevieDB();

}
