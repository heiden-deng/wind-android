package com.windchat.client.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.windchat.client.bean.Message;
import com.windchat.client.util.log.ZalyLogUtils;
import com.blankj.utilcode.util.ConvertUtils;

import java.util.Map;

/**
 * Created by Mr.kk on 2018/6/6.
 * This Project was client-android
 */

public class ZalyWebView extends WebView {

    public static String TAG = ZalyWebView.class.getSimpleName();

    public ZalyWebView(Context context) {
        super(context);
    }

    public ZalyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZalyWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ZalyWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //return super.onTouchEvent(ev);
        return false;
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        super.loadUrl(url, additionalHttpHeaders);
    }

    public void setSize(Message msg) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();

        int minWidth = ConvertUtils.dp2px(60);//180
        int maxWidth = dm.widthPixels - ConvertUtils.dp2px(100);;//780

        int minHeight = minWidth;//120
        int maxHeight = maxWidth;//1620

        int msgWidth = ConvertUtils.dp2px(msg.getMsgWidth());//900

        int msgHeight = ConvertUtils.dp2px(msg.getMsgHeight());//900

        if (msgWidth <= minWidth)
            layoutParams.width = minWidth;
        else if (msgWidth > maxWidth)
            layoutParams.width = maxWidth;
        else
            layoutParams.width = msgWidth;

        if (msgHeight < minHeight)
            layoutParams.height = minHeight;
        else if (msgHeight > maxHeight)
            layoutParams.height = maxHeight;
        else
            layoutParams.height = msgHeight;

        this.setLayoutParams(layoutParams);
        ZalyLogUtils.getInstance().info(TAG, msg.get_id()+":Min:" + minWidth + "*" + minHeight);
        ZalyLogUtils.getInstance().info(TAG, msg.get_id()+":Max:" + maxWidth + "*" + maxHeight);
        ZalyLogUtils.getInstance().info(TAG, msg.get_id()+":Msg:" + msg.getMsgWidth() + "*" + msg.getMsgHeight());
        ZalyLogUtils.getInstance().info(TAG, msg.get_id()+":View:" + layoutParams.width + "*" + layoutParams.height);
    }


    public void setNoticeHeightSize(Message msg) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();

        int minHeight = ConvertUtils.dp2px(60);//180
        int maxHeight = dm.widthPixels - ConvertUtils.dp2px(100);;//780

        int msgHeight = ConvertUtils.dp2px(msg.getMsgHeight());//900


        if (msgHeight < minHeight)
            layoutParams.height = minHeight;
        else if (msgHeight > maxHeight)
            layoutParams.height = maxHeight;
        else
            layoutParams.height = msgHeight;

        this.setLayoutParams(layoutParams);

        ZalyLogUtils.getInstance().info(TAG, msg.get_id()+":Msg:" + msg.getMsgWidth() + "*" + msg.getMsgHeight() + " height == " + msgHeight );
        ZalyLogUtils.getInstance().info(TAG, msg.get_id()+":View:" + layoutParams.width + "*" + layoutParams.height);
    }
}
