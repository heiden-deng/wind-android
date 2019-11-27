package com.windchat.client.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.windchat.client.Configs;
import com.windchat.client.R;
import com.windchat.client.ZalyApplication;
import com.windchat.client.adapter.SiteListAdapter;
import com.windchat.client.api.ApiClient;
import com.windchat.client.bean.Site;
import com.windchat.client.bean.event.AppEvent;
import com.windchat.client.bean.event.SiteEvent;
import com.windchat.client.constant.IntentKey;
import com.windchat.client.maintab.ZalyMainActivity;
import com.windchat.client.mvp.BaseMVPActivity;
import com.windchat.client.mvp.contract.SiteConnListContract;
import com.windchat.client.mvp.presenter.SiteConnListPresenter;
import com.windchat.client.register.RegisterActivity;
import com.windchat.client.util.AnimationUtil;
import com.windchat.client.util.EditTransfor;
import com.windchat.client.util.NetUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.security.RSAUtils;
import com.windchat.client.util.toast.Toaster;
import com.akaxin.proto.core.ConfigProto;
import com.akaxin.proto.site.ApiSiteConfigProto;
import com.blankj.utilcode.util.KeyboardUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.windchat.client.Configs.SUFFIX_USER_TOKEN;

/**
 * 这个 Activity 用于切换站点, 有两种情况需要进入这个 Activity:
 * (1) 登录或新站点. 用户从首页的顶部点击进入这里. 这时用户可以选择已登录的站点, 或者输入站点的的地址登录新的站点;
 * (2) 用户从别的任何地方用 startActivityForResult 请求调用这一 Activity 来切换站点 (比如在添加一个非当前站
 * 点的好友时), 可以根据请求结果RESULT_OK 或 RESULT_CANCEL 来判断是否已经成功切换.
 */

public class SiteConnListActivity extends BaseMVPActivity<SiteConnListContract.View, SiteConnListPresenter> implements SiteConnListContract.View, TextWatcher, MaterialDialog.SingleButtonCallback, SiteListAdapter.SiteListItemListener {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;


    protected static String changeSiteAddress;
    public static final int REQUEST_CODE_SWITCH_SITE = 101;

    @BindView(R.id.back)
    LinearLayout back;
    @BindView(R.id.site_input)
    EditText siteEditText;
    @BindView(R.id.clear_button)
    ImageView clearButton;
    @BindView(R.id.button_connect)
    Button connectButton;
    @BindView(R.id.choose_site_toolbar)
    RelativeLayout siteTitleBar;
    @BindView(R.id.site_desc)
    TextView siteDescTv;
    @BindView(R.id.site_layout)
    Toolbar siteLayout;
    @BindView(R.id.switch_site_desc)
    TextView switchSiteDesc;

    private SiteListAdapter siteListAdapter;
    protected boolean needUnreadNum;
    private Site currentSite;


