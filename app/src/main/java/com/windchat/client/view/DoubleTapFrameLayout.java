package com.windchat.client.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by yichao on 2017/12/14.
 */

// 这种写法屏蔽了长按

public class DoubleTapFrameLayout extends FrameLayout {

    DoubleTapListener tapListener;

    private long lastClickTime = 0;
    private final long mDoubleClickInterval = 400;
    private float x, y;
    private long downTime;

    public DoubleTapFrameLayout(Context context) {
        super(context);
    }

    public DoubleTapFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void setTapListener(DoubleTapListener tapListener) {
        this.tapListener = tapListener;
    }

    public interface DoubleTapListener {
        boolean onDoubleClick();

    }
}
