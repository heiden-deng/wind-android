package com.akaxin.client.group;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.LinearLayout;

import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.group.adapter.ChooseFriendListAdapter;
import com.akaxin.client.group.listener.ChooseFriendListener;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.maintab.ZalyMainActivity;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.site.ApiGroupAddMemberProto;
import com.akaxin.proto.site.ApiGroupNonMembersProto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yichao on 2017/10/25.
 */

public class GroupAddMemberActivity extends BaseActivity implements ChooseFriendListener {

    private static final String KEY_GROUP_ID = "key_group_id";

    @BindView(R.id.member_recycler)
    RecyclerView friendRv;
    @BindView(R.id.action_confirm)
    LinearLayout addConfirm;
    private LinearLayoutManager mLayoutManager;
    private ChooseFriendListAdapter listAdapter;
    private String groupId;
    private Set<String> chooseFriends;
    private Site currentSite;


    @Override
    public int getResLayout() {
        return R.layout.activity_choose_friend_list;
    }

    public void initView() {
        ButterKnife.bind(this);
        mLayoutManager = new LinearLayoutManager(this);
        friendRv.setLayoutManager(mLayoutManager);

    }

    public void initEvent() {

    }

    @Override
    public void initPresenter() {

    }

    public void onLoadData() {
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        groupId = getIntent().getStringExtra(KEY_GROUP_ID);
        if (StringUtils.isEmpty(groupId)) {
            finish();
            return;
        }
        setCenterTitle(getString(R.string.add_member));
        chooseFriends = new HashSet<>();
        listAdapter = new ChooseFriendListAdapter(this, currentSite);
        listAdapter.setChooseFriendListener(this);
        friendRv.setAdapter(listAdapter);
        ZalyTaskExecutor.executeUserTask(TAG, new GetFriendNonGroupMembersTask());
    }


    private void addFriends() {
        List<String> friends = new ArrayList<>(chooseFriends);
        if (friends.size() == 0) {
            Toaster.showInvalidate("请选择好友");
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new AddFriendGroupTask(friends));
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
    }


    @OnClick(R.id.action_confirm)
    public void onViewClicked() {
        addFriends();
    }

    class GetFriendNonGroupMembersTask extends ZalyTaskExecutor.Task<Void, Void, ApiGroupNonMembersProto.ApiGroupNonMembersResponse> {

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected ApiGroupNonMembersProto.ApiGroupNonMembersResponse executeTask(Void... params) throws Exception {
            return ApiClient.getInstance(currentSite).getGroupApi().getFriendNonMemberList(groupId);
        }

        @Override
        protected void onTaskSuccess(ApiGroupNonMembersProto.ApiGroupNonMembersResponse response) {
            super.onTaskSuccess(response);
            listAdapter.addItems(response.getProfileList());
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }

    class AddFriendGroupTask extends ZalyTaskExecutor.Task<Void, Void, ApiGroupAddMemberProto.ApiGroupAddMemberResponse> {

        private List<String> friends;

        public AddFriendGroupTask(List<String> friends) {
            this.friends = friends;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected ApiGroupAddMemberProto.ApiGroupAddMemberResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getGroupApi().addFriendGroup(groupId,friends);
        }

        @Override
        protected void onTaskSuccess(ApiGroupAddMemberProto.ApiGroupAddMemberResponse apiGroupAddMemberResponse) {
            super.onTaskSuccess(apiGroupAddMemberResponse);
            Toaster.showInvalidate("邀请好友成功");
            finish();
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            Toaster.showInvalidate("邀请好友失败，请稍候再试");
            finish();
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }

}
