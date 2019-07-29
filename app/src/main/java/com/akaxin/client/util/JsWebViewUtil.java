package com.akaxin.client.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bridge.ZalyRequest;
import com.akaxin.client.chat.LoadPluginWebViewListener;
import com.akaxin.client.chat.view.impl.BaseMsgActivity;
import com.akaxin.client.constant.HttpUrl;
import com.akaxin.client.constant.PackageSign;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.im.files.IMFileUtils;
import com.akaxin.client.jump.ZalyGotoPageByPlugin;
import com.akaxin.client.maintab.ZalyWebViewClient;
import com.akaxin.client.plugin.PluginUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.file.UploadFileUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.FileProto;
import com.akaxin.proto.core.PluginProto;
import com.akaxin.proto.site.ApiFileUploadProto;
import com.akaxin.proto.site.ApiPluginPageProto;
import com.akaxin.proto.site.ApiPluginProxyProto;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import static android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK;
import static android.webkit.WebSettings.LOAD_DEFAULT;

/**
 * Created by zhangjun on 30/05/2018.
 */

public class JsWebViewUtil {
    public static String TAG = JsWebViewUtil.class.getSimpleName();
    public String request_image_upload_callback;
    public Stack<ZalyRequest> backStack;
    public ZalyRequest currentRequest;
    public static final int REQUEST_FINE_LOCATION = 111;
    public String refererUrl;
    public Context mContext;
    public WebView mWebView;
    public PluginProto.Plugin pluginProfile;

    public Activity activity;
    public LoadPluginWebViewListener loadPluginWebViewListener;
    public boolean isAddCookie = false;
    public Site currentSite;

    PluginWebAppInterface pluginWebAppInterface;

    public void setLoadWebViewListener(LoadPluginWebViewListener loadPluginWebViewListener) {
        this.loadPluginWebViewListener = loadPluginWebViewListener;
    }

    public JsWebViewUtil(Site site, Context mContext, Activity activity, WebView mWebView, PluginProto.Plugin pluginProfile, String refererUrl, boolean isAddCookie) {
        backStack = new Stack<>();
        this.currentSite = site;

        this.mContext = mContext;
        this.mWebView = mWebView;
        this.activity = activity;

        this.isAddCookie = isAddCookie;
        this.refererUrl = StringUtils.changeReferer(refererUrl);
        this.pluginProfile = pluginProfile;

        setWebViewInfo(mWebView);
    }

