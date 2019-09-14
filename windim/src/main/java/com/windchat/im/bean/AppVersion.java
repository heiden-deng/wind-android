package com.windchat.im.bean;

/**
 * Created by Mr.kk on 2018/6/13.
 * This Project was client-android
 */

public class AppVersion {


    /**
     * version_code : 14
     * download_url : https://cdn-akaxin-1255552447.cos.ap-beijing.myqcloud.com/client/akaxin-release.apk
     * version_name : v0.12.6.20180626
     * upgrade_tips : 1.修复扩展图片问题
     2. 修复一些兼容性bug
     */

    private int version_code;
    private String download_url;
    private String version_name;
    private String upgrade_tips;

    @Override
    public String toString() {
        return "AppVersion{" +
                "version_code=" + version_code +
                ", download_url='" + download_url + '\'' +
                ", version_name='" + version_name + '\'' +
                ", upgrade_tips='" + upgrade_tips + '\'' +
                '}';
    }

    public int getVersion_code() {
        return version_code;
    }

    public void setVersion_code(int version_code) {
        this.version_code = version_code;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public String getUpgrade_tips() {
        return upgrade_tips == null ? "" : upgrade_tips;
    }

    public void setUpgrade_tips(String upgrade_tips) {
        this.upgrade_tips = upgrade_tips;
    }
}
