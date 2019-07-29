package com.akaxin.client.mvp.contract;

import com.akaxin.client.bean.Site;
import com.akaxin.client.mvp.BasePresenter;
import com.akaxin.client.mvp.BaseView;

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
