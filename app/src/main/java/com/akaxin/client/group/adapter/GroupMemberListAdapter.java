package com.akaxin.client.group.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.akaxin.client.R;
import com.akaxin.client.bean.Site;
import com.akaxin.client.group.listener.ChooseFriendListener;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.file.ImageUtils;
import com.akaxin.proto.core.GroupProto;
import com.akaxin.proto.core.UserProto;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yichao on 2017/10/17.
 */

public class GroupMemberListAdapter extends RecyclerView.Adapter<GroupMemberListAdapter.ViewHolder>{

    List<GroupProto.GroupMemberProfile> memberProfiles;
    private Context mContext;
    private Site currentSite;
    
    public GroupMemberListAdapter(Context mContext, Site site) {
        memberProfiles = new ArrayList<>();
        this.mContext    = mContext;
        this.currentSite = site;
    }

    public void addItems(List<GroupProto.GroupMemberProfile> memberProfiles) {
        this.memberProfiles.clear();
        this.memberProfiles.addAll(memberProfiles);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_member, parent, false);
        return new GroupMemberListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final UserProto.UserProfile profile = memberProfiles.get(position).getProfile();
        String username = profile.getUserName();
        if (StringUtils.isEmpty(username)){
            username = profile.getSiteUserId();
        }

//
//        ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(profile.getUserPhoto())
//                .setFileType(FileProto.FileType.MESSAGE_IMAGE)
//                .setSite(currentSite)
//                .build();
//        Glide.with(mContext).load(model).
//                apply(new RequestOptions().dontAnimate()).into(holder.friendImg);

        new ImageUtils(mContext, currentSite).loadImage(profile.getUserPhoto(), holder.friendImg);

        holder.friendName.setText(username);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (chooseFriendListener != null) {
                    chooseFriendListener.onFriendChangeCheck(profile.getSiteUserId(), isChecked);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return memberProfiles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.contact_avatar) ImageView friendImg;
        @BindView(R.id.contact_name) TextView friendName;
        @BindView(R.id.checkbox) CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private ChooseFriendListener chooseFriendListener;

    public void setChooseFriendListener(ChooseFriendListener chooseFriendListener) {
        this.chooseFriendListener = chooseFriendListener;
    }
}
