package com.windchat.client.friend;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.ZalyApplication;
import com.windchat.client.api.ApiClient;
import com.windchat.client.api.ZalyAPIException;
import com.windchat.client.bean.Site;

import com.windchat.client.bean.event.AppEvent;
import com.windchat.client.constant.IntentKey;
import com.windchat.client.constant.PackageSign;
import com.windchat.client.constant.SiteConfig;
import com.windchat.client.friend.adapter.FriendApplyListAdapter;
import com.windchat.client.friend.listener.FriendApplyListListener;
import com.windchat.client.maintab.BaseActivity;
import com.windchat.client.maintab.ZalyMainActivity;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.task.ZalyTaskExecutor;
import com.windchat.client.util.toast.Toaster;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.site.ApiFriendApplyListProto;
import com.akaxin.proto.site.ApiFriendApplyResultProto;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.windchat.client.Configs.KEY_NEW_APPLY_FRIEND;

/**
 * Created by yichao on 2017/10/27.
 */

public class FriendApplyListActivity extends BaseActivity implements FriendApplyListListener {

    @BindView(R.id.apply_list)
    RecyclerView applyListRv;
    @BindView(R.id.empty_view)
    TextView emptyTv;
    private FriendApplyListAdapter adapter;
    private Site currentSite;

    @Override
    public int getResLayout() {
        return R.layout.activity_apply_list;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        this.currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        applyListRv.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {
        setMultTitle(getString(R.string.title_friend_apply_list), StringUtils.getSiteSubTitle(this.currentSite));
        adapter = new FriendApplyListAdapter(this, currentSite);
        adapter.setListListener(this);
        applyListRv.setAdapter(adapter);
        ZalyTaskExecutor.executeUserTask(TAG, new GetFriendApplyListTask());
    }

    @Override
    public void onApplyFriend(int position, String friendSiteUserId) {
        if (StringUtils.isEmpty(friendSiteUserId)) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, "onApplyFriend friendSiteUserId is null");
            Toaster.showInvalidate(R.string.data_error);
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new AgreeApplyFriendTask(position, friendSiteUserId));
    }

    class GetFriendApplyListTask extends ZalyTaskExecutor.Task<Void, Void, ApiFriendApplyListProto.ApiFriendApplyListResponse> {

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected void onCacheTask() {
            String cacheFriendList = ZalyApplication.getCfgSP().getString(currentSite.getSiteIdentity() + SiteConfig.FRIEND_APPLY_LIST);
            if (!StringUtils.isEmpty(cacheFriendList)) {
                byte[] data = Base64.decode(cacheFriendList, Base64.NO_WRAP);
                try {
                    ApiFriendApplyListProto.ApiFriendApplyListResponse apiFriendApplyListResponse = ApiFriendApplyListProto.ApiFriendApplyListResponse.parseFrom(data);
                    displayUI(apiFriendApplyListResponse);
                } catch (Exception e) {
                    ZalyLogUtils.getInstance().info(TAG, e.getMessage());
                }
            }
        }

        @Override
        protected ApiFriendApplyListProto.ApiFriendApplyListResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getFriendApi().getApplyList();
        }

        @Override
        protected void onTaskSuccess(ApiFriendApplyListProto.ApiFriendApplyListResponse apiFriendApplyListResponse) {
            super.onTaskSuccess(apiFriendApplyListResponse);
            ZalyApplication.getCfgSP().put(currentSite.getSiteIdentity() + SiteConfig.FRIEND_APPLY_LIST, Base64.encodeToString(apiFriendApplyListResponse.toByteArray(), Base64.NO_WRAP));
            displayUI(apiFriendApplyListResponse);
        }

        public void displayUI(ApiFriendApplyListProto.ApiFriendApplyListResponse apiFriendApplyListResponse) {
            if (apiFriendApplyListResponse.getListList() == null || apiFriendApplyListResponse.getListList().size() == 0) {
                adapter.removeAllItem();
                emptyTv.setVisibility(View.VISIBLE);
                emptyTv.setText(R.string.empty_friend_apply_list);
                ZalyApplication.getCfgSP().put(currentSite.getSiteIdentity() + KEY_NEW_APPLY_FRIEND, false);
                return;
            }
            emptyTv.setVisibility(View.GONE);
            if (adapter != null) {
                List<UserProto.ApplyUserProfile> profiles = apiFriendApplyListResponse.getListList();
                adapter.addItems(profiles);
            }
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            emptyTv.setVisibility(View.VISIBLE);
            emptyTv.setText(R.string.empty_friend_apply_list);
            finish();
        }

        @Override
        protected void onAPIError(ZalyAPIException e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            adapter.removeAllItem();

            finish();
        }


        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            adapter.removeAllItem();

            hideProgress();
        }
    }

    class AgreeApplyFriendTask extends ZalyTaskExecutor.Task<Void, Void, ApiFriendApplyResultProto.ApiFriendApplyResultResponse> {

        private int position;
        private String friendSiteUserId;

        public AgreeApplyFriendTask(int position, String friendSiteUserId) {
            this.position = position;
            this.friendSiteUserId = friendSiteUserId;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected ApiFriendApplyResultProto.ApiFriendApplyResultResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getFriendApi().agreeApplyFriend(friendSiteUserId);
        }

        @Override
        protected void onTaskSuccess(ApiFriendApplyResultProto.ApiFriendApplyResultResponse apiFriendApplyResultResponse) {
            super.onTaskSuccess(apiFriendApplyResultResponse);
            Toaster.show(String.format(getString(R.string.accept_friend_application), adapter.getItemUsername(position)));
            adapter.removeItem(position);
            if (adapter.getItemCount() == 0) {
                ZalyApplication.getCfgSP().put(currentSite.getSiteIdentity() + KEY_NEW_APPLY_FRIEND, false);
                try {
                    Intent intent = new Intent(ZalyMainActivity.CHECK_BUDDLE);
                    intent.setPackage(PackageSign.getPackage());
                    ZalyApplication.getContext().sendBroadcast(intent);
                } catch (Exception e) {
                    Logger.e(TAG, e);
                }
                finish();
            }
            EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_RELOAD, null));
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
        }

        @Override
        protected void onAPIError(ZalyAPIException e) {
            super.onAPIError(e);
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }


}
