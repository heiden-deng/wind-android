package com.windchat.im.socket;


import com.windchat.im.bean.Site;

import org.apache.commons.lang3.StringUtils;

public class SiteAddress {
    public static String TAG = SiteAddress.class.getSimpleName();

    private String host;
    private int port;
    private ConnectionConfig config;

    public SiteAddress(ConnectionConfig config) {
        this.host = config.getHost();
        this.port = config.getPort();

        this.config = config;
    }

    // 兼容旧代码
    public SiteAddress(String address) {
        String[] ret = address.split("[:_]");
        int retLength = ret.length;
        if (retLength > 0) {
            this.host = StringUtils.join(ret, ".", "", 0, retLength - 1);
            this.port = Integer.valueOf(ret[retLength - 1]);
        } else {
            this.host = "127.0.0.1 这段代码还没有写完呢";
            this.port = 0;
        }
    }

    public SiteAddress(Site beanSite) {
        this.config = ConnectionConfig.getConnectionCfg(beanSite);
        this.host = this.config.getHost();
        this.port = this.config.getPort();
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getFullUrl() {
        return this.host + ":" + this.port;
    }

    public String toOldSiteIdentity() {
        return this.getHost().replace('.', '_') + "_" + this.getPort();
    }


    @Override
    public String toString() {
        return this.getFullUrl();
    }

//    public com.akaxin.client.bean.SiteAddress toOldSiteAddress() {
//        com.akaxin.client.bean.SiteAddress a = new com.akaxin.client.bean.SiteAddress();
//        a.setSiteHost(this.host);
//        a.setSitePort(this.port);
//        return a;
//    }

    // 兼容老代码
    public ConnectionConfig toConnectionConfig() {

        if (null != this.config) {
            return this.config;
        } else {
            ConnectionConfig tmpConfig = new ConnectionConfig();
            tmpConfig.setHost(this.host);
            tmpConfig.setPort(this.port);
            return tmpConfig;
        }
    }
}