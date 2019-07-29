package com.akaxin.client.mvp;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;


public interface BaseView {
    Context getContext();

    void showDialog(String content, String positiveText, String negativeText, MaterialDialog.SingleButtonCallback callback);

    void showDialog(String content, String positiveText, MaterialDialog.SingleButtonCallback callback);

    void showProgressDialog(String content);

    void hideProgressDialog();
}
