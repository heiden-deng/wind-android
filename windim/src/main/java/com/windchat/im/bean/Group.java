package com.windchat.im.bean;

/**
 * Created by zhangjun on 2018/3/8.
 */

public class Group {

    private String siteGroupId;
    private String siteGroupName;
    private String siteGroupIcon = "";
    private String groupOwnerId = "";
    private String groupOwnerName = "";
    private String groupOwnerIcon = "";

    public static Boolean isGroupMember = true;

    public String getSiteGroupId(){
        return siteGroupId;
    }
    public void setSiteGroupId(String siteGroupId){
        this.siteGroupId = siteGroupId;
    }

    public String getSiteGroupName(){
        return siteGroupName;
    }

    public void setSiteGroupName(String siteGroupName){
        this.siteGroupName = siteGroupName;
    }

    public String getSiteGroupIcon(){
        return siteGroupIcon;
    }

    public void setSiteGroupIcon(String siteGroupIcon){
        this.siteGroupIcon = siteGroupIcon;
    }

    public String getGroupOwnerId(){
        return groupOwnerId;
    }

    public void setGroupOwnerId(String groupOwnerId){
        this.groupOwnerId = groupOwnerId;
    }

    public String getGroupOwnerName(){
        return groupOwnerName;
    }

    public void setGroupOwnerName(String groupOwnerName){
        this.groupOwnerName = groupOwnerName;
    }

    public String getGroupOwnerIcon(){
        return groupOwnerIcon;
    }

    public void setGroupOwnerIcon(String groupOwnerIcon){
        this.groupOwnerIcon = groupOwnerIcon;
    }
}
