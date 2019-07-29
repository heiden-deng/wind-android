package com.akaxin.client.push.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.im.ZalyIM;
import com.akaxin.client.jump.ZalyGotoPageByPlugin;
import com.akaxin.client.util.NotificationUtils;
import com.akaxin.client.util.log.ZalyLogUtils;

import java.util.List;

public class DefaultBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "DefaultBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            String action = intent.getAction();
            ZalyLogUtils.getInstance().info(TAG, "action:" + action);
            switch (action) {
                case ZalyIM.PLATFORM_PUSH_ACTION:
                    if (isAppInBackground(context)) {
                        String pushContent = intent.getStringExtra(ZalyIM.KEY_PLATFORM_PUSH_CONTENT);
                        String pushTitle = intent.getStringExtra(ZalyIM.KEY_PLATFORM_PUSH_TITLE);
                        String url = intent.getStringExtra(ZalyIM.KEY_PLATFORM_PUSH_JUMP);
                        ZalyLogUtils.getInstance().info(TAG, "pushJump: " + url);
                        NotificationUtils.showNotification(
                                ZalyApplication.getContext(),
                                1,
                                pushTitle,
                                pushContent,
                                ZalyGotoPageByPlugin.executeGotoPage(url, true)
                        );
                    }
                    break;
            }
        }
    }

    public static boolean isAppInBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                    .getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(context.getPackageName())) {
                    return appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
                }
            }
        }
        return false;
    }
}
