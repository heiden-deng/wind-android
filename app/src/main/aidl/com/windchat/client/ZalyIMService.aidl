package com.windchat.client;

import com.windchat.client.bean.Session;
import com.windchat.client.bean.Message;
import com.windchat.client.bean.Site;

interface ZalyIMService {

    void initSiteConnection();

    boolean addSiteConnection(in Site site);

    boolean isConnected(String siteIdentity);

    int getConnectionStatus(String siteIdentity);

    void retryConnect(String siteIdentity);

    void disConnection(String siteIdentity);

    void sendMessage(String siteIdentity, in Message message);

    void syncMessage(String siteIdentity);

    void checkIMConnection(String siteIdentity);

    void buildPlatConnection(in Site site);

    void buildSiteConnection(in Site site);

    void forceRetryConnect(String siteIndentity);

    void syncMessageStatus(String siteIdentity, in List<String> msgIds, in int msgType);

    void removeSiteConnection(in Site site);
}
