package com.akaxin.client.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.akaxin.client.R;
import com.akaxin.client.util.ThemeUtils;

/**
 * Created by yichao on 2017/11/16.
 */

public class ZalyTextView extends TextView {

    public static final String TAG = "ZalyTextView";

    private boolean showBg;

    public ZalyTextView(Context context) {
        super(context);
        initView(context, null);
    }

    public ZalyTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public ZalyTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ZalyTextView,
                0, 0);
        try {
            showBg = a.getBoolean(R.styleable.ZalyTextView_showBg, false);
        } finally {
            a.recycle();
        }


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showBg) {
            setTextColor(ThemeUtils.getWhiteTxtColor());
            setBackgroundDrawable(ThemeUtils.getButtonStyle());
        } else {
            setTextColor(ThemeUtils.getThemeColor());
        }
    }
}
