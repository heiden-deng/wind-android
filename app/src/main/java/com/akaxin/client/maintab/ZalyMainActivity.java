package com.akaxin.client.maintab;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.activitys.ScanQRCodeActivity;
import com.akaxin.client.activitys.ShareQRCodeActivity;
import com.akaxin.client.activitys.SiteConnListActivity;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ApiClientForPlatform;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bean.event.AppEvent;
import com.akaxin.client.bean.event.SiteEvent;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.constant.ServerConfig;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.fragments.AppFragment;
import com.akaxin.client.fragments.PersonalFragment;
import com.akaxin.client.fragments.SessionFragment;
import com.akaxin.client.friend.ContactsFragment;
import com.akaxin.client.friend.FriendSearchActivity;
import com.akaxin.client.group.GroupCreateActivity;
import com.akaxin.client.maintab.adapter.MainTabPagerAdapter;
import com.akaxin.client.platform.task.ApiUserPushTokenTask;
import com.akaxin.client.platform.task.GetUserPhoneTask;
import com.akaxin.client.platform.task.PlatformLoginTask;
import com.akaxin.client.platform.task.PushAuthTask;
import com.akaxin.client.site.presenter.impl.PlatformPresenter;
import com.akaxin.client.site.task.GetSitesInfoTask;
import com.akaxin.client.site.task.GetSitesTask;
import com.akaxin.client.site.task.LoginSiteTask;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.client.view.ZalyNoScrollViewPager;
import com.akaxin.proto.client.ImStcNoticeProto;
import com.akaxin.proto.site.ApiGroupJoinByTokenProto;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.windchat.im.IMClient;
import com.windchat.im.IMConst;
import com.windchat.im.socket.Connection;
import com.windchat.im.socket.SiteAddress;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.akaxin.client.Configs.DEVICE_IMEI;
import static com.akaxin.client.Configs.KEY_NEW_APPLY_FRIEND;
import static com.akaxin.client.util.SiteUtils.syncSiteInfo;

/**
 * Created by yichao on 2017/10/9.
 */

