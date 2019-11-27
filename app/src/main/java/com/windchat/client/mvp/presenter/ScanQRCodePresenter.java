package com.windchat.client.mvp.presenter;

import android.util.Base64;

import com.windchat.client.Configs;
import com.windchat.client.ZalyApplication;
import com.windchat.client.api.ApiClient;
import com.windchat.client.api.ZalyAPIException;
import com.windchat.client.bean.Site;
import com.windchat.client.mvp.BasePresenterImpl;
import com.windchat.client.mvp.contract.ScanQRCodeContract;
import com.windchat.client.util.security.AESUtils;
import com.windchat.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.site.ApiGroupJoinByTokenProto;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Mr.kk on 2018/6/29.
 * This Project was client-android
 */

public class ScanQRCodePresenter extends BasePresenterImpl<ScanQRCodeContract.View> implements ScanQRCodeContract.Presenter {
    @Override
    public void sendTempSpaceContent(final String spaceKey, final String tskStr) {

    }

    @Override
    public void joinGroupByToken(final Site site, final String siteGroupId, final String token) {
        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, ApiGroupJoinByTokenProto.ApiGroupJoinByTokenResponse>() {
            @Override
            protected ApiGroupJoinByTokenProto.ApiGroupJoinByTokenResponse executeTask(Void... voids) throws Exception {
                return ApiClient.getInstance(site).getGroupApi().joinGroupByToken(siteGroupId, token);
            }

            @Override
            protected void onPreTask() {
                super.onPreTask();
                mView.onTaskStart("访问中....");

            }

            @Override
            protected void onTaskFinish() {
                super.onTaskFinish();
                mView.onTaskFinish();
            }

            @Override
            protected void onTaskSuccess(ApiGroupJoinByTokenProto.ApiGroupJoinByTokenResponse apiGroupJoinByTokenResponse) {
                super.onTaskSuccess(apiGroupJoinByTokenResponse);
                mView.onJoinSuccess();
            }

            @Override
            protected void onTaskError(Exception e) {
                super.onTaskError(e);
                mView.onTaskFinish();
            }

            @Override
            protected void onAPIError(ZalyAPIException zalyAPIException) {
                super.onAPIError(zalyAPIException);
                mView.onTaskFinish();
            }
        });
    }

}
