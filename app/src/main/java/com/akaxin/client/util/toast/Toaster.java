package com.akaxin.client.util.toast;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

/**
 * Toast显示器。如果你要使用静态方法的方式显示Toast，那么请务必保证之前调用过{@link #doEnable(Context)}
 *
 * @author <a href="mailt:wenlin56@sina.com"> wjh </a>
 */

public class Toaster {
    private final static int MSG_SHOW_D = 1365;
    private final static int MSG_SHOW_INVALIDATE = 1366;
    private final static int MSG_SHOW_INVALIDATE_D = 1367;
    private final static boolean DEBUG = false;

    private final static int DEFAULT_DURATION = Toast.LENGTH_SHORT;

    public final static int LENGTH_LONG = Toast.LENGTH_LONG;
    public final static int LENGTH_SHORT = Toast.LENGTH_SHORT;

    protected Toast toast = null;
    private static Context context = null;

    private static Toaster showner = null;

    protected Toaster() {
        if (context == null)
            throw new RuntimeException("Showner not been activated. You must call 'doEnable(Context c)' method before");
        makeNewToast();
    }


    public static void reset() {
        showner = null;
    }

    public void setGravity(int gravity, int xOffset, int yOffset) {
        toast.setGravity(gravity, xOffset, yOffset);
    }

    public void showMsg(String msg) {
        showMsg(msg, false);
    }

    public void showMsg(Object msg) {
        showMsg(msg.toString());
    }

    public void showMsg(int resId) {
        showMsg(context.getString(resId));
    }

    public void showMsg(String msg, boolean makeNew) {
        showMsg(msg, false, DEFAULT_DURATION);
    }

    public void showMsg(String msg, boolean makeNew, int duration) {
        try {
            if (makeNew) {
                makeNewToast();
            }

            toast.setText(msg);
            toast.setDuration(duration);
            toast.show();
        } catch (Throwable e) { //低端手机这里容易OOM, hold住先，不影响业务
        }
    }

    public int getDuration() {
        return toast.getDuration();
    }

    public void setDuration(int duration) {
        toast.setDuration(duration);
    }

    public void setView(View view) {
        toast.setView(view);
    }

    public static void show(Object obj, int duration) {
        show(obj.toString(), duration);
    }

    public static void show(int resId, int duration) {
        show(context.getString(resId), duration);
    }

    public static void show(Object text) {
        show(text, DEFAULT_DURATION);
    }

    public static void show(String text) {
        show(text, DEFAULT_DURATION);
    }


    public static void debug(String text) {
        if (DEBUG)
            show(text, DEFAULT_DURATION);
    }

    public static void show(String text, int duration) {
        Message msg = Message.obtain();
        msg.what = MSG_SHOW_D;
        msg.obj = text;
        msg.arg1 = duration;
        handler.sendMessage(msg);
    }

    public static void show(int resId) {
        String msg = context.getString(resId);
        show(msg);
    }

    /**
     * 设置ToastShowner的View。注意此修改是针对全局的，即，将影响到下一次ToastShwoner.show()的调用。
     *
     * @param view
     */
    public static void changeView(View view) {
        showner.setView(view);
    }


    protected void makeNewToast() {
        toast = Toast.makeText(context, "", DEFAULT_DURATION);
    }

    /**
     * 将ToastShowner激活，在ToastShowner使用之前必须要进行激活。
     * 通常情况下，ToastShowner的整个生命周期中只需要被激活一次。
     */
    public static void doEnable(Context context) {
        Toaster.context = context;
//		showner = MToaster.getInstance();
    }


    public static void showInvalidate(CharSequence message) {
        Message msg = new Message();
        msg.what = MSG_SHOW_INVALIDATE;
        msg.obj = message;
        handler.sendMessage(msg);
    }

    public static void showInvalidate(CharSequence message, int duration) {
        Message msg = new Message();
        msg.what = MSG_SHOW_INVALIDATE_D;
        msg.obj = message;
        msg.arg1 = duration;
        handler.sendMessage(msg);
    }

    public static void showInvalidate(int resId, int duration) {
        String str = context.getString(resId);
        showInvalidate(str, duration);
    }

    public static void debugInvalidate(CharSequence message) {
        if (!DEBUG) return;
        showInvalidate(message);
    }

    public static void showInvalidate(int resId) {
        String str = context.getString(resId);
        showInvalidate(str);
    }

    private static Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_SHOW_D) {
                doShow((String) msg.obj, msg.arg1);
            } else if (msg.what == MSG_SHOW_INVALIDATE) {
                doShowInvalidate((String) msg.obj);
            } else if (msg.what == MSG_SHOW_INVALIDATE_D) {
                doShowInvalidate((String) msg.obj, msg.arg1);
            }
        }
    };

    private static void doShow(String text, int duration) {
        try {
            if (showner == null) {
                showner = MToaster.getInstance();
            }
            int d = showner.getDuration();
            showner.showMsg(text, false, duration);
            showner.setDuration(d);
        } catch (Throwable e) {
            Logger.e(e);
//			Crashlytics.logException(e);
            //TODO ruanlei
        }
    }

    private static void doShowInvalidate(String str) {
        if (showner == null) {
            showner = MToaster.getInstance();
        }
        showner.showMsg(str);
    }

    private static void doShowInvalidate(String str, int dur) {
        if (showner == null) {
            showner = MToaster.getInstance();
        }
        int d = showner.getDuration();
        showner.showMsg(str, false, dur);
        showner.setDuration(d);
    }

}
