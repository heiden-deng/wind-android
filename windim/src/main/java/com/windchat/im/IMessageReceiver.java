package com.windchat.im;

import com.windchat.im.message.Message;
import com.windchat.im.bean.Site;
import com.windchat.im.message.Notification;
import com.windchat.proto.client.ImStcNoticeProto;

import java.util.List;


public interface IMessageReceiver {

    /**
     * msg_status: 1 发送成功
     * msg_status: 0 默认状态
     * msg_status: -1 用户非好友关系，二人消息发送失败
     * msg_status: -2 用户非群成员，群消息发送失败
     *
     * @param site
     * @param msgId
     * @param msgTime
     * @param msgStatus
     * @throws Exception
     */
    void handleMessageStatus(Site site, String msgId, long msgTime, MsgStatus msgStatus) throws Exception;

    void handleNoticeMessage(Site site, ImStcNoticeProto.ImStcNoticeRequest request) throws Exception;

    void handleNotification(Site site, List<Notification> notifications) throws Exception;

    void handleU2Message(Site site, List<? extends Message> u2Messages) throws Exception;

    void handleGroupMessage(Site site, List<? extends Message> groupMessages) throws Exception;

    void handleException(Site site, Throwable t);
}
