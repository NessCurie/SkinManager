package com.github.skinmanager;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.recyclerviewutils.BuildConfig;
import com.github.skinmanager.utils.DrawableUtils;
import com.github.skinmanager.utils.SelectorUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

/**
 * 提供skin的初始化和基本方法
 */
@SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef", "unused", "RedundantSuppression"})
public class SkinManager implements Skin {

    /**
     * 主题色,图片和一些标题栏会使用此颜色,有的时候大部分文字都会使用该颜色(比如金龙的应用中心)
     * 使用时需要保证SkinManager初始化完成
     */
    public int colorPrimary;
    /**
     * 主题色亮一些的颜色
     */
    public int colorPrimaryLight;
    /**
     * 主要的文字颜色,一般为接近白色,大部分正文的文字使用该颜色
     * 使用时需要保证SkinManager初始化完成
     */
    public int colorText;
    /**
     * 比主要的文字颜色还要亮一些的颜色
     */
    public int colorTextLight;
    /**
     * 比主要的文字颜色要黯淡一些的颜色,一般为灰白色
     * 使用时需要保证SkinManager初始化完成
     */
    public int colorTextDim;
    /**
     * 黯淡很多的主要的文字颜色,一般为灰色
     * 使用时需要保证SkinManager初始化完成
     */
    public int colorTextDark;
    /**
     * 主要的背景色
     * 使用时需要保证SkinManager初始化完成
     */
    public int colorBackground;
    /**
     * 亮一些的背景色
     * 使用时需要保证SkinManager初始化完成
     */
    public int colorBackgroundLight;
    /**
     * 横向的线
     * 使用时需要保证SkinManager初始化完成
     */
    private Drawable lineHorizontal;
    /**
     * 竖向的线
     * 使用时需要保证SkinManager初始化完成
     */
    private Drawable lineVertical;
    /**
     * 对话框的背景,为res/drawable下的.9图片,会自动拉伸
     * 使用时需要保证SkinManager初始化完成
     */
    private Drawable dialogBackground;
    /**
     * 很多方法不指定foldName时获取资源的默认文件夹(apk的assets下的文件目录名称).
     * 默认为 {@link SkinManager#GLOBAL_FOLDER}
     */
    private String defaultFolder = GLOBAL_FOLDER;
    /**
     * 需要与apk的包名对应,决定了res下的文件能否加载到,assets下的不需要包名只需要路径即可加载到
     */
    private static String skinPackage = "com.github.skin";
    /**
     * 兼容处理的包名
     */
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private final String[] compatiblePackages = {};
    /**
     * 图片选项,位图默认为{@link Bitmap.Config#RGB_565}
     */
    private BitmapFactory.Options defaultOps = LDOptions.ops;

    private Resources currentRes;
    private Resources defaultRes;
    private boolean initComplete = false;

    private HashMap<View, String> assets;
    private HashMap<View, String> assetsHD;
    private HashMap<View, String> drawableMap;
    private HashMap<View, Drawable> comMap;
    private ArrayList<TextView> colorPrimaryList;
    private ArrayList<TextView> colorTextList;
    private ArrayList<TextView> colorTextLightList;
    private ArrayList<TextView> colorTextDimList;
    private ArrayList<TextView> colorTextDarkList;
    private ArrayList<TextView> colorStateList;
    private HashMap<TextView, String> typefaceMap;
    private HashMap<String, Typeface> typefaceCacheMap;
    private HashMap<TextView, String> compoundDrawablesMap;

    /**
     * skin的资源加载完毕的回调
     */
    public interface OnResourceCompleteListener {
        void onResourceComplete();
    }

    private SkinManager() {
    }

    private static class SkinManagerHolder {
        static SkinManager INSTANCE = new SkinManager();
    }

    static class HDOptions {
        static BitmapFactory.Options ops = new BitmapFactory.Options();

        static {
            ops.inPreferredConfig = Bitmap.Config.ARGB_8888;
            ops.inSampleSize = 1;
            ops.inPurgeable = true;
            ops.inInputShareable = true;
        }
    }

    static class LDOptions {
        static BitmapFactory.Options ops = new BitmapFactory.Options();

        static {
            ops.inPreferredConfig = Bitmap.Config.RGB_565;
            ops.inSampleSize = 1;
            ops.inPurgeable = true;
            ops.inInputShareable = true;
        }
    }

    /**
     * 获取skin管理器对象.
     * 之后你需要调用{@link SkinManager#initResource(Context, String, OnResourceCompleteListener)}.
     * 一般如果activity或者service或者Receiver继承了Skin相关的基类
     * 且界面进行了显示或服务进行了启动或接收过广播,都可以保证已经进行了初始化
     */
    public static SkinManager getInstance() {
        return SkinManagerHolder.INSTANCE;
    }

    /**
     * 初始化skin的资源,该操作为异步操作,最终初始化完毕后会调用传入的回调
     *
     * @param context  上下文
     * @param path     skin.apk的绝对路径,为空时默认为 {@link SkinManager#DEFAULT_PATH}
     * @param listener 初始化完毕后的回调
     */
    public synchronized void initResource(final Context context, final String path,
                                          final OnResourceCompleteListener listener) {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        if (currentRes == null) {
            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                    if (TextUtils.isEmpty(path)) {
                        currentRes = getResource(context, DEFAULT_PATH);
                    } else {
                        currentRes = getResource(context, path);
                        if (!DEFAULT_PATH.equals(path) && defaultRes == null) {
                            defaultRes = getResource(context, DEFAULT_PATH);
                        }
                    }
                    initCom();
                    emitter.onNext(new Object());
                    emitter.onComplete();
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Object>() {
                        Disposable d;

                        @Override
                        public void onSubscribe(Disposable d) {
                            this.d = d;
                        }

                        @Override
                        public void onNext(Object o) {
                            initComplete = true;
                            setCacheView();
                            listener.onResourceComplete();
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {
                            d.dispose();
                        }
                    });
        } else {
            listener.onResourceComplete();
        }
    }

