package com.windchat.im.socket;

import com.akaxin.client.bean.Site;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 连接配置
 */
public class ConnectionConfig implements Cloneable {
    private static final String TAG = "ConnectionConfig";

    private String host;
    private int port;
    private int connStatus;
    private int connectionType;
    private String sessionId;
    private String siteUserId;

    private String platformUserId;

    public String getPlatformUserId() {
        return this.platformUserId;
    }

    public void setPlatformUserId(String id) {
        this.platformUserId = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(int connectionType) {
        this.connectionType = connectionType;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSiteUserId() {
        return siteUserId;
    }

    public void setSiteUserId(String siteUserId) {
        this.siteUserId = siteUserId;
    }

    public void setSiteConnStatus(int connStatus) {
        this.connStatus = connStatus;
    }

    public int getSiteConnStatus() {
        return this.connStatus;
    }

    /**
     * 建造者
     */
    public static class ConnCfgBuilder {

        private String host;
        private int port;
        private int connStatus;
        private String sessionId;
        private String siteUserId;


        public ConnCfgBuilder setHost(String host) {
            this.host = host;
            return this;
        }

        public ConnCfgBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        public ConnCfgBuilder setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public ConnCfgBuilder setSiteUserId(String siteUserId) {
            this.siteUserId = siteUserId;
            return this;
        }

        public ConnCfgBuilder setSiteConnStatus(int connStatus) {
            this.connStatus = connStatus;
            return this;
        }

        public static ConnCfgBuilder newBuilder() {
            return new ConnCfgBuilder();
        }

        public ConnectionConfig build() {
            ConnectionConfig connectionConfig = new ConnectionConfig();
            connectionConfig.setHost(host);
            connectionConfig.setPort(port);
            connectionConfig.setSessionId(sessionId);
            connectionConfig.setSiteUserId(siteUserId);
            connectionConfig.setSiteConnStatus(connStatus);
            return connectionConfig;
        }
    }

    /**
     * 获取连接配置
     *
     * @param site
     * @return
     */

    public static ConnectionConfig getConnectionCfg(Site site) {

        return ConnCfgBuilder.newBuilder()
                .setHost(site.getSiteHost())
                .setSessionId(site.getSiteSessionId())
                .setPort(isNumeric(site.getSitePort()) ? Integer.parseInt(site.getSitePort()) : 2021)
                .setSiteUserId(site.getSiteUserId())
                .setSiteConnStatus(site.getConnStatus())
                .build();
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
}
