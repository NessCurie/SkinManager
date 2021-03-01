package com.github.skinmanager.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.TextView;

/**
 * 文字颜色状态选择器{@link ColorStateList}和控件背景状态选择器{@link StateListDrawable}的工具类
 */
public class SelectorUtils {

    /**
     * 创建用于文字颜色状态选择器
     * 此方法为工具方法,可以在任意时刻调用
     *
     * @param normalColor  默认颜色
     * @param pressedColor 按下和按住的颜色
     * @return 文字颜色状态选择器
     */
    public static ColorStateList createColorState(int normalColor, int pressedColor) {
        int[][] stateArray = {{android.R.attr.state_pressed, android.R.attr.state_enabled},
                {android.R.attr.state_checked},
                {android.R.attr.state_selected},
                {android.R.attr.state_enabled, android.R.attr.state_focused},
                {android.R.attr.state_enabled},
                {android.R.attr.state_focused},
                {android.R.attr.state_window_focused}};
        int[] colors = {pressedColor, pressedColor, pressedColor, pressedColor,
                normalColor, normalColor, normalColor};
        return new ColorStateList(stateArray, colors);
    }

    /**
     * 使用Resource中的颜色资源创建文字颜色状态选择器
     * 此方法为工具方法,可以在任意时刻调用
     *
     * @param context           上下文
     * @param normalColorResId  默认图片
     * @param pressedColorResId 按下时的图片
     * @return 文字颜色状态选择器
     */
    public static ColorStateList createColorState(Context context, int normalColorResId, int pressedColorResId) {
        return createColorState(context.getResources().getColor(normalColorResId),
                context.getResources().getColor(pressedColorResId));
    }

    /**
     * 设置文字颜色状态
     * 此方法为工具方法,可以在任意时刻调用
     *
     * @param view         对应view
     * @param normalColor  默认颜色
     * @param pressedColor 按下颜色
     */
    public static void setColorState(TextView view, int normalColor, int pressedColor) {
        view.setTextColor(SelectorUtils.createColorState(normalColor, pressedColor));
    }

    /**
     * 创建图片状态选择器
     * 此方法为工具方法,可以在任意时刻调用
     * 注意的是,此处StateListDrawable的addState顺序是有关系的,如果设置pressed状态在最后的时候,
     * 状态选择器不会正常生效,设置state_enabled或者空的数组传入需要在后面传入,居然和顺序有关...
     *
     * @param normal  默认图片
     * @param pressed 按下时的图片
     * @return 图片状态选择器
     */
    public static StateListDrawable createSelector(Drawable normal, Drawable pressed) {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, pressed);
        drawable.addState(new int[]{android.R.attr.state_selected}, pressed);
        drawable.addState(new int[]{android.R.attr.state_checked}, pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled}, normal);
        return drawable;
    }

    /**
     * 使用Resource资源创建图片状态选择器
     * 此方法为工具方法,可以在任意时刻调用
     *
     * @param normalRes  默认图片res的id
     * @param pressedRes 按下时的图片res的id
     * @return 图片状态选择器
     */
    public static StateListDrawable createSelector(Context context, int normalRes, int pressedRes) {
        return createSelector(context.getResources().getDrawable(normalRes),
                context.getResources().getDrawable(pressedRes));
    }

    /**
     * 将2个颜色转为单色图片合成为状态选择器
     *
     * @param normalColor  默认的状态的图片的背景颜色
     * @param pressedColor 按下的状态的图片的背景颜色
     */
    public static StateListDrawable createSelector(int normalColor, int pressedColor) {
        return createSelector(new ColorDrawable(normalColor), new ColorDrawable(pressedColor));
    }
}
