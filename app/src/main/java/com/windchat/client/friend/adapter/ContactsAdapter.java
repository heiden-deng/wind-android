package com.windchat.client.friend.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.bean.Site;
import com.windchat.client.friend.listener.OnContactItemClickListener;
import com.windchat.client.util.ChineseUtils;
import com.windchat.client.util.SimpleUserProfileComparator;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.file.ImageUtils;
import com.akaxin.proto.core.UserProto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yichao on 2017/10/17.
 */

public class ContactsAdapter extends RecyclerView.Adapter
        implements SectionIndexer {

    private OnContactItemClickListener contactItemClickListener;
    private OnHeaderClickListener headerClickListener;
    private static final int TYPE_CONTACT_ITEM = 0;
    private static final int TYPE_CONTACT_HEADER = 1;
    private static final int TYPE_CONTACT_SUBTITLE = 2;
    private static final int NUM_HEADER_ITEMS = 1;
    private boolean showHeader = false;
    private boolean hasNewFriend = false;

    private List<UserProto.SimpleUserProfile> items = new ArrayList<>();
    private ArrayList<String> sections;
    private ArrayList<Integer> mSectionPositions;
    List<SimpleUserProfileComparator.Temp> tempList = new ArrayList<>();
    private Context mContext;
    private Site currentSite;

    public ContactsAdapter(Context mContext, Site site) {
        super();
        this.mContext = mContext;
        this.currentSite = site;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_CONTACT_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_contact_header, parent, false);
            HeaderViewHolder viewHolder = new HeaderViewHolder(v);
            viewHolder.newFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    headerClickListener.onNewFriendClick();
                }
            });
            viewHolder.groupContacts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    headerClickListener.onGroupContactsClick();
                }
            });
            return viewHolder;
        } else if (viewType == TYPE_CONTACT_SUBTITLE) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_contact_alphabetic_subtitle, parent, false);
            return new SubtitleViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_contact, parent, false);
            return new ContactViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof ContactViewHolder) {
            int index = position - NUM_HEADER_ITEMS - getSection(position);
            final ContactViewHolder holder = (ContactViewHolder) viewHolder;
            final UserProto.SimpleUserProfile profile = items.get(index);
            String friendName = profile.getUserName();
            if (StringUtils.isEmpty(friendName)) {
                friendName = profile.getSiteUserId();
            }
            holder.contactName.setText(friendName);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contactItemClickListener != null) {
                        contactItemClickListener.onFriendClick(profile);
                    }
                }

            });
//            ZalyGlideModel model = new ZalyGlideModel.Builder()
//                    .setImageID(profile.getUserPhoto())
//                    .setFileType(FileProto.FileType.USER_PORTRAIT)
//                    .setSite(currentSite)
//                    .build();
//            GlideApp.with(mContext).load(model).circleCrop().into(holder.contactAvatar);
            new ImageUtils(mContext, currentSite).loadImage(profile.getUserPhoto(), holder.contactAvatar);

        } else if (viewHolder instanceof HeaderViewHolder) {
            HeaderViewHolder holder = (HeaderViewHolder) viewHolder;
            holder.newFriendBubble.setVisibility(hasNewFriend ? View.VISIBLE : View.INVISIBLE);
        } else if (viewHolder instanceof SubtitleViewHolder) {
            SubtitleViewHolder holder = (SubtitleViewHolder) viewHolder;
            holder.alphabeticSubtitle.setText(sections.get(mSectionPositions.indexOf(position)));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_CONTACT_HEADER;
        if (mSectionPositions.contains(position)) return TYPE_CONTACT_SUBTITLE;
        else return TYPE_CONTACT_ITEM;
    }

    @Override
    public int getItemCount() {
        if (!showHeader) return 0;
        if (sections != null)
            return items.size() + NUM_HEADER_ITEMS + sections.size() - 1;
        else return items.size() + NUM_HEADER_ITEMS;
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.contact_name)
        TextView contactName;
        @BindView(R.id.contact_avatar)
        ImageView contactAvatar;
        @BindView(R.id.item_layout)
        View itemLayout;

        public ContactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.new_friend)
        View newFriend;
        @BindView(R.id.group_contacts)
        View groupContacts;
        @BindView(R.id.new_friend_bubble)
        View newFriendBubble;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class SubtitleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.alphabetic_subtitle)
        TextView alphabeticSubtitle;

        public SubtitleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setOnClickListener(OnContactItemClickListener contactItemClickListener,
                                   OnHeaderClickListener headerClickListener) {
        this.contactItemClickListener = contactItemClickListener;
        this.headerClickListener = headerClickListener;
    }

    public void refreshNewFriendBubble(boolean hasNewFriend) {
        this.hasNewFriend = hasNewFriend;
        notifyItemChanged(0);
    }


    public void addAllItems(List<UserProto.SimpleUserProfile> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    public void removeAllItems() {
        this.items.clear();
        notifyDataSetChanged();
    }

    public void sortItems() {
        tempList.clear();
        for (int i = 0; i < items.size(); i++) {
            tempList.add(new SimpleUserProfileComparator.Temp(items.get(i)));
        }
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(tempList, new SimpleUserProfileComparator());
        items.clear();
        for (int i = 0; i < tempList.size(); i++) {
            items.add(tempList.get(i).getUserProfile());
        }

    }

    public void showHeader() {
        if (!this.showHeader) {
            this.showHeader = true;
            notifyItemRangeInserted(0, NUM_HEADER_ITEMS);
        }
    }

    public void hideHeader() {
        if (this.showHeader) {
            if (items.size() > 0) return;
            this.showHeader = false;
            notifyItemRangeRemoved(0, NUM_HEADER_ITEMS);
        }
    }

    @Override
    public Object[] getSections() {
        sections = new ArrayList<>(27);
        mSectionPositions = new ArrayList<>(27);
        sections.add("▲");
        mSectionPositions.add(0);
        for (int i = 0, size = items.size(); i < size; i++) {
            if (items.get(i).getUserName().length() == 0) {
                continue;
            }
            String section = String.valueOf(ChineseUtils.getFirstSpell(String.valueOf(items.get(i).getUserName().charAt(0))).toUpperCase());
            if (!StringUtils.isAlpha(section)) {
                section = "#";
            }
            if (!sections.contains(section)) {
                mSectionPositions.add(NUM_HEADER_ITEMS + (i - 1) + sections.size());
                sections.add(section);
            }
        }
        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return mSectionPositions.get(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int positionIndex) {
        return 0;
    }

    private int getSection(int positionIndex) {
        for (int secPos : mSectionPositions) {
            if (positionIndex < secPos)
                return mSectionPositions.indexOf(secPos) - 1;
        }
        return mSectionPositions.size() - 1;
    }

    public interface OnHeaderClickListener {
        void onNewFriendClick();

        void onGroupContactsClick();
    }

    public Site getCurrentSite() {
        return currentSite;
    }

    public void setCurrentSite(Site currentSite) {
        this.currentSite = currentSite;
    }
}
