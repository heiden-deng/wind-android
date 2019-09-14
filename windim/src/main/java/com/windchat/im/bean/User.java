package com.windchat.im.bean;


/**
 * Created by anguoyue on 02/03/2018.
 */

public class User {

    public static final long RELATION_IS_FRIEND = 1;
    public static final long RELATION_IS_NOT_FRIEND = 0;

    public static final int DEL_USER_FAILED = 3;

    private String globalUserId;        //全局用户ID
    private String siteUserId;          //站点用户ID
    private String siteUserName;        //站点用户昵称
    private String siteUserIcon;        //站点用户头像
    private String userIdPrik;          //用户身份私钥
    private String userIdPuk;           //用户身份公钥
    private String deviceIdPrik;        //设备私钥
    private String deviceIdPubk;        //设备公钥
    private String identityName;        //身份名称
    private String identitySource;      //身份来源
    private String platformSessionId;
    private Long userIdNum; //身份表的主键id

    private Long relation;

    private Boolean isMute = false;

    public Boolean getIsMute() {
        return isMute;
    }

    public void setIsMute(Boolean isMute) {
        this.isMute = isMute;
    }

    public void setRelation(long relation) {
        this.relation = relation;
    }

    public long getRelation() {
        return relation;
    }

    public String getGlobalUserId() {
        return globalUserId;
    }

    public void setGlobalUserId(String globalUserId) {
        this.globalUserId = globalUserId;
    }

    public Long getUserIdNum() {
        return userIdNum;
    }

    public void setUserIdNum(Long userIdNum) {
        this.userIdNum = userIdNum;
    }

    public String getSiteUserId() {
        return siteUserId;
    }

    public void setSiteUserId(String siteUserId) {
        this.siteUserId = siteUserId;
    }

    public String getSiteUserName() {
        return siteUserName;
    }

    public void setSiteUserName(String siteUserName) {
        this.siteUserName = siteUserName;
    }

    public String getSiteUserIcon() {
        return siteUserIcon;
    }

    public void setSiteUserIcon(String siteUserIcon) {
        this.siteUserIcon = siteUserIcon;
    }

    public String getIdentitySource() {
        return identitySource;
    }

    public void setIdentitySource(String identitySource) {
        this.identitySource = identitySource;
    }

    public String getPlatformSessionId() {
        return platformSessionId;
    }

    public void setPlatformSessionId(String platformSessionId) {
        this.platformSessionId = platformSessionId;
    }

    public String getUserIdPrik() {
        return userIdPrik;
    }

    public void setUserIdPrik(String userIdPrik) {
        this.userIdPrik = userIdPrik;
    }

    public String getUserIdPuk() {
        return userIdPuk;
    }

    public void setUserIdPuk(String userIdPuk) {
        this.userIdPuk = userIdPuk;
    }

    public String getDeviceIdPrik() {
        return deviceIdPrik;
    }

    public void setDeviceIdPrik(String deviceIdPrik) {
        this.deviceIdPrik = deviceIdPrik;
    }

    public String getIdentityName() {
        return identityName;
    }

    public void setIdentityName(String identityName) {
        this.identityName = identityName;
    }

    public String getDeviceIdPubk() {
        return deviceIdPubk;
    }

    public void setDeviceIdPubk(String deviceIdPubk) {
        this.deviceIdPubk = deviceIdPubk;
    }

    @Override
    public String toString() {
        return "";
    }
}