public class ZalyMainActivity extends BaseActivity
        implements View.OnClickListener, BubbleUpdateListener {
    public static final String TAG = ZalyMainActivity.class.getSimpleName();
    public static final String KEY_TAB_INDEX = "tab_index";
    public static final int APP_TAB_INDEX = 0;
    public static final int SESSION_TAB_INDEX = 1;
    public static final int CONTACT_TAB_INDEX = 2;
    public static final int PERSONAL_TAB_INDEX = 3;
    private static final int NUM_TABS = 4;
    public static final String CHECK_BUDDLE = "check_buddle";

    public static final String KEY_SITE_INFO = "key_site_info";


    private int curIndex = SESSION_TAB_INDEX;
    private int selectIndex = SESSION_TAB_INDEX;
    public static ZalyMainActivity zalyMainActivity;
    private RelativeLayout[] tabs;
    private Fragment[] fragments;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.site_input)
    View siteInputHint;
    @BindView(R.id.titles)
    View titlesView;
    @BindView(R.id.title_root)
    View titleRoot;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.subtitle)
    TextView subtitle;
    @BindView(R.id.bubble)
    LinearLayout bubble;
    @BindView(R.id.bubble_message)
    ImageView headerBubbleMessage;
    @BindView(R.id.bubble_new_friend)
    ImageView headerBubbleNewFriend;
    @BindView(R.id.conn_status_bar)
    TextView connStatusBar;
    @BindView(R.id.view_pager)
    ZalyNoScrollViewPager viewPager;
    @BindView(R.id.msg_unread_bubble)
    ImageView bottomBubbleMessage;
    @BindView(R.id.contact_unread_bubble)
    ImageView bottomBubbleContact;
    @BindView(R.id.personal_unread_bubble)
    ImageView personalUnreadBubble;
    @BindView(R.id.action_more)
    ImageButton moreButton;
    private PopupMenu popupMenu;
    private MenuPopupHelper menuPopupHelper;
    private MainTabPagerAdapter tabAdapter;
    private boolean showSessionBubbleForCurrentSite = false;
    private boolean showContactBubbleForCurrentSite = false;
    private boolean showSessionBubbleForOtherSites = false;
    private boolean showContactBubbleForOtherSites = false;
    private long mExitTime;

    private Site currentSite;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter intentFilter = new IntentFilter(IMConst.UPDATE_SITES_ACTION);
        intentFilter.addAction(IMConst.IM_NOTICE_ACTION);
        intentFilter.addAction(IMConst.PLATFORM_PUSH_ACTION);
        registerReceiver(mainTabReceiver, intentFilter);
        registerNetInfo();
        doMainLogic();
        if (Build.VERSION.SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        finishRegisterActivity();
        tabSwitch(APP_TAB_INDEX);
    }


    protected void registerNetInfo() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mainTabReceiver);
        unregisterReceiver(networkChangeListener);
    }

    @Override
    public int getResLayout() {
        return R.layout.activity_new_maintab;
    }

    @Override
    @SuppressLint("RestrictedApi")
    public void initView() {
        ButterKnife.bind(this);

        zalyMainActivity = this;
        tabs = new RelativeLayout[NUM_TABS];
        tabs[APP_TAB_INDEX] = findViewById(R.id.tab_app);
        tabs[SESSION_TAB_INDEX] = findViewById(R.id.tab_session);
        tabs[CONTACT_TAB_INDEX] = findViewById(R.id.tab_contact);
        tabs[PERSONAL_TAB_INDEX] = findViewById(R.id.tab_personal);

        popupMenu = new PopupMenu(this, moreButton, Gravity.START);
        popupMenu.getMenuInflater().inflate(R.menu.menu_main, popupMenu.getMenu());
        menuPopupHelper = new MenuPopupHelper(this, (MenuBuilder) popupMenu.getMenu(), moreButton);
        menuPopupHelper.setForceShowIcon(true);
        onNewIntent(getIntent());
        loadVersion();

    }

    private void loadVersion() {
        UpgradeInfo upgradeInfo = Beta.getUpgradeInfo();
        if (upgradeInfo == null) {
            personalUnreadBubble.setVisibility(View.INVISIBLE);
            return;
        }
        if (upgradeInfo.versionCode > AppUtils.getAppVersionCode()) {
            personalUnreadBubble.setVisibility(View.VISIBLE);
        } else {
            personalUnreadBubble.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void initEvent() {
        for (RelativeLayout tab : tabs) {
            tab.setOnClickListener(this);
        }
        toolbar.setOnClickListener(this);
        siteInputHint.setOnClickListener(this);
        moreButton.setOnClickListener(this);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                Intent intent;
                switch (id) {
                    case R.id.menu_site_switch:
                        Intent intentSwitch = new Intent(ZalyMainActivity.this, SiteConnListActivity.class);
                        intentSwitch.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                        startActivity(intentSwitch);
                        break;
                    case R.id.menu_scan_qrcode:
                        requestPermissAndToScanActivity();
                        break;
                    case R.id.menu_create_group:
                        intent = new Intent(ZalyMainActivity.this, GroupCreateActivity.class);
                        intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                        startActivity(intent);
                        break;
                    case R.id.menu_share_site:
                        Intent shareIntent = new Intent(ZalyMainActivity.this, ShareQRCodeActivity.class);
                        shareIntent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                        shareIntent.putExtra(IntentKey.KEY_QR_CODE_TYPE, IntentKey.KEY_TYPE_SITE);
                        startActivity(shareIntent);
                        break;
                    case R.id.search_friend:
                        intent = new Intent(ZalyMainActivity.this, FriendSearchActivity.class);
                        intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }

    private void requestPermissAndToScanActivity() {
        PermissionUtils.permission(Manifest.permission.CAMERA).rationale(new PermissionUtils.OnRationaleListener() {
            @Override
            public void rationale(final ShouldRequest shouldRequest) {
                new MaterialDialog.Builder(ZalyMainActivity.this)
                        .content("使用扫一扫功能将获取相机权限")
                        .positiveText("好的")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which == DialogAction.POSITIVE) {
                                    shouldRequest.again(true);
                                } else {
//                                    shouldRequest.again(false);
                                }
                                dialog.dismiss();
                            }
                        }).cancelable(false)
                        .show();
            }
        }).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                Intent intent = new Intent(ZalyMainActivity.this, ScanQRCodeActivity.class);
                intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivityForResult(intent, ScanQRCodeActivity.SCAN_QRCODE_CODE);
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                new MaterialDialog.Builder(ZalyMainActivity.this)
                        .content(R.string.reject_permiss_dialog_notice)
                        .positiveText("好的")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which == DialogAction.POSITIVE) {
                                    PermissionUtils.launchAppDetailsSettings();
                                } else {
                                    dialog.dismiss();
                                }
                            }
                        }).cancelable(false)
                        .show();
            }
        }).request();
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitApp();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //退出方法
    private void exitApp() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            mExitTime = System.currentTimeMillis();
        } else {
            //用户退出处理
            moveTaskToBack(true);
            //System.exit(0);
        }
    }

    @Override
    public void onLoadData() {
    }

    @Override
    @SuppressWarnings("unchecked")
    @SuppressLint("RestrictedApi")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar:
            case R.id.site_input:
                Intent intent = new Intent(this, SiteConnListActivity.class);
                intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(intent);
                break;
            case R.id.action_more:
                menuPopupHelper.show();
                break;
            case R.id.tab_app:
                if (curIndex == APP_TAB_INDEX) {
                    EventBus.getDefault().postSticky(new AppEvent(AppEvent.ACTION_TO_TOP_APP_WEB, null));
                    return;
                }
                tabSwitch(APP_TAB_INDEX);
                break;
            case R.id.tab_session:
                tabSwitch(SESSION_TAB_INDEX);
                break;
            case R.id.tab_contact:
                tabSwitch(CONTACT_TAB_INDEX);
                break;
            case R.id.tab_personal:
                tabSwitch(PERSONAL_TAB_INDEX);
                break;
        }
    }

    private void tabSwitch(int index) {
        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setActivated(i == index);
        }
        if (curIndex == index) {
            return;
        }
        if (index == CONTACT_TAB_INDEX) {
            EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_RELOAD, null));
        }
        curIndex = index;
        updateBottomNaviBubbles(0);
        viewPager.setCurrentItem(index, false);
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        title.setText(currentSite.getSiteName());
        subtitle.setText(currentSite.getSiteHost());
        ZalyTaskExecutor.executeUserTask(TAG, new GetSitesInfoTask(currentSite));
        for (Fragment fragment : fragments) {
            if (fragment.getView() != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(IntentKey.KEY_CURRENT_SITE, currentSite);
                fragment.getArguments().putAll(bundle);
                fragment.onResume();
            }
        }
        popupMenu.getMenu().findItem(R.id.menu_create_group).setVisible(currentSite != null);
        popupMenu.getMenu().findItem(R.id.menu_share_site).setVisible(currentSite != null);
        // Load all the unread notifications for showing the bubbles.
        checkIMConnection();
        ZalyTaskExecutor.executeUserTask(TAG, new GetSitesUnreadNumTask());
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras() == null) {
            ZalyLogUtils.getInstance().info(TAG, "onNewIntent: get intent: null");
            return;
        }
        int tab_index = intent.getExtras().getInt(KEY_TAB_INDEX, -1);
        if (tab_index == APP_TAB_INDEX || tab_index == SESSION_TAB_INDEX
                || tab_index == CONTACT_TAB_INDEX
                || tab_index == PERSONAL_TAB_INDEX) {
            tabSwitch(tab_index);
        }
        currentSite = intent.getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        switch (intent.getIntExtra(IntentKey.KEY_QR_CODE_TYPE, -1)) {
            case IntentKey.KEY_TYPE_USER:
                break;
            case IntentKey.KEY_TYPE_SITE:
                break;
            case IntentKey.KEY_TYPE_GROUP:
                String token = intent.getStringExtra(IntentKey.TOKEN);
                String siteGroupID = intent.getStringExtra(IntentKey.KEY_GROUP_ID);
                applyJoinGroup(token, siteGroupID);
                break;
        }


        // onResume();
    }

    private void applyJoinGroup(final String token, final String siteGroupID) {
        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, ApiGroupJoinByTokenProto.ApiGroupJoinByTokenResponse>() {
            @Override
            protected ApiGroupJoinByTokenProto.ApiGroupJoinByTokenResponse executeTask(Void... voids) throws Exception {
                return ApiClient.getInstance(currentSite).getGroupApi().joinGroupByToken(siteGroupID, token);
            }

            @Override
            protected void onPreTask() {
                super.onPreTask();
            }

            @Override
            protected void onTaskFinish() {
                super.onTaskFinish();
            }

            @Override
            protected void onTaskSuccess(ApiGroupJoinByTokenProto.ApiGroupJoinByTokenResponse apiGroupJoinByTokenResponse) {
                super.onTaskSuccess(apiGroupJoinByTokenResponse);
                Toaster.show("添加成功");

            }

            @Override
            protected void onTaskError(Exception e) {
                super.onTaskError(e);

            }

            @Override
            protected void onAPIError(ZalyAPIException zalyAPIException) {
                super.onAPIError(zalyAPIException);

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSessionEvent(AppEvent event) {
        switch (event.getAction()) {
            case AppEvent.ACTION_UPDATE_MAIN_SESSION_TAB_BUBBLE:
                ZalyTaskExecutor.executeUserTask(TAG, new GetSitesUnreadNumTask());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onSiteEvent(SiteEvent event) {
        switch (event.getAction()) {
            case SiteEvent.SWITCH_SITE_KEY:
                currentSite = event.getData().getParcelable(IntentKey.KEY_CURRENT_SITE);
                ZalyLogUtils.getInstance().info(TAG, " current site ==" + currentSite.getSiteAddress());
                onResume();
                break;
            case SiteEvent.NEW_SITE_KEY:
                currentSite = event.getData().getParcelable(IntentKey.KEY_CURRENT_SITE);
                tabSwitch(APP_TAB_INDEX);
                onResume();
                break;
            case SiteEvent.UPDATE_SITE_INFO:
                try {
                    Site site = event.getData().getParcelable(ZalyMainActivity.KEY_SITE_INFO);
                    ZalyLogUtils.getInstance().info(TAG, " update site info " + site.getSiteAddress());
                    ZalyLogUtils.getInstance().info(TAG, " update site info " + site.getSiteName());

                    if (site.getSiteAddress().equals(currentSite.getSiteAddress())) {
                        title.setText(site.getSiteName());
                        subtitle.setText(site.getSiteHost());
                    }
                } catch (Exception e) {
                    ZalyLogUtils.getInstance().exceptionError(e);
                }
                break;


        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onAppEvent(AppEvent event) {
        ZalyLogUtils.getInstance().info(TAG, event.getAction() + "");
        switch (event.getAction()) {
            case AppEvent.LOGIN_PLATFORM_SUCCESS:
                ZalyTaskExecutor.executeUserTask(TAG, new ApiUserPushTokenTask());
                ZalyTaskExecutor.executeUserTask(TAG, new PushAuthTask(ApiClientForPlatform.getPlatformSite()));
                ZalyTaskExecutor.executeTask(TAG, new GetUserPhoneTask());
            case AppEvent.ERROR_SESSION:
                ZalyTaskExecutor.executeUserTask(TAG, new PlatformLoginTask());
                break;
            case AppEvent.NO_PLUGIN:
                tabSwitch(SESSION_TAB_INDEX);
                break;
            case AppEvent.SET_SITE_LOGIN_ID:
                currentSite = event.getData().getParcelable(IntentKey.KEY_CURRENT_SITE);
                updateBottomNaviBubbles(1);
                personalUnreadBubble.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                personalUnreadBubble.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }, 500);
                break;
        }
    }

    private void doMainLogic() {
        fragments = new Fragment[NUM_TABS];
        fragments[APP_TAB_INDEX] = AppFragment.getObject(currentSite);
        fragments[SESSION_TAB_INDEX] = SessionFragment.getObject(currentSite);
        fragments[CONTACT_TAB_INDEX] = ContactsFragment.getObject(currentSite);
        fragments[PERSONAL_TAB_INDEX] = PersonalFragment.getObject(currentSite);

        tabAdapter = new MainTabPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setAdapter(tabAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (curIndex != selectIndex) {
                        tabSwitch(selectIndex);
                        curIndex = selectIndex;
                    }
                }
            }
        });
        if (PlatformPresenter.getInstance().getPlatformSessionId() == null) {
            //2.登陆平台
            ZalyTaskExecutor.executeUserTask(TAG, new PlatformLoginTask());
        }


        getIMEI();
    }


    // implementing BubbleUpdateListener
    @Override
    public void onSessionBubbleChange(int unreadNum) {
        showSessionBubbleForCurrentSite = (unreadNum > 0);
        updateBottomNaviBubbles(2);
    }

    @Override
    public void onContactBubbleChange(boolean unread) {
        showContactBubbleForCurrentSite = unread;
        updateBottomNaviBubbles(3);
    }

    /**
     * 间听网络状态
     */
    private BroadcastReceiver networkChangeListener = new BroadcastReceiver() {
        public static final String TAG = "NetworkChangeListener";

        @Override
        public void onReceive(Context context, Intent intent) {
            // 这个监听网络连接的设置.
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                ConnectivityManager manager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = manager.getActiveNetworkInfo();

                if (activeNetwork == null) {
                    return;
                }
                if (!activeNetwork.isConnected()) {
                    return;
                }
                if (currentSite == null) {
                    return;
                }
                isSiteConnected(currentSite);
            }
        }
    };

    /**
     * 更新内存中的站点数据
     */
    private BroadcastReceiver mainTabReceiver = new BroadcastReceiver() {

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                switch (action) {
                    case IMConst.UPDATE_SITES_ACTION:
                        ZalyTaskExecutor.executeUserTask(TAG, new UpdateSitesTask());
                        break;
                    case IMConst.IM_NOTICE_ACTION:
                        int noticeType = intent.getIntExtra(IMConst.KEY_NOTICE_TYPE, -1);
                        switch (noticeType) {
                            // 新的好友
                            case ImStcNoticeProto.NoticeType.NEW_FRIEND_VALUE:
                                EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_RELOAD, null));
                                break;
                            // 离线?
                            case ImStcNoticeProto.NoticeType.OFFLINE_VALUE:
                                break;
                            // 好友申请
                            case ImStcNoticeProto.NoticeType.APPLY_FRIEND_VALUE:
                                // 根据站点身份存储是否有的好友申请
                                String siteIdentity = intent.getStringExtra(IMConst.KEY_NOTICE_SITE_IDENTITY);
                                if (StringUtils.isEmpty(siteIdentity)) {
                                    return;
                                }
                                ZalyLogUtils.getInstance().info(TAG, " 不是当前站点");
                                ZalyApplication.getCfgSP().put(siteIdentity + KEY_NEW_APPLY_FRIEND, true);
                                if (siteIdentity.equals(currentSite.getSiteIdentity())) {
                                    // 通知 ContactsFragment 更新气泡
                                    EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_NEW_FRIEND, null));
                                }
                                ZalyTaskExecutor.executeUserTask(TAG, new GetSitesUnreadNumTask());

                                break;
                        }
                        break;
                    case IMConst.CONNECTION_ACTION:
                        Bundle bundle = intent.getExtras();
                        if (bundle == null) break;
                        int statusType = bundle.getInt(IMConst.KEY_CONN_STATUS);
                        switch (statusType) {
                            // 避免SessionFragment注册该事件晚于STATUS_AUTH_SUCCESS
                            case Connection.STATUS_AUTH_SUCCESS:
                                syncSiteInfo(currentSite);
                                break;
                        }
                        break;
                    case ZalyMainActivity.CHECK_BUDDLE:
                        ZalyTaskExecutor.executeUserTask(TAG, new GetSitesUnreadNumTask());
                        break;
                }
            }
        }
    };

    /**
     * 初始化站点任务
     */
    class UpdateSitesTask extends GetSitesTask {

        public UpdateSitesTask() {
            super(true);
        }

        @Override
        protected void onTaskSuccess(List<Site> sites) {
            super.onTaskSuccess(sites);
            //获取所有站点
            ZalyApplication.siteList = sites;
            //查找当前站点
            for (Site site : sites) {
                if (site.getSiteIdentity().equals(currentSite.getSiteIdentity())) {
                    currentSite.setSiteUserId(site.getSiteUserId());
                    currentSite.setSiteSessionId(site.getSiteSessionId());
                }
            }
        }
    }

    /**
     * 获取所有站点未读消息数量
     */
    class GetSitesUnreadNumTask extends GetSitesTask {

        public GetSitesUnreadNumTask() {
            super(true);
        }

        @Override
        protected void onTaskSuccess(List<Site> sites) {
            super.onTaskSuccess(sites);
            showContactBubbleForOtherSites = false;
            ZalyApplication.siteList = sites;
            int unreadNum = 0;
            for (Site site : sites) {
                if (site.getSiteIdentity().equals(currentSite.getSiteIdentity())) {
                    showContactBubbleForCurrentSite = ZalyApplication.getCfgSP().getBoolean(site.getSiteIdentity() + KEY_NEW_APPLY_FRIEND);
                    continue;
                }
                if (!site.isMute()) unreadNum += site.getUnreadNum();
                Boolean isApplyFriend = ZalyApplication.getCfgSP().getBoolean(site.getSiteIdentity() + KEY_NEW_APPLY_FRIEND);
                if (isApplyFriend) {
                    showContactBubbleForOtherSites = true;
                }
            }

            showSessionBubbleForOtherSites = unreadNum > 0;
            updateHeaderBarBubbles();
        }
    }

    /**
     * 获得手机IMEI号
     */
    public void getIMEI() {
        String deviceId = ZalyApplication.getCfgSP().getString(DEVICE_IMEI);
        if (TextUtils.isEmpty(deviceId)) {
            TelephonyManager telephonyManager = (TelephonyManager) ZalyApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            deviceId = telephonyManager.getDeviceId();
            if (!TextUtils.isEmpty(deviceId)) {
                ZalyApplication.getCfgSP().put(DEVICE_IMEI, deviceId);
            }
        }
    }

    private volatile boolean firstFlag = false;

    /**
     * 检测IM连接状态
     */
    private void checkIMConnection() {

        try {
            boolean isConnected = isSiteConnected(currentSite);
            if (isConnected) {
                String curSiteIdentity = currentSite.getSiteIdentity();
                IMClient.getInstance(new SiteAddress(curSiteIdentity)).syncMessage();
            }

        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }
    }

    /**
     * IMManager.getInstance().isConnected() is false ，进行次操作
     */
    protected void retrySiteConnected() {
        boolean isNetConnected = NetUtils.getNetInfo();
        if (isNetConnected) {
            if (currentSite.getConnStatus() == Site.MANUAL_CONTROL_DISCONNECT_STATUS) {
                connStatusBar.setVisibility(View.VISIBLE);
                connStatusBar.setText(R.string.error_conn_manual_disconnected);
                connStatusBar.setOnClickListener(null);
            }
        } else {
            connStatusBar.setVisibility(View.VISIBLE);
            connStatusBar.setText(R.string.error_conn_nonet);
            connStatusBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connStatusBar.setText(R.string.error_conn_netretrying);
                    IMClient.getInstance(new SiteAddress(currentSite)).makeSureClientAlived(currentSite.toSiteAddress());

                }
            });
            ZalyLogUtils.getInstance().info(TAG, " is no net work ");
        }
    }

    @Override
    public void onConnectionChange(String connIdentity, int connType, int statusType) {
        super.onConnectionChange(connIdentity, connType, statusType);
        ZalyLogUtils.getInstance().info(TAG, " connIdentity  == " + connIdentity + " error.session == " + (Connection.STATUS_AUTH_LOGIN == statusType ? true : false));

        if (connIdentity.equals(currentSite.getSiteIdentity())) {
            switch (statusType) {
                case Connection.STATUS_CONN_NORMAL: // TODO: 目前永远收不到这个状态, 这里 syncSiteInfo() 是干什么的?
                    if (!firstFlag) {
                        firstFlag = true;
                    }
                    connStatusBar.setVisibility(View.GONE);
                    connStatusBar.setOnClickListener(null);
                    break;
                case Connection.STATUS_AUTH_SUCCESS:
                    connStatusBar.setVisibility(View.GONE);
                    connStatusBar.setOnClickListener(null);
                    break;
                case Connection.STATUS_AUTH_LOGIN:
                    ZalyTaskExecutor.executeUserTask(TAG, new LoginSiteTask(currentSite, this));
                    ZalyLogUtils.getInstance().info(TAG, "imconnection Auth failed. Need login");
                    break;
                case Connection.STATUS_CONN_RETRY:
                case Connection.STATUS_CONN_DISCONN:
                    if (currentSite.getConnStatus() == Site.MANUAL_CONTROL_DISCONNECT_STATUS) {
                        connStatusBar.setText(R.string.error_conn_manual_disconnected);
                    } else {
                        connStatusBar.setText(R.string.error_conn_retrying);
                    }
                    connStatusBar.setVisibility(View.VISIBLE);
                    connStatusBar.setOnClickListener(null);
                    break;

                case Connection.STATUS_CONN_RETRY_FAIL:
                    ////仅仅展示了文字，并没有重连操作， 虽然Im service进程有，但是这个并不准确
                    retrySiteConnected();
                    break;
                case Connection.STATUS_AUTH_FAIL:
                    connStatusBar.setVisibility(View.VISIBLE);
                    connStatusBar.setText(R.string.error_auth_failed);
                    connStatusBar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            IMClient.getInstance(new SiteAddress(currentSite)).makeSureClientAlived(currentSite.toSiteAddress());
                        }
                    });
                    break;
            }
        } else if (connIdentity.equals(ServerConfig.PLATFORM_INDENTIY)) {
            switch (statusType) {
                case Connection.STATUS_AUTH_LOGIN:
                    ZalyTaskExecutor.executeUserTask(TAG, new PlatformLoginTask());
                    ZalyLogUtils.getInstance().info(TAG, "imconnection Auth failed. Need login");
                    break;
            }
        }
    }

    private void updateHeaderBarBubbles() {
        if (!showSessionBubbleForOtherSites && !showContactBubbleForOtherSites) {
            bubble.setVisibility(View.GONE);
        } else if (showSessionBubbleForOtherSites && !showContactBubbleForOtherSites) {
            headerBubbleMessage.setVisibility(View.VISIBLE);
            headerBubbleNewFriend.setVisibility(View.GONE);
            bubble.setVisibility(View.VISIBLE);
        } else if (!showSessionBubbleForOtherSites && showContactBubbleForOtherSites) {
            headerBubbleMessage.setVisibility(View.GONE);
            headerBubbleNewFriend.setVisibility(View.VISIBLE);
            bubble.setVisibility(View.VISIBLE);
        } else if (showSessionBubbleForOtherSites && showContactBubbleForOtherSites) {
            headerBubbleMessage.setVisibility(View.VISIBLE);
            headerBubbleNewFriend.setVisibility(View.VISIBLE);
            bubble.setVisibility(View.VISIBLE);
        }
        if (showContactBubbleForCurrentSite) {
            EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_NEW_FRIEND, null));
        }
    }

    private void updateBottomNaviBubbles(int type) {
        if (this.curIndex != SESSION_TAB_INDEX) {
            bottomBubbleMessage.setVisibility(showSessionBubbleForCurrentSite ?
                    View.VISIBLE : View.INVISIBLE);
        } else {
            bottomBubbleMessage.setVisibility(View.INVISIBLE);
        }
        if (this.curIndex != CONTACT_TAB_INDEX) {
            bottomBubbleContact.setVisibility(showContactBubbleForCurrentSite ?
                    View.VISIBLE : View.INVISIBLE);
        } else {
            bottomBubbleContact.setVisibility(View.INVISIBLE);
        }

        if (currentSite == null) {
            personalUnreadBubble.setVisibility(View.INVISIBLE);
            return;
        }
        String siteLoginId = currentSite.getSiteLoginId();
        if (!StringUtils.isEmpty(siteLoginId) && currentSite.getProtocolVersion() >= SiteConfig.site_login_id_minProtocol) {
            personalUnreadBubble.setVisibility(View.INVISIBLE);
        } else {
            personalUnreadBubble.setVisibility(View.VISIBLE);
        }
    }
}
