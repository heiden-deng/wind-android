package com.akaxin.client.chat.view.impl;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akaxin.client.Configs;
import com.akaxin.client.R;
import com.akaxin.client.bean.AudioInfo;
import com.akaxin.client.bean.Message;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bean.event.GroupEvent;
import com.akaxin.client.bean.event.MessageEvent;
import com.akaxin.client.chat.MessageAdapter;
import com.akaxin.client.chat.MessageAdapterListener;
import com.akaxin.client.chat.MessageRecyclerOnScrollListener;
import com.akaxin.client.chat.MessageViewHolder;
import com.akaxin.client.chat.presenter.IGroupMsgPresenter;
import com.akaxin.client.chat.presenter.impl.GroupMessagePresenter;
import com.akaxin.client.chat.view.IGroupMsgView;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.db.ZalyDbContentHelper;
import com.akaxin.client.group.view.impl.GroupProfileActivity;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.ZalyMainThreadExecutor;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.GroupProto;
import com.akaxin.proto.core.PluginProto;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.iwf.photopicker.PhotoPicker;

/**
 * Created by yichao on 2017/10/10.
 */

public class GroupMessageActivity extends BaseMsgActivity implements IGroupMsgView, View.OnClickListener {

    private static final String TAG = GroupMessageActivity.class.getSimpleName();

    public static final String KEY_GROUP_ID = "key_group_id";
    public static final String KEY_GROUP_NAME = "key_group_name";
    public static final String KEY_GROUP_PROFILE = "key_group_profile";

    @BindView(R.id.app_bar)
    View appBar;
    @BindView(R.id.title_layout)
    View titleLayout;
    @BindView(R.id.conn_status_bar)
    TextView connStatusBar;
    @BindView(R.id.unread_tip)
    TextView unreadTip;
    @BindView(R.id.secret_tip_layout)
    LinearLayout secretTipLayout;
    @BindView(R.id.secret_tip_content)
    TextView secretTipContent;
    @BindView(R.id.secret_tip_action)
    TextView secretTipAction;
    @BindView(R.id.audio_recording)
    TextView audioRecordingView;

    private MessageAdapter msgAdapter;
    private GroupProto.SimpleGroupProfile groupProfile;
    private IGroupMsgPresenter iPresenter;
    private String curAudioFilePath = null;
    private Site currentSite;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        msgRecycler.postDelayed(new Runnable() {
            @Override
            public void run() {
                iPresenter.loadNewMsgFromDB();
            }
        }, 3);

        String pluginId = getIntent().getStringExtra(KEY_PLUGIN_ID_FOR_JUMP);

