package com.windchat.im.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mr.kk on 2018/4/27.
 * This Project was client-android
 */

public class TipMsg {
    public static final int TIP_MSG_RESEND = 1;
    public static final int TIP_MSG_DELETE = 2;
    public static final int TIP_MSG_COPY = 3;
    public static final String TIP_MSG_FOR_COPY = "已复制";

    private List<Msg> msgs = new ArrayList<>();

    public TipMsg(){
        if (!msgs.isEmpty())
            msgs.clear();
    }


    public TipMsg(int... tipmsgId){
        if (!msgs.isEmpty())
            msgs.clear();
        for (int i:tipmsgId)
            putTipMsg(i);
    }

    public void putTipMsg(int msgType){
        switch (msgType){
            case TIP_MSG_RESEND:
                msgs.add(new Msg(TIP_MSG_RESEND,"重发"));
                break;
            case TIP_MSG_DELETE:
                msgs.add(new Msg(TIP_MSG_DELETE,"删除"));
                break;
            case TIP_MSG_COPY:
                msgs.add(new Msg(TIP_MSG_COPY,"复制"));
                break;

        }
    }
    public List<Msg> getTipMsg(){
        return msgs;
    }
    public int getTipMsgSize(){
        return msgs.size();
    }

    public class Msg {
        private int msgId;
        private String msgContent;

        public Msg(int msgId, String msgContent) {
            this.msgId = msgId;
            this.msgContent = msgContent;
        }

        public int getMsgId() {
            return msgId;
        }

        public void setMsgId(int msgId) {
            this.msgId = msgId;
        }

        public String getMsgContent() {
            return msgContent;
        }

        public void setMsgContent(String msgContent) {
            this.msgContent = msgContent;

        }
    }

}
