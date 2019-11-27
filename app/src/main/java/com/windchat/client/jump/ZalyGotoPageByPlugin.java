package com.windchat.client.jump;

import android.content.Intent;

import com.windchat.client.ZalyApplication;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.jump.presenter.impl.GoToPagePresenter;

/**
 *
 */

public abstract class ZalyGotoPageByPlugin {
    public static final String TAG = "ZalyJumpByPlugin";


    /**
     * 处理跳转逻辑.
     */
    public static Intent executeGotoPage(String url, Boolean isIntent) {
        ZalyLogUtils.getInstance().info(TAG, "url:" + url);
        GoToPagePresenter goToPagePresenter = new GoToPagePresenter(null);
        return goToPagePresenter.handleGotoPage(ZalyApplication.getContext(), url, isIntent);
    }
}
