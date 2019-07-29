package com.akaxin.client.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.akaxin.client.socket.SiteAddress;
import com.akaxin.client.util.GsonUtils;
import com.akaxin.client.util.SiteUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;

/**
 * Created by yichao on 2017/10/10.
 * <p>
 * 站点
 */

public class Site implements Parcelable {

    public static final int STATUS_SITE_ONLINE = 1;
    public static final int STATUS_SITE_KEEPING = 2;

    public static final int AUTO_DISCONNECT_STATUS = 0;////自动断开连接
    public static final int MANUAL_CONTROL_DISCONNECT_STATUS = 1;////手动断开连接

//    public static final String SITE_VERSION = "0.2.2";

    private String siteHost;
    private String sitePort;
    private String siteName;
    private String siteIcon;
    private String globalUserId;
    private String siteUserId;
    private String siteUserName;
    private String siteUserImage;
    private String siteSessionId;
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

    public SiteAddress toSiteAddress() {
        return new SiteAddress(this);
    }

    protected Site(Parcel in) {
        siteHost = in.readString();
        sitePort = in.readString();
        siteName = in.readString();
        siteIcon = in.readString();
        siteUserId = in.readString();
        siteUserName = in.readString();
        siteUserImage = in.readString();
        siteSessionId = in.readString();
        siteStatus = in.readInt();
        lastLoginTime = in.readLong();
        unreadNum = in.readLong();
        realNameConfig = in.readInt();
        connStatus = in.readInt();
        codeConfig = in.readInt();
        mute = in.readByte() != 0;
        siteLoginId = in.readString();
        siteVersion = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(siteHost);
        dest.writeString(sitePort);
        dest.writeString(siteName);
        dest.writeString(siteIcon);
        dest.writeString(siteUserId);
        dest.writeString(siteUserName);
        dest.writeString(siteUserImage);
        dest.writeString(siteSessionId);
        dest.writeInt(siteStatus);
        dest.writeLong(lastLoginTime);
        dest.writeLong(unreadNum);
        dest.writeInt(realNameConfig);
        dest.writeInt(connStatus);
        dest.writeInt(codeConfig);
        dest.writeByte((byte) (mute ? 1 : 0));
        dest.writeString(siteLoginId);
        dest.writeString(siteVersion);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Site> CREATOR = new Creator<Site>() {
        @Override
        public Site createFromParcel(Parcel in) {
            return new Site(in);
        }

        @Override
        public Site[] newArray(int size) {
            return new Site[size];
        }
    };

    public String getPlatformUserId() {
        return this.platformUserId;
    }

    public void setPlatformUserId(String id) {
        this.platformUserId = id;
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

    public String getSitePort() {
        return sitePort;
    }

    public void setSitePort(String sitePort) {
        this.sitePort = sitePort;
    }

    public String getSiteName() {
        if (StringUtils.isEmpty(siteName)) {
            return siteHost == null ? "" : siteHost;
        }
        return siteName;
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
            ZalyLogUtils.getInstance().exceptionError(e);
            return "";
        }
    }

    public String getSiteAddress() {
        return siteHost + ":" + sitePort;
    }

    public String getSiteDisplayAddress() {
        if (String.valueOf(SiteUtils.DEFAULT_PORT).equals(sitePort))
            return siteHost;
        else return getSiteAddress();
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

    @Override
    public String toString() {
        return GsonUtils.toJson(this);
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

    public int getProtocolVersion() {
        try {
            String siteVersionArr[] = StringUtils.str2Arr(siteVersion, "\\.");
            return Integer.valueOf(siteVersionArr[2]);
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
            return 0;
        }
    }

}
