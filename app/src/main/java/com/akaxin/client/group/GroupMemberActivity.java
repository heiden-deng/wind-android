package com.akaxin.client.group;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.friend.FriendProfileActivity;
import com.akaxin.client.group.adapter.GroupMemberAdapter;
import com.akaxin.client.group.listener.GroupMemberListListener;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.maintab.ZalyMainActivity;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.site.ApiGroupMembersProto;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yichao on 2018/1/6.
 */

public class GroupMemberActivity extends BaseActivity implements GroupMemberListListener{

    @BindView(R.id.swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.member_recycler) RecyclerView friendRv;
    private GroupMemberAdapter adapter;
    private LinearLayoutManager mLayoutManager;
    private String groupId;
    private Site currentSite;


    @Override
    public int getResLayout() {
        return R.layout.activity_group_members;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefreshLayout.setEnabled(false);
        mLayoutManager = new LinearLayoutManager(this);
        friendRv.setLayoutManager(mLayoutManager);
    }

    @Override
    public void initEvent() {
        groupId = getIntent().getStringExtra(IntentKey.KEY_GROUP_ID);
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        if (StringUtils.isEmpty(groupId)) {
            finish();
            return;
        }
        setCenterTitle(getString(R.string.title_group_members));
        adapter = new GroupMemberAdapter(this, currentSite);
        adapter.setGroupMemberListListener(this);
        friendRv.setAdapter(adapter);
        ZalyTaskExecutor.executeUserTask(TAG, new GetGroupMember());
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {

    }

    @Override
    public void onMemberClick(String siteUserId) {
        Intent intent = new Intent(this, FriendProfileActivity.class);
        intent.putExtra(IntentKey.KEY_PROFILE_MODE, FriendProfileActivity.MODE_FRIEND_SITE_ID);
        intent.putExtra(IntentKey.KEY_FRIEND_SITE_ID, siteUserId);
        intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
        startActivity(intent);
    }


    /**
     * 获取群成员列表
     */
    class GetGroupMember extends ZalyTaskExecutor.Task<Void, Void, ApiGroupMembersProto.ApiGroupMembersResponse> {

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected ApiGroupMembersProto.ApiGroupMembersResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getGroupApi().getGroupMembers(groupId);
        }

        @Override
        protected void onTaskSuccess(ApiGroupMembersProto.ApiGroupMembersResponse response) {
            super.onTaskSuccess(response);
            adapter.addItems(response.getListList());

        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }



    @Override
    public void showProgress() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        swipeRefreshLayout.setRefreshing(false);
    }
}
