package com.akaxin.client.chat;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.akaxin.client.Configs;
import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.AudioInfo;
import com.akaxin.client.bean.ImageInfo;
import com.akaxin.client.bean.Message;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bean.event.MessageEvent;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.db.dao.SiteMessageDao;
import com.akaxin.client.friend.presenter.impl.UserProfilePresenter;
import com.akaxin.client.image.ImagePagerActivity;
import com.akaxin.client.jump.presenter.impl.GoToPagePresenter;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.file.ImageUtils;
import com.akaxin.client.util.file.UploadFileUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.security.RSAUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.view.CustomProgressBar;
import com.akaxin.client.view.DoubleTapFrameLayout;
import com.akaxin.client.view.TipPopWindow;
import com.akaxin.client.view.ZalyWebView;
import com.akaxin.proto.core.CoreProto;
import com.akaxin.proto.core.FileProto;
import com.akaxin.proto.core.UserProto;
import com.orhanobut.logger.Logger;
import com.windchat.im.IMConst;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK;
import static android.webkit.WebSettings.LOAD_DEFAULT;
import static com.akaxin.client.bean.ImageInfo.STATUS_RECEIVE_DOWNLOAD;
import static com.akaxin.client.bean.ImageInfo.STATUS_RECEIVE_NO_DOWNLOAD;
import static com.akaxin.client.chat.MsgContentActivity.KEY_MSG;

