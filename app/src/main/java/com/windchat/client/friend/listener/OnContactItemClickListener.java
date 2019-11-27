package com.windchat.client.friend.listener;

import com.akaxin.proto.core.UserProto;

/**
 * Created by yichao on 2017/10/25.
 */

public interface OnContactItemClickListener {
    void onFriendClick(UserProto.SimpleUserProfile profile);
}
