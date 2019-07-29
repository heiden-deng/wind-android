package com.akaxin.client.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.akaxin.client.R;
import com.akaxin.client.bean.ChatSession;
import com.akaxin.client.bean.Session;
import com.akaxin.client.bean.Site;
import com.akaxin.client.db.bean.UserFriendBean;
import com.akaxin.client.db.bean.UserGroupBean;
import com.akaxin.client.friend.presenter.impl.UserProfilePresenter;
import com.akaxin.client.group.presenter.impl.GroupPresenter;
import com.akaxin.client.maintab.adapter.ZalyListAdapter;
import com.akaxin.client.util.DateUtil;
import com.akaxin.client.util.UIUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.file.ImageUtils;
import com.akaxin.client.util.log.ZalyLogUtils;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yichao on 2017/10/9.
 */

public class SessionAdapter extends ZalyListAdapter<ChatSession, RecyclerView.ViewHolder> {

    public static final String TAG = "SessionAdapter";

    private static final int TYPE_SESSION = 0;

    private SessionListListener listListener;
    Context mContext;
    private Site currentSite;

    protected boolean isMute = false;

    public SessionAdapter(Context mContext, Site currentSite) {
        super();
        this.mContext = mContext;
        this.currentSite = currentSite;
    }

    public void setCurrentSite(Site site) {
        this.currentSite = site;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.listitem_session, parent, false);
        return new SessionViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        configureSessionViewHolder((SessionViewHolder) holder, position);
    }

    //填充聊天会话中的头像，昵称
    private void configureSessionViewHolder(final SessionViewHolder holder, int position) {
        final int index = position;
        final ChatSession chatSession = items.get(index);

        String sessionTitle = getAndCheckSessionTitle(chatSession);

        holder.sessionTitleTv.setText(sessionTitle);
        holder.sessionTimeTv.setText(DateUtil.getTimeLineString(new Date(chatSession.getLatestTime())));
        holder.sessionDescTv.setText(chatSession.getLatestMsg());

//        if (chatSession.getIcon() == null) {
//
//        } else {
//            ZalyGlideModel model = new ZalyGlideModel.Builder()
//                    .setImageID(chatSession.getIcon())
//                    .setFileType(FileProto.FileType.USER_PORTRAIT)
//                    .setSite(currentSite)
//                    .build();
//            GlideApp.with(mContext)
//                    .load(model)
//                    .circleCrop()
//                    .error(chatSession.getType() == Session.TYPE_GROUP_SESSION ? R.drawable.avatar_group_default : R.drawable.avatar_user_default)
//                    .fallback(chatSession.getType() == Session.TYPE_GROUP_SESSION ? R.drawable.avatar_group_default : R.drawable.avatar_user_default).into(holder.sessionItemImg);
//
//        }
        if (chatSession.getType() == Session.TYPE_GROUP_SESSION) {
            new ImageUtils(mContext, currentSite).loadImage(chatSession.getIcon(), holder.sessionItemImg, R.drawable.avatar_group_default);
        } else {
            new ImageUtils(mContext, currentSite).loadImage(chatSession.getIcon(), holder.sessionItemImg, R.drawable.avatar_user_default);
        }

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listListener != null) {
                    listListener.onSessionClick(chatSession);
                }
            }
        });
        holder.itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listListener != null) {
                    return listListener.onSessionLongClick(chatSession);
                }
                return false;
            }
        });
        // show bubble
        if (chatSession.getUnreadNum() == 0) {
            holder.sessionUnreadNumTv.setVisibility(View.INVISIBLE);
        } else {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) holder.sessionUnreadNumTv.getLayoutParams();
            if (isMute) {
                lp.width = UIUtils.getPixels(10);
                lp.height = UIUtils.getPixels(10);
                int margin = UIUtils.getPixels(2);
                lp.setMargins(margin, margin, margin, margin);
                holder.sessionUnreadNumTv.setText("");
            } else {
                lp.width = UIUtils.getPixels(16);
                lp.height = UIUtils.getPixels(16);
                lp.setMargins(0, 0, 0, 0);
                holder.sessionUnreadNumTv.setText(StringUtils.getBubbleString(chatSession.getUnreadNum()));
            }
            holder.sessionUnreadNumTv.setVisibility(View.VISIBLE);
            holder.sessionUnreadNumTv.setLayoutParams(lp);
        }
        holder.notificationOffIcon.setVisibility(isMute ? View.VISIBLE : View.INVISIBLE);
    }

    public String getAndCheckSessionTitle(ChatSession chatSession) {
        try {
            if (chatSession != null && StringUtils.isEmpty(chatSession.getTitle())) {
                if (Session.TYPE_FRIEND_SESSION == chatSession.getType()) {
                    String siteUserId = chatSession.getChatSessionId();
                    UserFriendBean userFriendBean = UserProfilePresenter.getInstance(currentSite).queryFriendBeanBySiteUserId(siteUserId);
                    if (userFriendBean != null && StringUtils.isNotEmpty(userFriendBean.getUserName())) {
                        isMute = userFriendBean.isMute();
                        return userFriendBean.getUserName();
                    }
                } else if (Session.TYPE_GROUP_SESSION == chatSession.getType()) {
                    //#TODO 加载群profile
                    String siteGroupId = chatSession.getChatSessionId();
                    UserGroupBean groupProfile = GroupPresenter.getInstance(currentSite).getGroupBeanByGroupId(siteGroupId, currentSite);

                    if (groupProfile != null && StringUtils.isNotEmpty(groupProfile.getGroupName())) {
                        isMute = groupProfile.isMute();
                        return groupProfile.getGroupName();
                    }
                }
            }
            isMute = chatSession.isMute();
        } catch (Exception e) {
            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
        }
        return chatSession.getTitle();
    }


    public void setListListener(SessionListListener listListener) {
        this.listListener = listListener;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_SESSION;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }


    public static class SessionViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_layout)
        View itemLayout;
        @BindView(R.id.session_item_img)
        ImageView sessionItemImg;
        @BindView(R.id.session_title)
        TextView sessionTitleTv;
        @BindView(R.id.session_time)
        TextView sessionTimeTv;
        @BindView(R.id.session_desc)
        TextView sessionDescTv;
        @BindView(R.id.unread_num)
        TextView sessionUnreadNumTv;
        @BindView(R.id.notification_off_icon)
        ImageView notificationOffIcon;

        public SessionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    public interface SessionListListener {
        void onSessionClick(ChatSession chatSession);

        boolean onSessionLongClick(ChatSession chatSession);
    }
}