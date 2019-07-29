package com.akaxin.client.mvp;

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
import com.akaxin.client.im.ZalyIM;
import com.akaxin.client.util.log.ZalyLogUtils;

import java.lang.reflect.ParameterizedType;


public abstract class MVPBaseFragment<V extends BaseView, T extends BasePresenterImpl<V>> extends Fragment implements BaseView {
    public T mPresenter;
    protected final String FRAGMENT_NAME = this.getClass().getSimpleName();
    protected final String FRAGMENT_LIFT = "fragmentLife";

    protected final String TAG = this.getClass().getSimpleName();
    protected View rootView;
    public MaterialDialog dialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ZalyLogUtils.getInstance().debug(FRAGMENT_LIFT, FRAGMENT_NAME + "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = getInstance(this, 1);
        mPresenter.attachView((V) this);
        IntentFilter intentFilter = new IntentFilter("com.akaxin.client.im_status.BROADCAST");
        registerReceiver(connectStatusReceiver, intentFilter);
        ZalyLogUtils.getInstance().debug(FRAGMENT_LIFT, FRAGMENT_NAME + "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ZalyLogUtils.getInstance().debug(FRAGMENT_LIFT, FRAGMENT_NAME + "onCreateView");
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        ZalyLogUtils.getInstance().debug(FRAGMENT_LIFT, FRAGMENT_NAME + "onStart");

    }

    @Override
    public void onResume() {
        super.onResume();
        ZalyLogUtils.getInstance().debug(FRAGMENT_LIFT, FRAGMENT_NAME + "onResume");

    }

    @Override
    public void onPause() {
        super.onPause();
        ZalyLogUtils.getInstance().debug(FRAGMENT_LIFT, FRAGMENT_NAME + "onPause");

    }

    @Override
    public void onStop() {
        super.onStop();
        ZalyLogUtils.getInstance().debug(FRAGMENT_LIFT, FRAGMENT_NAME + "onStop");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ZalyLogUtils.getInstance().debug(FRAGMENT_LIFT, FRAGMENT_NAME + "onDestroyView");

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null)
            mPresenter.detachView();
        unregisterReceiver(connectStatusReceiver);
        ZalyLogUtils.getInstance().debug(FRAGMENT_LIFT, FRAGMENT_NAME + "onDestroy");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        ZalyLogUtils.getInstance().debug(FRAGMENT_LIFT, FRAGMENT_NAME + "onDetach");

    }

    @Override
    public Context getContext() {
        return super.getContext();
    }

    public <T> T getInstance(Object o, int i) {
        try {
            return ((Class<T>) ((ParameterizedType) (o.getClass()
                    .getGenericSuperclass())).getActualTypeArguments()[i])
                    .newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void showDialog(String content, String positiveText, String negativeText, MaterialDialog.SingleButtonCallback callback) {
        new MaterialDialog.Builder(getActivity())
                .content(content)
                .positiveText(positiveText)
                .negativeText("取消")
                .onAny(callback)
                .show();
    }

    @Override
    public void showDialog(String content, String positiveText, MaterialDialog.SingleButtonCallback callback) {
        new MaterialDialog.Builder(getActivity())
                .content(content)
                .positiveText(positiveText)
                .onAny(callback)
                .show();
    }

    @Override
    public void showProgressDialog(String content) {
        if (dialog == null)
            dialog = new MaterialDialog.Builder(getActivity())
                    .content(content)
                    .progress(true, 0).build();
        if (dialog.isShowing()) {
            dialog.setContent(content);
            dialog.show();
        } else {
            dialog.show();
        }


    }

    @Override
    public void hideProgressDialog() {
        if (dialog != null) {
            if (dialog.isShowing())
                dialog.dismiss();
        }

    }

    protected void unregisterReceiver(BroadcastReceiver receiver) {
        getActivity().unregisterReceiver(receiver);
    }

    protected void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        getActivity().registerReceiver(receiver, filter);
    }

    /**
     * IM连接状广播接受器
     */
    private BroadcastReceiver connectStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String connIdentity = bundle.getString(ZalyIM.KEY_CONN_IDENTITY);
            int connType = bundle.getInt(ZalyIM.KEY_CONN_TYPE);
            int statusType = bundle.getInt(ZalyIM.KEY_CONN_STATUS);
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
