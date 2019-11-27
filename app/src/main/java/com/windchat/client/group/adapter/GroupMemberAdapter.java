package com.windchat.client.group.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.bean.Site;
import com.windchat.client.group.listener.GroupMemberListListener;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.file.ImageUtils;
import com.akaxin.proto.core.GroupProto;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yichao on 2017/10/17.
 */

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder>{

    List<GroupProto.GroupMemberProfile> groupMemberProfiles;
    private Context mContext;
    private Site currentSite;

    public GroupMemberAdapter(Context mContext, Site site) {
        groupMemberProfiles = new ArrayList<>();
        this.mContext    = mContext;
        this.currentSite = site;

    }

    public void addItems(List<GroupProto.GroupMemberProfile> groupMemberProfiles) {
        this.groupMemberProfiles.clear();
        this.groupMemberProfiles.addAll(groupMemberProfiles);
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_member, parent, false);
        return new GroupMemberAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final GroupProto.GroupMemberProfile profile = groupMemberProfiles.get(position);
        String username = profile.getProfile().getUserName();
        if (StringUtils.isEmpty(username)) {
            username = profile.getProfile().getSiteUserId();
        }
        holder.friendName.setText(username);
        holder.checkBox.setVisibility(View.GONE);
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupMemberListListener != null) {
                    groupMemberListListener.onMemberClick(profile.getProfile().getSiteUserId());
                }
            }
        });

//        ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(profile.getProfile().getUserPhoto())
//                .setFileType(FileProto.FileType.USER_PORTRAIT)
//                .setSite(currentSite)
//                .build();
//        Glide.with(mContext).load(model).
//                apply(new RequestOptions().dontAnimate()).into(holder.friendImg);
        new ImageUtils(mContext, currentSite).loadImage(profile.getProfile().getUserPhoto(), holder.friendImg);
    }

    @Override
    public int getItemCount() {
        return groupMemberProfiles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_layout) View itemLayout;
        @BindView(R.id.contact_name) TextView friendName;
        @BindView(R.id.contact_avatar) ImageView friendImg;
        @BindView(R.id.checkbox) CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private GroupMemberListListener groupMemberListListener;

    public void setGroupMemberListListener(GroupMemberListListener groupMemberListListener) {
        this.groupMemberListListener = groupMemberListListener;
    }
}
