package com.akaxin.client.mvp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.im.ZalyIM;
import com.akaxin.client.maintab.ActivityCollector;
import com.akaxin.client.maintab.WelcomeActivity;
import com.akaxin.client.util.ClientTypeHepler;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.umeng.message.PushAgent;

import java.lang.reflect.ParameterizedType;
import java.util.Stack;

import static com.akaxin.proto.core.ClientProto.ClientType.ANDROID_XIAOMI;

/**
 * Created by Mr.kk on 2018/5/9.
 * This Project was client-android
 */

public abstract class BaseMVPActivity<V extends BaseView, T extends BasePresenterImpl<V>> extends AppCompatActivity implements BaseView {
    public T mPresenter;
    protected final String ACTIVITY_NAME = this.getClass().getSimpleName();
    protected final String ACTIVITY_TAG = "activityLife";
    protected final String TAG = this.getClass().getSimpleName();
    protected LinearLayout navBackLayout;
    protected TextView mainTitleTv;
    protected TextView descTitleTv;
    private static Stack<Activity> activityStack;
    public MaterialDialog dialog, textDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ZalyLogUtils.getInstance().info(ACTIVITY_TAG, ACTIVITY_NAME + "onCreate");
        mPresenter = getInstance(this, 1);
        mPresenter.attachView((V) this);

        ActivityCollector.addActivity(this);
        if (ClientTypeHepler.getClientType() != ANDROID_XIAOMI) {
            PushAgent.getInstance(this).onAppStart();
        }
        checkApplication();
        registerBroadcast();
    }

    protected void checkApplication() {
        if (!ZalyApplication.active && !WelcomeActivity.active) {
            ActivityCollector.finishAll();
            Intent intent = new Intent(this, WelcomeActivity.class);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                ZalyLogUtils.getInstance().info(TAG, "activity not found: " + intent);
            }
        }
    }

    public void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter(ZalyIM.CONNECTION_ACTION);
        registerReceiver(connectStatusReceiver, intentFilter);
    }

    /**
     * IM连接状广播接受器
     */
    private BroadcastReceiver connectStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String connIdentity = bundle.getString(ZalyIM.KEY_CONN_IDENTITY);
                int connType = bundle.getInt(ZalyIM.KEY_CONN_TYPE);
                int statusType = bundle.getInt(ZalyIM.KEY_CONN_STATUS);
                onConnectionChange(connIdentity, connType, statusType);
            }
        }
    };

    public void onConnectionChange(String connIdentity, int connType, int statusType) {

    }


    @Override
    protected void onStart() {
        super.onStart();
        ZalyLogUtils.getInstance().info(ACTIVITY_TAG, ACTIVITY_NAME + "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        ZalyLogUtils.getInstance().info(ACTIVITY_TAG, ACTIVITY_NAME + "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ZalyLogUtils.getInstance().info(ACTIVITY_TAG, ACTIVITY_NAME + "onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        ZalyLogUtils.getInstance().info(ACTIVITY_TAG, ACTIVITY_NAME + "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        ZalyLogUtils.getInstance().info(ACTIVITY_TAG, ACTIVITY_NAME + "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZalyLogUtils.getInstance().info(ACTIVITY_TAG, ACTIVITY_NAME + "onDestroy");
        unregisterReceiver(connectStatusReceiver);
        if (mPresenter != null)
            mPresenter.detachView();
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
        }
        return null;
    }

    @Override
    public void showDialog(String content, String positiveText, String negativeText, MaterialDialog.SingleButtonCallback callback) {
        textDialog = new MaterialDialog.Builder(this)
                .content(content)
                .positiveText(positiveText)
                .negativeText("取消")
                .onAny(callback)
                .show();
    }

    @Override
    public void showDialog(String content, String positiveText, MaterialDialog.SingleButtonCallback callback) {
        textDialog = new MaterialDialog.Builder(this)
                .content(content)
                .positiveText(positiveText)
                .onAny(callback)
                .show();
    }

    @Override
    public Context getContext() {
        return this;
    }

    protected void initToolBar() {
        navBackLayout = findViewById(R.id.nav_back_layout);
        if (navBackLayout != null) {
            navBackLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    hideSoftKey();
//                    supportFinishAfterTransition();
//                    hideProgress();
                    finish();
                }
            });
        }
        mainTitleTv = findViewById(R.id.main_title);
        descTitleTv = findViewById(R.id.subtitle);
    }

    protected void setCenterTitle(String title) {
        descTitleTv.setVisibility(View.GONE);
        if (mainTitleTv != null) {
            mainTitleTv.setText(title);
        }
    }

    protected void setTitle(String title, String viceTitle) {
        mainTitleTv.setVisibility(View.VISIBLE);
        descTitleTv.setVisibility(View.VISIBLE);
        mainTitleTv.setText(title);
        descTitleTv.setText(viceTitle);
    }

    protected void setCenterTitle(int resId) {
        setCenterTitle(getString(resId));
    }


    @Override
    public void showProgressDialog(String content) {
        if (dialog == null)
            dialog = new MaterialDialog.Builder(this)
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

    public void addActivity(Activity activity) {
        initActivityStack();
        activityStack.add(activity);
    }

    /**
     * 初始化Stack<Activity>
     */
    private void initActivityStack() {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishRegisterActivity() {
        try {
            for (int i = 0, size = activityStack.size(); i < size; i++) {
                if (null != activityStack.get(i)) {
                    Activity activity = activityStack.get(i);
                    if (!activity.isFinishing()) {
                        activity.finish();
                    }
                }
            }
            activityStack.clear();
        } catch (Exception e) {
            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
        }
    }
}
