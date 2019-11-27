package com.windchat.client.push.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.umeng.message.UmengMessageService;

import org.android.agoo.common.AgooConstants;

/**
 * Created by Mr.kk on 2018/5/4.
 * This Project was client-android
 */

public class CustomUmengService extends UmengMessageService{
    @Override
    public void onMessage(Context context, Intent intent) {
        Log.i("Umeng","umeng msg show");
        String message = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
        Log.i("Umeng",message);

    }
}