        if (pluginId != null && pluginId.length() > 0) {
            String param = getIntent().getStringExtra(KEY_PLUGIN_PARAM);

            PluginProto.Plugin plugin = getPluginByPluginId(pluginId);

            if (plugin == null) {
                return;
            }
            loadMsgPlugin(plugin, param);
            getIntent().putExtra(KEY_PLUGIN_ID_FOR_JUMP, "");
            getIntent().putExtra(KEY_PLUGIN_PARAM, "");
        }
    }

    @Override
    public int getResLayout() {
        return R.layout.activity_group_message;
    }


    @Override
    public void initView() {
        super.initView();
        ButterKnife.bind(this);
    }

    @Override
    public void initEvent() {
        super.initEvent();
        msgRecycler.addOnScrollListener(new MessageRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                iPresenter.loadMoreHistoryMsg();
            }
        });

        sendButton.setOnClickListener(this);
        titleLayout.setOnClickListener(this);

        // noinspection AndroidLintClickableViewAccessibility
        msgAudio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    audioRecordingView.setVisibility(View.VISIBLE);
                    curAudioFilePath = Configs.getAudioDir() + "/" + new Date().getTime();
                    startRecording(curAudioFilePath);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setPressed(false);
                    audioRecordingView.setVisibility(View.GONE);
                    stopRecording();
                    long endRecordTime = System.currentTimeMillis();
                    long audioTime = endRecordTime - curAudioStartTime;
                    if (audioTime < 1000) {
                        Toaster.showInvalidate("录音时间过短");
                        return true;
                    }
                    if (iPresenter != null) {
                        iPresenter.sendAudioMessage(audioTime, curAudioFilePath);
                    }
                    return true;
                }
                return false;
            }
        });

        if (navBackLayout != null) {
            navBackLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iPresenter.cleanUnreadNum();
                    hideSoftKey();
                    hideProgress();
                    finish();
                }
            });
        }

    }

    boolean havePermiss = false;


    @Override
    public void onBackPressed() {
        if (expandView.getVisibility() == View.VISIBLE)
            hideExpandView();
        else {
            iPresenter.cleanUnreadNum();
            hideSoftKey();
            hideProgress();
            finish();
        }
    }

    @Override
    public void initPresenter() {
        iPresenter = new GroupMessagePresenter();
        iPresenter.bindView(this);
    }

    @Override
    public void onLoadData() {
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);

        String groupId = getIntent().getStringExtra(KEY_GROUP_ID);
        if (StringUtils.isEmpty(groupId)) {
            Toaster.showInvalidate(R.string.data_error);
            finish();
            return;
        }
        String groupName = getIntent().getStringExtra(KEY_GROUP_NAME);
        setMultTitle(groupName, StringUtils.getSiteSubTitle(currentSite));
        iPresenter.loadInitData(groupId, currentSite);
        chatSessionId = groupId;
        isGroup = true;

        try {
            if (getIntent().getByteArrayExtra(KEY_GROUP_PROFILE) != null) {
                groupProfile = GroupProto.SimpleGroupProfile.parseFrom(getIntent().getByteArrayExtra(KEY_GROUP_PROFILE));
                if (groupProfile != null) {
                    iPresenter.setGroupProfile(groupProfile);
                }
            }
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (chatSessionId.equals(chatSessionId)) {
            this.setIntent(intent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getAction()) {
            case Message.Action.UPDATE_MSG_STATUS:
                ZalyLogUtils.getInstance().info(TAG, "Receive UPDATE_MSG_STATUS");
                String msgId = event.getData().getString(ZalyDbContentHelper.KEY_MSG_ID);
                int msgStatus = event.getData().getInt(ZalyDbContentHelper.KEY_MSG_STATUS);
                iPresenter.updateMsgStatus(msgId, msgStatus);
                break;
            case Message.Action.MSG_RECEIVE:
                ZalyLogUtils.getInstance().info(TAG, "Receive MSG_RECEIVE");
                iPresenter.loadNewMsgFromRecevieDB();
                break;
            case Message.Action.GROUP_DELETE_MSG:
                Message msgDel = event.getData().getParcelable(Message.Action.MSG_INFO);
                iPresenter.deleteGroupMsg(msgDel.getMsgId());
                msgAdapter.removeMsgItems(msgDel);
                break;
            case Message.Action.GROUP_RESEND_MSG:
                Message msgResend = event.getData().getParcelable(Message.Action.MSG_INFO);
                iPresenter.resendMessage(msgResend);
                break;
            case Message.Action.IMG_PROGRESS:
                int num = event.getData().getInt(ZalyDbContentHelper.IMG_PROCESS_NUM);
                Message msg = event.getData().getParcelable(ZalyDbContentHelper.IMG_PROCESS_MSG_INFO);
                msgAdapter.setProcessNum(msg, num);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("UnusedDeclaration")
    public void onAudioEvent(final AudioInfo audioInfo) {
        Logger.i(TAG, "onAudioEvent: " + AudioInfo.toJSON(audioInfo));
        if (audioInfo != null && !StringUtils.isEmpty(audioInfo.getAudioFilePath())) {
            playingAudioFilePath = audioInfo.getAudioFilePath();
            audioManager.setSpeakerphoneOn(true);
            AnimationDrawable voiceAD = msgAdapter.getVoiceAnimationDrawable();
            startPlaying(audioInfo.getAudioFilePath(), voiceAD);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupEvent(GroupEvent event) {
        switch (event.getAction()) {
            case GroupEvent.ACTION_DEL_GROUP:
            case GroupEvent.ACTION_QUIT_GROUP:
                Bundle bundle = event.getData();
                if (bundle != null) {
                    String delGroupId = bundle.getString(GroupEvent.KEY_GROUP_ID);
                    if (!StringUtils.isEmpty(delGroupId) && iPresenter != null &&
                            delGroupId.equals(iPresenter.getGroupId())) {
                        finish();
                    }
                }
                break;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.send_button:
                sendMessage();
                break;
            case R.id.title_layout:
                Pair<View, String> appBarPair = Pair.create(appBar, "app_bar");
                Pair<View, String> titleLayoutPair = Pair.create(titleLayout, "title_layout");
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(GroupMessageActivity.this, appBarPair, titleLayoutPair);
                Intent intent = new Intent(this, GroupProfileActivity.class);
                intent.putExtra(IntentKey.KEY_GROUP_ID, iPresenter.getGroupId());
                intent.putExtra(IntentKey.KEY_IS_GROUP_MEMBER, true);
                intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(intent, options.toBundle());
                break;
        }
    }

    private void sendMessage() {
        String content = msgEdit.getText().toString();
        if (!StringUtils.isEmpty(content)) {
            Message message = new Message();
            message.setContent(content);
            iPresenter.sendTextMessage(message);
        }
    }

    /* implementing IGroupMsgView */

    @Override
    public void onStartSendingMessage(final Message message) {
        if (msgEdit != null && msgAdapter != null) {
            msgEdit.setText("");
            msgAdapter.addNewMsgItem(message);
            msgRecycler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    msgRecycler.scrollToPosition(0);
                }
            }, 50);
            boolean isNet = NetUtils.getNetInfo();
            if (!isNet) {
                iPresenter.refreshMsgStatus(message.getMsgId());
                return;
            }
            msgEdit.postDelayed(new Runnable() {
                @Override
                public void run() {
                    iPresenter.refreshMsgStatus(message.getMsgId());
                }
            }, MessageViewHolder.MSG_SEND_TIME_OUT + 2000);
        }
    }

    @Override
    public void refreshMsgStatus(final Message message) {
        if (msgEdit != null && msgAdapter != null) {
            msgEdit.setText("");
            msgRecycler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    msgRecycler.scrollToPosition(0);
                }
            }, 50);
            msgEdit.postDelayed(new Runnable() {
                @Override
                public void run() {
                    iPresenter.refreshMsgStatus(message.getMsgId());
                }
            }, MessageViewHolder.MSG_SEND_TIME_OUT + 2000);
        }
    }

    @Override
    public void onSendingMessageError(Message message) {

    }

    @Override
    public void onSendMessageFinish(final Message message) {
        msgAdapter.notifyItemChanged(message);
    }

    @Override
    public void onReceiveNewMessage() {
        if (!msgRecycler.canScrollVertically(1))
            msgRecycler.scrollToPosition(0);
    }

    @Override
    public void setMsgRvAdapter(MessageAdapter msgRvAdapter) {
        if (msgRecycler != null) {
            msgRecycler.setAdapter(msgRvAdapter);
            msgAdapter = msgRvAdapter;
            msgAdapter.setAdapterListener(new MessageAdapterListener() {
                @Override
                public void onMessageClick(int type, Message message) {
                    hideSoftKey();
                    hideExpandView();
                }
            });
        }
    }

    @Override
    public void openSecretFail() {
        Toaster.showInvalidate("开启绝密失败，请稍候再试");
    }

    @Override
    public Context getContext() {
        return this;
    }


    @Override
    public void setExpandViewData(List<PluginProto.Plugin> pluginProfiles) {
        if (expandView != null) {
            expandView.setPluginProfiles(pluginProfiles, currentSite);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iPresenter != null) {
            iPresenter.onDestroy();
        }
        ZalyMainThreadExecutor.cancelAllRunnables(TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PhotoPicker.REQUEST_CODE:
                    ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                    if (photos == null || photos.size() == 0) {
                        Toaster.showInvalidate("请稍候再试");
                        return;
                    }
                    if (iPresenter != null) {
                        iPresenter.sendImgMessage(photos.get(0));
                    }
                    break;
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            ZalyLogUtils.getInstance().info("finish time-page-change-chat-session-chat", System.currentTimeMillis() + "");
    }

    @Override
    public void quitWebView() {
        hideMsgPlugin();
        destoryMsgPluginWebView();
        generateMsgPluginWebView();
    }
}
