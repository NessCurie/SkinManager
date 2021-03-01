package com.github.skinmanager;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * skin的接口,规定了目前skin提供的固有的方法
 * 所有的set方法,都可以在任意时刻调用,
 * 但所有的get方法,需要在{@link SkinManager.OnResourceCompleteListener#onResourceComplete()}后调用
 */
public interface Skin {

    /**
     * 标题栏和公用资源文件夹名称
     */
    String GLOBAL_FOLDER = "Public";

    /**
     * 存储skin.apk的绝对路径的属性,决定加载哪个apk文件作为skin.
     * 为空时会加载/system/app/skin.apk
     */
    String SKIN_PATH = "persist.hq.lcd.skin.path";

    /**
     * skin被切换了的标识,切换之后,先设置为1,1s多之后设置为非1即可
     */
    String SKIN_PATH_CHANGE_MARK = "com.github.skinchange";

    String DEFAULT_PATH = "/system/app/skin.apk";
    String DEFAULT_PATH_US = "/system/app/skin_us.apk";
    String DEFAULT_PATH_FR = "/system/app/skin_fr.apk";
    String DEFAULT_PATH_RU = "/system/app/skin_ru.apk";

    void setDefaultFolder(String folderName);

    Drawable getDrawable(String name, String folder);

    Drawable getDrawable(String name);

    Drawable getHDDrawable(String name, String folder);

    Drawable getHDDrawable(String name);

    Drawable getJPGDrawable(String name, String folder);

    Drawable getJPGDrawable(String name);

    Drawable getJPGHDDrawable(String name, String folder);

    Drawable getJPGHDDrawable(String name);

    Drawable getResDrawable(String name);

    Typeface getTypeface(String name);

    int getColor(String name);

    StateListDrawable getSelector(String normal, String pressed);

    StateListDrawable getSelector(String normal, String pressed, String folder);

    void setBackground(View view, String name, String folderName);

    void setBackground(View view, String name);

    void setBackground(String name, String folderName, View... views);

    void setBackground(String name, View... views);

    void setHDBackground(View view, String name, String folderName);

    void setHDBackground(View view, String name);

    void setHDBackground(String name, String folderName, View... views);

    void setHDBackground(String name, View... views);

    void setSelector(View view, String normal, String pressed, String foldName);

    void setSelector(View view, String normal, String pressed);

    void setSelector(String normal, String pressed, String foldName, View... views);

    void setSelector(String normal, String pressed, View... views);

    void setResBackground(View view, String name);

    void setCompoundDrawables(TextView view, String left, String top, String right, String bottom, String foldName);

    void setCompoundDrawables(TextView view, String left, String top, String right, String bottom);

    void setProgressBarDrawable(ProgressBar progressBar, String background, String progress, String foldName);

    void setProgressBarDrawable(ProgressBar progressBar, String background, String progress);

    void setTypeface(TextView textView, String typefaceName);

    void setDialogBackground(View dialog);

    void setHorizontalLine(View view);

    void setHorizontalLine(View... views);

    void setHorizontalLine(View parent, int id);

    void setHorizontalLine(View parent, int... ids);

    void setVerticalLine(View view);

    void setVerticalLine(View... views);

    void setVerticalLine(View parent, int id);

    void setVerticalLine(View parent, int... ids);

    void setPrimaryColor(TextView view);

    void setPrimaryColor(TextView... views);

    void setPrimaryColor(View parentView, int id);

    void setPrimaryColor(View parentView, int... ids);

    void setTextColor(TextView view);

    void setTextColor(TextView... views);

    void setTextColor(View parentView, int id);

    void setTextColor(View parentView, int... ids);

    void setTextColorLight(TextView view);

    void setTextColorLight(TextView... views);

    void setTextColorLight(View parentView, int id);

    void setTextColorLight(View parentView, int... ids);

    void setTextColorDim(TextView view);

    void setTextColorDim(TextView... views);

    void setTextColorDim(View parentView, int id);

    void setTextColorDim(View parentView, int... ids);

    void setTextColorDark(TextView view);

    void setTextColorDark(TextView... views);

    void setTextColorDark(View parentView, int id);

    void setTextColorDark(View parentView, int... ids);

    void setTextPrimaryColorState(TextView view);

    void setTextPrimaryColorState(TextView... views);

    void setTextPrimaryColorState(View parentView, int id);

    void setTextPrimaryColorState(View parentView, int... ids);
}
