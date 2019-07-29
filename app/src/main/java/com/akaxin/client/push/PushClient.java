package com.akaxin.client.push;

import android.content.Context;

import com.akaxin.client.util.ClientTypeHepler;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.proto.core.ClientProto;
import com.xiaomi.mipush.sdk.MiPushClient;

/**
 * Created by anguoyue on 15/03/2018.
 */

public class PushClient {

    public static void clearNotification(Context context) {
        ClientProto.ClientType clientType = ClientTypeHepler.getClientType();
        try {
            switch (clientType) {
                case ANDROID_XIAOMI:
                    MiPushClient.clearNotification(context);
                    break;
                case ANDROID_HUAWEI:
                    break;
            }
        } catch (Exception e) {
            com.orhanobut.logger.Logger.e(e, "clear notification error");
        }

    }

}
