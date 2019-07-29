package com.akaxin.client.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.LinearLayout;

import com.akaxin.client.ZalyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Project momodev
 * Package com.immomo.framework.view
 * Created by tangyuchun on 4/29/16.
 */
public class UIUtils {
    public static Float sScreenDensity = null;
    public static Float sTextScale = null;

    /**
     * 获得Toolbar的高度
     *
     * @param pContext
     * @return
     */
    public static int getToolbarHeight(Context pContext) {
        // Calculate ActionBar height
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (pContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, pContext.getResources().getDisplayMetrics());
        }
        if (actionBarHeight <= 0) {
            final float scale = pContext.getResources().getDisplayMetrics().density;
            return (int) (52f * scale + 0.5f);
        }
        return actionBarHeight;
    }

    /**
     * 获取ListView的滚动距离，此方法不具有通用性，要求ListView中的条目高度必须一样才行
     *
     * @param listView
     * @param headerHeight 如果有header，需要加上header的高度,否则，传0即可
     * @return
     */
    public static int getScrollY(AbsListView listView, int headerHeight) {
        View c = listView.getChildAt(0);
        if (c == null) {
            return 0;
        }
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        int top = c.getTop();

        int extraHeight = 0;
        if (firstVisiblePosition >= 1) {
            extraHeight = headerHeight;
        }
        return -top + firstVisiblePosition * c.getHeight() + extraHeight;
    }

    public static float getScreenDensity() {
        if (sScreenDensity == null) {
            sScreenDensity = getDisplayMetrics().density;
        }
        return sScreenDensity.floatValue();
    }

    /**
     * 获取屏幕分辨率宽度 *
     */
    public static int getScreenWidth() {
        return getDisplayMetrics().widthPixels;
    }

    /**
     * 获取屏幕分辨率高度 *
     */
    public static int getScreenHeight() {
        return getDisplayMetrics().heightPixels;
    }

    public static Resources getResources() {
        return ZalyApplication.getContext().getResources();
    }

    public static DisplayMetrics getDisplayMetrics() {
        return getResources().getDisplayMetrics();
    }

    public static float getTextScale() {
        if (sTextScale == null) {
            sTextScale = getDisplayMetrics().scaledDensity;
        }
        return sTextScale.floatValue();
    }

    public static String getString(int resource) {
        return ZalyApplication.getContext().getString(resource);
    }

    public static int getInt(int resource) {
        return ZalyApplication.getContext().getResources().getInteger(resource);
    }

    public static String getString(int resource, Object... formatArgs) {
        return ZalyApplication.getContext().getString(resource, formatArgs);
    }

    public static int getResourceId(String name) {
        return getResources().getIdentifier(name, null, ZalyApplication.getContext().getPackageName());
    }

    public static Drawable getDrawable(int resource) {
        return getResources().getDrawable(resource);
    }

    public static int getColor(int resource) {
        return getResources().getColor(resource);
    }

    public static Bitmap getBitmap(int resource) {
        return BitmapFactory.decodeResource(getResources(), resource);
    }

    public static int getPixels(float dip) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getDisplayMetrics()));
    }

    /**
     * 像素转换成DIP *
     */
    public static int getDips(float pixel) {
        if (pixel >= 0) {
            return (int) (pixel / getDisplayMetrics().density + 0.5f);
        } else {
            return 0;
        }
    }

    public static int getDimensionPixelSize(int id) {
        return getResources().getDimensionPixelSize(id);
    }

    public static int sp2pix(float sp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getDisplayMetrics()));
    }

    /**
     * 解析图片文件，返回图片对象。根据设备自动调整图片大小
     *
     * @param file
     * @return
     */
    public static Bitmap decodeResourceBitmap(File file, int resid) {
        if (!file.exists()) {
            return null;
        }
        try {
            return decodeResourceBitmap(new FileInputStream(file), resid);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public static Bitmap decodeResourceBitmap(InputStream is, int resId) {
        try {
            //            if (resId < 0) {
            //                resId = R.drawable.app_icon;
            //            }

            Rect pad = new Rect();
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inScreenDensity = getDisplayMetrics().densityDpi;
            TypedValue value = new TypedValue();
            Resources resources = getResources();
            resources.getValue(resId, value, false);

            final int density = value.density;
            if (density == TypedValue.DENSITY_DEFAULT) {
                opts.inDensity = DisplayMetrics.DENSITY_DEFAULT;
            } else if (density != TypedValue.DENSITY_NONE) {
                opts.inDensity = density;
            }
            opts.inTargetDensity = resources.getDisplayMetrics().densityDpi;

            Bitmap bitmap = BitmapFactory.decodeStream(is, pad, opts);
            is.close();
            return bitmap;
        } catch (Throwable e) {
        }

        return null;
    }

    public static InputMethodManager getInputMethodManager() {
        return (InputMethodManager) ZalyApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public static void hideInputMethod(Activity activity) {
        InputMethodManager im = ((InputMethodManager) ZalyApplication.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE));
        View curFocusView = activity.getCurrentFocus();
        if (curFocusView != null) {
            im.hideSoftInputFromWindow(curFocusView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static RectF getViewLocationOnScreen(View v) {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        int vx = location[0];
        int vy = location[1];
        RectF viewRectF = new RectF(vx, vy, vx + v.getMeasuredWidth(), vy + v.getMeasuredHeight());
        return viewRectF;
    }

    public static void switchFullscreen(Activity activity, boolean isFullscreen) {
        if (isFullscreen) {
            WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            activity.getWindow().setAttributes(attrs);
        } else {
            WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().setAttributes(attrs);
        }
    }

    /**
     * 生成新的resId,同时不与之前的重复
     * <p>
     * Each resource identifier consists of 32 bits were 8 top bits defines the package
     * in which the resource belongs to.
     * Platform resources are 0x01 and 0x7F are application resources.
     * The idea is to use the remaining 253 values for extension packages.
     *
     * @param res       旧的resId
     * @param newOffset 生成新的resId的offset,最多支持生成253个不同
     * @return
     */
    public static long uniqueResId(long res, long newOffset) {
        if (newOffset <= 0 || newOffset >= 0x000000fd) return res;
        if (res>>24 == 0x0000007f || res>>24 == 0x00000001) {
            //0x7f -> 0x80
            //0x01 -> 0x02
            if (newOffset >= 0x0000007e) newOffset = newOffset + 1;
            return res + (newOffset<<24);
        } else {
            //other
            return res;
        }
    }

    /**
     * 获取虚拟按键高度
     *
     * @return
     */
    public static int getVirtualBarHeight() {
        return getRealScreenHeight() - getScreenHeight();
    }

    /**
     * 获取真正屏幕高度（若有虚拟按键，会加上虚拟按键）
     *
     * @return
     */
    public static int getRealScreenHeight() {
        int h = 0;
        WindowManager windowManager = (WindowManager) ZalyApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            Class c = Class.forName("android.view.Display");
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            h = dm.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (h <= 0)
            h = getScreenHeight();
        return h;
    }

    public static void screenRotateLeft(View v, int px, int py) {
        final int[] loc = new int[2];
        v.getLocationOnScreen(loc);
        final int vh = v.getHeight();
        final int top = loc[1];
        final int sw = getScreenWidth();
        final int sh = getRealScreenHeight();
        final int centerY = top + (vh>>1);
        v.setRotation(90);
        v.setTranslationY(py - centerY);
        int tx = sw - px - (vh>>1) - sw * top / sh;
        v.setTranslationX(tx);
    }

    public static void screenRotateLeftByCenter(View v) {
        screenRotateLeft(v, getScreenWidth()>>1, getRealScreenHeight()>>1);
    }

    public static void screenRotateNormal(View v) {
        v.setRotation(0);
        v.setTranslationY(0);
        v.setTranslationX(0);
    }

    /**
     * 收起AppBarLayout
     *
     * @param coordinatorLayout
     * @param appBarLayout
     */
    public static void collapseAppBarLayout(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout) {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        if (lp.getBehavior() != null) {
            AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) lp.getBehavior();
            behavior.onNestedFling(coordinatorLayout, appBarLayout, null, 0, 0, false);
        }
    }

    /**
     * 展开AppBarLayout
     *
     * @param coordinatorLayout
     * @param appBarLayout
     */
    public static void expandAppBarLayout(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout) {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        if (lp.getBehavior() != null) {
            AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) lp.getBehavior();

            behavior.setTopAndBottomOffset(0);
            behavior.onNestedPreScroll(coordinatorLayout, appBarLayout, null, 0, 1, new int[2]);
        }
    }

    public static Drawable getCornerBackGroundDrawable(int corner, @ColorInt int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(corner);
        drawable.setColor(color);
        return drawable;
    }

    public static int calculateTextWidth(String text, float sizeInSp) {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(UIUtils.sp2pix(sizeInSp));
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);

        return bounds.width();
    }

    public static void setTabLayoutIndicatorWidth(TabLayout tabLayout, int marginLeft, int marginRight) {
        setTabLayoutIndicatorWidth(tabLayout, tabLayout.getClass(), marginLeft, marginRight);
    }

    public static void setTabLayoutIndicatorWidth(TabLayout tabLayout, Class<?> tabLayoutClass, int marginLeft, int marginRight) {
        Field tabStrip = null;
        try {
            tabStrip = tabLayoutClass.getDeclaredField("mTabStrip");
            tabStrip.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        LinearLayout layout = null;
        try {
            if (tabStrip != null) {
                layout = (LinearLayout) tabStrip.get(tabLayout);
            }
            if (layout == null) {
                return;
            }
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                child.setPadding(0, 0, 0, 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    params.setMarginStart(UIUtils.getPixels(marginLeft));
                    params.setMarginEnd(UIUtils.getPixels(marginRight));
                }
                child.setLayoutParams(params);
                child.invalidate();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 只能用于Activity，其他情况，如Fragment，请使用{@link #findView(View, int)}
     */
    @SuppressWarnings("unchecked")
    public static <T extends View> T findView(@NonNull Activity activity, @IdRes int id) {
        return (T) activity.findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T findView(@NonNull View view, @IdRes int id) {
        return (T) view.findViewById(id);
    }

    /**
     *  计算单个 Item 扣除各种间距后的宽度
     * @param singleItemIntervalTotal  单个 Item 横向上的padding、margin和
     * @param otherIntervalTotal 其他间隙，比如横向上整体的。
     * @param columnsNum 列数
     * @return
     */
    public static int calculateItemWidthOnScreen(int singleItemIntervalTotal, int otherIntervalTotal, int columnsNum) {
        return  (UIUtils.getScreenWidth() - singleItemIntervalTotal * columnsNum - otherIntervalTotal) / columnsNum;
    }

    /**
     *  重设 View 的宽高，宽高不变时直接返回。
     * @param view
     * @param newHeight
     * @param newWidth
     * @return  view
     */
    public static <T extends View> T resetViewSize(T view , int newHeight, int newWidth){
        int oldHeight = view.getMeasuredHeight();
        int oldWidth = view.getMeasuredWidth();
        if(oldHeight==newHeight&&oldWidth==newWidth){
            return view;
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height=newHeight;
        layoutParams.width=newWidth;
        view.requestLayout();
        return view;
    }

}
