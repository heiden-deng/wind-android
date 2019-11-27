package com.windchat.client.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.windchat.client.R;
import com.windchat.client.ZalyApplication;
import com.windchat.client.api.ApiClient;
import com.windchat.client.api.ZalyAPIException;
import com.windchat.client.bean.Site;
import com.windchat.client.bean.event.AppEvent;
import com.windchat.client.chat.view.impl.U2MessageActivity;
import com.windchat.client.constant.IntentKey;
import com.windchat.client.constant.SiteConfig;
import com.windchat.client.friend.adapter.ContactsAdapter;
import com.windchat.client.friend.listener.OnContactItemClickListener;
import com.windchat.client.group.GroupContactsActivity;
import com.windchat.client.maintab.BaseFragment;
import com.windchat.client.maintab.BubbleUpdateListener;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.task.ZalyTaskExecutor;
import com.windchat.client.view.IndexFastScrollRecyclerView;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.site.ApiFriendListProto;
import com.blankj.utilcode.util.CacheDiskUtils;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.windchat.client.Configs.KEY_NEW_APPLY_FRIEND;

/**
 * Created by yichao on 2017/10/9.
 * <p>
 * 通讯录
 */

public class ContactsFragment extends BaseFragment {
    public static final String TAG = ContactsFragment.class.getSimpleName();
    @BindView(R.id.member_recycler)
    IndexFastScrollRecyclerView contactsRecycler;
    private LinearLayoutManager linearLayoutManager;
    private ContactsAdapter contactsAdapter;
    private BubbleUpdateListener bubbleUpdateListener;
    long start;
    public Site currentSite;

    /**
     * 传入需要的参数，设置给arguments
     *
     * @param site
     * @return
     */
    public static ContactsFragment getObject(Site site) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(IntentKey.KEY_CURRENT_SITE, site);
        ContactsFragment contactsFragment = new ContactsFragment();
        contactsFragment.setArguments(bundle);
        return contactsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            currentSite = bundle.getParcelable(IntentKey.KEY_CURRENT_SITE);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected int getLayout() {
        return R.layout.activity_fragment_contact;
    }

    @Override
    protected void initViews(View contentView) {

        ButterKnife.bind(this, contentView);
        linearLayoutManager = new LinearLayoutManager(getContext());
        contactsRecycler.setLayoutManager(linearLayoutManager);
        contactsRecycler.setIndexBarVisibility(false);
        contactsAdapter = new ContactsAdapter(getActivity(), currentSite);
        contactsRecycler.setAdapter(contactsAdapter);
        contactsAdapter.setOnClickListener(
                new OnContactItemClickListener() {
                    @Override
                    public void onFriendClick(UserProto.SimpleUserProfile profile) {
                        Intent intent = new Intent(getActivity(), U2MessageActivity.class);
                        intent.putExtra(IntentKey.KEY_FRIEND_SITE_USER_ID, profile.getSiteUserId());
                        intent.putExtra(IntentKey.KEY_FRIEND_USER_NAME, profile.getUserName());
                        intent.putExtra(IntentKey.KEY_FRIEND_PROFILE, profile.toByteArray());
                        intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                        startActivity(intent);
                    }
                },
                new ContactsAdapter.OnHeaderClickListener() {
                    @Override
                    public void onNewFriendClick() {
                        refreshNewFriendBubble();
                        Intent intent = new Intent(getActivity(), FriendApplyListActivity.class);
                        intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                        startActivity(intent);
                    }

                    @Override
                    public void onGroupContactsClick() {
                        Intent intent = new Intent(getActivity(), GroupContactsActivity.class);
                        intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                        startActivity(intent);
                    }
                });
        if (currentSite == null || ZalyApplication.siteList == null || ZalyApplication.siteList.size() == 0) {
            contactsAdapter.hideHeader();
            contactsAdapter.removeAllItems();
            bubbleUpdateListener.onContactBubbleChange(false);
            refreshIndexerVisibility();
            return;
        }
        contactsAdapter.showHeader();
        loadContact();
    }

