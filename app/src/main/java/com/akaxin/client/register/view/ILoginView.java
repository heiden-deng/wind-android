package com.akaxin.client.register.view;

import android.content.Context;

/**
 * Created by yichao on 2017/10/16.
 */

public interface ILoginView {

    /**
     * 跳转至主页
     */
    void gotoMainTabView();

    void showProgressDialog();

    void hideProgressDialog();

    Context getAppContext();
}
