package com.akaxin.client.group.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.akaxin.client.R;
import com.akaxin.client.bean.Site;
import com.akaxin.client.group.listener.GroupListListener;
import com.akaxin.client.maintab.adapter.ZalyListAdapter;
import com.akaxin.client.util.file.ImageUtils;
import com.akaxin.proto.core.GroupProto;

/**
 * Created by yichao on 2017/10/17.
 */

public class GroupListAdapter extends ZalyListAdapter<GroupProto.SimpleGroupProfile, GroupListAdapter.ViewHolder> {
    public static final String TAG = GroupListAdapter.class.getSimpleName();
    private GroupListListener listListener;
    private Context mContext;
    private Site currentSite;

    public GroupListAdapter(Context mContext, Site site) {
        super();
        this.mContext = mContext;
        this.currentSite = site;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_contact, parent, false);
        return new GroupListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final GroupProto.SimpleGroupProfile profile = items.get(position);

        holder.friendName.setText(profile.getGroupName());
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listListener != null) {
                    listListener.onGroupClick(profile);
                }
            }
        });
//        ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(profile.getGroupIcon())
//                .setFileType(FileProto.FileType.USER_PORTRAIT)
//                .setSite(currentSite)
//                .build();
//        Glide.with(mContext).load(model).
//                apply(new RequestOptions()
//                        .dontAnimate()
//                        .error(R.drawable.avatar_group_default)
//                        .fallback(R.drawable.avatar_group_default))
//                .into(holder.friendImg);
        new ImageUtils(mContext, currentSite).loadImage(profile.getGroupIcon(), holder.friendImg, R.drawable.avatar_group_default);

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView friendName;
        public ImageView friendImg;
        public View itemLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            friendName = itemView.findViewById(R.id.contact_name);
            friendImg = itemView.findViewById(R.id.contact_avatar);
            itemLayout = itemView.findViewById(R.id.item_layout);
        }
    }

    public void setListListener(GroupListListener listListener) {
        this.listListener = listListener;
    }
}
