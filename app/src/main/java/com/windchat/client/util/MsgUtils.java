package com.windchat.client.util;

import com.windchat.client.bean.Site;

/**
 * Created by alex on 18-2-28.
 */

public class MsgUtils {

    public static final int MSG_TYPE_U2 = 1;
    public static final int MSG_TYPE_GROUP = 2;
    public static final int MSG_TYPE_NOTICE = 3;

    public static String getCurMsgId(int type, Site site) {
        String msgId = "";
        switch (type) {
            case MSG_TYPE_U2:
                msgId += "U2-";
                break;
            case MSG_TYPE_GROUP:
                msgId += "GROUP-";
                break;
            case MSG_TYPE_NOTICE:
                msgId += "NOTICE-";
                break;
        }
        String curSiteUserId = site.getSiteUserId();
        if (curSiteUserId.length() > 8)
            msgId += curSiteUserId.substring(0, 8);
        else msgId += curSiteUserId;
        msgId += "-";
        msgId += System.currentTimeMillis();
        return msgId;
    }

}
