package com.akaxin.client.chat.view;

import android.content.Context;
import android.content.Intent;

import com.akaxin.proto.core.PluginProto;
import com.akaxin.client.bean.Message;
import com.akaxin.client.chat.MessageAdapter;

import java.util.List;

/**
 * Created by yichao on 2017/10/20.
 */

public interface IGroupMsgView extends IView {

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
     * 收到新消息并展示后调用.
     */
    void onReceiveNewMessage();

    /**
     * set adapter
     *
     * @param msgRvAdapter
     */
    void setMsgRvAdapter(MessageAdapter msgRvAdapter);

    /**
     * 开启绝密失败
     */
    void openSecretFail();


    Context getContext();

    void setExpandViewData(List<PluginProto.Plugin> pluginProfiles);

    void refreshMsgStatus(final  Message message);
}
