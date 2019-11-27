package com.windchat.client.maintab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.bean.Site;

/**
 * Created by yichao on 2017/11/4.
 */

public class ContactListAdapter extends ZalyListAdapter<Site, ContactListAdapter.ViewHolder> {

    private ContactListListener listListener;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_contact_site, parent, false);
        return new ContactListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Site site = items.get(position);
        holder.siteName.setText(site.getSitePort());
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listListener !=  null) {
                    listListener.onSiteClick(position, site);
                }
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView siteName;
        public View itemLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            siteName = itemView.findViewById(R.id.site_name);
            itemLayout = itemView.findViewById(R.id.item_layout);
        }
    }

    public void setListListener(ContactListListener listListener) {
        this.listListener = listListener;
    }

    public interface ContactListListener {
        void onSiteClick(int position, Site site);
    }

}
