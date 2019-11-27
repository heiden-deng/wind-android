package com.windchat.client.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.windchat.client.R;
import com.windchat.client.bean.Message;
import com.windchat.client.maintab.WelcomeActivity;

import java.util.ArrayList;

/**
 * 触发安卓客户端的push，消息通知栏展示
 * Created by yichao on 2017/11/6.
 */

public class NotificationUtils {

    private static final String TAG = "NotificationUtils";

    private static final String CHANNEL_NAME = "default";
    private static final String CHANNEL_DESCRIPTION = "";
    private static final String CHANNEL_ID = "akaxin_notification";

    private static final String KEY_TEXT_REPLY = "key_text_reply";

    public static void initChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESCRIPTION);
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) notificationManager.createNotificationChannel(channel);
        }
    }

    public static void showNotification(Context context, int _id, String title, String content, Intent notifyIntent) {

        long[] vibrate = {100, 200, 300, 400};//通知震动频率

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_akaxin_foreground)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(true)
                        .setColor(context.getResources().getColor(R.color.colorPrimary))
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setLights(0xFF0000, 3000, 3000)
                        .setDefaults(NotificationCompat.DEFAULT_SOUND);
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(notifyPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null)
            mNotificationManager.notify(_id, mBuilder.build());
    }


    public static void showMessageNotification(Context context, ArrayList<Message> messages) {

        int notifyId = 0;

        NotificationCompat.MessagingStyle style = new NotificationCompat.MessagingStyle(context.getString(R.string.me));
        style.setConversationTitle("conversation title");
        for (Message message : messages) {
            style.addMessage(message.getContent(), message.getMsgTime(), message.getSiteUserId());
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_default)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setStyle(style)
                .setAutoCancel(true);

        Intent notifyIntent = new Intent(context, WelcomeActivity.class);
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(notifyPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) mNotificationManager.notify(notifyId, mBuilder.build());

    }

}
