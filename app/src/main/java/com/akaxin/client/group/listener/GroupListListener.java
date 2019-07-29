package com.akaxin.client.group.listener;

import com.akaxin.proto.core.GroupProto;

/**
 * Created by yichao on 2017/10/25.
 */

public interface GroupListListener {
    void onGroupClick(GroupProto.SimpleGroupProfile profile);
}
