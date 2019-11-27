package com.windchat.client.friend.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.bean.Site;
import com.windchat.client.friend.listener.FriendApplyListListener;
import com.windchat.client.util.NetUtils;
import com.windchat.client.util.file.ImageUtils;
import com.windchat.client.util.toast.Toaster;
import com.akaxin.proto.core.UserProto;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by yichao on 2017/10/17.
 */

public class FriendApplyListAdapter extends RecyclerView.Adapter<FriendApplyListAdapter.ViewHolder> {

    private List<UserProto.ApplyUserProfile> applyUserProfiles;
    private FriendApplyListListener listListener;
    private Context mContext;
    private Site currentSite;

    public FriendApplyListAdapter(Context mContext, Site site) {
        this.applyUserProfiles = new ArrayList<>();
        this.mContext = mContext;
        this.currentSite = site;
    }

    public void addItems(List<UserProto.ApplyUserProfile> applyUserProfiles) {
        this.applyUserProfiles.clear();
        this.applyUserProfiles.addAll(applyUserProfiles);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        applyUserProfiles.remove(position);
        notifyDataSetChanged();
    }

    public void removeAllItem() {
        this.applyUserProfiles.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_friend_apply, parent, false);
        return new FriendApplyListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final UserProto.UserProfile profile = applyUserProfiles.get(position).getApplyUser();
//        ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(profile.getUserPhoto())
//                .setFileType(FileProto.FileType.MESSAGE_IMAGE)
//                .setSite(currentSite)
//                .build();
//        Glide.with(mContext).load(model).
//                apply(new RequestOptions().dontAnimate()).into(holder.friendImg);
        new ImageUtils(mContext, currentSite).loadImage(profile.getUserPhoto(), holder.friendImg);
        holder.applyNameTv.setText(profile.getUserName());
        String applyReason = applyUserProfiles.get(position).getApplyReason();
        holder.applyReasonTv.setText(applyReason);
        holder.applyFriendTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listListener != null) {
                    boolean isNet = NetUtils.getNetInfo();
                    if (!isNet) {
                        Toaster.showInvalidate("请稍候再试");
                        return;
                    }
                    listListener.onApplyFriend(position, profile.getSiteUserId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return applyUserProfiles.size();
    }

    public String getItemUsername(int position) {
        return applyUserProfiles.get(position).getApplyUser().getUserName();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.apply_name)
        TextView applyNameTv;
        @BindView(R.id.apply_reason)
        TextView applyReasonTv;
        @BindView(R.id.apply_friend)
        Button applyFriendTv;
        @BindView(R.id.contact_avatar)
        CircleImageView friendImg;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setListListener(FriendApplyListListener listListener) {
        this.listListener = listListener;
    }
}
