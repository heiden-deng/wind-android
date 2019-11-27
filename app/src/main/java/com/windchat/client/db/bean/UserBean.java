package com.windchat.client.db.bean;

import com.windchat.client.util.GsonUtils;
import com.windchat.client.util.data.StringUtils;

/**
 * Created by anguoyue on 21/03/2018.
 */

public class UserBean {

    protected String siteUserId;
    protected String userImage;
    protected String userName;
    protected String userIdPubk;
    protected String siteLoginId;
    protected String siteNickName;


    public String getSiteUserId() {
        return siteUserId == null ? "" : siteUserId;
    }

    public String getUserImage() {
        return userImage == null ? "" : userImage;
    }

    public String getUserName() {
        return userName == null ? "" : userName;
    }

    public String getUserIdPubk() {
        return userIdPubk == null ? "" : userIdPubk;
    }

    public String getSiteLoginId() {
        return siteLoginId == null ? "" : siteLoginId;
    }

    public String getSiteNickName() {
        return siteNickName == null ? "" : siteNickName;
    }

    public void setSiteUserId(String siteUserId) {
        this.siteUserId = siteUserId;
    }

    public void setUserImage(String userImage) {
        if (StringUtils.isEmpty(userImage)) {
            return;
        }
        this.userImage = userImage;
    }

    public void setSiteNickName(String siteNickName) {
        this.siteNickName = siteNickName;
    }


    public void setSiteLoginId(String siteLoginId) {
        this.siteLoginId = siteLoginId;
    }


    public void setUserName(String userName) {
        if (StringUtils.isEmpty(userName)) {
            return;
        }
        this.userName = userName;
    }


    public void setUserIdPubk(String userIdPubk) {
        this.userIdPubk = userIdPubk;
    }

    public String toString() {
        return GsonUtils.toJson(this);
    }
}
