package com.akaxin.client.maintab.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yichao on 2017/10/28.
 */

public abstract class ZalyListAdapter<T, M extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<M> {

    protected List<T> items;


    public ZalyListAdapter() {
        items = new ArrayList<>();
    }

    public void addItems(List<T> items) {
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void addAllItems(List<T> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void removeItem(int pos) {
        this.items.remove(pos);
        notifyItemRemoved(pos);
    }

    public void removeAllItems() {
        this.items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
