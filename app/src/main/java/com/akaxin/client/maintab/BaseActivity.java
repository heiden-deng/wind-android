package com.akaxin.client.maintab;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.PackageSign;
import com.akaxin.client.util.ClientTypeHepler;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.umeng.message.PushAgent;
import com.windchat.im.IMClient;
import com.windchat.im.IMConst;
import com.windchat.im.socket.Connection;
import com.windchat.im.socket.SiteAddress;

import java.util.Stack;

import static com.akaxin.proto.core.ClientProto.ClientType.ANDROID_XIAOMI;
import static com.windchat.im.socket.Connection.CONN_IM;

/**
 * Created by yichao on 2017/11/11.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected final String TAG = BaseActivity.class.getSimpleName();

    private ProgressDialog progressDialog;
    protected LinearLayout navBackLayout;
    private TextView mainTitleTv;
    private TextView descTitleTv;

    private static Stack<Activity> activityStack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResLayout());
        ActivityCollector.addActivity(this);
        if (ClientTypeHepler.getClientType() != ANDROID_XIAOMI) {
            PushAgent.getInstance(this).onAppStart();
        }
        checkApplication();
        registerBroadcast();
        initToolBar();
        initView();
        initEvent();
        initPresenter();
        onLoadData();
    }

    public Context getContext() {
        return this;
    }

    /**
     * 避免APP不允许后台运行而被长时间挂起造成的IM连接假活
     */
    @Override
    protected void onResume() {
        super.onResume();
        //清理本地SDK
        NotificationManager notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        if (notificationManager != null)
            notificationManager.cancelAll();
    }

    @Override
    protected void onDestroy() {
        hideProgress();
        unregisterReceiver(connectStatusReceiver);
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    private void initToolBar() {
        navBackLayout = findViewById(R.id.nav_back_layout);
        if (navBackLayout != null) {
            navBackLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideSoftKey();
                    //supportFinishAfterTransition();
                    hideProgress();
                    finish();
                }
            });
        }
        mainTitleTv = findViewById(R.id.main_title);
        descTitleTv = findViewById(R.id.subtitle);
    }

    public void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter(IMConst.CONNECTION_ACTION);
        registerReceiver(connectStatusReceiver, intentFilter);
    }


    public abstract int getResLayout();

    public abstract void initView();

    public abstract void initEvent();

    public abstract void initPresenter();

    public abstract void onLoadData();

    /**
     * 设置居中标题
     *
     * @param title
     */
    protected void setCenterTitle(String title) {
        descTitleTv.setVisibility(View.GONE);
        if (mainTitleTv != null) {
            mainTitleTv.setText(title);
        }
    }

    protected void setCenterTitle(int resId) {
        setCenterTitle(getString(resId));
    }

    /**
     * 设置多级标题
     *
     * @param mainTitle
     * @param descTitle
     */
    protected void setMultTitle(String mainTitle, String descTitle) {
        descTitleTv.setVisibility(View.VISIBLE);
        if (mainTitleTv != null && descTitleTv != null) {
            mainTitleTv.setText(mainTitle);
            descTitleTv.setText(descTitle);
        }
    }

    protected void setMultTitle(int mainResId, String descTitle) {
        setMultTitle(getString(mainResId), descTitle);
    }

    public void showProgress() {
        showProgress("请稍候...");
    }

    public void showProgress(String content) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(content);
        if (!this.isFinishing()) progressDialog.show();
    }

    public void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
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
            if (bundle != null) {
                String connIdentity = bundle.getString(IMConst.KEY_CONN_IDENTITY);
                int connType = bundle.getInt(IMConst.KEY_CONN_TYPE);
                int statusType = bundle.getInt(IMConst.KEY_CONN_STATUS);
                onConnectionChange(connIdentity, connType, statusType);
            }
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
        /////ZalyLogUtils.getInstance().info(TAG, "connIdentity: " + connIdentity +", connType: " + connType +", statusType: " + statusType);
    }

    /**
     * 显示软键盘
     */
    protected void showSoftKey() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 隐藏软键盘
     */
    protected void hideSoftKey() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * Application 被 GC 后需要重新进入 WelcomeActivity 初始化.
     */
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

    /**
     * 生成二维码
     *
     * @param content
     * @param size
     * @return
     * @throws WriterException
     */
    public Bitmap encodeAsBitmap(String content, int size, int qrCodeBlackColor, int qrCodeWhiteColor) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? qrCodeBlackColor : qrCodeWhiteColor;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, size, 0, 0, w, h);
        return bitmap;
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

    public boolean isSiteConnected(Site site) {
        boolean isConnected;
        try {
            isConnected = IMClient.getInstance(site.toSiteAddress()).isConnected();
        } catch (Exception e) {
            return false;
        }
        ZalyLogUtils.getInstance().info(TAG, "getConnStatus == " + site.getConnStatus());
        if (site.getConnStatus() == Site.MANUAL_CONTROL_DISCONNECT_STATUS) {
            Bundle bundle = new Bundle();
            bundle.putString(IMConst.KEY_CONN_IDENTITY, site.getSiteIdentity());
            bundle.putInt(IMConst.KEY_CONN_STATUS, Connection.STATUS_CONN_DISCONN);
            bundle.putInt(IMConst.KEY_CONN_TYPE, CONN_IM);
            Intent intent = new Intent(IMConst.CONNECTION_ACTION);
            intent.putExtras(bundle);
            intent.setPackage(PackageSign.getPackage());
            getContext().sendBroadcast(intent);
        }
        if (isConnected) {
            Bundle bundle = new Bundle();
            bundle.putString(IMConst.KEY_CONN_IDENTITY, site.getSiteIdentity());
            bundle.putInt(IMConst.KEY_CONN_STATUS, Connection.STATUS_AUTH_SUCCESS);
            bundle.putInt(IMConst.KEY_CONN_TYPE, CONN_IM);
            Intent intent = new Intent(IMConst.CONNECTION_ACTION);
            intent.putExtras(bundle);
            intent.setPackage(PackageSign.getPackage());
            getContext().sendBroadcast(intent);
        }
        if (!isConnected && site.getConnStatus() != Site.MANUAL_CONTROL_DISCONNECT_STATUS) {
            IMClient.getInstance(new SiteAddress(site)).makeSureClientAlived(site.toSiteAddress());
        }

        return isConnected;

    }


}
