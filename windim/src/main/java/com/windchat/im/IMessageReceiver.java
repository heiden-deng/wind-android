package com.windchat.im;

import com.windchat.im.bean.Message;
import com.windchat.im.socket.SiteAddress;
import com.windchat.im.socket.TransportPackage;
import com.windchat.proto.client.ImStcNoticeProto;

import java.util.List;


public interface IMessageReceiver {

    /**
     * msg_status: 1 发送成功
     * msg_status: 0 默认状态
     * msg_status: -1 用户非好友关系，二人消息发送失败
     * msg_status: -2 用户非群成员，群消息发送失败
     *
     * @param siteAddress
     * @param msgId
     * @param msgTime
     * @param messageStatus
     * @throws Exception
     */
    void handleMessageStatus(SiteAddress siteAddress, String msgId, long msgTime, int messageStatus) throws Exception;

    void handleNoticeMessage(SiteAddress siteAddress, ImStcNoticeProto.ImStcNoticeRequest request) throws Exception;

    void handleU2Message(SiteAddress siteAddress, List<Message> u2Messages) throws Exception;

    void handleGroupMessage(SiteAddress siteAddress, List<Message> groupMessages) throws Exception;

    void handleException(Throwable t);
}
