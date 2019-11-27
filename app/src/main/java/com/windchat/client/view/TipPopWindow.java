package com.windchat.client.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.windchat.client.R;
import com.windchat.client.adapter.MessageTipAdapter;
import com.windchat.client.bean.Message;
import com.windchat.client.bean.Site;
import com.windchat.client.bean.TipMsg;
import com.windchat.client.util.toast.Toaster;
import com.akaxin.proto.core.CoreProto;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ScreenUtils;

/**
 * Created by Mr.kk on 2018/4/27.
 * This Project was client-android-0.4.3
 */

public class TipPopWindow extends PopupWindow implements MessageTipAdapter.onTipSelectListener {
    private float density = 1.0f;
    private Context mContext;
    private Message msg;
    private onTipSelectListener mOnTipSelectListener;
    private View childView;
    TipMsg tipMsg;
    private Site currentSite;

    public TipPopWindow(Context context, Message msg, Site site) {
        View view = View.inflate(context, R.layout.popwindow_msg_content_tip, null);
        setContentView(view);
        this.childView = view;
        this.mContext = context;
        this.msg = msg;
        this.tipMsg = getMsgTips(msg);
        initPopupWindow();
        //设置popwindow的宽高，这个数字是多少就设置多少dp，注意单位是dp
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        this.currentSite = site;
    }

    //初始化popwindow
    private void initPopupWindow() {
        // setAnimationStyle(R.style.tipPopWindow);//设置动画
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        density = mContext.getResources().getDisplayMetrics().density;//

        RecyclerView dialogTipList = childView.findViewById(R.id.pop_msg_content_tip_list);
        dialogTipList.setLayoutManager(new LinearLayoutManager(mContext));
        dialogTipList.setItemAnimator(new DefaultItemAnimator());
        dialogTipList.setHasFixedSize(true);

        MessageTipAdapter adapter = new MessageTipAdapter(tipMsg, msg);
        dialogTipList.setAdapter(adapter);
        adapter.setOnTipSelectListener(this);

    }

    public void showPopWindow(View parent, int touchX, int touchY) {
        if (tipMsg == null || tipMsg.getTipMsgSize() == 0) {
            Toaster.show("tip error happen");
            return;
        }
        int toolBarHeight = ConvertUtils.dp2px(66);
        int popWidth = ConvertUtils.dp2px(120);
        int popHeight = ConvertUtils.dp2px(48 * getMsgTips(msg).getTipMsgSize());
        int inputHeight = 100;
        int screenHeight = ScreenUtils.getScreenHeight();
        int screenWidth = ScreenUtils.getScreenWidth();
        if (!this.isShowing()) {
            if ((touchY + popHeight) >= (screenHeight - inputHeight)) {
                if (touchX + popWidth >= screenWidth) {
                    this.showAtLocation(parent, Gravity.NO_GRAVITY, touchX-popWidth, touchY - popHeight);
                }else{
                    this.showAtLocation(parent, Gravity.NO_GRAVITY, touchX, touchY - popHeight);
                }
            } else {
                if (touchX + popWidth >= screenWidth) {
                    this.showAtLocation(parent, Gravity.NO_GRAVITY, touchX-popWidth, touchY);
                }else{
                    this.showAtLocation(parent, Gravity.NO_GRAVITY, touchX, touchY);
                }
            }

        } else {
            dismiss();
        }
    }


    @Override
    public void onItemClick(View view, int tipMsgId, Message msg) {
        mOnTipSelectListener.onTipSelect(msg, tipMsgId, this);
    }

    public interface onTipSelectListener {
        void onTipSelect(Message msg, int tipMsgId, PopupWindow tipView);
    }

    public void setOnTipSelectListener(onTipSelectListener listener) {
        this.mOnTipSelectListener = listener;
    }

    /**
     * 判断提示词
     *
     * @param msg
     * @return
     */
    protected TipMsg getMsgTips(Message msg) {
        int status = msg.getMsgStatus();
        int type = msg.getMsgType();
        if (status == Message.STATUS_SEND_SUCCESS) {
            if (msg.getMsgType() == CoreProto.MsgType.TEXT_VALUE || msg.getMsgType() == CoreProto.MsgType.SECRET_TEXT_VALUE || msg.getMsgType() == CoreProto.MsgType.GROUP_TEXT_VALUE) {
                return new TipMsg(TipMsg.TIP_MSG_DELETE, TipMsg.TIP_MSG_COPY);
            } else {
                return new TipMsg(TipMsg.TIP_MSG_DELETE);
            }
        }
        if (status == Message.STATUS_SEND_FAILED_NOT_IN_GROUP || status == Message.STATUS_SEND_FAILED || status == Message.STATUS_SEND_FAILED_NOT_FRIEND || status == Message.STATUS_SENDING) {
            if (msg.getMsgType() == CoreProto.MsgType.TEXT_VALUE || msg.getMsgType() == CoreProto.MsgType.SECRET_TEXT_VALUE || msg.getMsgType() == CoreProto.MsgType.GROUP_TEXT_VALUE) {
                return new TipMsg(TipMsg.TIP_MSG_RESEND, TipMsg.TIP_MSG_DELETE, TipMsg.TIP_MSG_COPY);

            } else {
                return new TipMsg(TipMsg.TIP_MSG_RESEND, TipMsg.TIP_MSG_DELETE);
            }
        }
        if (status == Message.STATUS_RECEIVE_NONE || status == Message.STATUS_RECEIVE_READ || status == Message.STATUS_RECEIVE_UNREAD) {
            if (msg.getMsgType() == CoreProto.MsgType.TEXT_VALUE || msg.getMsgType() == CoreProto.MsgType.SECRET_TEXT_VALUE || msg.getMsgType() == CoreProto.MsgType.GROUP_TEXT_VALUE) {
                return new TipMsg(TipMsg.TIP_MSG_DELETE, TipMsg.TIP_MSG_COPY);
            } else {
                return new TipMsg(TipMsg.TIP_MSG_DELETE);
            }
        }

        if (type == CoreProto.MsgType.GROUP_WEB_NOTICE_VALUE || type == CoreProto.MsgType.U2_WEB_NOTICE_VALUE
                || type == CoreProto.MsgType.GROUP_WEB_VALUE || type == CoreProto.MsgType.U2_WEB_VALUE
                ) {
            return new TipMsg(TipMsg.TIP_MSG_DELETE);
        }
        return null;
    }
}
