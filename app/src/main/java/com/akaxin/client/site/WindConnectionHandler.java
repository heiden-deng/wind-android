package com.akaxin.client.site;


import com.windchat.im.IConnectionHandler;
import com.windchat.im.IMessageReceiver;
import com.windchat.im.socket.TransportPackage;

/**
 * Created by anguoyue on 2019/10/7.
 */

public class WindConnectionHandler implements IConnectionHandler {

    @Override
    public void onConnectionDisconnected(Exception e) {
        e.printStackTrace();
    }

}