/**
 * Created by yichao on 2017/10/10.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = MessageAdapter.class.getSimpleName();
    public static int IS_U2_MESSAGE = 1;
    public static int IS_GROUP_MESSAGE = 2;
    public static long BREAK_POINT_TIME_FOR_TWO_MESSAGES = 15 * 1000;
    //二人消息/群组消息
    private int msgUGType;
    private boolean isFromGroup;
    private Context mContext;
    private List<Message> messages;
    private MessageAdapterListener adapterListener;
    private AnimationDrawable voiceAnimationDrawable;
    private String chatSessionId;
    private Site currentSite;

    private int x, y;

    public MessageAdapter(Context mContext, String chatSessionId, int msgUGType, Site site) {
        messages = new ArrayList<>();
        this.mContext = mContext;
        this.msgUGType = msgUGType;
        this.isFromGroup = (msgUGType == IS_GROUP_MESSAGE);
        this.chatSessionId = chatSessionId;
        this.currentSite = site;
    }

    /**
     * 从库里查询消息，并且展示在消息聊天界面，此时需要知道，如果群消息，需要获取发送这的头像和昵称展示
     *
     * @param msg
     * @return
     */
    public String getUserName(Message msg) {
        String userName = "";
        try {
            // 群组消息才会需要 username
            if (msg != null && StringUtils.isNotEmpty(msg.getGroupId())) {
                UserProto.SimpleUserProfile userProfile = UserProfilePresenter.getInstance(currentSite).queryFriendBySiteUserId(msg.getSiteUserId());
                if (userProfile != null) {
                    userName = userProfile.getUserName();
                }
            }
        } catch (Exception e) {
            Logger.e(e);
            ZalyLogUtils.getInstance().errorToInfo(TAG, "error msg is " + e.getMessage());
        }
        return userName;
    }

    /**
     * 设置进度
     *
     * @param msg
     * @param num
     */
    public void setProcessNum(Message msg, int num) {
        MessageViewHolder.setProcessNum(msg, num);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case Message.MESSAGE_SEND + CoreProto.MsgType.U2_NOTICE_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.GROUP_NOTICE_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.U2_NOTICE_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.GROUP_NOTICE_VALUE:
                View receiveNoticeView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_notice, parent, false);
                return new NoticeViewHolder(receiveNoticeView);

            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.GROUP_WEB_NOTICE_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.U2_WEB_NOTICE_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.GROUP_WEB_NOTICE_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.U2_WEB_NOTICE_VALUE:
                View receiveWebNoticeView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.msg_web_notice_view, parent, false);
                return new NoticeWebViewHolder(receiveWebNoticeView);

            case Message.MESSAGE_SEND + CoreProto.MsgType.TEXT_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.SECRET_TEXT_VALUE:
                View sendView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_sent, parent, false);
                return new TextMessageSentViewHolder(sendView);

            case Message.MESSAGE_SEND + CoreProto.MsgType.GROUP_TEXT_VALUE:
                View groupSendView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_sent, parent, false);
                return new TextMessageSentViewHolder(groupSendView);

            case Message.MESSAGE_SEND + CoreProto.MsgType.GROUP_WEB_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.U2_WEB_VALUE:
                View u2SendWebView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_sent, parent, false);
                return new WebMessageSentViewHolder(u2SendWebView);

            case Message.MESSAGE_SEND + CoreProto.MsgType.SECRET_VOICE_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.VOICE_VALUE:
                View sendAudioView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_sent, parent, false);
                return new AudioMessageSentViewHolder(sendAudioView);

            case Message.MESSAGE_SEND + CoreProto.MsgType.GROUP_VOICE_VALUE:
                View groupSendAudioView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_sent, parent, false);
                return new AudioMessageSentViewHolder(groupSendAudioView);

            case Message.MESSAGE_SEND + CoreProto.MsgType.SECRET_IMAGE_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.IMAGE_VALUE:
                View sendImgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_sent, parent, false);
                return new ImageMessageSentViewHolder(sendImgView);

            case Message.MESSAGE_SEND + CoreProto.MsgType.GROUP_IMAGE_VALUE:
                View groupSendImgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_sent, parent, false);
                return new ImageMessageSentViewHolder(groupSendImgView);

            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.SECRET_TEXT_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.TEXT_VALUE:
                View receiveView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_received, parent, false);
                return new TextMessageReceivedViewHolder(receiveView);

            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.U2_WEB_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.GROUP_WEB_VALUE:
                View webView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_received, parent, false);
                return new WebReceivedViewHolder(webView);

            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.GROUP_TEXT_VALUE:
                View groupReceiveView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_received, parent, false);
                return new TextMessageReceivedViewHolder(groupReceiveView);

            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.SECRET_VOICE_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.VOICE_VALUE:
                View receiveAudioView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_received, parent, false);
                return new AudioMessageReceivedViewHolder(receiveAudioView);

            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.GROUP_VOICE_VALUE:
                View groupReceiveAudioView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_received, parent, false);
                return new AudioMessageReceivedViewHolder(groupReceiveAudioView);

            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.SECRET_IMAGE_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.IMAGE_VALUE:
                View receiveImgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_received, parent, false);
                return new ImageMessageReceivedViewHolder(receiveImgView);

            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.GROUP_IMAGE_VALUE:
                View groupReceiveImgView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_received, parent, false);
                return new ImageMessageReceivedViewHolder(groupReceiveImgView);

            default:
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_msg_received, parent, false);
                return new TextMessageSentViewHolder(view);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Message msg = messages.get(position);
        boolean showTimeTip = true;
        boolean sameSenderAsPreviousMsg = false;
        if (position + 1 < messages.size()) {
            final Message previousMsg = messages.get(position + 1);
            if (Math.abs(previousMsg.getMsgTime() - msg.getMsgTime()) < BREAK_POINT_TIME_FOR_TWO_MESSAGES) {
                showTimeTip = false;
            }
            if (previousMsg.getMsgType() != CoreProto.MsgType.U2_NOTICE_VALUE &&
                    previousMsg.getMsgType() != CoreProto.MsgType.GROUP_NOTICE_VALUE)
                sameSenderAsPreviousMsg = msg.getSiteUserId().equals(previousMsg.getSiteUserId());
        }

        int viewType = holder.getItemViewType();

        switch (viewType) {
            case Message.MESSAGE_SEND + CoreProto.MsgType.U2_NOTICE_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.GROUP_NOTICE_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.U2_NOTICE_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.GROUP_NOTICE_VALUE:
                NoticeViewHolder noticeViewHolders = (NoticeViewHolder) holder;
                noticeViewHolders.noticeTv.setText(msg.getContent());
                /* TODO: this is a ugly way of judging whether the message is clickable, in the later versions this should be fixed. */
                if (msg.getContent().contains("点击查看")) {
                    noticeViewHolders.itemLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putString(Message.KEY_DEVICE_ID, msg.getToDeviceId());
                            EventBus.getDefault().post(new MessageEvent(Message.Action.SHOW_SECRET_DEVICE, bundle));
                        }
                    });
                }
                break;
            case Message.MESSAGE_SEND + CoreProto.MsgType.GROUP_WEB_NOTICE_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.U2_WEB_NOTICE_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.GROUP_WEB_NOTICE_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.U2_WEB_NOTICE_VALUE:
                NoticeWebViewHolder noticeWebViewHolders = (NoticeWebViewHolder) holder;
                noticeWebViewHolders.webViewNotice.setHorizontalScrollBarEnabled(false);
                noticeWebViewHolders.webViewNotice.setVerticalScrollBarEnabled(false);


                ///禁止加载网络资源
                WebSettings settings = noticeWebViewHolders.webViewNotice.getSettings();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    noticeWebViewHolders.webViewNotice.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                }

                noticeWebViewHolders.webViewNotice.getSettings().setSupportZoom(true);
                noticeWebViewHolders.webViewNotice.getSettings().setBuiltInZoomControls(true);
                noticeWebViewHolders.webViewNotice.getSettings().setUseWideViewPort(true);
                noticeWebViewHolders.webViewNotice.getSettings().setLoadWithOverviewMode(true);
                noticeWebViewHolders.webViewNotice.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                settings.setBlockNetworkLoads(false);

                noticeWebViewHolders.webViewResentFL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ZalyLogUtils.getInstance().info(TAG, " webViewReceived href_url ===" + msg.getHrefUrl());
                        handlWebViewClick(msg);
                    }
                });
                noticeWebViewHolders.webViewResentFL.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        TipPopWindow tip = new TipPopWindow(mContext, msg, currentSite);
                        tip.showPopWindow(v, x, y);
                        tip.setOnTipSelectListener(new TipPopWindow.onTipSelectListener() {
                            @Override
                            public void onTipSelect(Message msg, int tipMsgId, PopupWindow tipView) {
                                tipView.dismiss();
                            }
                        });
                        return true;
                    }
                });
                noticeWebViewHolders.webViewResentFL.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            x = (int) event.getRawX();
                            y = (int) event.getRawY();
                        }
                        return false;
                    }
                });


                noticeWebViewHolders.webViewNotice.setNoticeHeightSize(msg);
                noticeWebViewHolders.webViewNotice.loadDataWithBaseURL(null, msg.getContent(), "text/html; charset=UTF-8", "UTF-8", null);

                break;

            case Message.MESSAGE_SEND + CoreProto.MsgType.TEXT_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.SECRET_TEXT_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.GROUP_TEXT_VALUE:
                TextMessageSentViewHolder textHolder = (TextMessageSentViewHolder) holder;
                textHolder.bindMessage(msg, showTimeTip, position, sameSenderAsPreviousMsg);
                break;

            case Message.MESSAGE_SEND + CoreProto.MsgType.GROUP_WEB_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.U2_WEB_VALUE:
                WebMessageSentViewHolder webMessageSentViewHolder = (WebMessageSentViewHolder) holder;
                webMessageSentViewHolder.bindMessage(msg, showTimeTip, position, sameSenderAsPreviousMsg);
                break;

            case Message.MESSAGE_SEND + CoreProto.MsgType.VOICE_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.SECRET_VOICE_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.GROUP_VOICE_VALUE:
                final AudioMessageSentViewHolder audioViewHolder = (AudioMessageSentViewHolder) holder;
                audioViewHolder.bindMessage(msg, showTimeTip, position, sameSenderAsPreviousMsg);
                break;

            case Message.MESSAGE_SEND + CoreProto.MsgType.IMAGE_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.SECRET_IMAGE_VALUE:
            case Message.MESSAGE_SEND + CoreProto.MsgType.GROUP_IMAGE_VALUE:
                ImageMessageSentViewHolder imgViewHolder = (ImageMessageSentViewHolder) holder;
                imgViewHolder.bindMessage(msg, showTimeTip, position, sameSenderAsPreviousMsg);
                break;

            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.TEXT_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.SECRET_TEXT_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.GROUP_TEXT_VALUE:
                TextMessageReceivedViewHolder receiveHolder = (TextMessageReceivedViewHolder) holder;
                receiveHolder.bindMessage(msg, showTimeTip, position, sameSenderAsPreviousMsg);
                break;

            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.GROUP_WEB_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.U2_WEB_VALUE:
                final WebReceivedViewHolder webReceivedViewHolder = (WebReceivedViewHolder) holder;
                webReceivedViewHolder.bindMessage(msg, showTimeTip, position, sameSenderAsPreviousMsg);
                break;

            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.VOICE_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.SECRET_VOICE_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.GROUP_VOICE_VALUE:
                final AudioMessageReceivedViewHolder receiveAudioViewHolder = (AudioMessageReceivedViewHolder) holder;
                receiveAudioViewHolder.bindMessage(msg, showTimeTip, position, sameSenderAsPreviousMsg);
                break;

            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.IMAGE_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.SECRET_IMAGE_VALUE:
            case Message.MESSAGE_RECEIVE + CoreProto.MsgType.GROUP_IMAGE_VALUE:
                final ImageMessageReceivedViewHolder receiveImgViewHolder = (ImageMessageReceivedViewHolder) holder;
                receiveImgViewHolder.bindMessage(msg, showTimeTip, position, sameSenderAsPreviousMsg);
                break;
        }
    }


    public AnimationDrawable getVoiceAnimationDrawable() {
        return this.voiceAnimationDrawable;
    }

    public void setVoiceAnimationDrawable(AnimationDrawable voiceAnimationDrawable) {
        this.voiceAnimationDrawable = voiceAnimationDrawable;
    }

    public int getPosition(Message message) {
        return messages.indexOf(message);
    }

    public void notifyItemChanged(Message message) {
        int pos = getPosition(message);
        if (pos > 0) notifyItemChanged(pos);
    }

    public void addNewMsgItem(Message message) {
        messages.add(0, message);
        notifyItemInserted(0);
    }

    public void addNewMsgItems(List<Message> data) {
        messages.addAll(0, data);
        notifyItemRangeInserted(0, data.size());
    }

    public void addHistoryItems(List<Message> data) {
        int fromIndex = messages.size();
        messages.addAll(fromIndex, data);
        notifyItemRangeInserted(fromIndex, data.size());
    }

    public void removeMsgItems(Message msg) {
        messages.remove(msg);
        notifyDataSetChanged();
    }

    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        int viewType = 0;

        String currentUserId = currentSite.getSiteUserId();

        if (msg != null && StringUtils.isNotBlank(currentUserId)) {
            if (!currentUserId.equals(msg.getSiteUserId())) {
                viewType = Message.MESSAGE_RECEIVE + msg.getMsgType();
            } else {
                viewType = Message.MESSAGE_SEND + msg.getMsgType();
            }
        }
        return viewType;
    }

    /**
     * 第一次初始化messages=null
     * 加载旧消息用的
     *
     * @return
     */
    public long getNewMsgId() {
        if (messages != null && messages.size() > 0) {
            Long lastNewMsgId = messages.get(0).get_id();
            int messageSizeNum = messages.size();
            for (int j = 0; j < messageSizeNum; j++) {
                Long newMsgId = messages.get(j).get_id();
                if (newMsgId > lastNewMsgId) {
                    lastNewMsgId = newMsgId;
                }
            }
            ZalyLogUtils.getInstance().info(TAG, " messages new lastMsgId is " + lastNewMsgId);
            return lastNewMsgId;
        }
        return Integer.MAX_VALUE;
    }


    /**
     * 接收到新消息通知
     * 加载旧消息用的
     *
     * @return
     */
    public long getNewMsgIdByReceive() {
        if (messages != null && messages.size() > 0) {
            Long lastNewMsgId = messages.get(0).get_id();
            int messageSizeNum = messages.size();
            for (int j = 0; j < messageSizeNum; j++) {
                Long newMsgId = messages.get(j).get_id();
                if (newMsgId > lastNewMsgId) {
                    lastNewMsgId = newMsgId;
                }
            }
            ZalyLogUtils.getInstance().info(TAG, " messages new lastMsgId is " + lastNewMsgId);
            return lastNewMsgId;
        }
        ZalyLogUtils.getInstance().info(TAG, " messages new lastMsgId is " + 0);
        return -1;
    }

    ////加载旧消息用的
    public long getLastMsgId() {
        if (messages.size() == 0) {
            return 0;
        }
        Long lastMsgId = messages.get(messages.size() - 1).get_id();
        int messageSizeNum = messages.size();
        for (int j = 0; j < messageSizeNum; j++) {
            Long newMsgId = messages.get(j).get_id();
            if (newMsgId < lastMsgId) {
                lastMsgId = newMsgId;
            }
        }
        ZalyLogUtils.getInstance().info(TAG, " messages history lastMsgId is " + lastMsgId);
        return lastMsgId;
    }

    public Message getNewMsg() {
        if (messages.size() == 0) {
            return null;
        }
        ZalyLogUtils.getInstance().info(TAG, " messages list is " + messages);
        return messages.get(0);
    }

    public void setAdapterListener(MessageAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    public void handlWebViewClick(Message msg) {

        if (msg.getHrefUrl() == null || msg.getHrefUrl().length() < 0) {
            return;
        }

        ZalyLogUtils.getInstance().info(TAG, " test === siteAddress 1" + msg.getHrefUrl());
        //从hrefUrl里读取siteaddress  且协议必须为zaly或者zalys,如果host =.  host =site.gethostaddress
        String siteAddress = StringUtils.getSiteAddress(msg.getHrefUrl(), currentSite);
        String gotoPageUrl = msg.getHrefUrl();

        //hrefUrl 中协议必须为zaly或者zalys   且 site=.  return true
        boolean isUrlHostEqualPoint = StringUtils.isUrlHostEqualPoint(msg.getHrefUrl());

        //如果 site = .    href 替换. 为siteHost
        if (isUrlHostEqualPoint) {
            gotoPageUrl = gotoPageUrl.replace(Configs.LOCAL_SITE_DEFAULT_MARK, currentSite.getSiteHost());
        }

        if (!siteAddress.equals(currentSite.getSiteAddress())) {
            return;
        }
        Map<String, String> gotoParams = StringUtils.getParamsFromUrl(gotoPageUrl);

        if (gotoParams == null || gotoParams.size() < 1) {
            return;
        }
        GoToPagePresenter goToPagePresenter = new GoToPagePresenter(currentSite);
        if (isFromGroup) {
            String groupId = gotoParams.get(goToPagePresenter.getGroupIdKey());
            if (groupId != null && groupId.length() > 1 && groupId.equals(chatSessionId)) {
                goToPagePresenter.handleGotoPage(mContext, gotoPageUrl, false);
            }
        } else {
            String siteUserId = gotoParams.get(goToPagePresenter.getU2IdKey());
            if (siteUserId != null && siteUserId.length() > 1 && siteUserId.equals(chatSessionId)) {
                goToPagePresenter.handleGotoPage(mContext, gotoPageUrl, false);
            }
        }
    }


    class TimeTipViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.time_tip)
        TextView timeTip;

        public TimeTipViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class NoticeViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_layout)
        View itemLayout;
        @BindView(R.id.notice_content)
        TextView noticeTv;

        public NoticeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class NoticeWebViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.webview_notice)
        ZalyWebView webViewNotice;
        @BindView(R.id.webview_notice_parent)
        FrameLayout webViewResentFL;

        public NoticeWebViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    class WebMessageSentViewHolder extends MessageViewHolder {
        @BindView(R.id.webview_resent)
        ZalyWebView webViewResent;
        @BindView(R.id.webview_resent_parent)
        FrameLayout webViewResentFL;

        public WebMessageSentViewHolder(View itemView) {
            super(itemView, mContext);
            super.bindExtraViews(R.layout.msg_web_view_sent, currentSite);
            ButterKnife.bind(this, itemView);
        }

        public void bindMessage(final Message msg, boolean showTimeTip, int position, boolean sameSenderAsPreviousMsg) {
            super.bindMessage(msg, isFromGroup, showTimeTip, position, sameSenderAsPreviousMsg);
            ZalyLogUtils.getInstance().info(TAG, " href_url ===" + msg.getHrefUrl());
            ////通知WebView内核网络状态
            webViewResent.setNetworkAvailable(false);
            webViewResent.getSettings().setSupportZoom(true);
            webViewResent.getSettings().setBuiltInZoomControls(true);
            webViewResent.getSettings().setUseWideViewPort(true);
            webViewResent.getSettings().setLoadWithOverviewMode(true);
            webViewResent.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

            webViewResent.getSettings().setCacheMode(NetUtils.getNetInfo() ? LOAD_DEFAULT : LOAD_CACHE_ELSE_NETWORK);
            webViewResent.setHorizontalScrollBarEnabled(false);
            webViewResent.setVerticalScrollBarEnabled(false);
            ///禁止加载网络资源
            WebSettings settings = webViewResent.getSettings();
            settings.setBlockNetworkLoads(false);
            settings.setJavaScriptEnabled(false);
            webViewResentFL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ZalyLogUtils.getInstance().info(TAG, " webViewResentFL WebMessageSentViewHolder href_url ===" + msg.getHrefUrl());
                    handlWebViewClick(msg);
                }
            });
            webViewResentFL.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TipPopWindow tip = new TipPopWindow(mContext, msg, currentSite);
                    tip.showPopWindow(v, x, y);
                    tip.setOnTipSelectListener(new TipPopWindow.onTipSelectListener() {
                        @Override
                        public void onTipSelect(Message msg, int tipMsgId, PopupWindow tipView) {
                            tipView.dismiss();
                        }
                    });
                    return true;
                }
            });
            webViewResentFL.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                    }
                    return false;
                }
            });
            webViewResent.setSize(msg);
            webViewResent.loadDataWithBaseURL("about:blank", msg.getContent(), "text/html; charset=UTF-8", "UTF-8", null);
        }
    }

    class TextMessageSentViewHolder extends MessageViewHolder {
        @BindView(R.id.content)
        TextView content;
        @BindView(R.id.message_body)
        DoubleTapFrameLayout messageBody;
        @BindView(R.id.circleProgressBar)
        CustomProgressBar circleProgressBar;

        public TextMessageSentViewHolder(View itemView) {
            super(itemView, mContext);
            super.bindExtraViews(R.layout.msg_bubble_text_sent, currentSite);
            ButterKnife.bind(this, itemView);
        }

        @SuppressLint("ClickableViewAccessibility")
        public void bindMessage(final Message msg, boolean showTimeTip, int position, boolean sameSenderAsPreviousMsg) {
            super.bindMessage(msg, isFromGroup, showTimeTip, position, sameSenderAsPreviousMsg);
            super.showSecretBackground(messageBody, msg);

            circleProgressBar.setTag(msg.get_id());
            circleProgressBar.setVisibility(View.GONE);
            content.setText(msg.getContent());

            super.setListener(new onloadListener() {
                @Override
                public void onload(Message msg, int progress) {
                    if ((long) circleProgressBar.getTag() != msg.get_id()) {
                        circleProgressBar.setVisibility(View.GONE);
                    } else {
                        if (imageProgressMaps.size() > 0 && imageProgressMaps.get(msg.get_id()) < 100) {
                            circleProgressBar.setVisibility(View.VISIBLE);
                            circleProgressBar.setProgress(imageProgressMaps.get(msg.get_id()));
                        } else {
                            circleProgressBar.setVisibility(View.GONE);
                            circleProgressBar.setProgress(100);
                        }
                    }

                }
            });

            this.messageBody.setTapListener(new DoubleTapFrameLayout.DoubleTapListener() {
                @Override
                public boolean onDoubleClick() {
                    Intent intent = new Intent(mContext, MsgContentActivity.class);
                    intent.putExtra(KEY_MSG, msg);
                    mContext.startActivity(intent);
                    return true;
                }
            });
            this.messageBody.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TipPopWindow tip = new TipPopWindow(mContext, msg, currentSite);
                    tip.showPopWindow(messageBody, x, y);
                    // tip.showPopupWindow(messageBody);
                    tip.setOnTipSelectListener(new TipPopWindow.onTipSelectListener() {
                        @Override
                        public void onTipSelect(Message msg, int tipMsgId, PopupWindow tipView) {
                            tipView.dismiss();
                        }
                    });

                    return true;
                }
            });

            messageBody.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                    }
                    return false;
                }
            });


        }
    }

    class AudioMessageSentViewHolder extends MessageViewHolder {
        @BindView(R.id.message_body)
        View messageBody;
        @BindView(R.id.audio_icon)
        ImageView audioIcon;
        @BindView(R.id.audio_duration)
        TextView audioDuration;
        @BindView(R.id.circleProgressBar)
        CustomProgressBar circleProgressBar;


        public AudioMessageSentViewHolder(View itemView) {
            super(itemView, mContext);
            super.bindExtraViews(R.layout.msg_bubble_audio_sent, currentSite);
            ButterKnife.bind(this, itemView);
        }

        public void bindMessage(final Message msg, boolean showTimeTip, int position, boolean sameSenderAsPreviousMsg) {
            super.bindMessage(msg, isFromGroup, showTimeTip, position, sameSenderAsPreviousMsg);
            super.showSecretBackground(messageBody, msg);
            final AudioInfo audioInfo = AudioInfo.parseJSON(msg.getContent());
            int audioTime = (int) (audioInfo.getAudioTime() / 1000 + 1);
            this.setDuration(audioTime);
            circleProgressBar.setVisibility(View.GONE);
            circleProgressBar.setTag(msg.get_id());
            super.setListener(new onloadListener() {
                @Override
                public void onload(Message msg, int progress) {
                    if ((long) circleProgressBar.getTag() != msg.get_id()) {
                        circleProgressBar.setVisibility(View.GONE);
                    } else {
                        if (imageProgressMaps.size() > 0 && imageProgressMaps.containsKey(msg.get_id()) && imageProgressMaps.get(msg.get_id()) < 100) {
                            circleProgressBar.setVisibility(View.VISIBLE);
                            circleProgressBar.setProgress(imageProgressMaps.get(msg.get_id()));
                        } else {
                            circleProgressBar.setVisibility(View.GONE);
                            circleProgressBar.setProgress(100);
                        }
                    }

                }
            });

            messageBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (audioInfo.getAudioTime() > 0) {
                        if (audioIcon.getDrawable() instanceof AnimationDrawable)
                            setVoiceAnimationDrawable((AnimationDrawable) audioIcon.getDrawable());
                        EventBus.getDefault().post(audioInfo);
                    }
                }
            });

            messageBody.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    TipPopWindow tip = new TipPopWindow(mContext, msg, currentSite);
                    tip.showPopWindow(messageBody, x, y);
                    tip.setOnTipSelectListener(new TipPopWindow.onTipSelectListener() {
                        @Override
                        public void onTipSelect(Message msg, int tipMsgId, PopupWindow tipView) {
                            tipView.dismiss();
                        }
                    });
                    return true;
                }
            });
            messageBody.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                    }
                    return false;
                }
            });

        }

        public void setDuration(int time) {
            audioDuration.setText(String.format(audioDuration.getContext().getString(R.string.duration_seconds), time));

            int viewWidth = time * audioTimeFactor + audioMinWidth;
            if (viewWidth > audioMaxWidth) {
                viewWidth = audioMaxWidth;
            }
            if (viewWidth < audioMinWidth) {
                viewWidth = audioMinWidth;
            }
            ViewGroup.LayoutParams lp = audioDuration.getLayoutParams();
            lp.width = viewWidth;
            audioDuration.setLayoutParams(lp);
        }
    }


    class ImageMessageSentViewHolder extends MessageViewHolder {
        @BindView(R.id.content_img)
        ImageView contentImg;
        @BindView(R.id.message_body)
        FrameLayout messageBody;
        @BindView(R.id.imageCenterProgress)
        CustomProgressBar imageCenterProgress;
        @BindView(R.id.circleProgressBar)
        CustomProgressBar circleProgressBar;


        public ImageMessageSentViewHolder(View itemView) {
            super(itemView, mContext);
            super.bindExtraViews(R.layout.msg_bubble_img_sent, currentSite);
            ButterKnife.bind(this, itemView);
        }

        public void bindMessage(final Message msg, boolean showTimeTip, final int position, boolean sameSenderAsPreviousMsg) {
            super.bindMessage(msg, isFromGroup, showTimeTip, position, sameSenderAsPreviousMsg);
            super.showSecretBackground(messageBody, msg);
            this.contentImg.setTag(R.id.indexTag, position);
            this.imageCenterProgress.setTag(msg.get_id());
            imageCenterProgress.setVisibility(View.GONE);
            circleProgressBar.setVisibility(View.GONE);
            contentImg.setImageTintMode(PorterDuff.Mode.SCREEN);
            final Message finalMsg;
            if (Integer.parseInt(contentImg.getTag(R.id.indexTag).toString()) == position || this.contentImg.getTag(R.id.indexTag).toString() == null) {
                //图片没变
                finalMsg = msg;
            } else {
                finalMsg = messages.get(Integer.parseInt(contentImg.getTag(R.id.indexTag).toString()));
            }
            ImageInfo imageInfo = ImageInfo.parseJSON(finalMsg.getContent());
            if (StringUtils.isEmpty(imageInfo.getFileId()) || msg.getMsgType() == CoreProto.MsgType.SECRET_IMAGE_VALUE) {
                new ImageUtils(mContext, currentSite).loadOnlyByLocalFile(new File(imageInfo.getFilePath()), this.contentImg);
            } else {
                new ImageUtils(mContext, currentSite).loadByLocalIDAndNetWithoutLocalFile(imageInfo, this.contentImg, R.drawable.ic_default, currentSite);
            }
            this.contentImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ImagePagerActivity.class);
                    intent.putExtra("bundle", getImageFiles(finalMsg.getMsgId(), finalMsg.getChatSessionId(), isFromGroup));
                    intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                    mContext.startActivity(intent);
                }
            });

            contentImg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    TipPopWindow tip = new TipPopWindow(mContext, finalMsg, currentSite);
                    tip.showPopWindow(contentImg, x, y);
                    tip.setOnTipSelectListener(new TipPopWindow.onTipSelectListener() {
                        @Override
                        public void onTipSelect(Message msg, int tipMsgId, PopupWindow tipView) {
                            tipView.dismiss();
                        }
                    });
                    return true;
                }
            });
            contentImg.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                    }
                    return false;
                }
            });

            /**
             * 更新进度条，以及判断进度是否显示
             */
            super.setListener(new onloadListener() {
                @Override
                public void onload(Message msg, int progress) {
                    //在这里打印log  发现上面拿的position是错的  导致错位   也就是打印时msgid对的   但是positionMsgid是错的
                    ZalyLogUtils.getInstance().info("progress", "msgid:" + msg.get_id() + "postionMsgid:" + messages.get(position).get_id());
                    if (msg.get_id() != (long) imageCenterProgress.getTag()) {
                        contentImg.setImageTintMode(PorterDuff.Mode.SCREEN);
                        imageCenterProgress.setVisibility(View.GONE);
                    } else {
                        if (progress < 99) {

                            contentImg.setImageTintMode(PorterDuff.Mode.SRC_OVER);
                            imageCenterProgress.setVisibility(View.VISIBLE);
                            imageCenterProgress.setProgress(imageProgressMaps.get(msg.get_id()));
                        } else {
                            contentImg.setImageTintMode(PorterDuff.Mode.SCREEN);
                            imageCenterProgress.setVisibility(View.GONE);
                            imageCenterProgress.setProgress(100);
                        }
                    }
                }
            });
        }
    }

    class WebReceivedViewHolder extends MessageViewHolder {
        @BindView(R.id.webview_received)
        ZalyWebView webViewReceived;
        @BindView(R.id.webview_received_parent)
        FrameLayout webViewReceviewFL;

        public WebReceivedViewHolder(View itemView) {
            super(itemView, mContext);
            super.bindExtraViews(R.layout.msg_web_view_received, currentSite);
            ButterKnife.bind(this, itemView);
        }

        public void bindMessage(final Message msg, boolean showTimeTip, int position, boolean sameSenderAsPreviousMsg) {
            super.bindMessage(msg, isFromGroup, showTimeTip, position, sameSenderAsPreviousMsg);

            ZalyLogUtils.getInstance().info(TAG, " href_url ===" + msg.getHrefUrl());
            ////通知WebView内核网络状态

            // webViewReceived.setNetworkAvailable(false);
            webViewReceived.setHorizontalScrollBarEnabled(false);
            webViewReceived.setVerticalScrollBarEnabled(false);

            ///禁止加载网络资源
            WebSettings settings = webViewReceived.getSettings();
            settings.setBlockNetworkLoads(false);
            settings.setJavaScriptEnabled(false);

            webViewReceived.getSettings().setSupportZoom(true);
            webViewReceived.getSettings().setBuiltInZoomControls(true);
            webViewReceived.getSettings().setUseWideViewPort(true);
            webViewReceived.getSettings().setLoadWithOverviewMode(true);
            webViewReceived.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            webViewReceived.getSettings().setCacheMode(NetUtils.getNetInfo() ? LOAD_DEFAULT : LOAD_CACHE_ELSE_NETWORK);
            webViewReceviewFL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handlWebViewClick(msg);
                }
            });
            webViewReceviewFL.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TipPopWindow tip = new TipPopWindow(mContext, msg, currentSite);
                    tip.showPopWindow(v, x, y);
                    tip.setOnTipSelectListener(new TipPopWindow.onTipSelectListener() {
                        @Override
                        public void onTipSelect(Message msg, int tipMsgId, PopupWindow tipView) {
                            tipView.dismiss();
                        }
                    });
                    return true;
                }
            });
            webViewReceviewFL.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                    }
                    return false;
                }
            });
            webViewReceived.setSize(msg);

            webViewReceived.loadDataWithBaseURL("about:blank", msg.getContent(), "text/html; charset=UTF-8", "UTF-8", null);
        }
    }

    class TextMessageReceivedViewHolder extends MessageViewHolder {
        @BindView(R.id.content)
        TextView content;
        @BindView(R.id.message_body)
        DoubleTapFrameLayout messageBody;

        public TextMessageReceivedViewHolder(View itemView) {
            super(itemView, mContext);
            super.bindExtraViews(R.layout.msg_bubble_text_received, currentSite);
            ButterKnife.bind(this, itemView);
        }

        public void bindMessage(final Message msg, boolean showTimeTip, int position, boolean sameSenderAsPreviousMsg) {
            super.bindMessage(msg, isFromGroup, showTimeTip, position, sameSenderAsPreviousMsg);
            super.showSecretBackground(messageBody, msg);
            if (msg.isSecret() && StringUtils.isNotEmpty(msg.getMsgTsk())) {
                try {
                    String priKey = ZalyApplication.getCfgSP().getKey(Configs.DEVICE_PRI_KEY);
                    RSAUtils.getInstance().decryptMsg(msg, priKey);
                    msg.setMsgTsk("");//这里一定要置空
                    //这里一定要置更新数据库一次，解密一次，更新数据库，后期使用的均为解密以后的消息内容
                    SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort())).updateSecretU2MsgContent(msg.get_id(), msg.getContent(), msg.getMsgTsk());
                } catch (Exception e) {
                    //解密二人文本消息失败，则替换成notice消息
                    ZalyTaskExecutor.executeTask(TAG, new ZalyTaskExecutor.Task<Void, Void, Boolean>() {

                        @Override
                        protected Boolean executeTask(Void... voids) throws Exception {
                            String newContent = mContext.getString(R.string.unavailable_top_secret_msg);
                            if (msg.getMsgType() == CoreProto.MsgType.GROUP_TEXT_VALUE) {
                                msg.setMsgType(CoreProto.MsgType.GROUP_NOTICE_VALUE);
                            } else {
                                msg.setMsgType(CoreProto.MsgType.U2_NOTICE_VALUE);
                            }
                            msg.setContent(newContent);
                            SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort())).updateSecretU2MsgContent(msg.get_id(), msg.getContent(), msg.getMsgType());
                            return true;
                        }

                        @Override
                        protected void onTaskFinish() {

                        }

                        @Override
                        protected void onTaskSuccess(Boolean result) {
                            if (result) {
                                //notice
                                notifyItemChanged(getAdapterPosition());
                            }
                        }

                        @Override
                        protected void onTaskError(Exception e) {

                        }

                    });
                    return;
                }
            }
            content.setText(msg.getContent());
            this.messageBody.setTapListener(new DoubleTapFrameLayout.DoubleTapListener() {
                @Override
                public boolean onDoubleClick() {
                    Intent intent = new Intent(mContext, MsgContentActivity.class);
                    intent.putExtra(KEY_MSG, msg);
                    mContext.startActivity(intent);
                    return true;
                }
            });

            this.messageBody.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TipPopWindow tip = new TipPopWindow(mContext, msg, currentSite);
                    tip.showPopWindow(messageBody, x, y);
                    tip.setOnTipSelectListener(new TipPopWindow.onTipSelectListener() {
                        @Override
                        public void onTipSelect(Message msg, int tipMsgId, PopupWindow tipView) {
                            tipView.dismiss();
                        }
                    });
                    return true;
                }
            });
            this.messageBody.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                    }
                    return false;
                }
            });
        }

    }


    class AudioMessageReceivedViewHolder extends MessageViewHolder {
        @BindView(R.id.message_body)
        View messageBody;
        @BindView(R.id.audio_icon)
        ImageView audioIcon;
        @BindView(R.id.audio_duration)
        TextView audioDuration;

        public AudioMessageReceivedViewHolder(View itemView) {
            super(itemView, mContext);
            super.bindExtraViews(R.layout.msg_bubble_audio_received, currentSite);
            ButterKnife.bind(this, itemView);
        }

        public void setDuration(int time) {
            audioDuration.setText(String.format(audioDuration.getContext().getString(R.string.duration_seconds), time));

            int viewWidth = time * audioTimeFactor + audioMinWidth;
            if (viewWidth > audioMaxWidth) {
                viewWidth = audioMaxWidth;
            }
            if (viewWidth < audioMinWidth) {
                viewWidth = audioMinWidth;
            }
            ViewGroup.LayoutParams lp = audioDuration.getLayoutParams();
            lp.width = viewWidth;
            audioDuration.setLayoutParams(lp);
        }

        public void bindMessage(final Message msg, boolean showTimeTip, int position, boolean sameSenderAsPreviousMsg) {
            super.bindMessage(msg, isFromGroup, showTimeTip, position, sameSenderAsPreviousMsg);
            super.showSecretBackground(messageBody, msg);

            final AudioInfo receiveAudio = AudioInfo.parseJSON(msg.getContent());

            if (receiveAudio.getAudioTime() == AudioInfo.NONE_DOWNLOAD) {
                this.audioDuration.setText("加载中...");

                final String downloadFilePath = Configs.getAudioDir().getAbsolutePath() + "/" + receiveAudio.getAudioId();
                if (msg.isSecret()) {
                    String priKey = ZalyApplication.getCfgSP().getKey(Configs.DEVICE_PRI_KEY);
                    UploadFileUtils.downloadSecretFile(receiveAudio.getAudioId(), downloadFilePath, FileProto.FileType.MESSAGE_VOICE, new UploadFileUtils.DownloadSecretFileListener() {

                        @Override
                        public void onDownloadStartInBackground() {

                        }

                        @Override
                        public void onDownloadCompleteInBackground(String fileId, String filePath) {

                        }

                        @Override
                        public void decryptFileStartInBackground() {

                        }

                        @Override
                        public void decryptFileCompleteInBackground(String fileId, String filePath) {
                            Uri uri = Uri.parse(filePath);
                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(ZalyApplication.getContext(), uri);
                            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            int millSecond = Integer.parseInt(durationStr);
                            receiveAudio.setAudioTime(millSecond);
                            receiveAudio.setAudioFilePath(downloadFilePath);
                            msg.setContent(AudioInfo.toJSON(receiveAudio));
                            SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort())).updateU2MsgContent(msg.get_id(), msg.getContent(), msg.getMsgTsk());
                        }

                        @Override
                        public void onDownloadSuccess(String fileId, String filePath) {
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onDownloadFail(Exception e) {
                            msg.setContent(mContext.getString(R.string.unavailable_top_secret_msg));
                            if (msg.getMsgType() == CoreProto.MsgType.GROUP_VOICE_VALUE) {
                                msg.setMsgType(CoreProto.MsgType.GROUP_NOTICE_VALUE);
                            } else {
                                msg.setMsgType(CoreProto.MsgType.U2_NOTICE_VALUE);
                            }
                            SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort())).updateSecretU2MsgContent(msg.get_id(), msg.getContent(), msg.getMsgType());
                            notifyItemChanged(getAdapterPosition());
                        }
                    }, priKey, msg.getMsgTsk(), currentSite);
                } else {
                    UploadFileUtils.downloadFile(receiveAudio.getAudioId(), downloadFilePath, FileProto.FileType.MESSAGE_VOICE,
                            new UploadFileUtils.DownloadFileListener() {
                                @Override
                                public void onDownloadStartInBackground() {

                                }

                                @Override
                                public void onDownloadCompleteInBackground(String fileId, String filePath) {
                                    Uri uri = Uri.parse(filePath);
                                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                    mmr.setDataSource(ZalyApplication.getContext(), uri);
                                    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                    int millSecond = Integer.parseInt(durationStr);
                                    receiveAudio.setAudioTime(millSecond);
                                    receiveAudio.setAudioFilePath(downloadFilePath);
                                    msg.setContent(AudioInfo.toJSON(receiveAudio));
                                    SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort())).updateU2MsgContent(msg.get_id(), msg.getContent(), msg.getMsgTsk());
                                }

                                @Override
                                public void onDownloadSuccess(String fileId, String filePath) {
                                    notifyDataSetChanged();
                                }

                                @Override
                                public void onDownloadFail(Exception e) {
                                    receiveAudio.setAudioTime(AudioInfo.DOWNLOAD_FAIL);
                                    msg.setContent(AudioInfo.toJSON(receiveAudio));
                                    SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort())).updateSecretU2MsgContent(msg.get_id(), msg.getContent(), msg.getMsgType());
                                    notifyItemChanged(getAdapterPosition());
                                }
                            }, currentSite);
                }
            } else if (receiveAudio.getAudioTime() == AudioInfo.NONE_DOWNLOAD) {
                this.audioDuration.setText("接收失败...");
            } else {
                int duration = (int) (receiveAudio.getAudioTime() / 1000 + 1);
                this.setDuration(duration);
            }

            messageBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (receiveAudio.getAudioTime() > 0) {
                        if (audioIcon.getDrawable() instanceof AnimationDrawable)
                            setVoiceAnimationDrawable((AnimationDrawable) audioIcon.getDrawable());
                        EventBus.getDefault().post(receiveAudio);
                    }
                }
            });
            messageBody.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    TipPopWindow tip = new TipPopWindow(mContext, msg, currentSite);
                    tip.showPopWindow(messageBody, x, y);
                    tip.setOnTipSelectListener(new TipPopWindow.onTipSelectListener() {
                        @Override
                        public void onTipSelect(Message msg, int tipMsgId, PopupWindow tipView) {
                            tipView.dismiss();
                        }
                    });
                    return true;
                }
            });
            messageBody.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                    }
                    return false;
                }
            });
        }
    }

    class ImageMessageReceivedViewHolder extends MessageViewHolder {
        @BindView(R.id.content_img)
        ImageView contentImg;
        @BindView(R.id.message_body)
        FrameLayout messageBody;

        public ImageMessageReceivedViewHolder(View itemView) {
            super(itemView, mContext);
            super.bindExtraViews(R.layout.msg_bubble_img_received, currentSite);
            ButterKnife.bind(this, itemView);
        }


        public void bindMessage(final Message msg, boolean showTimeTip, final int position, boolean sameSenderAsPreviousMsg) {
            super.bindMessage(msg, isFromGroup, showTimeTip, position, sameSenderAsPreviousMsg);
            super.showSecretBackground(messageBody, msg);
            final Message finalMessage;

            this.contentImg.setTag(R.id.indexTag, position);
            if (Integer.parseInt(contentImg.getTag(R.id.indexTag).toString()) == position) {
                //未产生复用,这时候可以直接用msg
                finalMessage = msg;
            } else {
                //发生复用 这时候必须通过imageView.getTag来拿position  ,这个m才是对应正确的message
                finalMessage = messages.get(Integer.parseInt(contentImg.getTag(R.id.indexTag).toString()));
            }

            final ImageInfo receiveImg = ImageInfo.parseJSON(finalMessage.getContent());
            //动态设置
            if (!msg.isSecret()) {//非绝密直接加载即可
                this.contentImg.setVisibility(View.VISIBLE);
                new ImageUtils(mContext, currentSite).loadByLocalAndDownLoad(receiveImg.getFileId(), this.contentImg);

//                ZalyGlideModel model = new ZalyGlideModel.Builder()
//                        .setImageID(receiveImg.getFileId())
//                        .setFileType(FileProto.FileType.MESSAGE_IMAGE)
//                        .setSite(currentSite)
//                        .isEncrypt(false)
//                        .isSend(false)
//                        .build();
//                Glide.with(mContext).load(model).apply(new RequestOptions().dontAnimate()).into(contentImg);
            } else {
                final String downloadFilePath = Configs.getImgDir().getAbsolutePath() + "/" + receiveImg.getFileId();
                if (receiveImg.getStatus() == STATUS_RECEIVE_DOWNLOAD) {
                    new ImageUtils(mContext, currentSite).loadByLocalAndDownLoad(receiveImg.getFileId(), this.contentImg);
                } else if (receiveImg.getStatus() == STATUS_RECEIVE_NO_DOWNLOAD) {
                    this.contentImg.setImageResource(R.drawable.ic_default);
                    String priKey = ZalyApplication.getCfgSP().getKey(Configs.DEVICE_PRI_KEY);
                    UploadFileUtils.downloadSecretFile(receiveImg.getFileId(), downloadFilePath, FileProto.FileType.MESSAGE_IMAGE,
                            new UploadFileUtils.DownloadSecretFileListener() {

                                @Override
                                public void onDownloadStartInBackground() {
                                }

                                @Override
                                public void onDownloadCompleteInBackground(String fileId, String filePath) {
                                }

                                @Override
                                public void decryptFileStartInBackground() {

                                }

                                @Override
                                public void decryptFileCompleteInBackground(String fileId, String filePath) {
                                    receiveImg.setFilePath(filePath);
                                    receiveImg.setStatus(STATUS_RECEIVE_DOWNLOAD);
                                    contentImg.setVisibility(View.VISIBLE);
                                    finalMessage.setContent(ImageInfo.toJSON(receiveImg));
                                    SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort())).updateU2MsgContent(finalMessage.get_id(), finalMessage.getContent(), finalMessage.getMsgTsk());
                                }

                                @Override
                                public void onDownloadSuccess(String fileId, String filePath) {
                                    notifyDataSetChanged();
                                }

                                @Override
                                public void onDownloadFail(Exception e) {
                                    finalMessage.setContent(mContext.getString(R.string.unavailable_top_secret_msg));
                                    if (finalMessage.getMsgType() == CoreProto.MsgType.GROUP_IMAGE_VALUE) {
                                        finalMessage.setMsgType(CoreProto.MsgType.GROUP_NOTICE_VALUE);
                                    } else {
                                        finalMessage.setMsgType(CoreProto.MsgType.U2_NOTICE_VALUE);
                                    }
                                    SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort())).updateSecretU2MsgContent(finalMessage.get_id(), finalMessage.getContent(), finalMessage.getMsgType());
                                    notifyItemChanged(getAdapterPosition());
                                }
                            }, priKey, finalMessage.getMsgTsk(), currentSite);
                }

            }
