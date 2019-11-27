package com.windchat.client.chat;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.ZalyApplication;
import com.windchat.client.api.ApiClient;
import com.windchat.client.api.ZalyAPIException;
import com.windchat.client.bean.ImageInfo;
import com.windchat.client.bean.Message;
import com.windchat.client.bean.Site;
import com.windchat.client.constant.IntentKey;
import com.windchat.client.constant.SiteConfig;
import com.windchat.client.friend.FriendProfileActivity;
import com.windchat.client.friend.presenter.impl.UserProfilePresenter;
import com.windchat.client.util.DateUtil;
import com.windchat.client.util.UIUtils;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.file.ImageUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.task.ZalyTaskExecutor;
import com.windchat.client.util.toast.Toaster;
import com.akaxin.proto.core.CoreProto;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.site.ApiFriendProfileProto;
import com.windchat.im.IMClient;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by alexfan on 2018/3/22.
 */


public class MessageViewHolder extends RecyclerView.ViewHolder {

    public static final String TAG = MessageViewHolder.class.getSimpleName();

    protected static int audioMaxWidth;
    protected static int audioMinWidth;
    protected static int audioTimeFactor;

    protected TextView timeTip;
    protected LinearLayout itemLayout;
    protected CircleImageView avatar;
    protected FrameLayout msgParent;
    protected FrameLayout messageStatus;
    protected View statusFailed;
    protected ProgressBar statusSending;
    protected View securedLabel;
    protected TextView usernameLabel;
    static Map<Long, Integer> imageProgressMaps = new HashMap<>();
    private static Context mContext;
    private Site currentSite;

    public static final int STATUS_NULL = 0;
    public static final int STATUS_FAILED = 1;
    public static final int STATUS_SENDING = 2;
    public static final long MSG_SEND_TIME_OUT = 13 * 1000;//13秒  + 2秒延时执行 共计15秒

    public MessageViewHolder(View itemView, Context mContext) {
        super(itemView);
        this.mContext = mContext;
        itemLayout = itemView.findViewById(R.id.item_layout);
        timeTip = itemView.findViewById(R.id.time_tip);
        avatar = itemView.findViewById(R.id.avatar);

        msgParent = itemView.findViewById(R.id.message_parent);
        messageStatus = itemView.findViewById(R.id.message_status);
        statusFailed = itemView.findViewById(R.id.status_failed);
        statusSending = itemView.findViewById(R.id.status_sending);
        usernameLabel = itemView.findViewById(R.id.username_label);

        audioMaxWidth = (int) itemView.getResources().getDimension(R.dimen.width_msg_audio_max);
        audioMinWidth = (int) itemView.getResources().getDimension(R.dimen.width_msg_audio_min);
        audioTimeFactor = UIUtils.getPixels(8);
    }

    public void bindExtraViews(@LayoutRes int layoutId, Site currentSite) {
        LayoutInflater.from(itemView.getContext()).inflate(layoutId, msgParent);
        securedLabel = itemView.findViewById(R.id.secured);
        this.currentSite = currentSite;
    }

    public void bindMessage(final Message msg, boolean fromGroup, boolean showTimeTip, int position, boolean sameSenderAsPreviousMsg) {
        if (showTimeTip) {
            timeTip.setVisibility(View.VISIBLE);
            timeTip.setText(DateUtil.getTimeLineStringStyle2(new Date(msg.getMsgTime())));
        } else {
            timeTip.setVisibility(View.GONE);
        }
        if (msg.getMsgType() == CoreProto.MsgType.GROUP_WEB_VALUE
                || msg.getMsgType() == CoreProto.MsgType.GROUP_IMAGE_VALUE
                || msg.getMsgType() == CoreProto.MsgType.GROUP_TEXT_VALUE
                || msg.getMsgType() == CoreProto.MsgType.GROUP_VOICE_VALUE
                || msg.getMsgType() == CoreProto.MsgType.GROUP_NOTICE_VALUE) {
            usernameLabel.setVisibility(View.VISIBLE);
        }else
            usernameLabel.setVisibility(View.GONE);
        loadAvatarAndUsername(msg);
        showStatus(msg);
        showSecret(msg.isSecret());
        addOnAvatarClickListener(this.avatar, msg.getSiteUserId());
    }


