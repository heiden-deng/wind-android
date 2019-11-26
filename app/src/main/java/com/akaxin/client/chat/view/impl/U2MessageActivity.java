package com.akaxin.client.chat.view.impl;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akaxin.client.Configs;
import com.akaxin.client.R;
import com.akaxin.client.bean.AudioInfo;
import com.akaxin.client.bean.Message;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bean.event.MessageEvent;
import com.akaxin.client.chat.DeviceListActivity;
import com.akaxin.client.chat.MessageAdapter;
import com.akaxin.client.chat.MessageAdapterListener;
import com.akaxin.client.chat.MessageRecyclerOnScrollListener;
import com.akaxin.client.chat.MessageViewHolder;
import com.akaxin.client.chat.MsgContentActivity;
import com.akaxin.client.chat.presenter.IMessagePresenter;
import com.akaxin.client.chat.presenter.impl.U2MessagePresenter;
import com.akaxin.client.chat.view.IMessageView;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.db.ZalyDbContentHelper;
import com.akaxin.client.friend.FriendProfileActivity;
import com.akaxin.client.friend.presenter.impl.UserProfilePresenter;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.ZalyMainThreadExecutor;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.DeviceProto;
import com.akaxin.proto.core.PluginProto;
import com.akaxin.proto.core.UserProto;
import com.kyleduo.switchbutton.SwitchButton;
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

import static com.akaxin.client.chat.MsgContentActivity.KEY_DEVICE_NAME;
import static com.akaxin.client.chat.MsgContentActivity.KEY_NOT_VIEWABLE;

/**
 * Created by yichao on 2017/10/10.
 */

public class U2MessageActivity extends BaseMsgActivity implements IMessageView {

    public static final String TAG = U2MessageActivity.class.getSimpleName();


    public static final int MSG_PREVIEW_IMG = 1;

    @BindView(R.id.app_bar)
    View appBar;
    @BindView(R.id.title_layout)
    View titleLayout;
    @BindView(R.id.secret_switch)
    SwitchButton secretSwitch;
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

    private String curAudioFilePath = null;
    private IMessagePresenter iPresenter;
    private int unreadNum;
    private String friendSiteUserId;
    int type = 2;
    private Site currentSite;


