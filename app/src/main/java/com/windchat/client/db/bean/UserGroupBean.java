package com.windchat.client.db.bean;

import com.windchat.client.util.GsonUtils;
import com.windchat.client.util.data.StringUtils;

/**
 * Created by anguoyue on 21/03/2018.
 */

public class UserGroupBean {
    private String siteGroupId;
    private String groupName = "";
    private String groupImage = "";
    private String groupOwnerId;
    private String groupOwnerName = "";
    private String groupOwnerIcon = "";
    private boolean asGroupMember;//是否为群成员
    private boolean closeInviteGroupChat;//关闭邀请群成员功能
    private boolean mute;
    private long latestTime;
    private int countMember ;

    public String getGroupId() {
        return siteGroupId;
    }

    public void setGroupId(String siteGroupId) {
        this.siteGroupId = siteGroupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        if (StringUtils.isEmpty(groupName)) {
            return;
        }
        this.groupName = groupName;
    }


    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        if (StringUtils.isEmpty(groupImage)) {
            return;
        }
        this.groupImage = groupImage;
    }

    public String getGroupOwnerId() {
        return groupOwnerId;
    }

    public void setGroupOwnerId(String groupOwnerId) {
        this.groupOwnerId = groupOwnerId;
    }

    public String getGroupOwnerName() {
        return groupOwnerName;
    }

    public void setGroupOwnerName(String groupOwnerName) {
        if (StringUtils.isEmpty(groupOwnerName)) {
            return;
        }
        this.groupOwnerName = groupOwnerName;
    }

    public String getGroupOwnerIcon() {
        return groupOwnerIcon;
    }

    public void setGroupOwnerIcon(String groupOwnerIcon) {
        if (StringUtils.isEmpty(groupOwnerIcon)) {
            return;
        }
        this.groupOwnerIcon = groupOwnerIcon;
    }

    public boolean isAsGroupMember() {
        return asGroupMember;
    }

    public void setAsGroupMember(boolean asGroupMember) {
        this.asGroupMember = asGroupMember;
    }

    public boolean isCloseInviteGroupChat() {
        return closeInviteGroupChat;
    }

    public void setCloseInviteGroupChat(boolean closeInviteGroupChat) {
        this.closeInviteGroupChat = closeInviteGroupChat;
    }

    public void setGroupCountMember(int countMember) {
        this.countMember = countMember;
    }

    public int getGroupCountMember() {
        return this.countMember;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public long getLatestTime() {
        return latestTime;
    }

    public void setLatestTime(long latestTime) {
        this.latestTime = latestTime;
    }

    public String toString() {
        return GsonUtils.toJson(this);
    }
}
