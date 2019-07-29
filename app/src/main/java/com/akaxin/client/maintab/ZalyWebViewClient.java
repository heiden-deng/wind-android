package com.akaxin.client.maintab;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


/**
 * Created by yichao on 2018/1/13.
 */

public class ZalyWebViewClient extends WebViewClient {

    public static String TAG = ZalyWebViewClient.class.getSimpleName();
    public String referer;

    public ZalyWebViewClient(String referer){
        this.referer = referer;

    }
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        FileInputStream input;
        String url = request.getUrl().toString();
        String key = "http://akaxin/img";
        if (url.contains(key)) {
            String imgPath = url.replace(key, "");
            try {
                input = new FileInputStream(new File(imgPath.trim()));
                WebResourceResponse response = new WebResourceResponse("image/jpg", "UTF-8", input);
                return response;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return super.shouldInterceptRequest(view, request);
    }
}
