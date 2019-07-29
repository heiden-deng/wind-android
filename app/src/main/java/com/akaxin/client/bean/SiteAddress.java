package com.akaxin.client.bean;

import com.akaxin.client.util.data.StringUtils;

/**
 * Created by anguoyue on 28/02/2018.
 */

public class SiteAddress {
    private String siteHost;
    private int sitePort = 2021;

    public SiteAddress(String address) {
        if (StringUtils.isNotEmpty(address)) {
            String[] addrs = address.split(":");
            if (addrs.length == 1) {
                this.siteHost = addrs[0];
            } else if (addrs.length == 2) {
                this.siteHost = addrs[0];
                this.sitePort = Integer.parseInt(addrs[1]);
            }
        }
    }

    public SiteAddress(String siteHost, int sitePort) {
        this.siteHost = siteHost;
        this.sitePort = sitePort;
    }

    public String getSiteHost() {
        return siteHost;
    }

    public void setSiteHost(String siteHost) {
        this.siteHost = siteHost;
    }

    public int getSitePort() {
        return sitePort;
    }

    public void setSitePort(int sitePort) {
        this.sitePort = sitePort;
    }

    public String getSiteAddress() {
        if (this.siteHost != null) {
            return this.siteHost + ":" + this.sitePort;
        }
        return null;
    }

    public String getSiteDBAddress() {
        if (this.siteHost != null) {
            String dbHost = this.siteHost.replace(".", "-");
            return dbHost + "-" + this.sitePort;
        }
        return null;
    }
}
