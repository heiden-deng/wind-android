package com.windchat.client.db;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.windchat.client.BuildConfig;
import com.orhanobut.logger.Logger;
import com.windchat.client.ZalyApplication;

/**
 * Created by tanjie on 15/12/17.
 */
public class ZalyDbContentHelper {

    private static Uri contentUri;

    public static Bundle executeAction(String action, Bundle inBundle) {
        try {
            if (contentUri == null) {
                Context context = ZalyApplication.getContext();
                String packageName;
                if (context != null) {
                    packageName = context.getPackageName();
                } else {
                    packageName = BuildConfig.APPLICATION_ID;
                }
                contentUri = Uri.parse(String.format(ZalyDBContentProvider.CONTENT_AUTHORITY, packageName));
            }
            return ZalyApplication.getContext().getContentResolver().call(contentUri, action, "null", inBundle);
        } catch (Throwable e) {
            Logger.e(e);
        }
        return null;
    }


    public static class Action {
        public static final String MSG_STATUS = "msg_status";//返回发送消息的状态，是否已经送达服务器
        public static final String MSG_RECEIVE = "msg_receive";//收到服务端消息
        public static final String MSG_IMG_PROCESS = "msg_img_process"; ////进度
    }

    public static final String KEY_MSG_ID = "key_msg_id";
    public static final String KEY_SITE_IDENTITY = "key_site_identity";
    public static final String KEY_CUR_SITE_USER_ID = "key_cur_site_user_id";
    public static final String KEY_MSG_RECEIVE_LIST = "key_msg_receive_list";//收到的消息list
    public static final String KEY_MSG_RECEIVE_FINISH = "key_msg_receive_finish";
    public static final String KEY_MSG_STATUS = "key_msg_status";
    public static final String IMG_PROCESS_NUM = "img_process_num";
    public static final String IMG_PROCESS_MSG_INFO = "img_process_info";

}
