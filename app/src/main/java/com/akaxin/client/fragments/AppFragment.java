package com.akaxin.client.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.akaxin.client.R;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bean.event.AppEvent;
import com.akaxin.client.chat.LoadPluginWebViewListener;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.maintab.ZalyWebViewClient;
import com.akaxin.client.mvp.MVPBaseFragment;
import com.akaxin.client.mvp.contract.AppContract;
import com.akaxin.client.mvp.presenter.AppPresenter;
import com.akaxin.client.plugin.PluginActivity;
import com.akaxin.client.plugin.adapter.PluginPagerAdapter;
import com.akaxin.client.util.JsWebViewUtil;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.client.view.HomeWebView;
import com.akaxin.proto.core.PluginProto;
import com.akaxin.proto.site.ApiPluginListProto;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.iwf.photopicker.PhotoPicker;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Mr.kk on 2018/6/26.
 * This Project was client-android
 */

public class AppFragment extends MVPBaseFragment<AppContract.View, AppPresenter> implements AppContract.View
        , PluginPagerAdapter.OnAppClickListener, LoadPluginWebViewListener, SwipeRefreshLayout.OnRefreshListener, View.OnScrollChangeListener {
    @BindView(R.id.fragment_app_plugin)
    ViewPager fragmentAppPlugin;
    @BindView(R.id.fragment_app_web)
    HomeWebView fragmentAppWeb;
    @BindView(R.id.plugin_list_content)
    CardView pluginListContent;
    @BindView(R.id.webprogress)
    ProgressBar webprogress;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.plugin_bar_layout)
    AppBarLayout plugin_bar_layout;
    private PluginPagerAdapter pluginsPagerAdapter;
    private static final int MAX_NUM_PLUGINS = 5;
    private Site currentSite;
    JsWebViewUtil jsWebViewUtil;
    Unbinder unbinder;

    /**
     * 传入需要的参数，设置给arguments
     *
     * @param site
     * @return
     */
    public static AppFragment getObject(Site site) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(IntentKey.KEY_CURRENT_SITE, site);
        AppFragment appFragment = new AppFragment();
        appFragment.setArguments(bundle);
        return appFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frgment_app, container, false);
        unbinder = ButterKnife.bind(this, view);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            currentSite = bundle.getParcelable(IntentKey.KEY_CURRENT_SITE);
        }
        pluginsPagerAdapter = new PluginPagerAdapter(getActivity(), MAX_NUM_PLUGINS, currentSite);
        fragmentAppPlugin.setAdapter(pluginsPagerAdapter);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(this);
        pluginsPagerAdapter.setOnAppClickListener(this);
