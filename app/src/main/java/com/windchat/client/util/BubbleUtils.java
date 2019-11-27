package com.windchat.client.util;

import android.view.View;
import android.widget.TextView;

/**
 * Created by alexfan on 2018/3/21.
 */

public class BubbleUtils {

    public static final String MORE_THAN_99 = "99+";

    public static void updateBubble(TextView bubbleTextView, int notificationNum) {
        if (notificationNum > 0) {
            bubbleTextView.setVisibility(View.VISIBLE);
            if (notificationNum > 99)
                bubbleTextView.setText(MORE_THAN_99);
            else
                bubbleTextView.setText(String.valueOf(notificationNum));
        } else {
            bubbleTextView.setVisibility(View.INVISIBLE);
        }
    }
}