    private int mode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_list);
        ButterKnife.bind(this);
        initToolBar();
        initData();
    }

    private void initData() {
        this.currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        this.mode = getIntent().getIntExtra(IntentKey.KEY_MODE, IntentKey.MODE_NORMAL);
        switch (mode) {
            case IntentKey.MODE_NORMAL:
                connNormal();
                break;
            case IntentKey.MODE_FOR_RESULT:
                connForResult();
                break;
            case IntentKey.AUTO_MODE_NORMAL:
                connAuto();
                break;
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        siteListAdapter = new SiteListAdapter(this, currentSite);
        siteListAdapter.setItemOnClickListener(this);
        mRecyclerView.setAdapter(siteListAdapter);
        mPresenter.loadCurrentSites(needUnreadNum);
    }

    private void connAuto() {
        final String siteAddress = getIntent().getStringExtra(IntentKey.KEY_CURRENT_SITE_ADDRESS);
        changeSiteAddress = siteAddress;
        String text = "您即将访问" + siteAddress + "站点，是否继续？";
        showDialog(text, "确定", "取消", this);
        textDialog.setCancelable(false);
    }

    private void connForResult() {
        siteLayout.setVisibility(View.GONE);
        siteDescTv.setVisibility(View.GONE);
        switchSiteDesc.setVisibility(View.VISIBLE);
        siteTitleBar.setVisibility(View.VISIBLE);
        setCenterTitle(R.string.title_choose_site);
        needUnreadNum = false;
    }

    private void connNormal() {
//        if (currentSite != null) {
//            String currentSiteAddress = currentSite.getSiteDisplayAddress();
//            siteEditText.setText(currentSiteAddress);
//        }
        siteEditText.addTextChangedListener(this);
        siteEditText.setTransformationMethod(new EditTransfor());
        siteEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String siteAddress = siteEditText.getText().toString().trim();
                    if (TextUtils.isEmpty(siteAddress)) {
                        Toaster.show("请输入站点");
                        return true;
                    }
                    mPresenter.getSiteConfig(getContext(), siteAddress, currentSite);
                    return true;
                }
                return false;
            }
        });
        needUnreadNum = true;
    }

    @Override
    public void onTaskStart(String content) {
        showProgressDialog(content);
    }

    @Override
    public void onTaskFinish() {
        hideProgressDialog();
    }

    @Override
    public void onGetSiteConfigSuccess(Site site) {
        if (site != null && site.getSiteHost() != null && site.getSiteUserId() != null) {
            currentSite = site;
        }
        String userToken = UUID.randomUUID().toString();
        ZalyApplication.getCfgSP().put(site.getSiteIdentity() + SUFFIX_USER_TOKEN, userToken);
        String userPrivateKeyPem = ZalyApplication.getCfgSP().getKey(Configs.USER_PRI_KEY);
        String userPubKeyPem = ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY);
        String devicePubKeyPem = ZalyApplication.getCfgSP().getKey(Configs.DEVICE_PUB_KEY);
        String userSignBase64 = RSAUtils.getInstance().signInBase64String(userPrivateKeyPem, userPubKeyPem);
        String deviceSignBase64 = RSAUtils.getInstance().signInBase64String(userPrivateKeyPem, devicePubKeyPem);
        if (site.getRealNameConfig() == ConfigProto.RealNameConfig.REALNAME_YES_VALUE) {
            mPresenter.getPlatformToken(site);
        }
        mPresenter.loginSite(userSignBase64, deviceSignBase64, userToken, site);
    }

    @Override
    public void onTaskError() {
        hideProgressDialog();
    }

    @Override
    public void onLoginSiteError() {
        if (this.isDestroyed()) return;
        hideProgressDialog();
        Toaster.showInvalidate(getString(R.string.no_such_site));
    }

    @Override
    public void onLoginSiteNeedRegister(final Site site) {
        if (this.isDestroyed()) return;
        hideProgressDialog();
        try {
            final Site newSite = new Site();
            newSite.setSiteHost(site.getSiteHost());
            newSite.setSitePort(site.getSitePort());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final ApiSiteConfigProto.ApiSiteConfigResponse response = ApiClient.getInstance(site).getSiteApi().getSiteInfo();
                        if (response == null) return;
                        newSite.setSiteName(response.getSiteConfig().getSiteName());
                        newSite.setSiteIcon(response.getSiteConfig().getSiteLogo());
                        newSite.setSiteVersion(response.getSiteConfig().getSiteVersion());
                        newSite.setRealNameConfig(response.getSiteConfig().getRealNameConfigValue());
                        newSite.setCodeConfig(response.getSiteConfig().getInviteCodeConfigValue());
                        final String phoneNo = ZalyApplication.getCfgSP().getKey(Configs.PHONE_ID);
                        SiteConnListActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if ((phoneNo == null || phoneNo.length() <= 0) && response.getSiteConfig().getRealNameConfigValue() == ConfigProto.RealNameConfig.REALNAME_YES_VALUE) {
                                    Toaster.showInvalidate("此站点需要实名账户，与当前账户冲突");
                                    return;
                                }
                                RegisterActivity.actionStart(SiteConnListActivity.this, newSite);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
        }
    }

    @Override
    public void onLoginSiteSuccess(Site site) {
        mPresenter.addSiteAndChangeIdentity(site);
    }

    @Override
    public void onConnAndLoginSuccess(Site site) {
        if (this.isDestroyed()) return;
        hideProgressDialog();
        ZalyLogUtils.getInstance().info("EventBus", "onLoginSuccess it will switch site =" + site.toString());
        Bundle bundle = new Bundle();
        bundle.putParcelable(IntentKey.KEY_CURRENT_SITE, site);
        EventBus.getDefault().postSticky(new SiteEvent(SiteEvent.NEW_SITE_KEY, bundle));
        EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_SWITCH_SITE, bundle));
        finishActivityByEvent(SiteEvent.NEW_SITE_KEY);
    }

    @Override
    public void onSwitchSiteSuccess(Site currentSite) {
        if (mode == IntentKey.MODE_FOR_RESULT) {
            Intent intent = new Intent();
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
            setResult(RESULT_OK, intent);
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(IntentKey.KEY_CURRENT_SITE, currentSite);
        Log.i("li1", currentSite.toString());
        EventBus.getDefault().postSticky(new SiteEvent(SiteEvent.SWITCH_SITE_KEY, bundle));
        EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_SWITCH_SITE, bundle));
        finishActivityByEvent(SiteEvent.SWITCH_SITE_KEY);
    }

    @Override
    public void onGetSitesSuccess(List<Site> sites) {
        if (sites == null || sites.size() == 0) {
            return;
        }
        mRecyclerView.setVisibility(View.VISIBLE);
        siteListAdapter.addAllItems(sites);
    }

    private void finishActivityByEvent(int siteEvent) {
        KeyboardUtils.hideSoftInput(this);
        switch (siteEvent) {
            case SiteEvent.SWITCH_SITE_KEY:
                this.finish();
                break;
            case SiteEvent.NEW_SITE_KEY:
                Intent intent = new Intent(this, ZalyMainActivity.class);
                intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                this.startActivity(intent);
                this.finish();
                break;
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        updateClearButton();
    }

    private void updateClearButton() {
        Editable editable = siteEditText.getEditableText();
        connectButton.setEnabled(!editable.toString().isEmpty());
        clearButton.setVisibility(editable.toString().isEmpty() ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        if (which == DialogAction.POSITIVE)
            mPresenter.getSiteConfig(getContext(), changeSiteAddress, currentSite);
    }


    @OnClick({R.id.back, R.id.clear_button, R.id.button_connect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.clear_button:
                siteEditText.setText("");
                break;
            case R.id.button_connect:
                String siteAddress = siteEditText.getText().toString().toLowerCase().trim();
                boolean isNet = NetUtils.getNetInfo();
                if (!isNet) {
                    Toaster.show(R.string.error_conn_nonet);
                    break;
                }
                if (TextUtils.isEmpty(siteAddress)) {
                    Toaster.show("请输入站点");
                    break;
                }
                mPresenter.getSiteConfig(getContext(), siteAddress, currentSite);
                break;
        }
    }

    /**
     * 状态回调方法，子类需重写提供实现，不要求每个子类都重写
     *
     * @param connIdentity
     * @param connType
     * @param statusType
     */
    @Override
    public void onConnectionChange(String connIdentity, int connType, int statusType) {
        super.onConnectionChange(connIdentity, connType, statusType);
        siteListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (mode == IntentKey.MODE_FOR_RESULT) {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        } else {
            AnimationUtil.rightSlideFadeOut(back);
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KeyboardUtils.hideSoftInput(this);
    }

    @Override
    public void onSiteClick(Site site) {
        mPresenter.switchSite(site, currentSite);
    }
}