    public String getUserAgent(WebView mWebView) {
        String versionName = "";
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }
        return mWebView.getSettings().getUserAgentString() + " akaxin-" + versionName;
    }

    /**
     * <pre>
     * 加载扩展的落地页面，规则为：
     *  1.如果获取的地址http:// or https:// 开头，则直接通过url加载界面
     *  2.如果不满足1条件，则通过代理加载模式加载界面
     *  </pre>
     */
    public void loadRootPage() {
        currentRequest = new ZalyRequest(true);
        /////记录当前页面的url
        if (pluginProfile != null && StringUtils.isNotEmpty(pluginProfile.getUrlPage())) {
            if (pluginProfile.getUrlPage().startsWith(HttpUrl.HTTP_PREFIX) || pluginProfile.getUrlPage().startsWith(HttpUrl.HTTPS_PREFIX)) {
                mWebView.loadUrl(pluginProfile.getUrlPage());
            } else {
                ZalyTaskExecutor.executeUserTask(TAG, new GetPageContentTask(pluginProfile));
            }
        } else {
            Toaster.show("解析扩展地址失败");
        }
    }

    public void setWebViewInfo(WebView mWebView) {
        final String userAgentStr = getUserAgent(mWebView);
        mWebView.setWebViewClient(new ZalyWebViewClient(refererUrl));
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setCacheMode(NetUtils.getNetInfo() ? LOAD_DEFAULT : LOAD_CACHE_ELSE_NETWORK);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setUseWideViewPort(true); // 关键点
        webSettings.setAllowFileAccess(true); // 允许访问文件
        webSettings.setJavaScriptEnabled(true);  //这句话必须保留。。不解释
        webSettings.setDomStorageEnabled(true);
        webSettings.setUserAgentString(userAgentStr);
        ////debug模式
        if (PackageSign.AKAXIN_PACKAGE_DEBUG == PackageSign.getPackage()) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        try {
            pluginWebAppInterface = new PluginWebAppInterface(mContext, pluginProfile.getId());
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
            pluginWebAppInterface = new PluginWebAppInterface(mContext, null);
        }
        mWebView.addJavascriptInterface(pluginWebAppInterface, "Android");
        currentRequest = new ZalyRequest(true);
    }

    public void setMsgPluginInfo() {
        this.mWebView = BaseMsgActivity.getMsgPluginWebView();
        this.loadPluginWebViewListener = BaseMsgActivity.baseMsgActivity;

        mWebView.post(new Runnable() {
            @Override
            public void run() {
                setWebViewInfo(mWebView);
            }
        });
    }

    public void openMsgPluginUrl() {
        this.mWebView = BaseMsgActivity.getMsgPluginWebView();
        this.loadPluginWebViewListener = BaseMsgActivity.baseMsgActivity;

        mWebView.post(new Runnable() {
            @Override
            public void run() {
                PluginUtils.gotoWebActivity(currentSite, mContext, pluginProfile, refererUrl, true);
            }
        });
    }

    public void onLoadData() {
        /////记录当前页面的url
        if (pluginProfile != null && StringUtils.isNotEmpty(pluginProfile.getId())) {
            pluginWebAppInterface = new PluginWebAppInterface(mContext, pluginProfile.getId());
            mWebView.addJavascriptInterface(pluginWebAppInterface, "Android");
            if (pluginProfile.getUrlPage().startsWith(HttpUrl.HTTP_PREFIX) || pluginProfile.getUrlPage().startsWith(HttpUrl.HTTPS_PREFIX)) {
                ZalyLogUtils.getInstance().info(TAG, " ");
                if (isAddCookie) {
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.setAcceptCookie(true);
                    cookieManager.removeSessionCookie();//
                    String value = SiteConfig.AKAXIN_SITE_SESSION_ID + "=" + pluginProfile.getEncryptedSessionIdBase64();
                    cookieManager.setCookie(pluginProfile.getUrlPage(), value);
                }
                final Map<String, String> extraHeaders = new HashMap<>();

                if (("4.4.3".equals(android.os.Build.VERSION.RELEASE)) || ("4.4.4".equals(android.os.Build.VERSION.RELEASE))) {
                    extraHeaders.put("Referer ", refererUrl);
                } else {
                    extraHeaders.put("Referer", refererUrl);
                }
                ZalyLogUtils.getInstance().info(TAG, "success" + extraHeaders.toString());

                mWebView.loadUrl(pluginProfile.getUrlPage(), extraHeaders);
                loadPluginWebViewListener.loadWebSuccess();
            } else {
                ZalyTaskExecutor.executeUserTask(TAG, new GetPageContentTask(pluginProfile));
            }
        } else {
            loadPluginWebViewListener.loadWebFailed();
        }
    }

    public void loadDataByMsgContent(String msgContent) {
        mWebView.loadDataWithBaseURL(null, msgContent, "text/html; charset=UTF-8", "UTF-8", null);
    }

    public void requestHtml(String apiName, String params, boolean isHasPushBackStack) {
        ZalyLogUtils.getInstance().info(TAG, " mWebView isHasPushBackStack ==" + isHasPushBackStack);
        ZalyTaskExecutor.executeUserTask(TAG, pluginWebAppInterface.new PluginProxyHtmlTask(apiName, params, isHasPushBackStack));
    }

    public void uploadImage(Uri imageUri) {
        ZalyTaskExecutor.executeUserTask(TAG, pluginWebAppInterface.new UploadImageTask(imageUri));
    }

    /**
     * 通过站点代理，获取网页内容
     * api.plugin.page 接口获取界面html内容，加载到本地的webview进行渲染
     */
    class GetPageContentTask extends ZalyTaskExecutor.Task<Void, Void, ApiPluginPageProto.ApiPluginPageResponse> {
        private final String TAG = GetPageContentTask.class.getSimpleName();
        private PluginProto.Plugin pluginProfile;

        public GetPageContentTask(PluginProto.Plugin profile) {
            this.pluginProfile = profile;
        }

        @Override
        protected ApiPluginPageProto.ApiPluginPageResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getPluginApi().getPage(pluginProfile.getId(), refererUrl);
        }

        @Override
        protected void onTaskSuccess(ApiPluginPageProto.ApiPluginPageResponse apiPluginPageResponse) {
            super.onTaskSuccess(apiPluginPageResponse);
            if (apiPluginPageResponse.getData().isEmpty()) {
                loadPluginWebViewListener.loadWebFailed();
            }
            byte[] pageData = apiPluginPageResponse.getData().toByteArray();
            ZalyLogUtils.getInstance().info(TAG, "mWebView GetPageContentTask ===" + pageData);
            String url = pluginProfile.getApiUrl() + pluginProfile.getUrlPage();
            ZalyLogUtils.getInstance().info(TAG, "mWebView GetPageContentTask url===" + url);
            mWebView.loadDataWithBaseURL(url, new String(pageData), "text/html; charset=UTF-8", null, null);
            loadPluginWebViewListener.loadWebSuccess();
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
            loadPluginWebViewListener.loadWebFailed();
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyApiException) {
            ZalyLogUtils.getInstance().exceptionError(zalyApiException);
            loadPluginWebViewListener.loadWebFailed();
        }
    }


    /**
     * JS Interface
     */
    class PluginWebAppInterface {

        private static final String TAG = "PluginWebAppInterface";

        private Context mContext;
        private String pluginId;

        public PluginWebAppInterface(Context mContext, String pluginId) {
            this.mContext = mContext;
            this.pluginId = pluginId;
        }

        /**
         * Show a toast from the web page
         */
        @JavascriptInterface
        public void showToast(String toast) {
            ZalyLogUtils.getInstance().logJSInterface(TAG, "showToast", "toast:" + toast);
            Toaster.showInvalidate(toast, 10);
        }

        //Android.zaly_request(, , ,)
        @JavascriptInterface
        public void requestPost(String api_name, String params, String callback) {
            ZalyLogUtils.getInstance().logJSInterface(TAG, "zaly_request", "api_name:" + api_name + ", params:" + params + ", callback:" + callback);
            ZalyTaskExecutor.executeUserTask(TAG, new PluginWebAppInterface.PluginProxyTask(api_name, params, callback));
        }

        //Android.request_proxy_html(api_name, params)
        @JavascriptInterface
        public void requestPage(String api_name, String params) {
            ZalyLogUtils.getInstance().logJSInterface(TAG, "request_proxy_html", "api_name:" + api_name + ", params:" + params);
            try {
                currentRequest = new ZalyRequest(api_name, params);
                if (currentRequest != null) {
                    backStack.push(currentRequest);
                    ZalyLogUtils.getInstance().info(TAG, " current request == " + currentRequest.apiName);
                    ZalyLogUtils.getInstance().info(TAG, " current request backStack == " + backStack.toString());

                }
            } catch (Exception e) {
                ZalyLogUtils.getInstance().exceptionError(e);
            }
            ZalyTaskExecutor.executeUserTask(TAG, new PluginProxyHtmlTask(api_name, params, true));
        }

        @JavascriptInterface
        public void imageUpload(String callback) {
            ZalyLogUtils.getInstance().logJSInterface(TAG, "request_image_upload", "callback:" + callback);
            request_image_upload_callback = callback;
            loadPluginWebViewListener.startPhotoPicker();
        }

        @JavascriptInterface
        public void gotoPage(String url) {
            ZalyLogUtils.getInstance().logJSInterface(TAG, "zaly_request", "protocol:" + url);
            ZalyGotoPageByPlugin.executeGotoPage(url, false);
        }

        @JavascriptInterface
        public void quit() {
            loadPluginWebViewListener.quitWebView();
        }

        @JavascriptInterface
        public void refreshCurrentPage() {
            ZalyLogUtils.getInstance().logJSInterface(TAG, "refresh_page", "");
            try {
                if (currentRequest == null) {
                    return;
                }
            } catch (Exception e) {
                ZalyLogUtils.getInstance().exceptionError(e);
            }

            ZalyTaskExecutor.executeUserTask(TAG, new PluginProxyHtmlTask(currentRequest.apiName, currentRequest.apiParams, false));
        }

        @JavascriptInterface
        public void imageDownload(final String imageId, final String callback) {
            ZalyLogUtils.getInstance().logJSInterface(TAG, "request_image_download", "imageId:" + imageId + ", callback:" + callback);
            if (StringUtils.isEmpty(imageId)) {
                return;
            }
            final String imageFilePath = Configs.getImgDir().getAbsolutePath() + "/" + imageId;
            File imageFile = new File(imageFilePath);
            if (imageFile.exists() && imageFile.length() > 0) {
                String callbackStr = "javascript:" + callback + "(1, '" + imageId + "','" + imageFilePath + "')";
                callBackJS(callbackStr);
                return;
            }
            UploadFileUtils.downloadFile(imageId, imageFilePath, FileProto.FileType.USER_PORTRAIT, new UploadFileUtils.DownloadFileListener() {
                @Override
                public void onDownloadStartInBackground() {

                }

                @Override
                public void onDownloadCompleteInBackground(String fileId, String filePath) {

                }

                @Override
                public void onDownloadSuccess(String fileId, String filePath) {
                    callBackJS("javascript:" + callback + "(1, '" + imageId + "','" + filePath + "')");
                }

                @Override
                public void onDownloadFail(Exception e) {
                    callBackJS("javascript:" + callback + "(0, '" + imageId + "','')");
                }
            }, currentSite);
        }

        /**
         * 获取当前所在的经纬度. 其中 longitude 为经度, latitude 为纬度.
         *
         * @param callback
         */
        @JavascriptInterface
        public void getLatLong(final String callback) {
            final LocationListener locationListener = new LocationListener() {
                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }

                @Override
                public void onLocationChanged(Location location) {
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    ZalyLogUtils.getInstance().info(TAG, "long: " + longitude + ", lat: " + latitude);
                    callBackJS(String.format(Locale.getDefault(), "javascript:%s(%.4f, %.4f)", callback, longitude, latitude));
                }
            };
            if (ContextCompat.checkSelfPermission(mContext,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationManager lm = (LocationManager) ZalyApplication.getContext().getSystemService(Context.LOCATION_SERVICE);
                if (lm != null)
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            2000,
                            10,
                            locationListener);
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION);
                // TODO: 一般情况不会走到这里, 因为在用户进应用时已经授予权限. 如果真的到了这里, 需要在获取权限后重新调一次这个方法.
            }
        }


        /**
         * 给WebView同步Cookie
         *
         * @param context 上下文
         * @param url     可以使用[domain][host]
         */
        private void syncCookie(Context context, String url) {
            CookieSyncManager.createInstance(context);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.removeSessionCookie();// 移除旧的[可以省略]
            String value = SiteConfig.AKAXIN_SITE_SESSION_ID + "=" + pluginProfile.getEncryptedSessionIdBase64();
            cookieManager.setCookie(url, value);
            CookieSyncManager.getInstance().sync();// To get instant sync instead of waiting for the timer to trigger, the host can call this.
        }

        /**
         * 上传图片任务
         */
        class UploadImageTask extends ZalyTaskExecutor.Task<Void, Void, ApiFileUploadProto.ApiFileUploadResponse> {

            private Uri imageUri;

            public UploadImageTask(Uri imageUri) {
                this.imageUri = imageUri;
            }

            @Override
            protected void onPreTask() {
                super.onPreTask();
            }

            @Override
            protected ApiFileUploadProto.ApiFileUploadResponse executeTask(Void... voids) throws Exception {
                File file = new File(imageUri.getPath());
                byte[] bytesArray = new byte[(int) file.length()];
                try {
                    FileInputStream fis = new FileInputStream(file);
                    fis.read(bytesArray); //read file into bytes[]
                    fis.close();
                } catch (Exception e) {
                    throw e;
                }
                byte[] resizedImage = IMFileUtils.resizeImageByWidth(bytesArray, 256);
                return IMFileUtils.uploadImg(currentSite, resizedImage, FileProto.FileType.USER_PORTRAIT);
            }

            @Override
            protected void onTaskSuccess(ApiFileUploadProto.ApiFileUploadResponse apiFileUploadResponse) {
                super.onTaskSuccess(apiFileUploadResponse);
                if (apiFileUploadResponse == null) {
                    Toaster.showInvalidate("上传失败，请稍候再试");
                    return;
                }
                callBackJS("javascript:" + request_image_upload_callback + "(1, '" + apiFileUploadResponse.getFileId() + "','" + imageUri.getPath() + "')");
            }

            @Override
            protected void onTaskError(Exception e) {
                super.onTaskError(e);
                callBackJS("javascript:" + request_image_upload_callback + "(0, '', '')");
            }

            @Override
            protected void onTaskFinish() {
                super.onTaskFinish();
            }
        }


        private void callBackJS(final String callback) {
            if (mWebView != null) {
                Logger.w(TAG, "callback:  " + callback);
                ZalyMainThreadExecutor.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(callback);
                    }
                });
            } else {
                Logger.e(TAG, "callback fail:  " + callback);
            }
        }

        /**
         * 发出代理请求
         */
        class PluginProxyTask extends ZalyTaskExecutor.Task<Void, Void, ApiPluginProxyProto.ApiPluginProxyResponse> {
            public final String TAG = PluginProxyTask.class.getSimpleName();
            String apiName;
            String params;
            String callBack;

            public PluginProxyTask(String apiName, String params, String callBack) {
                this.apiName = apiName;
                this.params = params;
                this.callBack = callBack;
            }

            @Override
            protected ApiPluginProxyProto.ApiPluginProxyResponse executeTask(Void... voids) throws Exception {
                return ApiClient.getInstance(currentSite).getPluginApi().pluginProxy(apiName, params, pluginProfile.getId(), refererUrl);
            }

            @Override
            protected void onTaskSuccess(ApiPluginProxyProto.ApiPluginProxyResponse apiPluginProxyResponse) {
                super.onTaskSuccess(apiPluginProxyResponse);
                String response = apiPluginProxyResponse.getData().toString();

                byte[] data = new byte[apiPluginProxyResponse.getData().size()];
                for (int i = 0; i < data.length; i++) {
                    data[i] = apiPluginProxyResponse.getData().byteAt(i);
                }
                String resultStr = new String(data);
                callBackJS("javascript:" + callBack + "('" + resultStr + "')");
            }

            @Override
            protected void onTaskError(Exception e) {
                ZalyLogUtils.getInstance().exceptionError(e);
                loadPluginWebViewListener.loadWebFailed();
            }

            @Override
            protected void onAPIError(ZalyAPIException zalyApiException) {
                ZalyLogUtils.getInstance().apiError(TAG, zalyApiException);
                loadPluginWebViewListener.loadWebFailed();
            }

        }

        /**
         * 发出代理请求并填充webView
         */
        public class PluginProxyHtmlTask extends ZalyTaskExecutor.Task<Void, Void, ApiPluginProxyProto.ApiPluginProxyResponse> {
            public final String TAG = PluginProxyHtmlTask.class.getSimpleName();

            String apiName;
            String params;
            boolean hasPushBackStack;

            public PluginProxyHtmlTask(String apiName, String params, boolean hasPushBackStack) {
                this.apiName = apiName;
                this.params = params;
                this.hasPushBackStack = hasPushBackStack;
            }

            @Override
            protected ApiPluginProxyProto.ApiPluginProxyResponse executeTask(Void... voids) throws Exception {
                return ApiClient.getInstance(currentSite).getPluginApi().pluginProxy(apiName, params, pluginProfile.getId(), refererUrl);
            }

            @Override
            protected void onTaskSuccess(ApiPluginProxyProto.ApiPluginProxyResponse apiPluginProxyResponse) {
                super.onTaskSuccess(apiPluginProxyResponse);
                byte[] data = apiPluginProxyResponse.getData().toByteArray();
                String loadData = new String(data);
                if (mWebView != null) {
                    if (loadData.length() > 10 && (loadData.substring(0, 10).contains("http://") || loadData.substring(0, 10).contains("https://"))) {
                        Map<String, String> additionalHttpHeaders = new HashMap<String, String>();
                        additionalHttpHeaders.put("Referer", refererUrl);
                        final Map<String, String> extraHeaders = new HashMap<>();

                        if (("4.4.3".equals(android.os.Build.VERSION.RELEASE)) || ("4.4.4".equals(android.os.Build.VERSION.RELEASE))) {
                            extraHeaders.put("Referer ", refererUrl);
                        } else {
                            extraHeaders.put("Referer", refererUrl);
                        }
                        mWebView.loadUrl(loadData, additionalHttpHeaders);
                    } else {
                        String url = pluginProfile.getApiUrl() + apiName;
                        mWebView.loadDataWithBaseURL(url, loadData, "text/html; charset=UTF-8", "UTF-8", null);
                    }
                    loadPluginWebViewListener.loadWebSuccess();
                }
            }

            @Override
            protected void onTaskError(Exception e) {
                if (hasPushBackStack && backStack != null) {
                    backStack.pop();
                }
                ZalyLogUtils.getInstance().exceptionError(e);
                loadPluginWebViewListener.loadWebFailed();
            }

            @Override
            protected void onAPIError(ZalyAPIException zalyApiException) {
                if (hasPushBackStack && backStack != null) {
                    backStack.pop();
                }
                ZalyLogUtils.getInstance().exceptionError(zalyApiException);
                loadPluginWebViewListener.loadWebFailed();
            }
        }
    }
}


