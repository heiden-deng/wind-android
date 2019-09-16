package com.akaxin.client.maintab;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.windchat.im.IMConst;

/**
 * Created by yichao on 2018/1/12.
 */

public abstract class BaseFragment extends Fragment {

    protected final String TAG = getClass().getSimpleName();
    private View rootView;
    private MaterialDialog dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter intentFilter = new IntentFilter("com.akaxin.client.im_status.BROADCAST");
        registerReceiver(connectStatusReceiver, intentFilter);
    }

    public void showDialog(String content, String positiveText, String negativeText, MaterialDialog.SingleButtonCallback callback) {
        dialog = new MaterialDialog.Builder(getActivity())
                .content(content)
                .positiveText(positiveText)
                .negativeText("取消")
                .onAny(callback)
                .show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayout(), container, false);
        initViews(rootView);
        onLoad();
        return rootView;
    }

    protected abstract int getLayout();

    protected abstract void initViews(View contentView);

    public View findViewById(int id) {
        return rootView.findViewById(id);
    }

    protected abstract void onLoad();

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectStatusReceiver);
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        getActivity().unregisterReceiver(receiver);
    }

    public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        getActivity().registerReceiver(receiver, filter);
    }

    /**
     * 进度dialog
     */
    private ProgressDialog progressDialog;

    public void showProgress() {
        showProgress("请稍候...");
    }

    public void showProgress(String content) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(content);
        progressDialog.show();
    }

    public void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    /**
     * IM连接状广播接受器
     */
    private BroadcastReceiver connectStatusReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Bundle bundle = intent.getExtras();
                    String connIdentity = bundle.getString(IMConst.KEY_CONN_IDENTITY);
                    int connType = bundle.getInt(IMConst.KEY_CONN_TYPE);
                    int statusType = bundle.getInt(IMConst.KEY_CONN_STATUS);
                    onConnectionChange(connIdentity, connType, statusType);
                }
            };

    /**
     * 状态回调方法，子类需重写提供实现，不要求每个子类都重写
     *
     * @param connIdentity
     * @param connType
     * @param statusType
     */
    public void onConnectionChange(String connIdentity, int connType, int statusType) {
//        ZalyLogUtils.getInstance().info(TAG, "connIdentity: " + connIdentity +
//                ", connType: " + connType +
//                ", statusType: " + statusType);
    }


}
