package com.windchat.client.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.bean.PersonalItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mr.kk on 2018/6/28.
 * This Project was client-android
 */

public class PersonalDialogAdapter extends RecyclerView.Adapter<PersonalDialogAdapter.ViewHolder> {
    List<PersonalItem> items;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public PersonalDialogAdapter(List<PersonalItem> items) {
        this.items = items;
    }

    @Override
    public PersonalDialogAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_user_setting, parent, false);
        ViewHolder holder = new ViewHolder(v);
        holder.content.setOnClickListener(new itemClick());
        return holder;
    }

    @Override
    public void onBindViewHolder(PersonalDialogAdapter.ViewHolder holder, int position) {
        holder.content.setTag(position);
        holder.adapterItemUserSettingTv.setText(items.get(position).getItemDesc());
        holder.adapterItemUserSettingBubble.setVisibility(items.get(position).isShowTip() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {
        @BindView(R.id.adapter_item_user_setting_tv)
        TextView adapterItemUserSettingTv;
        @BindView(R.id.adapter_item_user_setting_bubble)
        ImageView adapterItemUserSettingBubble;
        @BindView(R.id.content)
        LinearLayout content;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    /**
     * 设置条目监听
     *
     * @param listener
     */
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    /**
     * 条目点击事件接口
     */
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * 条目监听
     */
    class itemClick implements View.OnClickListener {


        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                //注意这里使用getTag方法获取数据
                mOnItemClickListener.onItemClick(v, Integer.parseInt(v.getTag().toString()));
            }
        }
    }
}
