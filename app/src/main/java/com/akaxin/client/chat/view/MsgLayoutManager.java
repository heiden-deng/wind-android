package com.akaxin.client.chat.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.orhanobut.logger.Logger;

/**
 * Created by yichao on 2017/12/17.
 */

public class MsgLayoutManager extends LinearLayoutManager {
    public MsgLayoutManager(Context context) {
        super(context);
    }

    public MsgLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public MsgLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void collectAdjacentPrefetchPositions(int dx, int dy, RecyclerView.State state, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        try {
            super.collectAdjacentPrefetchPositions(dx, dy, state, layoutPrefetchRegistry);
        } catch (IllegalArgumentException e) {
            Logger.e("catch exception");
        }
    }
}
