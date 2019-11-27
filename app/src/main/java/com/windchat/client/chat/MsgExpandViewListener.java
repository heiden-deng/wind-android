package com.windchat.client.chat;

import com.akaxin.proto.core.PluginProto;

/**
 * Created by yichao on 2017/11/7.
 */

public interface MsgExpandViewListener {

    int ITEM_PHOTO = 1;
    int ITEM_CAMERA = 2;
    int ITEM_PLUGIN = 3;

    void onItemClick(int itemType);

    void onEmojiClick(String emoji);

    void onDelClick();

    void onMsgPluginClick(PluginProto.Plugin plugin);

}
