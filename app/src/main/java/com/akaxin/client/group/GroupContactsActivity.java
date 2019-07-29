package com.akaxin.client.group;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.akaxin.client.R;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.chat.view.impl.GroupMsgActivity;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.group.adapter.GroupListAdapter;
import com.akaxin.client.group.listener.GroupListListener;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.GroupProto;
import com.akaxin.proto.site.ApiGroupListProto;
import com.blankj.utilcode.util.CacheDiskUtils;


import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupContactsActivity extends BaseActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    GroupListAdapter adapter;

    private Site currentSite;

    @Override
    public int getResLayout() {
        return R.layout.activity_group_contacts;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        setMultTitle(getString(R.string.group_contacts), StringUtils.getSiteSubTitle(currentSite));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupListAdapter(this, currentSite);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void initEvent() {
        adapter.setListListener(new GroupListListener() {
            @Override
            public void onGroupClick(GroupProto.SimpleGroupProfile profile) {
                if (profile == null) {
                    Toaster.showInvalidate("请稍候再试");
                    return;
                }
                Intent intent = new Intent(GroupContactsActivity.this, GroupMsgActivity.class);
                intent.putExtra(GroupMsgActivity.KEY_GROUP_ID, profile.getGroupId());
                intent.putExtra(GroupMsgActivity.KEY_GROUP_NAME, profile.getGroupName());
                intent.putExtra(GroupMsgActivity.KEY_GROUP_PROFILE, profile.toByteArray());
                intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(intent);
            }
        });
    }

    @Override
    public void initPresenter() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        ZalyTaskExecutor.executeUserTask(TAG, new GetGroupListTask());
    }

    @Override
    public void onLoadData() {
        boolean isNet = NetUtils.getNetInfo();
        if (isNet) {
            ZalyTaskExecutor.executeUserTask(TAG, new GetGroupListTask());
        }
    }

    /**
     * 从网络获取群通讯录.
     */
    class GetGroupListTask extends ZalyTaskExecutor.Task<Void, Void, ApiGroupListProto.ApiGroupListResponse> {

        @Override
        protected void onCacheTask() {
            byte[] cache = CacheDiskUtils.getInstance().getBytes(currentSite.getSiteIdentity() + SiteConfig.GROUP_LIST);
            try {
                ApiGroupListProto.ApiGroupListResponse apiGroupListResponse = ApiGroupListProto.ApiGroupListResponse.parseFrom(cache);
                displayUI(apiGroupListResponse);
            } catch (Exception e) {
                ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            }


//            String cacheFriendList = ZalyApplication.getCfgSP().getString(currentSite.getSiteIdentity() + SiteConfig.GROUP_LIST);
//            if (!StringUtils.isEmpty(cacheFriendList)) {
//                byte[] data = Base64.decode(cacheFriendList, Base64.NO_WRAP);
//                try {
//                    ApiGroupListProto.ApiGroupListResponse apiGroupListResponse = ApiGroupListProto.ApiGroupListResponse.parseFrom(data);
//                    displayUI(apiGroupListResponse);
//                } catch (Exception e) {
//                    ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
//                }
//            }
        }

        @Override

        protected ApiGroupListProto.ApiGroupListResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getGroupApi().getGroupFromSite(currentSite.getSiteUserId());
        }

        @Override
        protected void onTaskSuccess(ApiGroupListProto.ApiGroupListResponse apiGroupListResponse) {
            super.onTaskSuccess(apiGroupListResponse);
            List<GroupProto.SimpleGroupProfile> groupsimpleProfiles = apiGroupListResponse.getListList();
            if (groupsimpleProfiles != null && groupsimpleProfiles.size() > 0) {
                CacheDiskUtils.getInstance().put(currentSite.getSiteIdentity() + SiteConfig.GROUP_LIST, apiGroupListResponse.toByteArray());
                //    ZalyApplication.getCfgSP().put(currentSite.getSiteIdentity() + SiteConfig.GROUP_LIST, Base64.encodeToString(apiGroupListResponse.toByteArray(), Base64.NO_WRAP));
            }
            displayUI(apiGroupListResponse);
        }

        public void displayUI(ApiGroupListProto.ApiGroupListResponse apiGroupListResponse) {
            if (apiGroupListResponse.getListList() == null) {
                // TODO: show error info
                adapter.removeAllItems();
                return;
            }
            if (apiGroupListResponse.getListCount() == 0) {
                adapter.removeAllItems();
                return;
            }
            adapter.addAllItems(apiGroupListResponse.getListList());
        }


        @Override
        protected void onTaskError(Exception e) {
            adapter.removeAllItems();
            // TODO: show error info
            super.platformLoginByError(e);
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyApiExcetion) {
            adapter.removeAllItems();
            super.platformLoginByApiError(zalyApiExcetion);
        }

    }

}
