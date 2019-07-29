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
import com.akaxin.proto.core.UserProto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yichao on 2017/10/17.
 */

public class ChooseFriendListAdapter extends RecyclerView.Adapter<ChooseFriendListAdapter.ViewHolder> {

    List<UserProto.SimpleUserProfile> friendSimpleProfiles;
    Map<Integer, Boolean> selectStatus = new HashMap<>();
    private Context mContext;
    private Site currentSite;

    public ChooseFriendListAdapter(Context mContext, Site site) {
        friendSimpleProfiles = new ArrayList<>();
        this.mContext    = mContext;
        this.currentSite = site;
    }

    public void addItems(List<UserProto.SimpleUserProfile> friendSimpleProfiles) {
        this.friendSimpleProfiles.clear();
        this.friendSimpleProfiles.addAll(friendSimpleProfiles);
        for (int i = 0; i < friendSimpleProfiles.size(); i++)
            selectStatus.put(i, false);
        notifyDataSetChanged();
    }

    public void removeAllItems() {
        this.friendSimpleProfiles.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_member, parent, false);
        return new ChooseFriendListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final UserProto.SimpleUserProfile profile = friendSimpleProfiles.get(position);
        holder.checkBox.setTag(position);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selectStatus.put(position, isChecked);
                if (chooseFriendListener != null) {
                    chooseFriendListener.onFriendChangeCheck(profile.getSiteUserId(), isChecked);
                }
            }
        });
        if (selectStatus.get(position) == null) {
            selectStatus.put(position, false);
        }
        holder.checkBox.setChecked(selectStatus.get(position));


        String username = profile.getUserName();
        if (StringUtils.isEmpty(username)) {
            username = profile.getSiteUserId();
        }
        holder.friendName.setText(username);
        new ImageUtils(mContext, currentSite).loadImage(profile.getUserPhoto(), holder.friendImg);
//        ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(profile.getUserPhoto())
//                .setFileType(FileProto.FileType.MESSAGE_IMAGE)
//                .setSite(currentSite)
//                .build();
//        Glide.with(mContext).load(model).
//                apply(new RequestOptions()).into(holder.friendImg);
    }

    @Override
    public int getItemCount() {
        return friendSimpleProfiles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.contact_name)
        TextView friendName;
        @BindView(R.id.contact_avatar)
        ImageView friendImg;
        @BindView(R.id.checkbox)
        CheckBox checkBox;

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
