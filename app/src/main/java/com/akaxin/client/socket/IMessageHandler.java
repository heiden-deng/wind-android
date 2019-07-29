package com.akaxin.client.socket;

import com.akaxin.client.socket.TransportPackage;


/**
 * 消息匹配器
 */
public interface IMessageHandler {
    boolean matchReceive(TransportPackage packet) throws Exception;
}
