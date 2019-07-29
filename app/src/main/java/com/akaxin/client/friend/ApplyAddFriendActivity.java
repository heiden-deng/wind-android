package com.akaxin.client.friend;

import android.view.View;
import android.widget.EditText;

import com.akaxin.client.R;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.site.ApiFriendApplyProto;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yichao on 2017/10/27.
 */

public class ApplyAddFriendActivity extends BaseActivity {



    private String friendSiteUserId;
    @BindView(R.id.action_confirm)
    View actionApply;
    @BindView(R.id.reason_et)
    EditText reasonEt;
    private Site currentSite;

    @Override
    public int getResLayout() {
        return R.layout.activity_apply_addfriend;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        setMultTitle("添加好友", StringUtils.getSiteSubTitle(this.currentSite));
        friendSiteUserId = getIntent().getStringExtra(IntentKey.KEY_USER_SITE_ID);
        if (StringUtils.isEmpty(friendSiteUserId)) {
            Toaster.showInvalidate("数据异常，请稍候再试");
            finish();
        }
    }

    @OnClick(R.id.action_confirm)
    public void onViewClicked() {
        String reason = reasonEt.getText().toString().trim();
        if (StringUtils.isEmpty(reason)) {
            Toaster.showInvalidate("请输入申请理由");
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new ApplyFriendTask(friendSiteUserId, reason));
    }

    class ApplyFriendTask extends ZalyTaskExecutor.Task<Void, Void, ApiFriendApplyProto.ApiFriendApplyResponse> {

        private String friendSiteUserId;
        private String reason;

        public ApplyFriendTask(String friendSiteUserId, String reason) {
            this.friendSiteUserId = friendSiteUserId;
            this.reason = reason;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected ApiFriendApplyProto.ApiFriendApplyResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getFriendApi().applyFriend(friendSiteUserId,reason);
        }

        @Override
        protected void onTaskSuccess(ApiFriendApplyProto.ApiFriendApplyResponse apiFriendApplyResponse) {
            super.onTaskSuccess(apiFriendApplyResponse);
            Toaster.showInvalidate("已发送申请");
            finish();
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            finish();
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }


}