    @Override
    protected void onLoad() {
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        Bundle bundle = getArguments();
//        currentSite = bundle.getParcelable(IntentKey.KEY_CURRENT_SITE);
//
//        if (currentSite != null) {
//            refreshNewFriendBubble();
//        }
//    }

    /**
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BubbleUpdateListener)
            this.bubbleUpdateListener = (BubbleUpdateListener) context;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactEvent(AppEvent event) {
        start = System.currentTimeMillis();
        switch (event.getAction()) {
            case AppEvent.ACTION_NEW_FRIEND:
                refreshNewFriendBubble();
                break;
            case AppEvent.ACTION_RELOAD:
                loadContact();
                break;
            case AppEvent.ACTION_SWITCH_SITE:
                currentSite = event.getData().getParcelable(IntentKey.KEY_CURRENT_SITE);
                contactsAdapter.setCurrentSite(currentSite);
                contactsAdapter.removeAllItems();
                loadContact();
                break;

        }
    }

    /**
     * 更新新朋友请求的小气泡和主页底部的小气泡.
     */
    private void refreshNewFriendBubble() {
        Boolean isApplyFriend = ZalyApplication.getCfgSP().getBoolean(currentSite.getSiteIdentity() + KEY_NEW_APPLY_FRIEND);
        contactsAdapter.refreshNewFriendBubble(isApplyFriend);
        bubbleUpdateListener.onContactBubbleChange(isApplyFriend);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            refreshIndexerVisibility();
            contactsAdapter.notifyDataSetChanged();
        }
    };


    /**
     * 加载通讯录.
     */
    private void loadContact() {
        start = System.currentTimeMillis();
        if (currentSite == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                ZalyTaskExecutor.executeUserTask(TAG, new GetFriendListTask());
            }
        }).start();

    }

    private void refreshIndexerVisibility() {
        boolean showIndexer = (contactsRecycler.canScrollVertically(-1)) ||
                (contactsRecycler.canScrollVertically(1));
        contactsRecycler.setIndexBarVisibility(showIndexer);
    }


    /**
     * 好友通讯录.
     */
    class GetFriendListTask extends ZalyTaskExecutor.Task<Void, Void, ApiFriendListProto.ApiFriendListResponse> {

        private long start;

        @Override
        protected void onPreTask() {
            super.onPreTask();
            this.start = System.currentTimeMillis();
        }

        @Override
        protected void onCacheTask() {
            byte[] cache = CacheDiskUtils.getInstance().getBytes(currentSite.getSiteIdentity() + SiteConfig.FRIEND_LIST);
            if (cache == null)
                return;
            try {
                ApiFriendListProto.ApiFriendListResponse response = ApiFriendListProto.ApiFriendListResponse.parseFrom(cache);
                displayUI(response);
            } catch (Exception ex) {
                ZalyLogUtils.getInstance().exceptionError(ex);
            }

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
                //   ZalyApplication.getCfgSP().put(currentSite.getSiteIdentity() + SiteConfig.FRIEND_LIST, Base64.encodeToString(response.toByteArray(), Base64.NO_WRAP));
            }
            displayUI(response);

        }

        public void displayUI(ApiFriendListProto.ApiFriendListResponse response) {

            List<UserProto.SimpleUserProfile> friendSimpleProfiles = response.getListList();
            ZalyLogUtils.getInstance().info(TAG, " friendSimpleProfile sie ==" + friendSimpleProfiles.size());
            if (friendSimpleProfiles == null || friendSimpleProfiles.size() == 0) {
                contactsAdapter.removeAllItems();
            } else {
                contactsAdapter.addAllItems(friendSimpleProfiles);
                contactsAdapter.sortItems();
                handler.sendEmptyMessage(1);
            }

        }

        @Override
        protected void onTaskError(Exception e) {

            // TODO: show error
            super.platformLoginByError(e);
            contactsAdapter.removeAllItems();
            ZalyLogUtils.getInstance().exceptionError(e);

        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            ZalyLogUtils.getInstance().exceptionError(zalyAPIException);
            super.platformLoginByApiError(zalyAPIException);
            contactsAdapter.removeAllItems();
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();

        }
    }
}