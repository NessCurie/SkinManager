package com.github.skinmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.SystemProperties;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.skinmanager.utils.SelectorUtils;


/**
 * 如果你需要在Receiver中使用到skin,可以继承该类
 */
public class SkinReceiver extends BroadcastReceiver implements Skin, SkinManager.OnResourceCompleteListener {

    /**
     * @see SkinManager#colorPrimary
     */
    public int colorPrimary;
    /**
     * @see SkinManager#colorText
     */
    public int colorText;
    /**
     * @see SkinManager#colorTextLight
     */
    public int colorTextLight;
    /**
     * @see SkinManager#colorTextDim
     */
    public int colorTextDim;
    /**
     * @see SkinManager#colorTextDark
     */
    public int colorTextDark;
    /**
     * @see SkinManager#colorBackground
     */
    public int colorBackground;
    /**
     * @see SkinManager#colorBackgroundLight
     */
    public int colorBackgroundLight;

    private boolean skinComplete = false;
    private SkinManager skinManager = SkinManager.getInstance();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        skinManager.initResource(context.getApplicationContext(), SystemProperties.get(SKIN_PATH), this);
    }

    /**
     * 加载完skin后调用,运行在主线程
     */
    public void onResourceComplete() {
        colorPrimary = skinManager.colorPrimary;
        colorText = skinManager.colorText;
        colorTextLight = skinManager.colorTextLight;
        colorTextDim = skinManager.colorTextDim;
        colorTextDark = skinManager.colorTextDark;
        colorBackground = skinManager.colorBackground;
        colorBackgroundLight = skinManager.colorBackgroundLight;
        skinComplete = true;
    }

    /**
     * 获取skinManager对象,如果未在其他地方进行过初始化,只有在onResourceComplete中获取才不为null
     *
     * @return skinManager对象
     */
    public SkinManager getSkinManager() {
        return skinManager;
    }

    /**
     * skin.apk的资源是否加载完毕
     */
    public boolean isResourceComplete() {
        return skinComplete;
    }

    /**
     * 不指定foldName时获取资源的默认foldName,需要在
     *
     * @param folderName skin.apk的assets下的文件目录名称
     */
    public void setDefaultFolder(String folderName) {
        skinManager.setDefaultFolder(folderName);
    }

    public Drawable getDrawable(String name, String folder) {
        return skinManager.getDrawable(name, folder);
    }

    public Drawable getDrawable(String name) {
        return skinManager.getDrawable(name);
    }

    public Drawable getHDDrawable(String name) {
        return skinManager.getHDDrawable(name);
    }

    public Drawable getHDDrawable(String name, String folder) {
        return skinManager.getHDDrawable(name, folder);
    }

    public Drawable getJPGDrawable(String name, String folder) {
        return skinManager.getJPGDrawable(name, folder);
    }

    public Drawable getJPGDrawable(String name) {
        return skinManager.getJPGDrawable(name);
    }

    public Drawable getJPGHDDrawable(String name, String folder) {
        return skinManager.getJPGHDDrawable(name, folder);
    }

    public Drawable getJPGHDDrawable(String name) {
        return skinManager.getJPGHDDrawable(name);
    }

    public Drawable getResDrawable(String name) {
        return skinManager.getResDrawable(name);
    }

    @Override
    public Typeface getTypeface(String name) {
        return skinManager.getTypeface(name);
    }

    public int getColor(String name) {
        return skinManager.getColor(name);
    }

    public StateListDrawable getSelector(String normal, String pressed) {
        return skinManager.getSelector(normal, pressed);
    }

    public StateListDrawable getSelector(String normal, String pressed, String folder) {
        return skinManager.getSelector(normal, pressed, folder);
    }

    public void setBackground(View view, String name, String folder) {
        skinManager.setBackground(view, name, folder);
    }

    public void setBackground(View view, String name) {
        skinManager.setBackground(view, name);
    }

    @Override
    public void setBackground(String name, String folderName, View... views) {
        skinManager.setBackground(name, folderName, views);
    }

    @Override
    public void setBackground(String name, View... views) {
        skinManager.setBackground(name, views);
    }

    public void setHDBackground(View view, String name, String folder) {
        skinManager.setHDBackground(view, name, folder);
    }

    public void setHDBackground(View view, String name) {
        skinManager.setHDBackground(view, name);
    }

    @Override
    public void setHDBackground(String name, String folderName, View... views) {
        skinManager.setHDBackground(name, folderName, views);
    }

    @Override
    public void setHDBackground(String name, View... views) {
        skinManager.setHDBackground(name, views);
    }

    public void setSelector(View view, String normal, String pressed, String foldName) {
        skinManager.setSelector(view, normal, pressed, foldName);
    }

    public void setSelector(View view, String normal, String pressed) {
        skinManager.setSelector(view, normal, pressed);
    }

    @Override
    public void setSelector(String normal, String pressed, String foldName, View... views) {
        skinManager.setSelector(normal, pressed, foldName, views);
    }

    @Override
    public void setSelector(String normal, String pressed, View... views) {
        skinManager.setSelector(normal, pressed, views);
    }

    public void setResBackground(View view, String name) {
        skinManager.setResBackground(view, name);
    }

    @Override
    public void setCompoundDrawables(TextView view, String left, String top, String right,
                                     String bottom, String foldName) {
        skinManager.setCompoundDrawables(view, left, top, right, bottom, foldName);
    }

    @Override
    public void setCompoundDrawables(TextView view, String left, String top, String right, String bottom) {
        skinManager.setCompoundDrawables(view, left, top, right, bottom);
    }

    @Override
    public void setProgressBarDrawable(ProgressBar progressBar, String background, String progress, String foldName) {
        skinManager.setProgressBarDrawable(progressBar, background, progress, foldName);
    }

    @Override
    public void setProgressBarDrawable(ProgressBar progressBar, String background, String progress) {
        skinManager.setProgressBarDrawable(progressBar, background, progress);
    }

    @Override
    public void setTypeface(TextView textView, String typefaceName) {
        skinManager.setTypeface(textView, typefaceName);
    }

    public void setDialogBackground(View view) {
        skinManager.setDialogBackground(view);
    }

    /**
     * 将指定layout的文件初始化为view对象并设置为对话框背景并返回
     *
     * @param layoutId layout文件的id
     * @return layout文件初始化的view对象
     */
    public View setDialogBackground(Context context, int layoutId) {
        View view = View.inflate(context, layoutId, null);
        setDialogBackground(view);
        return view;
    }

    public void setHorizontalLine(View view) {
        skinManager.setHorizontalLine(view);
    }

    public void setHorizontalLine(View... views) {
        skinManager.setHorizontalLine(views);
    }

    public void setHorizontalLine(View parent, int id) {
        skinManager.setHorizontalLine(parent.findViewById(id));
    }

    public void setHorizontalLine(View parent, int... ids) {
        skinManager.setHorizontalLine(parent, ids);
    }

    public void setVerticalLine(View view) {
        skinManager.setVerticalLine(view);
    }

    public void setVerticalLine(View... views) {
        skinManager.setVerticalLine(views);
    }

    public void setVerticalLine(View parent, int id) {
        skinManager.setVerticalLine(parent.findViewById(id));
    }

    public void setVerticalLine(View parent, int... ids) {
        skinManager.setVerticalLine(parent, ids);
    }

    public void setPrimaryColor(TextView view) {
        skinManager.setPrimaryColor(view);
    }

    public void setPrimaryColor(TextView... views) {
        skinManager.setPrimaryColor(views);
    }

    public void setPrimaryColor(View parentView, int id) {
        skinManager.setPrimaryColor(parentView, id);
    }

    public void setPrimaryColor(View parentView, int... ids) {
        skinManager.setPrimaryColor(parentView, ids);
    }

    public void setTextColor(TextView view) {
        skinManager.setTextColor(view);
    }

    public void setTextColor(TextView... views) {
        skinManager.setTextColor(views);
    }

    public void setTextColor(View parentView, int id) {
        skinManager.setTextColor(parentView, id);
    }

    public void setTextColor(View parentView, int... ids) {
        skinManager.setTextColor(parentView, ids);
    }

    @Override
    public void setTextColorLight(TextView view) {
        skinManager.setTextColorLight(view);
    }

    @Override
    public void setTextColorLight(TextView... views) {
        skinManager.setTextColorLight(views);
    }

    @Override
    public void setTextColorLight(View parentView, int id) {
        skinManager.setTextColorLight(parentView, id);
    }

    @Override
    public void setTextColorLight(View parentView, int... ids) {
        skinManager.setTextColorLight(parentView, ids);
    }

    @Override
    public void setTextColorDim(TextView view) {
        skinManager.setTextColorDim(view);
    }

    @Override
    public void setTextColorDim(TextView... views) {
        skinManager.setTextColorDim(views);
    }

    @Override
    public void setTextColorDim(View parentView, int id) {
        skinManager.setTextColorDim(parentView, id);
    }

    @Override
    public void setTextColorDim(View parentView, int... ids) {
        skinManager.setTextColorDim(parentView, ids);
    }

    @Override
    public void setTextColorDark(TextView view) {
        skinManager.setTextColorDark(view);
    }

    @Override
    public void setTextColorDark(TextView... views) {
        skinManager.setTextColorDark(views);
    }

    @Override
    public void setTextColorDark(View parentView, int id) {
        skinManager.setTextColorDark(parentView, id);
    }

    @Override
    public void setTextColorDark(View parentView, int... ids) {
        skinManager.setTextColorDark(parentView, ids);
    }

    public void setTextPrimaryColorState(TextView view) {
        skinManager.setTextPrimaryColorState(view);
    }

    public void setTextPrimaryColorState(TextView... views) {
        skinManager.setTextPrimaryColorState(views);
    }

    public void setTextPrimaryColorState(View parentView, int id) {
        skinManager.setTextPrimaryColorState(parentView, id);
    }

    public void setTextPrimaryColorState(View parentView, int... ids) {
        skinManager.setTextPrimaryColorState(parentView, ids);
    }

    public void setSelector(View view, int normalColor, int pressedColor) {
        view.setBackground(SelectorUtils.createSelector(normalColor, pressedColor));
    }

    public void setColorState(TextView view, int normal, int pressed) {
        view.setTextColor(SelectorUtils.createColorState(normal, pressed));
    }

    public void setColorState(int normal, int pressed, TextView... views) {
        for (TextView view : views) {
            view.setTextColor(SelectorUtils.createColorState(normal, pressed));
        }
    }
}
