package com.windchat.client.chat.view;

import android.content.Context;

import com.windchat.client.bean.Message;
import com.windchat.client.chat.MessageAdapter;
import com.akaxin.proto.core.PluginProto;

import java.util.List;

/**
 * 这一接口用于 U2MessageActivity 的 View 层调用.
 */

public interface IMessageView extends IView {

    /**
     * 图片异常时调用.
     */
    void onSentImageInvalidate();

    /**
     * 开始发送消息时调用. 直接把开始要发送的消息展示在界面上, 不需等待.
     */
    void onStartSendingMessage(Message message);

    /**
     * 发送消息错误时调用.
     */
    void onSendingMessageError(Message message);

    /**
     * 发送消息完成
     * 已入本地数据库，并发送IM，但是无法确定是否到达服务器
     *
     * @param message
     */
    void onSendMessageFinish(Message message);

    /**
     * set adapter
     *
     * @param msgRvAdapter
     */
    void setMsgRvAdapter(MessageAdapter msgRvAdapter);

    /**
     * 收到新消息并展示后调用.
     */
    void onNewMessagesReceived();

    /**
     * 开启绝密失败
     */
    void onTopSecretOff();

    void onTopSecretOn(String deviceName);

    void scrollUplMsgList();

    /**
     * 显示该条加密消息的指定加密设备信息
     *
     */
    void showSecretDeviceInfoDialog(String deviceName);

    Context getContext();

    void setExpandViewData(List<PluginProto.Plugin> pluginProfiles);

    void refreshMsgStatus(final  Message message);

}
