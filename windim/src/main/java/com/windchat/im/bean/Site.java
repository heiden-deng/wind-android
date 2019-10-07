package com.windchat.im.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.windchat.im.socket.SiteAddress;
import com.windchat.logger.WindLogger;


/**
 * Created by yichao on 2017/10/10.
 * <p>
 * 站点
 */

public class Site {

    protected String siteHost;
    protected int sitePort;

    protected String siteUserId;
    protected String siteSessionId;

    private String siteName;
    private String siteIcon;
    private String globalUserId;
    private String siteUserName;
    private String siteUserImage;
    private String siteVersion;
    private int siteStatus;
    private long lastLoginTime;
    private boolean mute;
    private int connStatus;
    private int realNameConfig;
    private int codeConfig;
    private String siteLoginId;

    /**
     * 仅在平台请求中使用
     */
    private String platformUserId;
    private long unreadNum;

    public Site() {
    }

    public SiteAddress getSiteAddress() {
        return new SiteAddress(this);
    }

    public String getHostAndPort() {
        return this.siteHost + ":" + this.sitePort;
    }

    public Site(String host, int port) {
        this.siteHost = host;
        this.sitePort = port;
    }

    public int getConnStatus() {
        return connStatus;
    }

    public void setConnStatus(int connStatus) {
        this.connStatus = connStatus;
    }

    public String getSiteHost() {
        return siteHost;
    }

    public void setSiteHost(String siteHost) {
        this.siteHost = siteHost;
    }

    public int getSitePort() {
        return sitePort;
    }

    public void setSitePort(int sitePort) {
        this.sitePort = sitePort;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteIcon() {
        return siteIcon == null ? "" : siteIcon;
    }

    public void setSiteIcon(String siteIcon) {
        this.siteIcon = siteIcon;
    }

    public String getSiteUserId() {
        return siteUserId == null ? "" : siteUserId;
    }

    public void setSiteUserId(String siteUserId) {
        this.siteUserId = siteUserId;
    }

    public String getSiteUserName() {
        return siteUserName == null ? "" : siteUserName;
    }

    public void setSiteUserName(String siteUserName) {
        this.siteUserName = siteUserName;
    }

    public String getSiteUserImage() {
        return siteUserImage == null ? "" : siteUserImage;
    }

    public void setSiteUserImage(String siteUserImage) {
        this.siteUserImage = siteUserImage;
    }

    public String getSiteSessionId() {
        return siteSessionId == null ? "" : siteSessionId;
    }

    public void setSiteSessionId(String siteSessionId) {
        this.siteSessionId = siteSessionId;
    }

    public String getSiteLoginId() {
        return siteLoginId == null ? "" : siteLoginId;
    }

    public void setSiteLoginId(String siteLoginId) {
        this.siteLoginId = siteLoginId;
    }

    public int getSiteStatus() {
        return siteStatus;
    }

    public void setSiteStatus(int siteStatus) {
        this.siteStatus = siteStatus;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getSiteIdentity() {
        try {
            return siteHost.replace('.', '_') + "_" + sitePort;
        } catch (Exception e) {
            WindLogger.getInstance().error("site", e, "");
            return "";
        }
    }

    public long getUnreadNum() {
        return unreadNum;
    }

    public void setUnreadNum(long unreadNum) {
        this.unreadNum = unreadNum;
    }

    public String getGlobalUserId() {
        return globalUserId;
    }

    public void setGlobalUserId(String globalUserId) {
        this.globalUserId = globalUserId;
    }

    public String getSiteVersion() {
        return siteVersion;
    }

    public void setSiteVersion(String siteVersion) {
        this.siteVersion = siteVersion;
    }

    public boolean isMute() {
        return this.mute;
    }

    public void setMute(boolean isMute) {
        this.mute = isMute;
    }

    public void setRealNameConfig(int realNameConfig) {
        this.realNameConfig = realNameConfig;
    }

    public int getRealNameConfig() {
        return this.realNameConfig;
    }


    public void setCodeConfig(int codeConfigValue) {
        this.codeConfig = codeConfigValue;
    }

    public int getCodeConfig() {
        return this.codeConfig;
    }

    @Override
    public String toString() {
        return this.siteHost + ":" + this.sitePort;
    }
}
