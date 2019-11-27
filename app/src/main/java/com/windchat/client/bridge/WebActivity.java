package com.windchat.client.bridge;

import android.os.Build;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.windchat.client.R;
import com.windchat.client.constant.IntentKey;
import com.windchat.client.maintab.BaseActivity;
import com.windchat.client.util.data.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yichao on 2018/1/19.
 */

public class WebActivity extends BaseActivity {


    @BindView(R.id.web_parent)
    WebView webParent;

    @Override
    public int getResLayout() {
        return R.layout.activity_web;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        String url = getIntent().getStringExtra(IntentKey.KEY_WEB_URL);
        if (!StringUtils.isEmpty(url)) {
            webParent.loadUrl(url);
        }
        webParent.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO 设置不加载外面的浏览器
                view.loadUrl(url);
                return true;
            }
        });
        webParent.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                setCenterTitle(title);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webParent.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        WebSettings settings = webParent.getSettings();
        settings.setSupportZoom(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
    }


    @Override
    public void initEvent() {

    }

    @Override
    public void initPresenter() {

    }


    @Override
    public void onLoadData() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webParent != null) {
            webParent.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webParent.clearHistory();
            ((ViewGroup) webParent.getParent()).removeView(webParent);
            webParent.destroy();
            webParent = null;
        }
        super.onDestroy();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webParent.canGoBack()) {
                webParent.goBack();
                return true;
            } else {
                onBackPressed();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
