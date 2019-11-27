package com.windchat.client.mvp.contract;

import com.windchat.client.bean.Site;
import com.windchat.client.mvp.BasePresenter;
import com.windchat.client.mvp.BaseView;

/**
 * Created by Mr.kk on 2018/6/29.
 * This Project was client-android
 */

public class ScanQRCodeContract {
    public interface View extends BaseView {
        void onTaskStart(String content);

        void onTaskFinish();

        void onJoinSuccess();
    }

    public interface Presenter extends BasePresenter<View> {
        void sendTempSpaceContent(String spaceKey, String tskStr);

        void joinGroupByToken(Site site,String siteGroupId, String token);

    }
}
