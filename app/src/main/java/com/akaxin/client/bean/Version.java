package com.akaxin.client.bean;

import com.akaxin.client.util.GsonUtils;
import com.akaxin.client.util.data.StringUtils;

/**
 * Created by anguoyue on 29/01/2018.
 */

public class Version {
    private int major;//主版本
    private int minor;//子版本
    private int revision;//proto版本（标准格式为修订版本，zaly协议中表示proto版本）

    public Version(String v) {
        this.setVersion(v);
    }

    public Version setVersion(String version) {
        if (StringUtils.isNotBlank(version)) {
            String[] vers = version.split("\\.");
            if (vers.length == 2) {
                this.major = Integer.parseInt(vers[0]);
                this.minor = Integer.parseInt(vers[1]);
            } else if (vers.length == 3) {
                this.major = Integer.parseInt(vers[0]);
                this.minor = Integer.parseInt(vers[1]);
                this.revision = Integer.parseInt(vers[2]);
            }

        }
        return this;
    }

    public String getVersion() {
        return this.major + "." + this.minor + "." + this.revision;
    }

    public boolean isCorrect() {
        return this.major + this.minor + this.revision > 0;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String toString() {
        return GsonUtils.toJson(this);
    }
}
