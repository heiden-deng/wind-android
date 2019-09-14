package com.windchat.im.socket;

/**
 * 消息匹配器
 */
public interface IMessageHandler {
    boolean matchReceive(TransportPackage packet) throws Exception;
}
