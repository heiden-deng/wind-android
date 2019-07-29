package com.akaxin.client.adapter;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.Message;
import com.akaxin.client.bean.TipMsg;
import com.akaxin.client.bean.event.MessageEvent;
import com.akaxin.client.util.toast.Toaster;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mr.kk on 2018/4/27.
 * This Project was client-android-0.4.3
 */

public class MessageTipAdapter extends RecyclerView.Adapter<MessageTipAdapter.ViewHolder> {
    private  static final String TAG = MessageTipAdapter.class.getSimpleName();
    private onTipSelectListener mOnTipSelectListener = null;
    private TipMsg tipMsg;
    private Message msg;

    public MessageTipAdapter(TipMsg tipMsg, Message msg) {
        this.tipMsg = tipMsg;
        this.msg=msg;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_message_tip, parent, false);
        ViewHolder holder = new ViewHolder(v);
        holder.itemMsgTipContent.setOnClickListener(new itemClick());
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemMsgTipContent.setTag(tipMsg.getTipMsg().get(position).getMsgId());
        holder.itemMsgTipTv.setText(tipMsg.getTipMsg().get(position).getMsgContent());
    }

    @Override
    public int getItemCount() {
        return tipMsg == null ? 0 : tipMsg.getTipMsgSize();
    }

    // 重写的自定义ViewHolder
    public static class ViewHolder
            extends RecyclerView.ViewHolder {
        @BindView(R.id.item_msg_tip_tv)
        TextView itemMsgTipTv;
        @BindView(R.id.item_msg_tip_content)
        LinearLayout itemMsgTipContent;
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
    public void setOnTipSelectListener(onTipSelectListener listener) {
        this.mOnTipSelectListener = listener;
    }

    /**
     * 条目点击事件接口
     */
    public interface onTipSelectListener {
        void onItemClick(View view, int tipMsgId, Message msg);
    }

    /**
     * 条目监听
     */
    class itemClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mOnTipSelectListener != null) {

                //注意这里使用getTag方法获取数据
                mOnTipSelectListener.onItemClick(v, Integer.parseInt(v.getTag().toString()),msg);
                int option = Integer.parseInt(v.getTag().toString());
                Bundle bundle = new Bundle();
                bundle.putParcelable(Message.Action.MSG_INFO, msg);

                /////判断是U2, Group
                if(msg.getGroupId() == null) {
                    switch(option) {
                        case TipMsg.TIP_MSG_RESEND:
                            EventBus.getDefault().post(new MessageEvent(Message.Action.U2_RESEND_MSG, bundle));
                            break;

                        case TipMsg.TIP_MSG_DELETE:
                            EventBus.getDefault().post(new MessageEvent(Message.Action.U2_DELETE_MSG, bundle));
                            break;

                        case TipMsg.TIP_MSG_COPY:
                            Toaster.show(TipMsg.TIP_MSG_FOR_COPY);
                            ClipboardManager cmb = (ClipboardManager) ZalyApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            cmb.setText(msg.getContent()); //将内容放入粘贴管理器,在别的地方长按选择"粘贴"即可
                            break;
                    }
                } else {
                    switch(option) {
                        case TipMsg.TIP_MSG_RESEND:
                            EventBus.getDefault().post(new MessageEvent(Message.Action.GROUP_RESEND_MSG, bundle));
                            break;

                        case TipMsg.TIP_MSG_DELETE:
                            EventBus.getDefault().post(new MessageEvent(Message.Action.GROUP_DELETE_MSG, bundle));
                            break;

                        case TipMsg.TIP_MSG_COPY:
                            Toaster.show(TipMsg.TIP_MSG_FOR_COPY);
                            ClipboardManager cmb = (ClipboardManager) ZalyApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            cmb.setText(msg.getContent()); //将内容放入粘贴管理器,在别的地方长按选择"粘贴"即可
                            break;

                    }
                }
            }
        }
    }
}