    public synchronized void reloadResource(final Context context, final String path,
                                            final OnResourceCompleteListener listener) {
        initComplete = false;
        currentRes = null;
        initResource(context, path, listener);
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private Resources getResource(Context context, String path) throws Exception {
        if (!TextUtils.isEmpty(path) && new File(path).exists()) {
            AssetManager assetManager = AssetManager.class.newInstance();//这里需要用反射new,不然获取不到res
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, path);
            Resources superRes = context.getResources();
            return new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        }
        return null;
    }

    /**
     * 初始化获取一些常用资源文件
     */
    private void initCom() {
        colorPrimary = getColor("colorPrimary");
        colorPrimaryLight = getColor("colorPrimaryLight");
        colorText = getColor("colorText");
        colorTextLight = getColor("colorTextLight");
        colorTextDim = getColor("colorTextDim");
        colorTextDark = getColor("colorTextDark");
        colorBackground = getColor("colorBackground");
        colorBackgroundLight = getColor("colorBackgroundLight");
        lineHorizontal = getDrawable("line_horizontal", GLOBAL_FOLDER);
        lineVertical = getDrawable("line_vertical", GLOBAL_FOLDER);
        dialogBackground = getResDrawable("dialog_background");
    }

    private void setCacheView() {
        if (assets != null && !assets.isEmpty()) {
            for (View view : assets.keySet()) {
                String path = assets.get(view);
                if (path != null && path.contains("|")) {  //状态选择器
                    int splitter = path.indexOf("|");
                    if (path.contains(":")) {
                        int fold = path.indexOf(":");
                        if (view instanceof ProgressBar) {
                            setProgressBarDrawable((ProgressBar) view, path.substring(0, splitter),
                                    path.substring(splitter + 1, fold), path.substring(fold + 1));
                        } else {
                            setSelector(view, path.substring(0, splitter),
                                    path.substring(splitter + 1, fold), path.substring(fold + 1));
                        }
                    } else {
                        if (view instanceof ProgressBar) {
                            setProgressBarDrawable((ProgressBar) view, path.substring(0, splitter),
                                    path.substring(splitter + 1));
                        } else {
                            setSelector(view, path.substring(0, splitter), path.substring(splitter + 1));
                        }
                    }
                } else {
                    if (path != null && path.contains(":")) {
                        int fold = path.indexOf(":");
                        setBackground(view, path.substring(0, fold), path.substring(fold + 1));
                    } else {
                        setBackground(view, path);
                    }
                }
            }
            assets.clear();
        }
        if (assetsHD != null && !assetsHD.isEmpty()) {
            for (View view : assetsHD.keySet()) {
                String path = assetsHD.get(view);
                if (path != null && path.contains(":")) {
                    int fold = path.indexOf(":");
                    setHDBackground(view, path.substring(0, fold), path.substring(fold + 1));
                } else {
                    setHDBackground(view, path);
                }
            }
            assetsHD.clear();
        }
        if (drawableMap != null && !drawableMap.isEmpty()) {
            for (View view : drawableMap.keySet()) {
                setResBackground(view, drawableMap.get(view));
            }
            drawableMap.clear();
        }
        if (comMap != null && !comMap.isEmpty()) {
            for (View view : comMap.keySet()) {
                Drawable drawable = comMap.get(view);
                if (drawable instanceof BitmapDrawable) {
                    view.setBackground(new BitmapDrawable(null, ((BitmapDrawable) drawable).getBitmap()));
                } else if (drawable instanceof NinePatchDrawable) {
                    view.setBackground(getResDrawable("dialog_background"));
                }
            }
            comMap.clear();
        }
        if (colorPrimaryList != null && !colorPrimaryList.isEmpty()) {
            for (TextView textView : colorPrimaryList) {
                textView.setTextColor(colorPrimary);
            }
            colorPrimaryList.clear();
        }
        if (colorTextList != null && !colorTextList.isEmpty()) {
            for (TextView textView : colorTextList) {
                textView.setTextColor(colorText);
            }
            colorTextList.clear();
        }
        if (colorTextLightList != null && !colorTextLightList.isEmpty()) {
            for (TextView textView : colorTextLightList) {
                textView.setTextColor(colorTextLight);
            }
            colorTextLightList.clear();
        }
        if (colorTextDimList != null && !colorTextDimList.isEmpty()) {
            for (TextView textView : colorTextDimList) {
                textView.setTextColor(colorTextDim);
            }
            colorTextDimList.clear();
        }
        if (colorTextDarkList != null && !colorTextDarkList.isEmpty()) {
            for (TextView textView : colorTextDarkList) {
                textView.setTextColor(colorTextDark);
            }
            colorTextDarkList.clear();
        }
        if (colorStateList != null && !colorStateList.isEmpty()) {
            for (TextView textView : colorStateList) {
                textView.setTextColor(SelectorUtils.createColorState(colorText, colorPrimary));
            }
            colorStateList.clear();
        }
        if (typefaceMap != null && !typefaceMap.isEmpty()) {
            for (TextView tv : typefaceMap.keySet()) {
                setTypeface(tv, typefaceMap.get(tv));
            }
            typefaceMap.clear();
        }
        if (compoundDrawablesMap != null && !compoundDrawablesMap.isEmpty()) {
            for (TextView textView : compoundDrawablesMap.keySet()) {
                String value = compoundDrawablesMap.get(textView);
                if (value != null) {
                    String[] drawables = value.split("\\|");
                    setCompoundDrawables(textView, drawables[0], drawables[1], drawables[2],
                            drawables[3], drawables[4]);
                }
            }
            compoundDrawablesMap.clear();
        }
    }

