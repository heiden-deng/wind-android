package com.windchat.client.bean;

/**
 * Created by Mr.kk on 2018/6/28.
 * This Project was client-android
 */

public class PersonalItem {
    private String itemDesc;
    private boolean showTip;
    private int itemType;

    public static final int ITEM_SHOW_IMAGE_LARGE = 0;
    public static final int ITEM_SET_SITE_ACCOUNT = 1;
    public static final int ITEM_CHANGE_USER_HEAD = 2;
    public static final int ITEM_CHANGE_USER_NICK_NAME = 3;

    public PersonalItem(String itemDesc, boolean showTip, int itemType) {
        this.itemDesc = itemDesc;
        this.showTip = showTip;
        this.itemType = itemType;
    }

    public String getItemDesc() {
        return itemDesc == null ? "" : itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    public boolean isShowTip() {
        return showTip;
    }

    public void setShowTip(boolean showTip) {
        this.showTip = showTip;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }
}
