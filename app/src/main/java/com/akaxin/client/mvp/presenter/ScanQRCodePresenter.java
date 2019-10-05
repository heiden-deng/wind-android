package com.akaxin.client.mvp.presenter;

import android.util.Base64;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.mvp.BasePresenterImpl;
import com.akaxin.client.mvp.contract.ScanQRCodeContract;
import com.akaxin.client.util.security.AESUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.platform.ApiTempUploadProto;
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
        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, ApiTempUploadProto.ApiTempUploadResponse>() {
            @Override
            protected ApiTempUploadProto.ApiTempUploadResponse executeTask(Void... voids) throws Exception {
                byte[] tsk = Base64.decode(tskStr, Base64.NO_WRAP);
                String userPubKey = ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY);
                String userPriKey = ZalyApplication.getCfgSP().getKey(Configs.USER_PRI_KEY);
                byte[] encryptedUserPubKey = AESUtils.encrypt(tsk, userPubKey.getBytes());
                byte[] encryptedUserPriKey = AESUtils.encrypt(tsk, userPriKey.getBytes());
                HashMap<String, String> map = new HashMap<>();
                map.put("pubk", Base64.encodeToString(encryptedUserPubKey, Base64.NO_WRAP));
                map.put("prik", Base64.encodeToString(encryptedUserPriKey, Base64.NO_WRAP));
                String tempSpaceContent = new JSONObject(map).toString();
//                return ApiClient.getInstance(ApiClientForPlatform.getPlatformSite()).getTempApi().applyTempSpace(spaceKey, tempSpaceContent);
                return null;
            }

            @Override
            protected void onTaskFinish() {
                super.onTaskFinish();
                mView.onTaskFinish();
            }

            @Override
            protected void onPreTask() {
                super.onPreTask();
                mView.onTaskStart("正在授权...");
            }
        });
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
