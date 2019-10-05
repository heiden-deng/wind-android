package com.akaxin.client.group;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.akaxin.client.R;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.chat.view.impl.GroupMsgActivity;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.db.bean.UserGroupBean;
import com.akaxin.client.group.adapter.ChooseFriendListAdapter;
import com.akaxin.client.group.listener.ChooseFriendListener;
import com.akaxin.client.group.presenter.impl.GroupPresenter;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.GroupProto;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.site.ApiFriendListProto;
import com.akaxin.proto.site.ApiGroupCreateProto;
import com.blankj.utilcode.util.CacheDiskUtils;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yichao on 2017/10/26.
 */

public class GroupCreateActivity extends BaseActivity implements ChooseFriendListener {

    @BindView(R.id.group_name_edit)
    EditText groupNameEt;
    @BindView(R.id.member_recycler)
    RecyclerView friendRv;
    @BindView(R.id.action_confirm)
    View addConfirm;
    private LinearLayoutManager mLayoutManager;
    private Set<String> chooseFriends;
    private ChooseFriendListAdapter listAdapter;
    private Site currentSite;


    @Override
    public int getResLayout() {
        return R.layout.activity_create_group;
    }

    public void initView() {
        ButterKnife.bind(this);
        mLayoutManager = new LinearLayoutManager(this);
        friendRv.setLayoutManager(mLayoutManager);
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {
        setCenterTitle(R.string.title_create_group);
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        chooseFriends = new HashSet<>();
        listAdapter = new ChooseFriendListAdapter(this, currentSite);
        listAdapter.setChooseFriendListener(this);
        friendRv.setAdapter(listAdapter);
        ZalyTaskExecutor.executeUserTask(TAG, new GetFriendListTask());
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


    private void addFriends() {
        hideSoftKey();
        String groupName = groupNameEt.getText().toString().trim();
        if (StringUtils.isEmpty(groupName)) {
            Toaster.showInvalidate("请输入群名称");
            return;
        }

        List<String> friends = new ArrayList<>(chooseFriends);
        if (friends.size() == 0) {
            Toaster.showInvalidate("请选择好友");
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new CreateGroupTask(groupName, friends));
    }


    @OnClick(R.id.action_confirm)
    public void onViewClicked() {
        addFriends();
    }


    class GetFriendListTask extends ZalyTaskExecutor.Task<Void, Void, ApiFriendListProto.ApiFriendListResponse> {

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected void onCacheTask() {
            byte[] cache = CacheDiskUtils.getInstance().getBytes(currentSite.getSiteIdentity() + SiteConfig.FRIEND_LIST);
            try {
                ApiFriendListProto.ApiFriendListResponse response = ApiFriendListProto.ApiFriendListResponse.parseFrom(cache);
                listAdapter.addItems(response.getListList());
            } catch (Exception ex) {
                ZalyLogUtils.getInstance().errorToInfo(TAG, ex.getMessage());
            }

//            String cacheFriendList = ZalyApplication.getCfgSP().getString(currentSite.getSiteIdentity() + SiteConfig.FRIEND_LIST);
//            if (!StringUtils.isEmpty(cacheFriendList)) {
//                byte[] data = Base64.decode(cacheFriendList, Base64.NO_WRAP);
//                try {
//                    ApiFriendListProto.ApiFriendListResponse response = ApiFriendListProto.ApiFriendListResponse.parseFrom(data);
//                    listAdapter.addItems(response.getListList());
//                } catch (Exception ex) {
//                    WindLogger.getInstance().errorToInfo(TAG, ex.getMessage());
//                }
//            }

        }

        @Override
        protected ApiFriendListProto.ApiFriendListResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getFriendApi().getSiteFriend(currentSite.getSiteUserId());
        }


        @Override
        protected void onTaskSuccess(ApiFriendListProto.ApiFriendListResponse response) {
            super.onTaskSuccess(response);
            List<UserProto.SimpleUserProfile> simpleUserProfiles = response.getListList();
            if (simpleUserProfiles != null) {
                CacheDiskUtils.getInstance().put(currentSite.getSiteIdentity() + SiteConfig.FRIEND_LIST, response.toByteArray());
                //  ZalyApplication.getCfgSP().put(currentSite.getSiteIdentity() + SiteConfig.FRIEND_LIST, Base64.encodeToString(response.toByteArray(), Base64.NO_WRAP));
            }
            if (simpleUserProfiles == null || simpleUserProfiles.size() == 0) {
                Toaster.showInvalidate("暂无好友");
                return;
            }
            listAdapter.addItems(simpleUserProfiles);
        }

        @Override
        protected void onTaskError(Exception e) {
            listAdapter.removeAllItems();
            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            listAdapter.removeAllItems();
            ZalyLogUtils.getInstance().info(TAG, zalyAPIException.getMessage());
        }

    }

    class CreateGroupTask extends ZalyTaskExecutor.Task<Void, Void, ApiGroupCreateProto.ApiGroupCreateResponse> {

        private String groupName;
        private List<String> friends;

        public CreateGroupTask(String groupName, List<String> friends) {
            this.groupName = groupName;
            this.friends = friends;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected ApiGroupCreateProto.ApiGroupCreateResponse executeTask(Void... voids) throws Exception {
            friends.add(currentSite.getSiteUserId());
            return ApiClient.getInstance(currentSite).getGroupApi().createGroup(groupName, friends);
        }

        @Override
        protected void onTaskSuccess(ApiGroupCreateProto.ApiGroupCreateResponse apiGroupCreateResponse) {
            super.onTaskSuccess(apiGroupCreateResponse);
            ////TODO 群组信息写入数据库
            GroupProto.GroupProfile profile = apiGroupCreateResponse.getProfile();
            GroupProto.SimpleGroupProfile simpleGroupProfile = GroupProto.SimpleGroupProfile.newBuilder()
                    .setGroupId(profile.getId())
                    .setGroupName(profile.getName())
                    .setGroupIcon(profile.getIcon())
                    .build();
            UserGroupBean userGroupBean = new UserGroupBean();

            userGroupBean.setGroupId(profile.getId());
            userGroupBean.setGroupName(simpleGroupProfile.getGroupName());
            userGroupBean.setGroupCountMember(friends.size());
            userGroupBean.setGroupOwnerId(currentSite.getSiteUserId());
            userGroupBean.setGroupOwnerName(currentSite.getSiteUserName());
            userGroupBean.setGroupOwnerIcon(currentSite.getSiteUserImage());
            userGroupBean.setMute(false);///默认不是静音的
            userGroupBean.setCloseInviteGroupChat(true);////默认只有群主可以邀请人入群
            userGroupBean.setAsGroupMember(true);///是群成员

            GroupPresenter.getInstance(currentSite).createGroupSimpleProfile(userGroupBean);

            Intent intent = new Intent(getContext(), GroupMsgActivity.class);
            intent.putExtra(GroupMsgActivity.KEY_GROUP_ID, simpleGroupProfile.getGroupId());
            intent.putExtra(GroupMsgActivity.KEY_GROUP_NAME, groupName);
            intent.putExtra(GroupMsgActivity.KEY_GROUP_PROFILE, simpleGroupProfile.toByteArray());
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
            startActivity(intent);
            finish();
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }
}