//        plugin_bar_layout.addOnOffsetChangedListener(this);
        mPresenter.getPluginsOnSite(currentSite, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fragmentAppWeb.setOnScrollChangeListener(this);
        }
        return view;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactEvent(AppEvent event) {
        switch (event.getAction()) {
            case AppEvent.ACTION_SWITCH_SITE:
                currentSite = event.getData().getParcelable(IntentKey.KEY_CURRENT_SITE);
                pluginsPagerAdapter.removePlugins();
                fragmentAppWeb.clearHistory();
                fragmentAppWeb.clearCache(true);
                //   fragmentAppWeb.loadUrl("about:blank");
                mPresenter.getPluginsOnSite(currentSite, false);
                break;
            case AppEvent.ACTION_TO_TOP_APP_WEB:
                break;

        }
    }

    @Override
    public void onAppClick(View v, int position, List<PluginProto.Plugin> pluginProfiles) {
        PluginProto.Plugin item = pluginProfiles.get(position);
        if (pluginsPagerAdapter.getAllCount() > MAX_NUM_PLUGINS && position == MAX_NUM_PLUGINS - 1) {
            Intent intent = new Intent(getActivity(), PluginActivity.class);
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
            startActivity(intent);
        } else {
            showApp(item, true);
        }
    }

    public void showApp(PluginProto.Plugin plugin, boolean isAddCookie) {
        String referrer = SiteConfig.PLUGIN_HOME_REFERER.replace("siteAddress", currentSite.getHostAndPort());
        jsWebViewUtil = new JsWebViewUtil(currentSite, getContext(), getActivity(), fragmentAppWeb, plugin, referrer, isAddCookie);
        jsWebViewUtil.setLoadWebViewListener(this);
        jsWebViewUtil.onLoadData();
        fragmentAppWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                webprogress.setProgress(newProgress);
                webprogress.setVisibility(View.VISIBLE);
                if (newProgress == 100) {
                    webprogress.setVisibility(View.GONE);
                }

            }

        });

        fragmentAppWeb.setWebViewClient(new ZalyWebViewClient(referrer) {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                //    fragmentAppWeb.loadUrl("file:///android_asset/404.html");
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                //  fragmentAppWeb.loadUrl("file:///android_asset/404.html");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });


    }


    @Override
    public void loadWebSuccess() {

    }

    @Override
    public void loadWebFailed() {

    }


    @Override
    public void startPhotoPicker() {
        PhotoPicker.PhotoPickerBuilder builder = PhotoPicker.builder()
                .setPhotoCount(1)
                .setShowCamera(true)
                .setShowGif(false)
                .setPreviewEnabled(true);
        startActivityForResult(builder.getIntent(getActivity()), PhotoPicker.REQUEST_CODE);
    }

    @Override
    public void quitWebView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fragmentAppWeb.clearHistory();
                fragmentAppWeb.clearCache(true);
                fragmentAppWeb.loadUrl("about:blank");
                fragmentAppWeb.setVisibility(View.GONE);
            }
        });

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
    public void onRefresh() {
        pluginsPagerAdapter.removePlugins();
        mPresenter.getPluginsOnSite(currentSite, true);
//        fragmentAppWeb.clearHistory();
//        fragmentAppWeb.clearCache(true);
//        fragmentAppWeb.loadUrl("about:blank");
    }
//
//    @Override
//    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//        if (verticalOffset >= 0) {
//            swipeRefreshLayout.setEnabled(false);
//        } else {
//            swipeRefreshLayout.setEnabled(false);
//        }
//    }

    @Override
    public void onGetPluginsOnSiteSuccess(ApiPluginListProto.ApiPluginListResponse apiPluginListResponse, boolean isRefresh) {
        if (apiPluginListResponse.getPluginList() != null &&
                apiPluginListResponse.getPluginCount() > 0) {
            pluginsPagerAdapter.updatePlugins(apiPluginListResponse.getPluginList(), currentSite);
            if (!isRefresh)
                showApp(apiPluginListResponse.getPluginList().get(0), true);
            pluginListContent.setVisibility(View.VISIBLE);
            fragmentAppWeb.setVisibility(View.VISIBLE);
            webprogress.setVisibility(View.VISIBLE);
        } else {
            EventBus.getDefault().postSticky(new AppEvent(AppEvent.NO_PLUGIN, null));
            pluginListContent.setVisibility(View.GONE);
            fragmentAppWeb.setVisibility(View.GONE);
            webprogress.setVisibility(View.GONE);
            pluginsPagerAdapter.removePlugins();
        }
    }

    @Override
    public void onGetPluginsOnSiteError() {
        pluginsPagerAdapter.removePlugins();
    }

    @Override
    public void onTaskStart(String content) {

    }

    @Override
    public void onTaskFinish() {
        swipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (scrollY == 0)
            swipeRefreshLayout.setEnabled(true);
        else
            swipeRefreshLayout.setEnabled(false);
//        ZalyLogUtils.getInstance().info("onScroll:", scrollY + "");
//        ZalyLogUtils.getInstance().info("webView:", fragmentAppWeb.getScrollY() + "");
    }
}