//                ZalyGlideModel model = new ZalyGlideModel.Builder()
//                        .setImageID(receiveImg.getFileId())
//                        .setFileType(FileProto.FileType.MESSAGE_IMAGE)
//                        .setSite(currentSite)
//                        .setPrivateKey(priKey)
//                        .setMsgTsk(finalMessage.getMsgTsk())
//                        .isEncrypt(true)
//                        .isSend(false)
//                        .build();
//                Glide.with(mContext).load(model).
//                        apply(new RequestOptions().dontAnimate()).listener(new RequestListener<Drawable>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                        ZalyGlideModel m = (ZalyGlideModel) model;
//                        if (m.isEncrypt() && !m.isSend()) {
//                            finalMessage.setContent(mContext.getString(R.string.unavailable_top_secret_msg));
//                            if (finalMessage.getMsgType() == CoreProto.MsgType.GROUP_IMAGE_VALUE) {
//                                finalMessage.setMsgType(CoreProto.MsgType.GROUP_NOTICE_VALUE);
//                            } else {
//                                finalMessage.setMsgType(CoreProto.MsgType.U2_NOTICE_VALUE);
//                            }
//                            SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getSiteAddress())).updateSecretU2MsgContent(finalMessage.get_id(), finalMessage.getContent(), finalMessage.getMsgType());
//                            notifyItemChanged(getAdapterPosition());
//                        }
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                        ZalyGlideModel m = (ZalyGlideModel) model;
//                        if (m.isEncrypt() && !m.isSend()) {
//                            receiveImg.setFilePath(m.getImagePath());
//                            receiveImg.setStatus(STATUS_RECEIVE_DOWNLOAD);
//                            contentImg.setVisibility(View.VISIBLE);
//                            finalMessage.setContent(U2ImageMessage.toJSON(receiveImg));
//                            SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getSiteAddress())).updateU2MsgContent(finalMessage.get_id(), finalMessage.getContent(), finalMessage.getMsgTsk());
//                        }
//                        return false;
//                    }
//                }).into(contentImg);


            this.contentImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ImagePagerActivity.class);
                    intent.putExtra("bundle", getImageFiles(finalMessage.getMsgId(), finalMessage.getChatSessionId(), isFromGroup));
                    intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                    mContext.startActivity(intent);
                }
            });

            contentImg.setOnLongClickListener(new View.OnLongClickListener()

            {
                @Override
                public boolean onLongClick(View view) {
                    TipPopWindow tip = new TipPopWindow(mContext, finalMessage, currentSite);
                    tip.showPopWindow(contentImg, x, y);
                    tip.setOnTipSelectListener(new TipPopWindow.onTipSelectListener() {
                        @Override
                        public void onTipSelect(Message msg, int tipMsgId, PopupWindow tipView) {
                            tipView.dismiss();
                        }
                    });
                    return true;
                }
            });
            contentImg.setOnTouchListener(new View.OnTouchListener()

            {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                    }
                    return false;
                }
            });

        }

    }

    private Bundle getImageFiles(String msgId, String chateSessionId, boolean isFromGroup) {
        Bundle bundle = new Bundle();
        List<Message> images;
        if (isFromGroup) {
            images = SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort())).queryGroupImageMsg(chateSessionId);
        } else {
            images = SiteMessageDao.getInstance(ZalyApplication.getSiteAddressObj(currentSite.getHostAndPort())).queryU2ImageMsg(chateSessionId);
        }
        int finalPostion = 0;
        for (int j = 0; j < images.size(); j++) {
            Message imageMsg = images.get(j);
            if (imageMsg.getMsgId().equals(msgId)) {
                finalPostion = j;
            }
        }
        bundle.putInt(ImagePagerActivity.EXTRA_IMAGE_INDEX, images.size() - finalPostion - 1);
        bundle.putParcelableArrayList(ImagePagerActivity.EXTRA_IMAGE_URLS, (ArrayList<Message>) images);
        return bundle;
    }

}
