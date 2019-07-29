package com.akaxin.client.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.adapter.SessionAdapter;
import com.akaxin.client.bean.ChatSession;
import com.akaxin.client.bean.Session;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bean.event.AppEvent;
import com.akaxin.client.chat.view.impl.GroupMsgActivity;
import com.akaxin.client.chat.view.impl.MessageActivity;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.dialog.SessionMenuItem;
import com.akaxin.client.maintab.BubbleUpdateListener;
import com.akaxin.client.mvp.MVPBaseFragment;
import com.akaxin.client.mvp.contract.SessionContract;
import com.akaxin.client.mvp.presenter.SessionPresenter;
import com.akaxin.client.site.task.GetSitesInfoTask;
import com.akaxin.client.util.task.ZalyTaskExecutor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Created by yichao on 2017/10/9.
 */

public class SessionFragment extends MVPBaseFragment<SessionContract.View, SessionPresenter> implements SessionContract.View, SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.session_list)
    RecyclerView sessionRv;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    Unbinder unbinder;
    private SessionAdapter sessionTabAdapter;
    private BubbleUpdateListener bubbleUpdateListener;
    public Site currentSite;


    /**
     * 传入需要的参数，设置给arguments
     *
     * @param site
     * @return
     */
    public static SessionFragment getObject(Site site) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(IntentKey.KEY_CURRENT_SITE, site);
        SessionFragment contactsFragment = new SessionFragment();
        contactsFragment.setArguments(bundle);
        return contactsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment_session, container, false);
        unbinder = ButterKnife.bind(this, view);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            currentSite = bundle.getParcelable(IntentKey.KEY_CURRENT_SITE);
        }

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(this);
        sessionTabAdapter = new SessionAdapter(getContext(), currentSite);
        sessionTabAdapter.setListListener(new SessionAdapter.SessionListListener() {
            @Override
            public void onSessionClick(ChatSession chatSession) {
                if (chatSession.getType() == Session.TYPE_FRIEND_SESSION) {
                    Intent intent = new Intent(getActivity(), MessageActivity.class);
                    intent.putExtra(IntentKey.KEY_FRIEND_SITE_USER_ID, chatSession.getChatSessionId());
                    intent.putExtra(IntentKey.KEY_FRIEND_USER_NAME, chatSession.getTitle());
                    intent.putExtra(IntentKey.KEY_MSG_UNREAD_NUM, chatSession.getUnreadNum());
                    intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), GroupMsgActivity.class);
                    intent.putExtra(GroupMsgActivity.KEY_GROUP_ID, chatSession.getChatSessionId());
                    intent.putExtra(GroupMsgActivity.KEY_GROUP_NAME, chatSession.getTitle());
                    intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                    startActivity(intent);
                }
            }

            @Override
            public boolean onSessionLongClick(ChatSession chatSession) {
                SessionMenuItem menuItem = new SessionMenuItem(getContext(), chatSession, currentSite);
                menuItem.show();
                return true;
            }
        });
        sessionRv.setLayoutManager(new LinearLayoutManager(getContext()));
        sessionRv.setAdapter(sessionTabAdapter);


        if (currentSite == null || ZalyApplication.siteList == null || ZalyApplication.siteList.size() == 0) {
            bubbleUpdateListener.onSessionBubbleChange(0);
            sessionTabAdapter.removeAllItems();
        }

        return view;
    }


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

    @Override
    public void onResume() {
        super.onResume();
        // loading session
        mPresenter.loadChatSession(currentSite);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
        super.onDestroyView();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ZalyTaskExecutor.cancleAllTasksByTag(TAG);
    }

    /**
     * im 进程每次 Sync 到新消息通过 ContentProvider 与主进程通讯, 若是本站新消息, 则把有新消息到事件传递给
     * SessionFragment, 使得 SessionFragment 从库里拉取新的 session. 这时 EventBus 不会把事件发送给
     * mainActivity 使之更新底栏消息帧气泡, 只有当 SessionFragment 每次拉取完消息发现有未读时, 才通知
     * mainActivity 更新底栏消息帧气泡.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSessionEvent(AppEvent event) {
        switch (event.getAction()) {
            case AppEvent.ACTION_UPDATE_SESSION_LIST:
                mPresenter.loadChatSession(currentSite);
                break;
            case AppEvent.ACTION_SWITCH_SITE:
                currentSite = event.getData().getParcelable(IntentKey.KEY_CURRENT_SITE);
                sessionTabAdapter.setCurrentSite(currentSite);
                mPresenter.loadChatSession(currentSite);
                break;

        }
    }

    @Override
    public void onRefresh() {
        if (currentSite == null || ZalyApplication.siteList == null || ZalyApplication.siteList.size() == 0) {
            bubbleUpdateListener.onSessionBubbleChange(0);
            sessionTabAdapter.removeAllItems();
            return;
        }
        mPresenter.loadChatSession(currentSite);
        ZalyTaskExecutor.executeTask(TAG, new GetSitesInfoTask(currentSite));
    }

    @Override
    public void onTaskStart(String content) {

    }

    @Override
    public void onTaskFinish() {

    }

    @Override
    public void onLoadChatSessionSuccess(List<ChatSession> chatSessions) {
        int totalUnread = 0;
        if (chatSessions != null && chatSessions.size() > 0) {
            for (ChatSession chatSession : chatSessions) {
                totalUnread += chatSession.getUnreadNum();
            }
            sessionTabAdapter.addAllItems(chatSessions);
            emptyView.setVisibility(View.GONE);
        } else {
            sessionTabAdapter.removeAllItems();
            emptyView.setVisibility(View.VISIBLE);
        }
        bubbleUpdateListener.onSessionBubbleChange(totalUnread);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoadChatSessionError() {
        sessionTabAdapter.removeAllItems();
        emptyView.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }
}
