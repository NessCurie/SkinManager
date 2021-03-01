package com.github.skinmanager;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.skinmanager.utils.SelectorUtils;

import java.util.List;
import java.util.Locale;

/**
 * 提供与主题包skin相关的一些方法,如果不需要标题栏只需要一些方法和skin相关的内容,可以只继承该类
 * 使用该类中的颜色成员变量必须保证skin初始化完成,需要在 {@link #onResourceComplete()}中才能使用
 * 但是使用一些方法如果当初始化完成后会直接设定,若未完成会保存待初始化完成后进行设置
 */
public class SkinActivity extends Activity implements Skin, SkinManager.OnResourceCompleteListener {

    /**
     * @see SkinManager#colorPrimary
     */
    public int colorPrimary;
    /**
     * @see SkinManager#colorPrimaryLight
     */
    public int colorPrimaryLight;
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
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreate();
        skinManager.initResource(getApplicationContext(), SystemProperties.get(SKIN_PATH), this);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /**
     * 相当于{@link android.app.Activity#onCreate(Bundle)}
     * 如果你在{@link SkinActivity#onResourceComplete}中使用到了正文界面的控件或者id,就必须
     * 在这个方法中调用{@link android.app.Activity#setContentView(int)}否则会造成空指针
     */
    protected void onCreate() {

    }

    /**
     * 获取skinManager对象,什么时候都可获取到,但是其中的get系列方法需要skin资源初始化完毕,
     * 在{@link SkinActivity#onResourceComplete}中才能正常使用get系列方法
     *
     * @return skinManager对象
     */
    public SkinManager getSkinManager() {
        return skinManager;
    }

    /**
     * skin.apk的资源是否加载完毕,可以调用get类方法
     */
    public boolean isResourceComplete() {
        return skinComplete;
    }

    /**
     * skin为异步加载,加载完毕后执行该方法,skin.apk资源初始化完成,该方法回到了主线程,
     * 使用skin.apk中的资源应该在此方法中调用
     */
    @Override
    public void onResourceComplete() {
        colorPrimary = skinManager.colorPrimary;
        colorPrimaryLight = skinManager.colorPrimaryLight;
        colorText = skinManager.colorText;
        colorTextLight = skinManager.colorTextLight;
        colorTextDim = skinManager.colorTextDim;
        colorTextDark = skinManager.colorTextDark;
        colorBackground = skinManager.colorBackground;
        colorBackgroundLight = skinManager.colorBackgroundLight;
        skinComplete = true;
        handler.post(checkSkinChange);
    }

    public void checkSkinPath() {
        String propSkinPath = SystemProperties.get(SKIN_PATH);
        if ("en".equals(Locale.getDefault().getLanguage()) && !propSkinPath.contains("us")) {
            SystemProperties.set(SKIN_PATH, SkinManager.DEFAULT_PATH_US);
        } else if ("fr".equals(Locale.getDefault().getLanguage()) && !propSkinPath.contains("fr")) {
            SystemProperties.set(SKIN_PATH, SkinManager.DEFAULT_PATH_FR);
        } else if ("ru".equals(Locale.getDefault().getLanguage()) && !propSkinPath.contains("ru")) {
            SystemProperties.set(SKIN_PATH, SkinManager.DEFAULT_PATH_RU);
        } else if ("zh".equals(Locale.getDefault().getLanguage())
                && (propSkinPath.contains("us") || propSkinPath.contains("fr"))) {
            SystemProperties.set(SKIN_PATH, SkinManager.DEFAULT_PATH);
        }
    }

