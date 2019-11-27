package com.windchat.client.push;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.windchat.client.BuildConfig;
import com.windchat.client.Configs;
import com.windchat.client.ZalyApplication;
import com.windchat.client.util.log.ZalyLogUtils;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

/**
 * Created by Mr.kk on 2018/5/3.
 * This Project was client-android
 */

public class UmengPushUtils {
    private static final String TAG = UmengPushUtils.class.getSimpleName();

    private static final String UMENG_APP_KEY = "5aeaae6bf29d9812810000b1";
    private static final String UMENG_APP_SECRET = "af99370734a5112ca4c73ebb343fd090";

    private static final String UMENG_APP_KEY_DEBUG = "5aeaadf98f4a9d06c200010b";
    private static final String UMENG_APP_SECRET_DEBUG = "0aaef9717b7c62002d40341704f0fd79";

    public static Context mContext;

    private UmengPushUtils() {
    }

    public static UmengPushUtils getInstance() {
        return UmengPushUtils.SingletonHolder.instance;
    }

    static class SingletonHolder {
        private static UmengPushUtils instance = new UmengPushUtils();
    }


    public void init(Context mContext) {
        this.mContext = mContext;
        UMConfigure.setLogEnabled(true);
        if (BuildConfig.DEBUG)
            UMConfigure.init(mContext, UMENG_APP_KEY_DEBUG, "", UMConfigure.DEVICE_TYPE_PHONE,
                    UMENG_APP_SECRET_DEBUG);
        else
            UMConfigure.init(mContext, UMENG_APP_KEY, "", UMConfigure.DEVICE_TYPE_PHONE,
                    UMENG_APP_SECRET);
        PushAgent mPushAgent = PushAgent.getInstance(mContext);
        mPushAgent.setResourcePackageName("com.akaxin.client");
       // mPushAgent.setPushCheck(true);
        mPushAgent.setNotificaitonOnForeground(false);
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回device token
                ZalyLogUtils.getInstance().info(TAG, "DeviceToken:" + deviceToken);
                ZalyApplication.getCfgSP().put(Configs.PUSH_TOKEN, deviceToken);
            }

            @Override
            public void onFailure(String s, String s1) {
                ZalyLogUtils.getInstance().info(TAG, s + ":" + s1);
            }
        });
        mPushAgent.setNotificationClickHandler(notificationClickHandler);
    }


    UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {
        @Override
        public void dealWithCustomAction(Context context, UMessage msg) {
            Log.i(TAG,msg.custom);
            Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
//            String url = null;
//            Intent intent = ZalyGotoPageByPlugin.executeGotoPage(url, true);
//            context.startActivity(intent);
        }
    };

    public static String getPushToken() {
        //先从application获取，在从本地
        PushAgent mPushAgent = PushAgent.getInstance(mContext);
        return mPushAgent.getRegistrationId();
    }
}
