package com.akaxin.client.jump;

import android.content.Context;
import android.content.Intent;

import com.akaxin.client.ZalyApplication;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.jump.presenter.impl.GoToPagePresenter;

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