    /**
     * 每秒检测skin路径是否变化,变化会杀掉自己进程
     */
    private Runnable checkSkinChange = new Runnable() {
        @Override
        public void run() {
            String s = SystemProperties.get(SKIN_PATH_CHANGE_MARK);
            if ((!TextUtils.isEmpty(s)) && s.equals("1")) {
                onSkinChange();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
            handler.postDelayed(this, 1000);
        }
    };

    /**
     * 在检测到skin变化后会在杀掉自己进程前调用
     */
    public void onSkinChange() {
    }

    /**
     * 切换皮肤的方法
     *
     * @param skinPath 皮肤包的路径
     */
    public void changeSkin(String skinPath, final SkinManager.OnResourceCompleteListener listener) {
        SystemProperties.set(SKIN_PATH, skinPath);
        skinComplete = false;
        handler.removeCallbacks(checkSkinChange);
        skinManager.reloadResource(getApplicationContext(), skinPath,
                new SkinManager.OnResourceCompleteListener() {
                    @Override
                    public void onResourceComplete() {
                        SkinActivity.this.onResourceComplete();
                        if (listener != null) listener.onResourceComplete();
                        handler.removeCallbacks(checkSkinChange);
                        SystemProperties.set(SKIN_PATH_CHANGE_MARK, "1");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                SystemProperties.set(SKIN_PATH_CHANGE_MARK, "0");
                                handler.post(checkSkinChange);
                            }
                        }, 2200);
                    }
                });
    }

    @Override
    public void setDefaultFolder(String folderName) {
        skinManager.setDefaultFolder(folderName);
    }

    @Override
    public Drawable getDrawable(String name, String folder) {
        return skinManager.getDrawable(name, folder);
    }

    @Override
    public Drawable getDrawable(String name) {
        return skinManager.getDrawable(name);
    }

    @Override
    public Drawable getHDDrawable(String name) {
        return skinManager.getHDDrawable(name);
    }

    @Override
    public Drawable getHDDrawable(String name, String folder) {
        return skinManager.getHDDrawable(name, folder);
    }

    @Override
    public Drawable getJPGDrawable(String name, String folder) {
        return skinManager.getJPGDrawable(name, folder);
    }

    @Override
    public Drawable getJPGDrawable(String name) {
        return skinManager.getJPGDrawable(name);
    }

    @Override
    public Drawable getJPGHDDrawable(String name, String folder) {
        return skinManager.getJPGHDDrawable(name, folder);
    }

    @Override
    public Drawable getJPGHDDrawable(String name) {
        return skinManager.getJPGHDDrawable(name);
    }

    @Override
    public Drawable getResDrawable(String name) {
        return skinManager.getResDrawable(name);
    }

    @Override
    public Typeface getTypeface(String name) {
        return skinManager.getTypeface(name);
    }

    @Override
    public int getColor(String name) {
        return skinManager.getColor(name);
    }

    @Override
    public StateListDrawable getSelector(String normal, String pressed) {
        return skinManager.getSelector(normal, pressed);
    }

    @Override
    public StateListDrawable getSelector(String normal, String pressed, String folder) {
        return skinManager.getSelector(normal, pressed, folder);
    }

    @Override
    public void setBackground(View view, String name, String folder) {
        skinManager.setBackground(view, name, folder);
    }

    @Override
    public void setBackground(View view, String name) {
        skinManager.setBackground(view, name);
    }

    public void setBackground(int id, String name, String folder) {
        setBackground(findViewById(id), name, folder);
    }

    public void setBackground(int id, String name) {
        setBackground(findViewById(id), name);
    }

    @Override
    public void setBackground(String name, String folderName, View... views) {
        skinManager.setBackground(name, folderName, views);
    }

    @Override
    public void setBackground(String name, View... views) {
        skinManager.setBackground(name, views);
    }

    public void setBackground(String name, String folderName, int... ids) {
        for (int id : ids) {
            skinManager.setBackground(findViewById(id), name, folderName);
        }
    }

    public void setBackground(String name, int... ids) {
        for (int id : ids) {
            skinManager.setBackground(findViewById(id), name);
        }
    }

    @Override
    public void setHDBackground(View view, String name, String folder) {
        skinManager.setHDBackground(view, name, folder);
    }

    @Override
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

    public void setHDBackground(int id, String name, String folder) {
        setHDBackground(findViewById(id), name, folder);
    }

    public void setHDBackground(int id, String name) {
        setHDBackground(findViewById(id), name);
    }

    @Override
    public void setSelector(View view, String normal, String pressed, String foldName) {
        skinManager.setSelector(view, normal, pressed, foldName);
    }

    @Override
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

    public void setSelector(String normal, String pressed, String foldName, int... ids) {
        for (int id : ids) {
            setSelector(findViewById(id), normal, pressed, foldName);
        }
    }

    public void setSelector(String normal, String pressed, int... ids) {
        for (int id : ids) {
            setSelector(findViewById(id), normal, pressed);
        }
    }

    public void setSelector(int id, String normal, String pressed, String foldName) {
        setSelector(findViewById(id), normal, pressed, foldName);
    }

    public void setSelector(int id, String normal, String pressed) {
        setSelector(findViewById(id), normal, pressed);
    }

    @Override
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

    /**
     * 设置textView的字体为思源黑体_regular
     */
    public void setSHSCNRegular(TextView view) {
        skinManager.setTypeface(view, "SourceHanSansCN_regular.OTF");
        view.setIncludeFontPadding(false);
    }

    public void setSHSCNRegular(TextView... views) {
        for (TextView view : views) {
            skinManager.setTypeface(view, "SourceHanSansCN_regular.OTF");
            view.setIncludeFontPadding(false);
        }
    }

    public void setSHSCNRegular(int id) {
        TextView textView = findViewById(id);
        skinManager.setTypeface(textView, "SourceHanSansCN_regular.OTF");
        textView.setIncludeFontPadding(false);
    }

    public void setSHSCNRegular(int... ids) {
        for (int id : ids) {
            TextView textView = findViewById(id);
            skinManager.setTypeface(textView, "SourceHanSansCN_regular.OTF");
            textView.setIncludeFontPadding(false);
        }
    }

    public void setResBackground(int id, String name) {
        setResBackground(findViewById(id), name);
    }

    @Override
    public void setDialogBackground(View view) {
        skinManager.setDialogBackground(view);
    }

    /**
     * 将指定layout的文件初始化为view对象并设置为对话框背景并返回
     *
     * @param layoutId layout文件的id
     * @return layout文件初始化的view对象
     */
    public View setDialogBackground(int layoutId) {
        View view = View.inflate(this, layoutId, null);
        setDialogBackground(view);
        return view;
    }

    @Override
    public void setHorizontalLine(View view) {
        skinManager.setHorizontalLine(view);
    }

    @Override
    public void setHorizontalLine(View... views) {
        skinManager.setHorizontalLine(views);
    }

    @Override
    public void setHorizontalLine(View parent, int id) {
        skinManager.setHorizontalLine(parent.findViewById(id));
    }

    @Override
    public void setHorizontalLine(View parent, int... ids) {
        skinManager.setHorizontalLine(parent, ids);
    }

    public void setHorizontalLine(int id) {
        setHorizontalLine(findViewById(id));
    }

    public void setHorizontalLine(int... ids) {
        for (int id : ids) {
            setHorizontalLine(findViewById(id));
        }
    }

    @Override
    public void setVerticalLine(View view) {
        skinManager.setVerticalLine(view);
    }

    @Override
    public void setVerticalLine(View... views) {
        skinManager.setVerticalLine(views);
    }

    @Override
    public void setVerticalLine(View parent, int id) {
        skinManager.setVerticalLine(parent.findViewById(id));
    }

    @Override
    public void setVerticalLine(View parent, int... ids) {
        skinManager.setVerticalLine(parent, ids);
    }

    public void setVerticalLine(int id) {
        setVerticalLine(findViewById(id));
    }

    public void setVerticalLine(int... ids) {
        for (int id : ids) {
            setVerticalLine(findViewById(id));
        }
    }

    @Override
    public void setPrimaryColor(TextView view) {
        skinManager.setPrimaryColor(view);
    }

    @Override
    public void setPrimaryColor(TextView... views) {
        skinManager.setPrimaryColor(views);
    }

    @Override
    public void setPrimaryColor(View parentView, int id) {
        skinManager.setPrimaryColor(parentView, id);
    }

    @Override
    public void setPrimaryColor(View parentView, int... ids) {
        skinManager.setPrimaryColor(parentView, ids);
    }

    public void setPrimaryColor(int id) {
        setPrimaryColor((TextView) findViewById(id));
    }

    public void setPrimaryColor(int... ids) {
        for (int id : ids) {
            setPrimaryColor((TextView) findViewById(id));
        }
    }

    @Override
    public void setTextColor(TextView view) {
        skinManager.setTextColor(view);
    }

    @Override
    public void setTextColor(TextView... views) {
        skinManager.setTextColor(views);
    }

    public void setTextColor(List<TextView> views) {
        for (TextView view : views) {
            setTextColor(view);
        }
    }

    @Override
    public void setTextColor(View parentView, int id) {
        skinManager.setTextColor(parentView, id);
    }

    @Override
    public void setTextColor(View parentView, int... ids) {
        skinManager.setTextColor(parentView, ids);
    }

    public void setTextColor(int id) {
        setTextColor((TextView) findViewById(id));
    }

    public void setTextColor(int... ids) {
        for (int id : ids) {
            setTextColor((TextView) findViewById(id));
        }
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

    public void setTextColorLight(int id) {
        setTextColorLight((TextView) findViewById(id));
    }

    public void setTextColorLight(int... ids) {
        for (int id : ids) {
            setTextColorLight((TextView) findViewById(id));
        }
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

    public void setTextColorDim(int id) {
        setTextColorDim((TextView) findViewById(id));
    }

    public void setTextColorDim(int... ids) {
        for (int id : ids) {
            setTextColorDim((TextView) findViewById(id));
        }
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

    public void setTextColorDark(int id) {
        setTextColorDark((TextView) findViewById(id));
    }

    public void setTextColorDark(int... ids) {
        for (int id : ids) {
            setTextColorDark((TextView) findViewById(id));
        }
    }

    @Override
    public void setTextPrimaryColorState(TextView view) {
        skinManager.setTextPrimaryColorState(view);
    }

    @Override
    public void setTextPrimaryColorState(TextView... views) {
        skinManager.setTextPrimaryColorState(views);
    }

    @Override
    public void setTextPrimaryColorState(View parentView, int id) {
        skinManager.setTextPrimaryColorState(parentView, id);
    }

    @Override
    public void setTextPrimaryColorState(View parentView, int... ids) {
        skinManager.setTextPrimaryColorState(parentView, ids);
    }

    public void setTextPrimaryColorState(int id) {
        setTextPrimaryColorState((TextView) findViewById(id));
    }

    public void setTextPrimaryColorState(int... ids) {
        for (int id : ids) {
            setTextPrimaryColorState((TextView) findViewById(id));
        }
    }

    public void setSelector(View view, int normalColor, int pressedColor) {
        view.setBackground(SelectorUtils.createSelector(normalColor, pressedColor));
    }

    public void setSelector(int id, int normalColor, int pressedColor) {
        findViewById(id).setBackground(SelectorUtils.createSelector(normalColor, pressedColor));
    }

    public void setColorState(TextView view, int normal, int pressed) {
        view.setTextColor(SelectorUtils.createColorState(normal, pressed));
    }

    public void setColorState(int id, int normal, int pressed) {
        ((TextView) findViewById(id)).setTextColor(SelectorUtils.createColorState(normal, pressed));
    }

    public void setColorState(int normal, int pressed, TextView... views) {
        for (TextView view : views) {
            view.setTextColor(SelectorUtils.createColorState(normal, pressed));
        }
    }

    public void setColorState(int normal, int pressed, int... ids) {
        for (int id : ids) {
            ((TextView) findViewById(id)).setTextColor(SelectorUtils.createColorState(normal, pressed));
        }
    }

    public void setPrimaryLines(View... views) {
        for (View view : views) {
            view.setBackground(new ColorDrawable(colorPrimary));
        }
    }

    public void setPrimaryLine(View parent, int... ids) {
        for (int id : ids) {
            parent.findViewById(id).setBackground(new ColorDrawable(colorPrimary));
        }
    }

    public void setTextColorLines(View... views) {
        for (View view : views) {
            view.setBackgroundColor(colorText);
        }
    }

    public void setTextColorLine(View parent, int... ids) {
        for (int id : ids) {
            parent.findViewById(id).setBackgroundColor(colorText);
        }
    }

    public void setOnClickListener(View.OnClickListener listener, View... views) {
        for (View view : views) {
            view.setOnClickListener(listener);
        }
    }
}