    public interface onloadListener {
        void onload(Message msg, int progress);
    }


    private static onloadListener listener;


    public void setListener(onloadListener listener) {
        MessageViewHolder.listener = listener;
    }

    public static void setProcessNum(Message msg, int num) {
        int current;

        if (imageProgressMaps.get(msg.get_id()) == null) {
            current = 0;
        } else {
            current = imageProgressMaps.get(msg.get_id());
        }

        if (current >= 100) {
            if (listener != null) {
                listener.onload(msg, 100);
            }
            imageProgressMaps.put(msg.get_id(), 100);
            return;
        } else {
            if (listener != null) {
                listener.onload(msg, num);
            }
            imageProgressMaps.put(msg.get_id(), num);
        }
    }

    private void addOnAvatarClickListener(final ImageView avatar, final String siteUserId) {
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(avatar.getContext(), FriendProfileActivity.class);
                intent.putExtra(IntentKey.KEY_PROFILE_MODE, FriendProfileActivity.MODE_FRIEND_SITE_ID);
                intent.putExtra(IntentKey.KEY_FRIEND_SITE_ID, siteUserId);
                intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                avatar.getContext().startActivity(intent);
            }
        });
    }

    private void showStatus(Message msg) {
        long nowTime = System.currentTimeMillis();
        switch (msg.getMsgStatus()) {
            case Message.STATUS_SENDING:
                if (nowTime - msg.getMsgTime() > MSG_SEND_TIME_OUT) {
                    List<String> msgIds = new ArrayList<>();
                    msgIds.add(0, msg.getMsgId());
                    try {
                        IMClient.getInstance(currentSite).syncMessageStatus(msgIds, msg.getMsgType());
                    } catch (Exception e) {
                        ZalyLogUtils.getInstance().exceptionError(e);
                    }
                    this.showStatus(MessageViewHolder.STATUS_FAILED);
                } else {
                    this.showStatus(MessageViewHolder.STATUS_SENDING);
                }

                break;
            case Message.STATUS_SEND_FAILED:
                this.showStatus(MessageViewHolder.STATUS_FAILED);
                break;
            case Message.STATUS_SEND_FAILED_NOT_FRIEND:
                this.showStatus(MessageViewHolder.STATUS_FAILED);
                messageStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toaster.show(R.string.error_msg_not_friend);
                    }
                });
                break;
            case Message.STATUS_SEND_FAILED_NOT_IN_GROUP:
                this.showStatus(MessageViewHolder.STATUS_FAILED);
                messageStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toaster.show(R.string.error_msg_not_in_group);
                    }
                });
                break;
            default:
                this.showStatus(MessageViewHolder.STATUS_NULL);
                break;
        }
    }

    private void showStatus(int status) {
        switch (status) {
            case STATUS_NULL:
                messageStatus.setVisibility(View.GONE);
                statusFailed.setVisibility(View.GONE);
                statusSending.setVisibility(View.GONE);
                break;
            case STATUS_FAILED:
                messageStatus.setVisibility(View.VISIBLE);
                statusFailed.setVisibility(View.VISIBLE);
                statusSending.setVisibility(View.GONE);
                break;
            case STATUS_SENDING:
                messageStatus.setVisibility(View.VISIBLE);
                statusSending.setVisibility(View.VISIBLE);
                statusFailed.setVisibility(View.GONE);
                break;
        }
    }

    public void showSecret(boolean show) {
        securedLabel.setVisibility(show ? View.VISIBLE : View.GONE);
        int color = mContext.getResources().getColor(show ? R.color.secretNormal : R.color.colorPrimary);
        ColorStateList myColorStateList = ColorStateList.valueOf(color);
        //statusSending.setIndeterminateTintList(myColorStateList);
    }

    protected void showSecretBackground(View bubble, Message msg) {
        if (msg == null) return;
        boolean sent = false;
        String currentUserId = currentSite.getSiteUserId();
        if (StringUtils.isNotBlank(currentUserId)) {
            if (currentUserId.equals(msg.getSiteUserId())) {
                sent = true;
            }
        }
        boolean secret = msg.isSecret();
        @DrawableRes int drawableId;
        if (sent) {
            if (secret)
                drawableId = R.drawable.bg_msg_send_item_secret;
            else
                drawableId = R.drawable.bg_msg_send_item;
        } else {
            if (secret)
                drawableId = R.drawable.bg_msg_receive_item_secret;
            else
                drawableId = R.drawable.bg_msg_receive_item;
        }
        Drawable drawable = bubble.getContext().getResources().getDrawable(drawableId);
        bubble.setBackground(drawable);
    }

    public void setUsername(String username) {
        if (usernameLabel != null)
            usernameLabel.setText(username);
    }

    public String loadMsgImgByFileId(ImageInfo imageInfo) {
        String msgImgPath = ZalyApplication.getCurSP().getKey(SiteConfig.MSG_IMG_CACHE + imageInfo.getFileId());
        if (StringUtils.isNotEmpty(msgImgPath)) {
            return msgImgPath;
        }
        return "";
    }

    /**
     * 先后尝试从 message, cache, db, 网络获取消息的头像和用户名.
     */
    private void loadAvatarAndUsername(Message msg) {
        // Try loading avatar and username directly from msg
        if (StringUtils.isNotEmpty(msg.getImg()) && StringUtils.isNotEmpty(msg.getUserName())) {
            if (avatar.getVisibility() == View.VISIBLE) {

                new ImageUtils(mContext, currentSite).loadImage(msg.getImg(), this.avatar);
//                ZalyGlideModel model = new ZalyGlideModel.Builder()
//                        .setImageID(msg.getImg())
//                        .setFileType(FileProto.FileType.USER_PORTRAIT)
//                        .setSite(currentSite)
//                        .build();
//                Glide.with(mContext).load(model).
//                        apply(new RequestOptions()
//                                .dontAnimate()
//                                .error(R.drawable.ic_default)
//                                .fallback(R.drawable.ic_default))
//                        .into(this.avatar);
            }
            this.usernameLabel.setText(msg.getUserName());
            return;
        }
        // Try loading avatar and username from cache
        String siteUserId = msg.getSiteUserId();
        if (StringUtils.isEmpty(siteUserId)) {
            return;
        }
        String userImg = ZalyApplication.getCurSP().getKey(SiteConfig.USER_ICON_CACHE + siteUserId);
        String userName = ZalyApplication.getCurSP().getKey(SiteConfig.USER_NAME_CACHE + siteUserId);
        if (StringUtils.isNotEmpty(userImg) && StringUtils.isNotEmpty(userName)) {
            msg.setImg(userImg);
            msg.setUserName(userName);
            if (avatar.getVisibility() == View.VISIBLE) {
                new ImageUtils(mContext, currentSite).loadImage(msg.getImg(), this.avatar);
//                ZalyGlideModel model = new ZalyGlideModel.Builder()
//                        .setImageID(msg.getImg())
//                        .setFileType(FileProto.FileType.USER_PORTRAIT)
//                        .setSite(currentSite)
//                        .build();
//                Glide.with(mContext).load(model).
//                        apply(new RequestOptions()
//                                .dontAnimate()
//                                .error(R.drawable.ic_default)
//                                .fallback(R.drawable.ic_default))
//                        .into(avatar);
            }

            this.usernameLabel.setText(msg.getUserName());
        } else {
            // Try loading avatar and username from db
            UserProto.SimpleUserProfile userProfile = UserProfilePresenter.getInstance(currentSite).queryFriendBySiteUserId(msg.getSiteUserId());
            if (userProfile != null) {
                // Put db data into cache
                ZalyApplication.setUserInfo(siteUserId, userProfile.getUserPhoto(), userProfile.getUserName());
                msg.setImg(userProfile.getUserPhoto());
                msg.setUserName(userProfile.getUserName());
                if (avatar.getVisibility() == View.VISIBLE) {
                    new ImageUtils(mContext, currentSite).loadImage(userProfile.getUserPhoto(), this.avatar);
//                    ZalyGlideModel model = new ZalyGlideModel.Builder()
//                            .setImageID(userProfile.getUserPhoto())
//                            .setFileType(FileProto.FileType.USER_PORTRAIT)
//                            .setSite(currentSite)
//                            .build();
//                    Glide.with(mContext).load(model).
//                            apply(new RequestOptions()
//                                    .dontAnimate()
//                                    .error(R.drawable.ic_default)
//                                    .fallback(R.drawable.ic_default))
//                            .into(avatar);
                }
                this.usernameLabel.setText(userProfile.getUserName());
            } else {
                // Try loading avatar and username from internet
                ZalyTaskExecutor.executeUserTask(TAG,
                        new FindUserTask(
                                siteUserId, msg,
                                new WeakReference<>((ImageView) this.avatar),
                                new WeakReference<>(this.usernameLabel)));
            }
        }
    }

    /**
     * 在本地未查到该好友资料时, 发出查询资料请求.
     */
    class FindUserTask extends ZalyTaskExecutor.Task<Void, Void, ApiFriendProfileProto.ApiFriendProfileResponse> {

        private String siteUserId;
        private Message message;
        private WeakReference<ImageView> avatarViewWeakReference;
        private WeakReference<TextView> usernameViewWeakReference;

        public FindUserTask(String siteUserId, Message message,
                            WeakReference<ImageView> avatarViewWeakReference,
                            WeakReference<TextView> usernameViewWeakReference) {
            this.siteUserId = siteUserId;
            this.message = message;
            this.avatarViewWeakReference = avatarViewWeakReference;
            this.usernameViewWeakReference = usernameViewWeakReference;
        }

        @Override
        protected ApiFriendProfileProto.ApiFriendProfileResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getFriendApi().findUser(siteUserId);
        }

        @Override
        protected void onTaskSuccess(ApiFriendProfileProto.ApiFriendProfileResponse response) {
            super.onTaskSuccess(response);
            UserProto.UserProfile userProfile = response.getProfile();
            // Save to db and cache.
            UserProfilePresenter.getInstance(currentSite).insertStrangerFriend(userProfile);
            ZalyApplication.setUserInfo(siteUserId, userProfile.getUserPhoto(), userProfile.getUserName());

            message.setImg(userProfile.getUserPhoto());
            message.setUserName(userProfile.getUserName());

            ImageView avatarView = avatarViewWeakReference.get();
            TextView usernameView = usernameViewWeakReference.get();
            if (avatarView != null && avatarView.getVisibility() == View.VISIBLE) {
                new ImageUtils(mContext, currentSite).loadImage(userProfile.getUserPhoto(), avatarView);
//                ZalyGlideModel model = new ZalyGlideModel.Builder()
//                        .setImageID(userProfile.getUserPhoto())
//                        .setFileType(FileProto.FileType.USER_PORTRAIT)
//                        .setSite(currentSite)
//                        .build();
//                Glide.with(mContext).load(model).
//                        apply(new RequestOptions()
//                                .dontAnimate()
//                                .error(R.drawable.ic_default)
//                                .fallback(R.drawable.ic_default))
//                        .into(avatarView);
            }
            if (usernameView != null && usernameView.getVisibility() == View.VISIBLE) {
                usernameView.setText(userProfile.getUserName());
            }
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }

        @Override
        protected void onAPIError(ZalyAPIException e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
        }

    }
}