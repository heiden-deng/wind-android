package com.akaxin.client.bridge;

/**
 * Created by yichao on 2018/1/16.
 */

public class ZalyRequest {
    public boolean isRoot;
    public String apiName;
    public String apiParams;

    public ZalyRequest(String apiName, String apiParams) {
        this.apiName = apiName;
        this.apiParams = apiParams;
    }

    public ZalyRequest(boolean isRoot) {
        this.isRoot = isRoot;
    }
}
