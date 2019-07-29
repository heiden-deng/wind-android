package com.akaxin.client.bridge;

import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.akaxin.client.R;
import com.akaxin.client.bean.Site;
import com.akaxin.client.chat.LoadPluginWebViewListener;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.util.JsWebViewUtil;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.PluginProto;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

import me.iwf.photopicker.PhotoPicker;

/**
 * Created by yichao on 2017/11/13.
 */

public class PluginWebActivity extends BaseActivity implements LoadPluginWebViewListener {

    public static final String TAG = "PluginWebActivity";
    public static final int REQUEST_FINE_LOCATION = 111;
    public static final String KEY_WEB_VIEW_DATA = "key_data";
    public static final String REFERER = "referer";

    public static final String IS_ADD_COOKIE = "is_add_cookie";

    private WebView mWebView;
    private String request_image_upload_callback;

    private PluginProto.Plugin pluginProfile;
    private Stack<ZalyRequest> backStack;

    private ZalyRequest currentRequest;
    public String refererUrl;
    public boolean isAddCookie;

    JsWebViewUtil jsWebViewUtil;

    private Site currentSite;

    @Override
    public int getResLayout() {
        return R.layout.activity_plungin_web;
    }

    @Override
    public void initView() {
        mWebView = findViewById(R.id.webview);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                setCenterTitle(title);
            }
        });

        navBackStack();
    }


    /**
     * navBar 回退栈
     */
    public void navBackStack() {
        if (navBackLayout != null) {
            navBackLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (jsWebViewUtil.mWebView != null) {
                        if (jsWebViewUtil.mWebView.canGoBack()) {
                            jsWebViewUtil.mWebView.goBack();
                        } else {
                            onBackPressed();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {
        pluginProfile = null;
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        try {
            byte[] data = getIntent().getByteArrayExtra(KEY_WEB_VIEW_DATA);
            pluginProfile = PluginProto.Plugin.parseFrom(data);
            refererUrl = getIntent().getStringExtra(REFERER);
            isAddCookie = getIntent().getBooleanExtra(IS_ADD_COOKIE, false);

        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            Toaster.show("请稍候再试");
            finish();
        }

        if (StringUtils.isEmpty(pluginProfile.getId())) {
            Toaster.show("请稍候再试");
            finish();
            return;
        }
        //加载默认落地页
        loadRootPage();
    }

    /**
     * <pre>
     * 加载扩展的落地页面，规则为：
     *  1.如果获取的地址http:// or https:// 开头，则直接通过url加载界面
     *  2.如果不满足1条件，则通过代理加载模式加载界面
     *  </pre>
     */
    private void loadRootPage() {
        currentRequest = new ZalyRequest(true);
        jsWebViewUtil = new JsWebViewUtil(currentSite, getContext(), this, mWebView, pluginProfile, refererUrl, isAddCookie);
        jsWebViewUtil.setLoadWebViewListener(this);
        jsWebViewUtil.onLoadData();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            } else {
                onBackPressed();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void startPhotoPicker() {
        PhotoPicker.PhotoPickerBuilder builder = PhotoPicker.builder()
                .setPhotoCount(1)
                .setShowCamera(true)
                .setShowGif(false)
                .setPreviewEnabled(true);
        startActivityForResult(builder.getIntent(PluginWebActivity.this), PhotoPicker.REQUEST_CODE);
    }

    @Override
    public void quitWebView() {
        finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PhotoPicker.REQUEST_CODE:
                    if (data != null) {
                        ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                        if (photos == null || photos.size() == 0) {
                            Toaster.showInvalidate("请稍候再试");
                            return;
                        }
                        Uri sourceUri = Uri.fromFile(new File(photos.get(0)));
                        jsWebViewUtil.uploadImage(sourceUri);
                    }
                    break;
            }
        }
    }

    @Override
    public void loadWebSuccess() {
        hideProgress();
    }

    @Override
    public void loadWebFailed() {
        hideProgress();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null && mWebView.getParent() != null) {
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.stopLoading();
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        mWebView.onResume();
        super.onResume();
    }
}
