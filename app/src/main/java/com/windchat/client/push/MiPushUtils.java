package com.windchat.client.push;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.windchat.client.BuildConfig;
import com.windchat.client.Configs;
import com.windchat.client.ZalyApplication;
import com.windchat.client.util.data.StringUtils;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.List;

/**
 * Created by alexfan on 2018/3/2.
 */

public class MiPushUtils {

    // xiaomi push
    private static final String TAG = "MiPushUtils";

    private static final String APP_ID = "2882303761517721388";
    private static final String APP_KEY = "5801772175388";

    private static final String APP_ID_DEBUG = "2882303761517729833";
    private static final String APP_KEY_DEBUG = "5661772967833";
    private static String regId;

    public static void initMiPush() {

        //初始化push推送服务
        if (shouldInit()) {
            if (BuildConfig.DEBUG) {
                MiPushClient.registerPush(ZalyApplication.getContext(), APP_ID_DEBUG, APP_KEY_DEBUG);
            } else {
                MiPushClient.registerPush(ZalyApplication.getContext(), APP_ID, APP_KEY);
            }
        }
        //打开Log
        LoggerInterface newLogger = new LoggerInterface() {
            @Override
            public void setTag(String tag) {
                // ignore
            }

            @Override
            public void log(String content, Throwable t) {
                Log.d(ZalyApplication.TAG, content, t);
            }

            @Override
            public void log(String content) {
                Log.d(ZalyApplication.TAG, content);
            }
        };
        com.xiaomi.mipush.sdk.Logger.setLogger(ZalyApplication.getContext(), newLogger);
    }

    private static boolean shouldInit() {
        ActivityManager am = ((ActivityManager) ZalyApplication.getContext().getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = ZalyApplication.getContext().getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    public static void setRegId(String id) {
        //放入application中
        if (StringUtils.isNotEmpty(id)) {
            ZalyApplication.getCfgSP().put(Configs.PUSH_TOKEN, id);
        }
        regId = id;
        Log.d(TAG, "regId: " + id);
    }

    public static String getRegId() {
        //先从application获取，在从本地
        String pushToken = ZalyApplication.getCfgSP().getString(Configs.PUSH_TOKEN);
        return StringUtils.isNotBlank(pushToken) ? pushToken : "IS_NULL";
    }
}
