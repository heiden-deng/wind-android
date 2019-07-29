package com.akaxin.client.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.akaxin.client.ZalyApplication;

/**
 * Created by alexfan on 2018/4/12.
 */

public abstract class ClipboardUtils {
    private static final String CLIPBOARD_LABEL = "akaxin";

    public static boolean copyToClipboard(String string) {
        ClipboardManager clipboard = (ClipboardManager) ZalyApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(CLIPBOARD_LABEL, string);
            clipboard.setPrimaryClip(clip);
            return true;
        }
        return false;
    }
}
