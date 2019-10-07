package com.akaxin.client.site;


import android.content.Intent;
import android.os.Bundle;

import com.akaxin.client.ZalyApplication;
import com.akaxin.client.constant.PackageSign;
import com.akaxin.client.db.ZalyDbContentHelper;
import com.akaxin.client.db.dao.SiteMessageDao;
import com.windchat.im.IMConst;
import com.windchat.im.IMessageReceiver;
import com.windchat.im.MsgStatus;
import com.windchat.im.bean.Message;
import com.windchat.im.bean.Site;
import com.windchat.im.socket.SiteAddress;
import com.windchat.im.socket.TransportPackage;
import com.windchat.proto.client.ImStcNoticeProto;

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
    public void handleU2Message(Site site, List<Message> u2Messages) throws Exception {
        SiteMessageDao.getInstance(site.getSiteAddress()).batchInsertU2Messages(u2Messages);

        // goto ui
    }

    @Override
    public void handleGroupMessage(Site site, List<Message> groupMessages) throws Exception {
        SiteMessageDao.getInstance(site.getSiteAddress()).batchInsertGroupMessages(groupMessages);
    }

    @Override
    public void handleException(Site site, Throwable t) {

    }

}