    /**
     * 获取指定资源id
     *
     * @param type 资源类型 drawable，style, color, string
     * @param name 资源名词
     * @return 资源id
     */
    private int getResourceId(String type, String name) {
        int identifier = 0;
        if (currentRes != null) {
            identifier = currentRes.getIdentifier(name, type, skinPackage);
            if (identifier == 0) {
                for (String compatiblePackage : compatiblePackages) {
                    identifier = currentRes.getIdentifier(name, type, compatiblePackage);
                    if (identifier != 0) {
                        break;
                    }
                }
            }
        }
        return identifier;
    }

    /**
     * 获取指定资源id
     *
     * @param type 资源类型 drawable，style, color, string
     * @param name 资源名词
     * @return 资源id
     */
    private int getDefaultResId(String type, String name) {
        int identifier = 0;
        if (defaultRes != null) {
            identifier = defaultRes.getIdentifier(name, type, skinPackage);
            if (identifier == 0) {
                for (String compatiblePackage : compatiblePackages) {
                    identifier = defaultRes.getIdentifier(name, type, compatiblePackage);
                    if (identifier != 0) {
                        break;
                    }
                }
            }
        }
        return identifier;
    }

    /**
     * 设置获取资源时的skin的包名,包名决定了res下的文件能否加载到,assets下的不需要包名即可加载到
     * 默认为{@link SkinManager#skinPackage}
     */
    public static void setSkinPackage(String packageName) {
        skinPackage = packageName;
    }

    /**
     * 设置从assets下获取图片时的配置
     */
    public void setDefaultDrawableConfig(BitmapFactory.Options ops) {
        defaultOps = ops;
    }

    /**
     * 如果要全部以高分辨率获取图片,在获取图片之前调用此方法.
     * 会影响图片的质量（颜色深度）以及显示透明/半透明颜色的能力
     * 调用此方法,获取skin中的assets下的图片时会都以RGB8888标准获取图片
     */
    public void useHighDrawable() {
        defaultOps = HDOptions.ops;
    }

    /**
     * 如果要还原全部以低分辨率获取图片,在获取图片之前调用此方法即可.
     * 会影响图片的质量（颜色深度）以及显示透明/半透明颜色的能力
     * 调用此方法,获取skin中的assets下的图片时会都以RGB565标准获取图片,默认使用该分辨率获取,这时有些图片会花
     */
    public void useLowDrawable() {
        defaultOps = LDOptions.ops;
    }

    /**
     * 获取assets下的资源时不指定foldName时获取资源的默认文件夹名称,默认为 {@link SkinManager#GLOBAL_FOLDER}
     *
     * @param folderName skin.apk的assets下的文件目录名称
     */
    public void setDefaultFolder(String folderName) {
        defaultFolder = folderName;
    }

    public String getDefaultFolder() {
        return defaultFolder;
    }

