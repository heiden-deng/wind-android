package com.windchat.client.jump.presenter;

import android.content.Context;
import android.content.Intent;

/**
 * Created by zhangjun on 2018/3/20.
 */

public interface IGotoPagePresenter {

    /**
     * 获取activity
     * @param context
     * @param url
     * @param isIntent
     * @return
     */
    Intent handleGotoPage(Context context, String url, Boolean isIntent);

}
