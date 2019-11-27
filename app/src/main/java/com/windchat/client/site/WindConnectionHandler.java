package com.windchat.client.site;


import com.windchat.im.IConnectionHandler;

/**
 * Created by anguoyue on 2019/10/7.
 */

public class WindConnectionHandler implements IConnectionHandler {

    @Override
    public void onConnectionDisconnected(Exception e) {
        e.printStackTrace();
    }

}
