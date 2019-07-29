package com.akaxin.client.util;

import android.graphics.drawable.GradientDrawable;

import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;

/**
 * Created by yichao on 2017/11/16.
 */

public class ThemeUtils {

    /**
     * 获取主题颜色可以做定制化配置
     *
     * @return
     */
    public static int  getThemeColor() {
        //todo 需要赋值给变量避免重复get resources 特别是在二维码绘制时
        return ZalyApplication.getContext().getResources().getColor(R.color.theme_color1);
    }

    /**
     * 获取白色字体颜色
     *
     * @return
     */
    public static int getWhiteTxtColor() {
        return ZalyApplication.getContext().getResources().getColor(R.color.white);
    }

    /**
     * 获取主要背景色
     *
     * @return
     */
    public static int getMainBgColor() {
        return ZalyApplication.getContext().getResources().getColor(R.color.bg);
    }

    /**
     * 获取按钮格式
     *
     * @return
     */
    public static GradientDrawable getButtonStyle() {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadii(new float[]{100, 100, 100, 100, 100, 100, 100, 100});
        shape.setColor(getThemeColor());
        shape.setStroke(0, getThemeColor());
        return shape;
    }

}
