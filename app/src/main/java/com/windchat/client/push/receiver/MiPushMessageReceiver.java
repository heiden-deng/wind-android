package com.windchat.client.push.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.windchat.client.jump.ZalyGotoPageByPlugin;
import com.windchat.client.push.MiPushUtils;
import com.windchat.client.util.toast.Toaster;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.util.List;

/**
 * Created by alexfan on 2018/3/2.
 */

public class MiPushMessageReceiver extends PushMessageReceiver {

    private static final String TAG = "MiPushMessageReceiver";
    private static final String KEY_PUSH_GOTO = "push-goto";

    /**
     * onReceivePassThroughMessage用来接收服务器发送的透传消息
     * @param context
     * @param miPushMessage
     */
    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
        super.onReceivePassThroughMessage(context, miPushMessage);
        Toaster.show(miPushMessage.getContent());
    }

    /**
     * onNotificationMessageClicked用来接收服务器发来的通知栏消息（用户点击通知栏时触发）
     * @param context
     * @param miPushMessage
     */
    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage miPushMessage) {
        super.onNotificationMessageClicked(context, miPushMessage);
        String url = miPushMessage.getExtra().get(KEY_PUSH_GOTO);
        Log.d(TAG, "received url: " + url);
        Intent intent = ZalyGotoPageByPlugin.executeGotoPage(url, true);
        context.startActivity(intent);
    }

    /**
     * onNotificationMessageArrived用来接收服务器发来的通知栏消息（消息到达客户端时触发，并且可以接收应用在前台时不弹出通知的通知消息）
     * @param context
     * @param miPushMessage
     */
    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage miPushMessage) {
        super.onNotificationMessageArrived(context, miPushMessage);
    }

    /** onCommandResult用来接收客户端向服务器发送命令消息后返回的响应
     *
     * @param context
     * @param miPushCommandMessage
     */
    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        String command = miPushCommandMessage.getCommand();
        Log.d(TAG, command);

        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (miPushCommandMessage.getResultCode() == ErrorCode.SUCCESS) {
                Log.i(TAG, "注册成功");
            } else {
                Log.e(TAG, "注册失败" + miPushCommandMessage.getResultCode());
            }
        } else {
            Log.e(TAG, "其他情况" + miPushCommandMessage.getReason());
        }
    }

    /**
     * onReceiveRegisterResult用来接受客户端向服务器发送注册命令消息后返回的响应
     * @param context
     * @param miPushCommandMessage
     */
    @Override
    public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        String command = miPushCommandMessage.getCommand();
        Log.d(TAG, command);

        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (miPushCommandMessage.getResultCode() == ErrorCode.SUCCESS) {
                List<String> arguments = miPushCommandMessage.getCommandArguments();
                MiPushUtils.setRegId((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
                Log.i(TAG, "注册成功, regId: " + MiPushUtils.getRegId());
            } else {
                Log.e(TAG, "注册失败" + miPushCommandMessage.getResultCode());
            }
        } else {
            Log.e(TAG, "其他情况" + miPushCommandMessage.getReason());
        }
    }
}
