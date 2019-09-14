package com.windchat.im.bean;


public class SiteTask {

    private int _id;
    private String siteIdentity;
    private String taskIdentity;
    private int taskType;
    private String taskContent;
    private String taskTime;

    public String getSiteIdentity() {
        return siteIdentity;
    }

    public void setSiteIdentity(String siteIdentity) {
        this.siteIdentity = siteIdentity;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getTaskIdentity() {
        return taskIdentity;
    }

    public void setTaskIdentity(String taskIdentity) {
        this.taskIdentity = taskIdentity;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public String getTaskContent() {
        return taskContent;
    }

    public void setTaskContent(String taskContent) {
        this.taskContent = taskContent;
    }

    public String getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(String taskTime) {
        this.taskTime = taskTime;
    }

    @Override
    public String toString() {
        return "";
    }
}