    /**
     * 获取assets下的图片
     *
     * @param name    图片全称
     * @param folder  图片文件夹名称
     * @param options 图片选项,主要是位图{@link Bitmap.Config}影响图片的质量（颜色深度）以及显示透明/半透明颜色的能力
     * @return 图片Drawable对象
     */
    @NonNull
    public Drawable getDrawable(String name, String folder, BitmapFactory.Options options) {
        Drawable drawable = new ColorDrawable(BuildConfig.DEBUG ? Color.WHITE : Color.TRANSPARENT);

        InputStream is = null;
        try {
            is = currentRes.getAssets().open(folder + "/" + name);
            drawable = new BitmapDrawable(null, BitmapFactory.decodeStream(is, null, options));
        } catch (Exception ignored) {
            try {
                is = defaultRes.getAssets().open(folder + "/" + name);
                drawable = new BitmapDrawable(null, BitmapFactory.decodeStream(is, null, options));
            } catch (Exception ignored2) {
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
        return drawable;
    }

    /**
     * 获取assets下指定文件夹下的指定名称的png图片
     *
     * @param name   xxx.png的xxx部分,不带后缀名
     * @param folder 文件夹名称
     */
    @NonNull
    public Drawable getDrawable(String name, String folder) {
        return getDrawable(name + ".png", folder, defaultOps);
    }

    /**
     * 获取 {@link #defaultFolder}文件夹下指定名称的png图片
     *
     * @param name xxx.png的xxx部分,不带后缀名
     */
    @NonNull
    public Drawable getDrawable(String name) {
        return getDrawable(name, defaultFolder);
    }

    /**
     * 获取高清的assets下的指定目录的图片
     * 默认图片配置获取assets下的图片时为低分辨率,可能有些图片会花,可以尝试使用该方法获取高清图片
     *
     * @param name   xxx.png的xxx部分,不带后缀名
     * @param folder assets下的文件夹名称
     */
    @NonNull
    public Drawable getHDDrawable(String name, String folder) {
        return getDrawable(name + ".png", folder, HDOptions.ops);
    }

    /**
     * 获取高清的assets下的默认文件夹下的图片
     *
     * @param name xxx.png的xxx部分,不带后缀名
     */
    @NonNull
    public Drawable getHDDrawable(String name) {
        return getHDDrawable(name, defaultFolder);
    }

    /**
     * 获取assets下指定文件夹下的指定名称的JPG图片
     *
     * @param name   图片名称 xx.jpg 的xx部分
     * @param folder 文件夹名称
     * @return jpg图片Drawable对象
     */
    @NonNull
    public Drawable getJPGDrawable(String name, String folder) {
        return getDrawable(name + ".jpg", folder, defaultOps);
    }

    /**
     * 获取assets下{@link #defaultFolder}路径文件夹下指定名称的JPG图片
     *
     * @param name 图片名称 xx.jpg 的xx部分
     */
    @NonNull
    public Drawable getJPGDrawable(String name) {
        return getJPGDrawable(name, defaultFolder);
    }

    /**
     * 获取高清的assets下指定文件夹下的指定名称的JPG图片
     */
    @NonNull
    public Drawable getJPGHDDrawable(String name, String folder) {
        return getDrawable(name + ".jpg", folder, HDOptions.ops);
    }

    /**
     * 获取高清的assets下{@link #defaultFolder}路径文件夹下指定JPG图片
     */
    @NonNull
    public Drawable getJPGHDDrawable(String name) {
        return getJPGHDDrawable(name, defaultFolder);
    }

    /**
     * 从skin加载字体
     *
     * @param name 字体名称
     * @return 在skin的assets/Typeface目录下
     */
    @Nullable
    public synchronized Typeface getTypeface(String name) {
        Typeface typeface = null;
        if (typefaceCacheMap == null) {
            typefaceCacheMap = new HashMap<>();
        } else {
            typeface = typefaceCacheMap.get(name);
        }
        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(currentRes.getAssets(), "Typeface/" + name);
            } catch (Exception ignored) {
            }
            if (typeface == null) {
                try {
                    typeface = Typeface.createFromAsset(defaultRes.getAssets(), "Typeface/" + name);
                } catch (Exception ignored) {
                }
            }
            typefaceCacheMap.put(name, typeface);
        }
        return typeface;
    }

    /**
     * 获取res/drawable下指定图片
     *
     * @param name name 图片名称
     */
    @NonNull
    public Drawable getResDrawable(String name) {
        Drawable drawable = new ColorDrawable(BuildConfig.DEBUG ? Color.WHITE : Color.TRANSPARENT);
        int drawableId = getResourceId("drawable", name);
        if (drawableId != 0) {
            drawable = currentRes.getDrawable(drawableId);
        } else {
            drawableId = getDefaultResId("drawable", name);
            if (drawableId != 0) {
                drawable = defaultRes.getDrawable(drawableId);
            }
        }
        return drawable;
    }

    /**
     * 获取指定颜色
     *
     * @param name 颜色名称
     * @return 颜色的值
     */
    public int getColor(String name) {
        int color = BuildConfig.DEBUG ? Color.RED : -1;
        int colorId = getResourceId("color", name);
        if (colorId != 0) {
            color = currentRes.getColor(colorId);
        } else {
            colorId = getDefaultResId("color", name);
            if (colorId != 0) {
                color = defaultRes.getColor(colorId);
            }
        }
        return color;
    }

    /**
     * -1 是白色,有时候需要判断是否获取成功
     */
    public Pair<Boolean, Integer> getColorWithResult(String name) {
        int color = BuildConfig.DEBUG ? Color.RED : -1;
        boolean result = false;
        int colorId = getResourceId("color", name);
        if (colorId != 0) {
            result = true;
            color = currentRes.getColor(colorId);
        } else {
            colorId = getDefaultResId("color", name);
            if (colorId != 0) {
                result = true;
                color = defaultRes.getColor(colorId);
            }
        }
        return new Pair<>(result, color);
    }

    public int getColor(String name, String compatible) {
        int color = BuildConfig.DEBUG ? Color.RED : -1;
        int colorId = getResourceId("color", name);
        if (colorId != 0) {
            color = currentRes.getColor(colorId);
        } else {
            colorId = getDefaultResId("color", name);
            if (colorId != 0) {
                color = defaultRes.getColor(colorId);
            } else {
                colorId = getResourceId("color", compatible);
                if (colorId != 0) {
                    color = currentRes.getColor(colorId);
                } else {
                    colorId = getDefaultResId("color", compatible);
                    if (colorId != 0) {
                        color = defaultRes.getColor(colorId);
                    }
                }
            }
        }
        return color;
    }

    /**
     * 使用skin的资源创建图片状态选择器
     *
     * @param normal  默认图片名称
     * @param pressed 按下时的图片名称
     * @return 图片状态选择器
     */
    @NonNull
    public StateListDrawable getSelector(String normal, String pressed) {
        return SelectorUtils.createSelector(getDrawable(normal), getDrawable(pressed));
    }

    /**
     * 使用skin的资源创建图片状态选择器
     *
     * @param normal  默认图片名称
     * @param pressed 按下时的图片名称
     * @param folder  所在文件夹名称
     * @return 图片状态选择器
     */
    @NonNull
    public StateListDrawable getSelector(String normal, String pressed, String folder) {
        return SelectorUtils.createSelector(getDrawable(normal, folder), getDrawable(pressed, folder));
    }