    @Override
    public int getResLayout() {
        return R.layout.activity_message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                iPresenter.loadMoreMsg();
            }
        });

        msgRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                WebView webView = new WebView(U2MessageActivity.this);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    /**
                     * 恢复所有WebView的所有布局，解析和JavaScript计时器，将恢复调度所有计时器.
                     */
                    webView.resumeTimers();
                } else {
                    /**
                     * 暂停所有WebView的布局，解析和JavaScript定时器。 这个是一个全局请求，不仅限于这个WebView。
                     */
                    webView.pauseTimers();
                }
            }
        });
        titleLayout.setOnClickListener(this);

        secretSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    iPresenter.switchSecretMode(true);
                } else {
                    iPresenter.switchSecretMode(false);
                }
            }
        });

        sendButton.setOnClickListener(this);
        unreadTip.setOnClickListener(this);
        secretTipAction.setOnClickListener(this);

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
                        Toaster.showInvalidate("录音时间过短");  // TODO
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




    @Override
    public void initPresenter() {
        iPresenter = new U2MessagePresenter();
        iPresenter.bindView(this);
    }

    @Override
    public void onLoadData() {
        this.currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);

        friendSiteUserId = getIntent().getStringExtra(IntentKey.KEY_FRIEND_SITE_USER_ID);
        if (StringUtils.isEmpty(friendSiteUserId)) {
            Toaster.showInvalidate("消息异常请稍候再试");
            finish();
            return;
        }
        chatSessionId = friendSiteUserId;
        isGroup = false;
        String friendUserName = getIntent().getStringExtra(IntentKey.KEY_FRIEND_USER_NAME);
        if (StringUtils.isEmpty(friendUserName)) {
            setMultTitle(friendSiteUserId, StringUtils.getSiteSubTitle(this.currentSite));
        } else {
            setMultTitle(friendUserName, StringUtils.getSiteSubTitle(this.currentSite));
        }

        iPresenter.loadInitData(friendSiteUserId, currentSite);
        unreadNum = getIntent().getIntExtra(IntentKey.KEY_MSG_UNREAD_NUM, 0);
        showUnreadMsgNum();
        iPresenter.LoadIsOpenTsFromChatSession();

        try {
            byte[] friendProfileBytes = getIntent().getByteArrayExtra(IntentKey.KEY_FRIEND_PROFILE);
            if (friendProfileBytes != null && friendProfileBytes.length > 0) {
                UserProto.SimpleUserProfile profile = UserProto.SimpleUserProfile.parseFrom(friendProfileBytes);
                iPresenter.setFriendSimpleProfile(profile);
            }
        } catch (Exception e) {
            // TODO: show error
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
        }
        this.currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("UnusedDeclaration")
    public void onMessageEvent(MessageEvent event) {

        switch (event.getAction()) {
            case Message.Action.UPDATE_MSG_STATUS:
                String msgId = event.getData().getString(ZalyDbContentHelper.KEY_MSG_ID);
                int msgStatus = event.getData().getInt(ZalyDbContentHelper.KEY_MSG_STATUS);
                iPresenter.updateMsgStatus(msgId, msgStatus);
                break;
            case Message.Action.MSG_RECEIVE:
                ZalyLogUtils.getInstance().info(TAG, "Receive MSG_RECEIVE");
                iPresenter.loadNewMsgFromRecevieDB();
                break;
            case Message.Action.SHOW_SECRET_DEVICE:
                String secretDeviceId = event.getData().getString(Message.KEY_DEVICE_ID);
                iPresenter.getSecretDeviceInfo(secretDeviceId);
                break;
            case Message.Action.U2_DELETE_MSG:
                Message msgDel = event.getData().getParcelable(Message.Action.MSG_INFO);
                iPresenter.deleteU2Msg(msgDel.getMsgId());
                msgAdapter.removeMsgItems(msgDel);
                break;
            case Message.Action.U2_RESEND_MSG:
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
        if (audioInfo != null && !StringUtils.isEmpty(audioInfo.getAudioFilePath())) {
            playingAudioFilePath = audioInfo.getAudioFilePath();
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_FASTEST);//SENSOR_DELAY_FASTEST 0ms; SENSOR_DELAY_GAME延迟时间20ms
            setPlayingAudioFilePath(playingAudioFilePath);
            AnimationDrawable voiceAD = msgAdapter.getVoiceAnimationDrawable();
            startPlaying(audioInfo.getAudioFilePath(), voiceAD);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserProto.SimpleUserProfile profile = UserProfilePresenter.getInstance(currentSite).queryFriend(friendSiteUserId);
        if (profile != null) {
            setMultTitle(profile.getUserName(), StringUtils.getSiteSubTitle(this.currentSite));
        }
        msgRecycler.postDelayed(new Runnable() {
            @Override
            public void run() {
                iPresenter.loadNewMsgFromDB();
            }
        }, 3);

        /////外部跳进来
        String pluginId = getIntent().getStringExtra(KEY_PLUGIN_ID_FOR_JUMP);
        String param = getIntent().getStringExtra(KEY_PLUGIN_PARAM);

        if (pluginId != null && pluginId.length() > 0) {
            PluginProto.Plugin plugin = getPluginByPluginId(pluginId);
            if (plugin == null) {
                return;
            }
            loadMsgPlugin(plugin, param);
        }
        getIntent().putExtra(KEY_PLUGIN_ID_FOR_JUMP, "");
        getIntent().putExtra(KEY_PLUGIN_PARAM, "");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        audioRecordingView.setVisibility(View.GONE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String chatSessionId = getIntent().getStringExtra(IntentKey.KEY_FRIEND_SITE_USER_ID);
        if (chatSessionId.equals(friendSiteUserId)) {
            this.setIntent(intent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        EventBus.getDefault().unregister(this);
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
                case DeviceListActivity.REQUEST_CODE:
                    byte[] byteArray = data.getByteArrayExtra(DeviceListActivity.KEY_DEVICE_INFO);
                    try {
                        DeviceProto.SimpleDeviceProfile deviceProfile = DeviceProto.SimpleDeviceProfile.parseFrom(byteArray);
                        iPresenter.setFriendDevPubKey64Str(deviceProfile);
                        String deviceName = deviceProfile.getDeviceName();
                        secretTipContent.setText(String.format(getString(R.string.sending_in_secret_mode), deviceName));
                        secretTipLayout.setVisibility(View.VISIBLE);
                        sendButton.setImageResource(R.drawable.ic_send_secret);
                    } catch (Exception e) {
                        Logger.e(e);
                    }
                    break;
                case MSG_PREVIEW_IMG:
                    Bundle mPickerOptionsBundle = data.getExtras();
                    String imgPath = mPickerOptionsBundle.getString(PhotoPicker.EXTRA_PREVIEW_ONE_IMG_PATH);
                    ZalyLogUtils.getInstance().info(TAG, " for result, file imgPath is " + imgPath);
                    break;
            }
        }
    }

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

    /* Overriding methods in BaseMsgActivity */
    @Override
    @SuppressWarnings("unchecked")
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.title_layout:
                Pair<View, String> appBarPair = Pair.create(appBar, "app_bar");
                Pair<View, String> titleLayoutPair = Pair.create(titleLayout, "title_layout");
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(U2MessageActivity.this, appBarPair, titleLayoutPair);
                Intent intent = new Intent(this, FriendProfileActivity.class);
                intent.putExtra(IntentKey.KEY_PROFILE_MODE, FriendProfileActivity.MODE_FRIEND_SITE_ID);
                intent.putExtra(IntentKey.KEY_FRIEND_SITE_ID, friendSiteUserId);
                intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(intent, options.toBundle());
            case R.id.send_button:
                sendMessage();
                break;
            case R.id.unread_tip:
                unreadTip.setVisibility(View.GONE);
                iPresenter.loadMoreMsg(unreadNum);
                break;
            case R.id.secret_tip_action:
                Intent intentTip = new Intent(this, DeviceListActivity.class);
                intentTip.putExtra(IntentKey.KEY_FRIEND_SITE_USER_ID, friendSiteUserId);
                intentTip.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivityForResult(intentTip, DeviceListActivity.REQUEST_CODE);
                break;
        }
    }

    private void sendMessage() {
        String content = msgEdit.getText().toString();
        if (!StringUtils.isEmpty(content)) {
            iPresenter.sendTextMessage(content);
        }
    }

    /**
     * 显示未读消息数量.
     */
    private void showUnreadMsgNum() {
        if (unreadNum < 10) {
            unreadTip.setVisibility(View.GONE);
        } else {
            unreadTip.setText(String.format(getString(R.string.num_unread_messages), unreadNum));
        }
    }

    /**
     * 显示该条加密消息的指定加密设备信息
     */
    @Override
    public void showSecretDeviceInfoDialog(String deviceName) {
        Intent intent = new Intent(this, MsgContentActivity.class);
        intent.putExtra(KEY_NOT_VIEWABLE, true);
        intent.putExtra(KEY_DEVICE_NAME, deviceName);
        startActivity(intent);
    }

    @Override
    public void onSentImageInvalidate() {

    }

    @Override
    public void onSendingMessageError(Message message) {
        msgAdapter.notifyItemChanged(message);
    }

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
            msgEdit.postDelayed(new Runnable() {
                @Override
                public void run() {
                    iPresenter.refreshMsgStatus(message.getMsgId());
                }
            }, MessageViewHolder.MSG_SEND_TIME_OUT + 2000);
        }
    }

    @Override
    public void onSendMessageFinish(final Message message) {
        msgAdapter.notifyItemChanged(message);
    }

    @Override
    public void setMsgRvAdapter(MessageAdapter msgRvAdapter) {
        if (msgRecycler != null) {
            msgRecycler.setAdapter(msgRvAdapter);
            msgAdapter = msgRvAdapter;
            msgAdapter.setAdapterListener(new MessageAdapterListener() {
                @Override
                public void onMessageClick(int type, Message message) {
                    // TODO: on message item clicked
                    hideSoftKey();
                    hideExpandView();
                }
            });
        }
    }

    @Override
    public void onNewMessagesReceived() {
        if (!msgRecycler.canScrollVertically(1))
            msgRecycler.scrollToPosition(0);
    }

    @Override
    public void onTopSecretOff() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                secretTipLayout.setVisibility(View.GONE);
                secretSwitch.setChecked(false);
                sendButton.setImageResource(R.drawable.ic_send);
            }
        });
    }

    @Override
    public void onTopSecretOn(final String deviceName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                secretTipContent.setText(String.format(getString(R.string.sending_in_secret_mode), deviceName));
                secretTipLayout.setVisibility(View.VISIBLE);
                secretSwitch.setChecked(true);
                sendButton.setImageResource(R.drawable.ic_send_secret);
            }
        });

    }

    @Override
    public void scrollUplMsgList() {
        msgRecycler.scrollToPosition(msgAdapter.getItemCount() - 5);
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
    public void quitWebView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideMsgPlugin();
                destoryMsgPluginWebView();
                generateMsgPluginWebView();
            }
        });

    }
}


