package com.akaxin.client.chat;

/**
 * Created by zhangjun on 30/05/2018.
 */

public interface LoadPluginWebViewListener {

    void loadWebSuccess();
    void loadWebFailed();
    void startPhotoPicker();

    void quitWebView();
}
