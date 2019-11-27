package com.windchat.client.db.bean;

import com.windchat.client.util.GsonUtils;

/**
 * Created by anguoyue on 21/03/2018.
 */

public class UserFriendBean extends UserBean {

    private boolean mute;
    private int relation;

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public int getRelation() {
        return relation;
    }

    public void setRelation(int relation) {
        this.relation = relation;
    }


    public String toString() {
        return GsonUtils.toJson(this);
    }
}
