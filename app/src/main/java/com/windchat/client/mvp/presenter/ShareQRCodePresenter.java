package com.windchat.client.mvp.presenter;

import com.windchat.client.api.ApiClient;
import com.windchat.client.bean.Site;
import com.windchat.client.mvp.BasePresenterImpl;
import com.windchat.client.mvp.contract.ShareQRCodeContract;
import com.windchat.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.site.ApiGroupApplyTokenProto;

/**
 * Created by Mr.kk on 2018/6/29.
 * This Project was client-android
 */

public class ShareQRCodePresenter extends BasePresenterImpl<ShareQRCodeContract.View> implements ShareQRCodeContract.Presenter {
    @Override
    public void getGroupToken(final Site site, final String siteGroupId) {
        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, ApiGroupApplyTokenProto.ApiGroupApplyTokenResponse>() {
            @Override
            protected ApiGroupApplyTokenProto.ApiGroupApplyTokenResponse executeTask(Void... voids) throws Exception {
                return ApiClient.getInstance(site).getGroupApi().getGroupApplyToken(siteGroupId);
            }

            @Override
            protected void onTaskSuccess(ApiGroupApplyTokenProto.ApiGroupApplyTokenResponse apiGroupApplyTokenResponse) {
                super.onTaskSuccess(apiGroupApplyTokenResponse);
                mView.onGetGroupTokenSuccess(apiGroupApplyTokenResponse);
            }
        });
    }
}
