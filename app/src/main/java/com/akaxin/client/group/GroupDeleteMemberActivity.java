package com.akaxin.client.group;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.akaxin.client.R;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.group.adapter.GroupMemberListAdapter;
import com.akaxin.client.group.listener.ChooseFriendListener;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.maintab.ZalyMainActivity;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.GroupProto;
import com.akaxin.proto.site.ApiGroupMembersProto;
import com.akaxin.proto.site.ApiGroupRemoveMemberProto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yichao on 2017/10/25.
 * 删除群成员
 */

public class GroupDeleteMemberActivity extends BaseActivity implements ChooseFriendListener {

    private static final String KEY_GROUP_ID = "key_group_id";

    private GroupMemberListAdapter listAdapter;
    private LinearLayoutManager mLayoutManager;

    @BindView(R.id.member_recycler)
    RecyclerView friendRv;
    @BindView(R.id.action_confirm)
    View actionConfirm;
    private String groupId;
    private Set<String> chooseFriends;
    private Site currentSite;

    @Override
    public int getResLayout() {
        return R.layout.activity_delete_group_member;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        mLayoutManager = new LinearLayoutManager(this);
        friendRv.setLayoutManager(mLayoutManager);
        setCenterTitle(getString(R.string.remove_group_member));
        actionConfirm.setEnabled(false);
    }

    public void initEvent() {

    }

    @Override
    public void initPresenter() {

    }

    public void onLoadData() {
        groupId = getIntent().getStringExtra(KEY_GROUP_ID);
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        if (StringUtils.isEmpty(groupId)) {
            finish();
            return;
        }
        chooseFriends = new HashSet<>();
        listAdapter = new GroupMemberListAdapter(this, currentSite);
        listAdapter.setChooseFriendListener(this);
        friendRv.setAdapter(listAdapter);
        ZalyTaskExecutor.executeUserTask(TAG, new GetGroupMember());
    }


    private void addFriends() {
        List<String> friends = new ArrayList<>(chooseFriends);
        if (friends.size() == 0) {
            Toaster.showInvalidate("请选择好友");
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new RemoveGroupMemberTask(friends));
    }

    @Override
    public void onFriendChangeCheck(String userId, boolean isChecked) {
        if (chooseFriends != null) {
            if (isChecked) {
                chooseFriends.add(userId);
            } else {
                chooseFriends.remove(userId);
            }
        }
        actionConfirm.setEnabled(chooseFriends != null && chooseFriends.size() != 0);
    }

    @OnClick(R.id.action_confirm)
    public void onViewClicked() {
        addFriends();
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
            List<GroupProto.GroupMemberProfile> list = response.getListList();
            listAdapter.addItems(list);
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }

    /**
     * 删除群成员
     */
    class RemoveGroupMemberTask extends ZalyTaskExecutor.Task<Void, Void, ApiGroupRemoveMemberProto.ApiGroupRemoveMemberResponse> {

        private List<String> members;

        public RemoveGroupMemberTask(List<String> members) {
            this.members = members;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected ApiGroupRemoveMemberProto.ApiGroupRemoveMemberResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getGroupApi().deleteGroupMembers(groupId,members);
        }

        @Override
        protected void onTaskSuccess(ApiGroupRemoveMemberProto.ApiGroupRemoveMemberResponse apiGroupDeleteMemberResponse) {
            super.onTaskSuccess(apiGroupDeleteMemberResponse);
            Toaster.show(String.format(getString(R.string.removed_group_members), members.size()));
            finish();
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }


}
