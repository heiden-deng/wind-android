package com.windchat.im;

import com.akaxin.client.Configs;
import com.akaxin.client.util.SPUtils;

/**
 * Created by yichao on 2017/10/18.
 */

public abstract class ZalyIM {

    /**
     * SharedPreferences 虽然支持多进程模式但是实际上是 not work!!!!!  Fuckinggggggg!!!
     *
     * 获取密钥可以调用，但是万万不能put数据
     * @return
     */
    @Deprecated
    public static SPUtils getCfgSP() {
        return SPUtils.getInstance(Configs.ZALY_SP);
    }

    /**
     * 消息action
     */
    public static class Action {
        public static final String Hello = "im.site.hello";
        public static final String Auth = "im.site.auth";
        public static final String PSN = "im.stc.psn";
        public static final String ImCtsMessage = "im.cts.message";
        public static final String ReceiveMsgFrmSite = "im.stc.message";
        public static final String MsgFinish = "im.msg.finish";
        public static final String Sync = "im.sync.message";
        public static final String SyncFinish = "im.sync.finish";
        public static final String SyncMsgStatus = "im.sync.msgStatus";

        public static final String Notice = "im.stc.notice";

        public static final String Ping = "im.cts.ping";
        public static final String Pong = "im.stc.pong";
    }

    /**
     * 连接状态action
     */
    public static final String CONNECTION_ACTION = "com.akaxin.client.im_status.BROADCAST";
    public static final String KEY_CONN_TYPE = "key_conn_type";
    public static final String KEY_CONN_STATUS = "key_conn_status";
    public static final String KEY_CONN_IDENTITY = "key_conn_identity";

    /**
     * 更新站点数据
     */
    public static final String UPDATE_SITES_ACTION = "com.akaxin.client.updatesites.BROADCAST";

    /**
     * 需要注册
     */
    public static final String NEED_REGISTER_SITE = "com.akaxin.client.need_regsiter_site";

    /**
     * notice操作IM_NOTICE_ACTION
     */
    public static final String IM_NOTICE_ACTION = "com.akaxin.client.im.notice";
    public static final String KEY_NOTICE_SITE_IDENTITY = "key_notice_site_identity";
    public static final String KEY_NOTICE_TYPE = "key_notice_type";

    /**
     * 平台通知
     */
    public static final String PLATFORM_PUSH_ACTION = "com.akaxin.client.platform.push";
    public static final String KEY_PLATFORM_PUSH_CONTENT = "key_platform_push_content";
    public static final String KEY_PLATFORM_PUSH_TITLE = "key_platform_push_title";
    public static final String KEY_PLATFORM_PUSH_JUMP = "key_platform_push_jump";
    public static final String IM_SUCCESS = "success";

}
