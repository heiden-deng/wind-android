package com.windchat.client.activitys;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.windchat.client.R;
import com.windchat.client.bean.Site;
import com.windchat.client.bridge.WebActivity;
import com.windchat.client.constant.IntentKey;
import com.windchat.client.friend.FriendProfileActivity;
import com.windchat.client.mvp.BaseMVPActivity;
import com.windchat.client.mvp.contract.ScanQRCodeContract;
import com.windchat.client.mvp.presenter.ScanQRCodePresenter;
import com.windchat.client.util.NetUtils;
import com.windchat.client.util.UrlUtils;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.toast.Toaster;
import com.blankj.utilcode.util.RegexUtils;

import org.json.JSONObject;

import butterknife.ButterKnife;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;


/**
 * Created by yichao on 2017/10/27.
 */


public class ScanQRCodeActivity extends BaseMVPActivity<ScanQRCodeContract.View, ScanQRCodePresenter> implements ScanQRCodeContract.View, ZBarScannerView.ResultHandler, MaterialDialog.SingleButtonCallback {
    public static final int SCAN_QRCODE_CODE = 22;

    private ZBarScannerView mScannerView;

    protected String spaceKey;
    protected String tskStr;
    protected Site currentSite;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        ButterKnife.bind(this);
        initToolBar();
        ViewGroup contentFrame = findViewById(R.id.content_frame);
        mScannerView = new ZBarScannerView(this);
        contentFrame.addView(mScannerView);
        setCenterTitle("扫一扫");
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        ZalyLogUtils.getInstance().info(TAG, "scan qrcode " + rawResult.getContents());
        String content = rawResult.getContents();
        boolean isNet = NetUtils.getNetInfo();
        if (!isNet) {
            Toaster.show("请稍后再试");
            return;
        }
        if (!StringUtils.isEmpty(content)) {
            if (isJson(content)) {
                doJsonWork(content);
            } else if (RegexUtils.isURL(content)) {
                doHttpWork(content);
            } else {
                finish();
            }
        }
    }

    private void doHttpWork(String content) {
        try {
            Uri uri = Uri.parse(content);
            if (uri.getAuthority().equals("url.akaxin.com")) {
                doAkaxinUri(Uri.parse(uri.getQueryParameter("u")));
            } else {
                Intent intent = new Intent(this, WebActivity.class);
                intent.putExtra(IntentKey.KEY_WEB_URL, content);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }
    }

    private void doAkaxinUri(final Uri uri) {
        String page = uri.getQueryParameter(UrlUtils.KEY_ACTIVITY);
        if (page == null) {
            finish();
            return;
        }
        String auth = uri.getAuthority();
        switch (page) {
            case UrlUtils.JOIN_GROUP_BY_TOKEN:
                showDialog("确定要加入该群聊吗?", "确定", "取消", new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (which == DialogAction.POSITIVE) {
                            String groupSiteId = uri.getQueryParameter(UrlUtils.KEY_GROUP_ID);
                            String token = uri.getQueryParameter(UrlUtils.TOKEN);
                            mPresenter.joinGroupByToken(currentSite, groupSiteId, token);
                        } else {
                            dialog.dismiss();
                            finish();
                        }

                    }
                });
                break;
            case UrlUtils.ACTIVITY_USER_PROFILE:
                String siteUserID = uri.getQueryParameter(UrlUtils.KEY_USER_ID);
                Intent intent = new Intent(ScanQRCodeActivity.this, FriendProfileActivity.class);
                intent.putExtra(IntentKey.KEY_PROFILE_MODE, FriendProfileActivity.MODE_FRIEND_SITE_ID);
                intent.putExtra(IntentKey.KEY_FRIEND_SITE_ID, siteUserID);
                intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(intent);
                finish();
                break;
            case UrlUtils.ACTIVITY_MAIN_TAB_MESSAGE:
                String siteAddress = uri.getAuthority();
                Intent intentMessage = new Intent(this, SiteConnListActivity.class);
                intentMessage.putExtra(IntentKey.KEY_MODE, IntentKey.AUTO_MODE_NORMAL);
                intentMessage.putExtra(IntentKey.KEY_CURRENT_SITE_ADDRESS, siteAddress);
                intentMessage.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(intentMessage);
                finish();
                break;
            default:
                finish();
                break;
        }


    }

    private void doJsonWork(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            if (jsonObject.has("address") && jsonObject.has("tsKey")) {
                spaceKey = jsonObject.optString("address");
                tskStr = jsonObject.optString("tsKey");
                new MaterialDialog.Builder(this)
                        .content(getResources().getString(R.string.auth_identity))
                        .positiveText("确认")
                        .negativeText("取消")
                        .onAny(this)
                        .show();
            } else {
                ZalyLogUtils.getInstance().info(TAG, "scan qrcode  add friend ");
                String globalUserId = jsonObject.getString("data");
                Intent intent = new Intent(ScanQRCodeActivity.this, FriendProfileActivity.class);
                intent.putExtra(IntentKey.KEY_PROFILE_MODE, FriendProfileActivity.MODE_FRIEND_SITE_ID);
                intent.putExtra(IntentKey.KEY_FRIEND_SITE_ID, globalUserId);
                intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(intent);
                finish();
            }

        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, " error msg is " + e.getMessage());
            finish();
        }
    }

    @Override
    public void onTaskStart(String content) {
        showProgressDialog(content);
    }

    @Override
    public void onTaskFinish() {
        hideProgressDialog();
        finish();
    }

    @Override
    public void onJoinSuccess() {
        finish();
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        if (which == DialogAction.POSITIVE)
            mPresenter.sendTempSpaceContent(spaceKey, tskStr);
        else
            dialog.dismiss();
    }

    public boolean isJson(String content) {
        try {
            JSONObject jsonStr = new JSONObject(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

