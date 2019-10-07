package com.akaxin.client.site;


import android.content.Intent;
import android.os.Bundle;

import com.akaxin.client.ZalyApplication;
import com.akaxin.client.constant.PackageSign;
import com.akaxin.client.db.ZalyDbContentHelper;
import com.akaxin.client.db.dao.SiteMessageDao;
import com.windchat.im.IMConst;
import com.windchat.im.IMessageReceiver;
import com.windchat.im.bean.Message;
import com.windchat.im.socket.SiteAddress;
import com.windchat.im.socket.TransportPackage;
import com.windchat.proto.client.ImStcNoticeProto;

import java.util.List;

/**
 * Created by anguoyue on 2019/10/7.
 */

public class WindMessageReceiver implements IMessageReceiver {

    @Override
    public void handleMessageStatus(SiteAddress siteAddressO, String msgId, long msgTime, int msgStatus) throws Exception {


        String siteIdentity = siteAddressO.getHost().replace('.', '_') + "_" + siteAddressO.getPort();
        String siteAddress = siteAddressO.getFullUrl();

        int updateStatusFlag = SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(siteAddress)).updateU2MsgStatusForSend(msgId, msgTime, msgStatus);
        // 如果不是单人消息则去群组表中更新数据, 回写时间
        if (updateStatusFlag == 0) {
            if (SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(siteAddress)).updateGroupMsgStatusForSend(msgId, msgTime, msgStatus) == 0) {
            }
        }

        //通知UI进程
        Bundle bundle = new Bundle();
        bundle.putString(ZalyDbContentHelper.KEY_MSG_ID, msgId);
        bundle.putString(ZalyDbContentHelper.KEY_SITE_IDENTITY, siteIdentity);
//        bundle.putString(ZalyDbContentHelper.KEY_CUR_SITE_USER_ID, curSiteUserId);
        bundle.putInt(ZalyDbContentHelper.KEY_MSG_STATUS, msgStatus);
        ZalyDbContentHelper.executeAction(ZalyDbContentHelper.Action.MSG_STATUS, bundle);
    }

    @Override
    public void handleNoticeMessage(SiteAddress siteAddress, ImStcNoticeProto.ImStcNoticeRequest request) throws Exception {

        String siteIdentity = siteAddress.getHost().replace('.', '_') + "_" + siteAddress.getPort();

        Intent intent = new Intent(IMConst.IM_NOTICE_ACTION);
        intent.setPackage(PackageSign.getPackage());
        intent.putExtra(IMConst.KEY_NOTICE_SITE_IDENTITY, siteIdentity);
        intent.putExtra(IMConst.KEY_NOTICE_TYPE, request.getTypeValue());
        ZalyApplication.getContext().sendBroadcast(intent);
    }

    @Override
    public void handleU2Message(SiteAddress siteAddress, List<Message> u2Messages) throws Exception {
        SiteMessageDao.getInstance(siteAddress).batchInsertU2Messages(u2Messages);
    }

    @Override
    public void handleGroupMessage(SiteAddress siteAddress, List<Message> groupMessages) throws Exception {
        SiteMessageDao.getInstance(siteAddress).batchInsertGroupMessages(groupMessages);
    }

    @Override
    public void handleException(Throwable t) {

    }

}
