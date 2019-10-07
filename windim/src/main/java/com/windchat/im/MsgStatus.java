package com.windchat.im;

/**
 * msg_status: 1 发送成功
 * msg_status: 0 默认状态
 * msg_status: -1 用户非好友关系，二人消息发送失败
 * msg_status: -2 用户非群成员，群消息发送失败
 * <p>
 * Created by anguoyue on 2019/10/7.
 */
public enum MsgStatus {


    MSG_SENDING(0, "发送中"),
    MSG_SUCCESS(1, "发送成功"),
    MSG_FAIL_NOTFRIEND(-1, "用户非好友关系，二人消息发送失败"),
    MSG_FAIL_NOTGROUPMEMBER(-2, "用户非群成员，群消息发送失败");

    int value;
    String msg;

    MsgStatus(int value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    public int getValue() {
        return this.value;
    }

    public String getMsg() {
        return this.msg;
    }

    public static MsgStatus parseFrom(int value) {
        for (MsgStatus status : MsgStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return MSG_SENDING;
    }

}
