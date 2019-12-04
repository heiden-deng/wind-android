package com.windchat.client.site;


import android.content.Intent;
import android.os.Bundle;

import com.windchat.client.ZalyApplication;
import com.windchat.client.constant.PackageSign;
import com.windchat.client.db.ZalyDbContentHelper;
import com.windchat.client.db.dao.SiteMessageDao;
import com.windchat.im.IMConst;
import com.windchat.im.IMessageReceiver;
import com.windchat.im.MsgStatus;
import com.windchat.im.message.Message;
import com.windchat.im.bean.Site;
import com.windchat.im.message.Notification;
import com.windchat.proto.client.ImStcNoticeProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anguoyue on 2019/10/7.
 */

public class WindMessageReceiver implements IMessageReceiver {

    @Override
    public void handleMessageStatus(Site site, String msgId, long msgTime, MsgStatus msgStatus) throws Exception {

        String siteIdentity = site.getSiteHost().replace('.', '_') + "_" + site.getSitePort();
        String siteAddress = site.getHostAndPort();

        int status = msgStatus.getValue() > 0 ? msgStatus.getValue() + 1 : msgStatus.getValue();

        if (msgId.startsWith("U2")) {
            SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(siteAddress)).updateU2MsgStatusForSend(msgId, msgTime, status);
        } else {
            SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(siteAddress)).updateGroupMsgStatusForSend(msgId, msgTime, status);
        }

        //通知UI进程
        Bundle bundle = new Bundle();
        bundle.putString(ZalyDbContentHelper.KEY_MSG_ID, msgId);
        bundle.putString(ZalyDbContentHelper.KEY_SITE_IDENTITY, siteIdentity);
        bundle.putString(ZalyDbContentHelper.KEY_CUR_SITE_USER_ID, site.getSiteUserId());
        bundle.putInt(ZalyDbContentHelper.KEY_MSG_STATUS, status);
        ZalyDbContentHelper.executeAction(ZalyDbContentHelper.Action.MSG_STATUS, bundle);

    }

    @Override
    public void handleNoticeMessage(Site site, ImStcNoticeProto.ImStcNoticeRequest request) throws Exception {

        String siteIdentity = site.getSiteHost().replace('.', '_') + "_" + site.getSitePort();

        Intent intent = new Intent(IMConst.IM_NOTICE_ACTION);
        intent.setPackage(PackageSign.getPackage());
        intent.putExtra(IMConst.KEY_NOTICE_SITE_IDENTITY, siteIdentity);
        intent.putExtra(IMConst.KEY_NOTICE_TYPE, request.getTypeValue());
        ZalyApplication.getContext().sendBroadcast(intent);
    }

    @Override
    public void handleNotification(Site site, List<Notification> notifications) throws Exception {

    }

    @Override
    public void handleU2Message(Site site, List<? extends Message> u2Messages) throws Exception {
        SiteMessageDao.getInstance(site.getSiteAddress()).batchInsertU2Messages(u2Messages);

        ArrayList<com.windchat.client.bean.Message> u2MsgList = (ArrayList<com.windchat.client.bean.Message>) u2Messages;

        String siteIdentity = site.getSiteHost().replace('.', '_') + "_" + site.getSitePort();
        // goto ui
        // 通知UI进程
        Bundle bundle = new Bundle();
        bundle.putString(ZalyDbContentHelper.KEY_SITE_IDENTITY, siteIdentity);
        bundle.putString(ZalyDbContentHelper.KEY_CUR_SITE_USER_ID, site.getSiteUserId());
        bundle.putParcelableArrayList(ZalyDbContentHelper.KEY_MSG_RECEIVE_LIST, u2MsgList);
        ZalyDbContentHelper.executeAction(ZalyDbContentHelper.Action.MSG_RECEIVE, bundle);
    }

    @Override
    public void handleGroupMessage(Site site, List<? extends Message> groupMessages) throws Exception {
        SiteMessageDao.getInstance(site.getSiteAddress()).batchInsertGroupMessages(groupMessages);

        ArrayList<com.windchat.client.bean.Message> gMsgList = (ArrayList<com.windchat.client.bean.Message>) groupMessages;
        String siteIdentity = site.getSiteHost().replace('.', '_') + "_" + site.getSitePort();

        // 通知UI进程
        Bundle bundle = new Bundle();
        bundle.putString(ZalyDbContentHelper.KEY_SITE_IDENTITY, siteIdentity);
        bundle.putString(ZalyDbContentHelper.KEY_CUR_SITE_USER_ID, site.getSiteUserId());
        bundle.putParcelableArrayList(ZalyDbContentHelper.KEY_MSG_RECEIVE_LIST, gMsgList);
        ZalyDbContentHelper.executeAction(ZalyDbContentHelper.Action.MSG_RECEIVE, bundle);
    }

    @Override
    public void handleException(Site site, Throwable t) {
        t.printStackTrace();
    }

}