    /**
     * 从skin中使用默认配置获取对应文件夹中对应名称的PNG图片并设置为background,如果未获取到会设置为透明状态
     *
     * @param view       要设置background的控件
     * @param name       图片名称
     * @param folderName 图片所在文件夹名称
     */
    public void setBackground(final View view, String name, final String folderName) {
        if (initComplete) {
            Observable.just(name)
                    .map(new Function<String, Drawable>() {
                        @Override
                        public Drawable apply(String s) {
                            return getDrawable(s, folderName);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Drawable>() {

                        Disposable d;

                        @Override
                        public void onSubscribe(Disposable d) {
                            this.d = d;
                        }

                        @Override
                        public void onNext(Drawable drawable) {
                            view.setBackground(drawable);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            d.dispose();
                        }
                    });
        } else {
            if (assets == null) assets = new HashMap<>();
            assets.put(view, name + ":" + folderName);
        }
    }

    /**
     * 从skin中获取{@link #defaultFolder}名称文件夹中对应名称的PNG图片设置为background,如果未获取到会设置为透明状态
     *
     * @param view 要设置background的控件
     * @param name Public中对应图片的名称
     */
    public void setBackground(View view, String name) {
        setBackground(view, name, defaultFolder);
    }

    /**
     * 将传入的view的背景全部设置为从skin中使用默认配置获取对应文件夹中对应名称的PNG图片,如果未获取到会设置为透明状态
     *
     * @param name       图片名称
     * @param folderName 图片文件夹
     * @param views      需要设置背景的控件
     */
    public void setBackground(String name, final String folderName, final View... views) {
        if (initComplete) {
            Observable.just(name)
                    .map(new Function<String, Drawable>() {
                        @Override
                        public Drawable apply(String s) {
                            return getDrawable(s, folderName);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Drawable>() {

                        Disposable d;

                        @Override
                        public void onSubscribe(Disposable d) {
                            this.d = d;
                        }

                        @Override
                        public void onNext(Drawable drawable) {
                            for (View view : views) {
                                view.setBackground(drawable);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            d.dispose();
                        }
                    });
        } else {
            if (assets == null) assets = new HashMap<>();
            for (View view : views) {
                assets.put(view, name + ":" + folderName);
            }
        }
    }

    public void setBackground(String name, View... views) {
        setBackground(name, defaultFolder, views);
    }

    /**
     * 从skin中使用对应位图配置获取格式为PNG的图片并设置为背景,如果未获取到会设置为透明状态
     * 位图配置默认为{@link Bitmap.Config#RGB_565}
     *
     * @param view       要设置background的控件
     * @param name       图片名称
     * @param folderName 图片所在文件夹名称
     */
    public void setHDBackground(final View view, String name, final String folderName) {
        if (initComplete) {
            Observable.just(name)
                    .map(new Function<String, Drawable>() {
                        @Override
                        public Drawable apply(String s) {
                            return getHDDrawable(s, folderName);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Drawable>() {

                        Disposable d;

                        @Override
                        public void onSubscribe(Disposable d) {
                            this.d = d;
                        }

                        @Override
                        public void onNext(Drawable drawable) {
                            view.setBackground(drawable);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            d.dispose();
                        }
                    });
        } else {
            if (assetsHD == null) assetsHD = new HashMap<>();
            assetsHD.put(view, name + ":" + folderName);
        }
    }

    /**
     * 从skin中获取{@link #defaultFolder}名称文件夹中对应名称的PNG图片设置为background,如果未获取到会设置为透明状态
     *
     * @param view 要设置background的控件
     * @param name Public中对应图片的名称
     */
    public void setHDBackground(View view, String name) {
        setHDBackground(view, name, defaultFolder);
    }

    @Override
    public void setHDBackground(String name, final String folderName, final View... views) {
        if (initComplete) {
            Observable.just(name)
                    .map(new Function<String, Drawable>() {
                        @Override
                        public Drawable apply(String s) {
                            return getHDDrawable(s, folderName);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Drawable>() {

                        Disposable d;

                        @Override
                        public void onSubscribe(Disposable d) {
                            this.d = d;
                        }

                        @Override
                        public void onNext(Drawable drawable) {
                            for (View view : views) {
                                view.setBackground(drawable);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            d.dispose();
                        }
                    });
        } else {
            if (assetsHD == null) assetsHD = new HashMap<>();
            for (View view : views) {
                assetsHD.put(view, name + ":" + folderName);
            }
        }
    }

    @Override
    public void setHDBackground(String name, View... views) {
        setHDBackground(name, defaultFolder, views);
    }

    public void setTypeface(final TextView tv, String name) {
        if (initComplete) {
            Observable.just(name)
                    .map(new Function<String, Typeface>() {
                        @Override
                        public Typeface apply(String s) {
                            return getTypeface(s);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Typeface>() {
                        Disposable d;

                        @Override
                        public void onSubscribe(Disposable d) {
                            this.d = d;
                        }

                        @Override
                        public void onNext(Typeface typeface) {
                            tv.setTypeface(typeface);
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onComplete() {
                            d.dispose();
                        }
                    });
        } else {
            if (typefaceMap == null) typefaceMap = new HashMap<>();
            typefaceMap.put(tv, name);
        }
    }

    /**
     * 从skin中获取drawable文件夹中对应名称的PNG图片设置为背景,如果未获取到会设置为透明状态
     *
     * @param view 要设置background的控件
     * @param name drawable中对应图片的名称
     */
    public void setResBackground(final View view, String name) {
        if (initComplete) {
            Observable.just(name)
                    .map(new Function<String, Drawable>() {
                        @Override
                        public Drawable apply(String s) {
                            return getResDrawable(s);
                        }
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Drawable>() {

                        Disposable d;

                        @Override
                        public void onSubscribe(Disposable d) {
                            this.d = d;
                        }

                        @Override
                        public void onNext(Drawable drawable) {
                            int paddingLeft = view.getPaddingLeft();
                            int paddingTop = view.getPaddingTop();
                            int paddingRight = view.getPaddingRight();
                            int paddingBottom = view.getPaddingBottom();
                            view.setBackground(drawable);
                            view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            d.dispose();
                        }
                    });
        } else {
            if (drawableMap == null) drawableMap = new HashMap<>();
            drawableMap.put(view, name);
        }
    }

    /**
     * 为TextView设置上下左右位置的Drawable
     */
    @Override
    public void setCompoundDrawables(final TextView view, final String left, final String top,
                                     final String right, final String bottom, final String foldName) {
        if (initComplete) {
            Observable.create(new ObservableOnSubscribe<Drawable[]>() {
                @Override
                public void subscribe(ObservableEmitter<Drawable[]> emitter) {
                    Drawable[] drawables = new Drawable[4];
                    if (!TextUtils.isEmpty(left)) {
                        Drawable leftDrawable = getDrawable(left, foldName);
                        leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(),
                                leftDrawable.getMinimumHeight());
                        drawables[0] = leftDrawable;
                    }
                    if (!TextUtils.isEmpty(top)) {
                        Drawable topDrawable = getDrawable(top, foldName);
                        topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(),
                                topDrawable.getMinimumHeight());
                        drawables[1] = topDrawable;
                    }
                    if (!TextUtils.isEmpty(right)) {
                        Drawable rightDrawable = getDrawable(right, foldName);
                        rightDrawable.setBounds(0, 0, rightDrawable.getMinimumWidth(),
                                rightDrawable.getMinimumHeight());
                        drawables[2] = rightDrawable;
                    }
                    if (!TextUtils.isEmpty(bottom)) {
                        Drawable bottomDrawable = getDrawable(right);
                        bottomDrawable.setBounds(0, 0, bottomDrawable.getMinimumWidth(),
                                bottomDrawable.getMinimumHeight());
                        drawables[3] = bottomDrawable;
                    }
                    emitter.onNext(drawables);
                    emitter.onComplete();
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Drawable[]>() {
                        Disposable d;

                        @Override
                        public void onSubscribe(Disposable d) {
                            this.d = d;
                        }

                        @Override
                        public void onNext(Drawable[] drawables) {
                            view.setCompoundDrawables(drawables[0], drawables[1],
                                    drawables[2], drawables[3]);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            d.dispose();
                        }
                    });
        } else {
            if (compoundDrawablesMap == null) compoundDrawablesMap = new HashMap<>();
            compoundDrawablesMap.put(view, (TextUtils.isEmpty(left) ? "" : left) + "|"
                    + (TextUtils.isEmpty(top) ? "" : top) + "|"
                    + (TextUtils.isEmpty(right) ? "" : right) + "|"
                    + (TextUtils.isEmpty(bottom) ? "" : bottom) + "|" + foldName);
        }
    }

    @Override
    public void setCompoundDrawables(TextView view, String left, String top, String right, String bottom) {
        setCompoundDrawables(view, left, top, right, bottom, defaultFolder);
    }

    /**
     * 设置进度条 ProgressBar 的样式
     *
     * @param progressBar 控件对象
     * @param background  进度条背景
     * @param progress    进度条进度样式
     * @param foldName    资源所在目录
     */
    @Override
    public void setProgressBarDrawable(final ProgressBar progressBar, final String background,
                                       final String progress, final String foldName) {
        if (initComplete) {
            Observable.create(new ObservableOnSubscribe<Drawable>() {
                @Override
                public void subscribe(ObservableEmitter<Drawable> emitter) {
                    emitter.onNext(DrawableUtils.getProgressDrawable(getDrawable(background, foldName),
                            getDrawable(progress, foldName)));
                    emitter.onComplete();
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Drawable>() {
                        Disposable d;

                        @Override
                        public void onSubscribe(Disposable d) {
                            this.d = d;
                        }

                        @Override
                        public void onNext(Drawable drawable) {
                            progressBar.setProgressDrawable(drawable);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            d.dispose();
                        }
                    });
        } else {
            if (assets == null) assets = new HashMap<>();
            assets.put(progressBar, background + "|" + progress + ":" + foldName);
        }
    }

    @Override
    public void setProgressBarDrawable(ProgressBar progressBar, String background, String progress) {
        setProgressBarDrawable(progressBar, background, progress, defaultFolder);
    }


    /**
     * 异步线程加载skin中指定目录的2张PNG图片合成为状态选择器,在主线程中设置为background
     *
     * @param view     对应的控件
     * @param normal   默认的状态的图片的名称
     * @param pressed  按下的状态的图片的名称
     * @param foldName skin下assets图片所在文件夹的名称
     */
    public void setSelector(final View view, final String normal,
                            final String pressed, final String foldName) {
        if (initComplete) {
            Observable.create(new ObservableOnSubscribe<Drawable>() {
                @Override
                public void subscribe(ObservableEmitter<Drawable> emitter) {
                    emitter.onNext(getSelector(normal, pressed, foldName));
                    emitter.onComplete();
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Drawable>() {

                        Disposable d;

                        @Override
                        public void onSubscribe(Disposable d) {
                            this.d = d;
                        }

                        @Override
                        public void onNext(Drawable drawable) {
                            view.setBackground(drawable);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            if (!d.isDisposed()) d.dispose();
                        }
                    });
        } else {
            if (assets == null) assets = new HashMap<>();
            assets.put(view, normal + "|" + pressed + ":" + foldName);
        }
    }

    /**
     * 异步加载skin中{@link #defaultFolder}名称文件夹中的2张PNG图片合成为状态选择器,然后在主线程中设置为background
     *
     * @param view    对应的控件
     * @param normal  默认的状态的图片的名称
     * @param pressed 按下的状态的图片的名称
     */
    public void setSelector(View view, String normal, String pressed) {
        setSelector(view, normal, pressed, defaultFolder);
    }

    @Override
    public void setSelector(String normal, String pressed, String foldName, View... views) {
        for (View view : views) {
            setSelector(view, normal, pressed, foldName);
        }
    }

    @Override
    public void setSelector(String normal, String pressed, View... views) {
        for (View view : views) {
            setSelector(view, normal, pressed);
        }
    }

    /**
     * 设置对话框的背景
     *
     * @param dialog 对话框控件对象
     */
    public void setDialogBackground(View dialog) {
        if (initComplete) {
            Drawable dialogBackground = getResDrawable("dialog_background");
            if (dialogBackground instanceof ColorDrawable) {
                dialog.setBackground(getResDrawable("dialog_fram"));
            } else {
                dialog.setBackground(dialogBackground);
            }
        } else {
            if (comMap == null) comMap = new HashMap<>();
            comMap.put(dialog, dialogBackground);
        }
    }

    /**
     * 设置横向分割线的背景
     *
     * @param view 线控件的对象
     */
    public void setHorizontalLine(View view) {
        if (initComplete) {
            if (lineHorizontal instanceof ColorDrawable) {
                view.setBackground(lineHorizontal);
                Pair<Boolean, Integer> pair = getColorWithResult("colorLine");
                if (pair.first) {
                    view.setBackgroundColor(pair.second);
                } else {
                    view.setBackground(lineHorizontal);
                }
            } else {
                view.setBackground(new BitmapDrawable(null, ((BitmapDrawable) lineHorizontal).getBitmap()));
            }
        } else {
            if (comMap == null) comMap = new HashMap<>();
            comMap.put(view, lineHorizontal);
        }
    }

    /**
     * 批量设置横向分割线的背景
     *
     * @param views 线控件的数组
     */
    public void setHorizontalLine(View... views) {
        for (View view : views) {
            setHorizontalLine(view);
        }
    }

    /**
     * 设置view中的id为指定id的背景为横向线
     *
     * @param parent id所在的view
     * @param id     需要设置为横向分割线的控件id
     */
    public void setHorizontalLine(View parent, int id) {
        setHorizontalLine(parent.findViewById(id));
    }

    /**
     * 批量设置view中的id为指定id的背景为横向线
     *
     * @param parent id所在的view
     * @param ids    需要设置为横向分割线的控件id数组
     */
    public void setHorizontalLine(View parent, int... ids) {
        for (int id : ids) {
            setHorizontalLine(parent.findViewById(id));
        }
    }

    /**
     * 设置竖向分割线的背景
     *
     * @param view 线的对象
     */
    public void setVerticalLine(View view) {
        if (initComplete) {
            if (lineHorizontal instanceof ColorDrawable) {
                Pair<Boolean, Integer> pair = getColorWithResult("colorLine");
                if (pair.first) {
                    view.setBackgroundColor(pair.second);
                } else {
                    view.setBackground(lineVertical);
                }
            } else {
                view.setBackground(new BitmapDrawable(null, ((BitmapDrawable) lineVertical).getBitmap()));
            }
        } else {
            if (comMap == null) comMap = new HashMap<>();
            comMap.put(view, lineVertical);
        }
    }

    /**
     * 批量设置竖向分割线的背景
     *
     * @param views 线控件的数组
     */
    public void setVerticalLine(View... views) {
        for (View view : views) {
            setVerticalLine(view);
        }
    }

    /**
     * 设置view中的id为指定id的背景为竖向线
     *
     * @param parent id所在的view
     * @param id     需要设置为横向分割线的控件id
     */
    public void setVerticalLine(View parent, int id) {
        setVerticalLine(parent.findViewById(id));
    }

    /**
     * 批量设置view中的id为指定id的背景为竖向线
     *
     * @param parent id所在的view
     * @param ids    需要设置为竖向分割线的控件id数组
     */
    public void setVerticalLine(View parent, int... ids) {
        for (int id : ids) {
            setVerticalLine(parent.findViewById(id));
        }
    }

    /**
     * 将控件文字颜色设置为主题色
     *
     * @param view 需要设置颜色的TextView控件对象
     */
    public void setPrimaryColor(TextView view) {
        if (initComplete) {
            view.setTextColor(colorPrimary);
        } else {
            if (colorPrimaryList == null) colorPrimaryList = new ArrayList<>();
            colorPrimaryList.add(view);
        }
    }

    /**
     * 批量设置为主题色
     *
     * @param views 需要设置颜色的TextView控件对象数组
     */
    public void setPrimaryColor(TextView... views) {
        for (TextView view : views) {
            setPrimaryColor(view);
        }
    }

    /**
     * 设置view中控件id的控件的文字颜色为主题色
     *
     * @param parentView 父控件对象
     * @param id         子TextView控件id
     */
    public void setPrimaryColor(View parentView, int id) {
        setPrimaryColor((TextView) parentView.findViewById(id));
    }

    /**
     * 批量设置view中控件id的控件的文字颜色为主题色
     *
     * @param parentView 父控件对象
     * @param ids        子TextView控件id数组
     */
    public void setPrimaryColor(View parentView, int... ids) {
        for (int id : ids) {
            setPrimaryColor((TextView) parentView.findViewById(id));
        }
    }

    /**
     * 将控件文字颜色设置为主色
     *
     * @param view 需要设置颜色的TextView控件对象
     */
    public void setTextColor(TextView view) {
        if (initComplete) {
            view.setTextColor(colorText);
        } else {
            if (colorTextList == null) colorTextList = new ArrayList<>();
            colorTextList.add(view);
        }
    }

    /**
     * 批量设置为主色
     *
     * @param views 需要设置颜色的TextView控件对象数组
     */
    public void setTextColor(TextView... views) {
        for (TextView view : views) {
            setTextColor(view);
        }
    }

    /**
     * 设置view中控件id的控件的文字颜色为主色
     *
     * @param parentView 父控件对象
     * @param id         子TextView控件id
     */
    public void setTextColor(View parentView, int id) {
        setTextColor((TextView) parentView.findViewById(id));
    }

    /**
     * 批量设置为文字主色
     *
     * @param parentView 父控件对象
     * @param ids        子TextView控件id数组
     */
    public void setTextColor(View parentView, int... ids) {
        for (int id : ids) {
            setTextColor((TextView) parentView.findViewById(id));
        }
    }

    @Override
    public void setTextColorLight(TextView view) {
        if (initComplete) {
            view.setTextColor(colorTextLight);
        } else {
            if (colorTextLightList == null) colorTextLightList = new ArrayList<>();
            colorTextLightList.add(view);
        }
    }

    @Override
    public void setTextColorLight(TextView... views) {
        for (TextView view : views) {
            setTextColorLight(view);
        }
    }

    @Override
    public void setTextColorLight(View parentView, int id) {
        setTextColorLight((TextView) parentView.findViewById(id));
    }

    @Override
    public void setTextColorLight(View parentView, int... ids) {
        for (int id : ids) {
            setTextColorLight((TextView) parentView.findViewById(id));
        }
    }

    @Override
    public void setTextColorDim(TextView view) {
        if (initComplete) {
            view.setTextColor(colorTextDim);
        } else {
            if (colorTextDimList == null) colorTextDimList = new ArrayList<>();
            colorTextDimList.add(view);
        }
    }

    @Override
    public void setTextColorDim(TextView... views) {
        for (TextView view : views) {
            setTextColorDim(view);
        }
    }

    @Override
    public void setTextColorDim(View parentView, int id) {
        setTextColorDim((TextView) parentView.findViewById(id));
    }

    @Override
    public void setTextColorDim(View parentView, int... ids) {
        for (int id : ids) {
            setTextColorDim((TextView) parentView.findViewById(id));
        }
    }

    @Override
    public void setTextColorDark(TextView view) {
        if (initComplete) {
            view.setTextColor(colorTextDark);
        } else {
            if (colorTextDarkList == null) colorTextDarkList = new ArrayList<>();
            colorTextDarkList.add(view);
        }
    }

    @Override
    public void setTextColorDark(TextView... views) {
        for (TextView view : views) {
            setTextColorDark(view);
        }
    }

    @Override
    public void setTextColorDark(View parentView, int id) {
        setTextColorDark((TextView) parentView.findViewById(id));
    }

    @Override
    public void setTextColorDark(View parentView, int... ids) {
        for (int id : ids) {
            setTextColorDark((TextView) parentView.findViewById(id));
        }
    }

    /**
     * 将控件文字颜色设置为默认为文字颜色,点击时为主题色
     */
    @Override
    public void setTextPrimaryColorState(TextView view) {
        if (initComplete) {
            view.setTextColor(SelectorUtils.createColorState(colorText, colorPrimary));
        } else {
            if (colorStateList == null) colorStateList = new ArrayList<>();
            colorStateList.add(view);
        }
    }

    /**
     * 批量将控件文字颜色设置为默认为文字颜色
     */
    @Override
    public void setTextPrimaryColorState(TextView... views) {
        for (TextView view : views) {
            setTextPrimaryColorState(view);
        }
    }

    /**
     * 将指定控件中的指定id的控件文字颜色设置为默认为文字颜色,点击时为主题色
     *
     * @param parentView 父控件
     * @param id         要设置文字颜色的id
     */
    @Override
    public void setTextPrimaryColorState(View parentView, int id) {
        setTextPrimaryColorState((TextView) parentView.findViewById(id));
    }

    /**
     * 将指定控件中的指定id的多个控件文字颜色设置为默认为文字颜色,点击时为主题色
     *
     * @param parentView 父控件
     * @param ids        要设置文字颜色的控件们id
     */
    @Override
    public void setTextPrimaryColorState(View parentView, int... ids) {
        for (int id : ids) {
            setTextPrimaryColorState((TextView) parentView.findViewById(id));
        }
    }
}