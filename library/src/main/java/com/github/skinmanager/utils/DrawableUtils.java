package com.github.skinmanager.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.view.Gravity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import io.reactivex.annotations.Nullable;

public class DrawableUtils {

    /**
     * 将bitmap转换为圆角drawable
     */
    public static Drawable getRoundDrawable(Bitmap bitmap) {
        return getRoundDrawable(bitmap, 15f);
    }

    public static Drawable getRoundDrawable(Bitmap bitmap, float radius) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        RectF rect = new RectF(0f, 0f, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawRoundRect(rect, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0f, 0f, paint);
        return new BitmapDrawable(null, target);
    }

    /**
     * drawable转bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (w == 0 || h == 0) {
            w = 200;
            h = 200;
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 获取shape类型的drawable
     *
     * @param solidColor 颜色
     * @param radius     圆角角度
     */
    public static GradientDrawable getShapeDrawable(int solidColor, float radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(solidColor);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    /**
     * 获取shape类型的drawable
     *
     * @param solidColor  主要颜色
     * @param strokeColor 边框颜色
     * @param strokeWidth 边框宽度
     * @param radius      圆角角度
     */
    public static GradientDrawable getShapeDrawable(int solidColor, int strokeColor,
                                                    int strokeWidth, float radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(solidColor);
        drawable.setStroke(strokeWidth, strokeColor);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    /**
     * 获取进度条的Drawable
     *
     * @param background 进度条的背景
     * @param progress   进度条的进度
     */
    public static LayerDrawable getProgressDrawable(Drawable background, Drawable progress) {
        Drawable[] layers = new Drawable[2];
        layers[0] = background;
        layers[1] = new ClipDrawable(progress, Gravity.START, ClipDrawable.HORIZONTAL);
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.progress);
        return layerDrawable;
    }

    /**
     * 将bitmap切割为圆形返回为Drawable
     */
    @Nullable
    public static Drawable toRoundDrawable(Bitmap bitmap) {
        if (bitmap == null) return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int left = 0, top = 0, right = width, bottom = height;
        float roundPx = height / 2f;
        if (width > height) {
            left = (width - height) / 2;
            top = 0;
            right = left + height;
            bottom = height;
        } else if (height > width) {
            top = (height - width) / 2;
            right = width;
            bottom = top + width;
            roundPx = width / 2f;
        }
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = 0xff424242;
        Paint paint = new Paint();
        Rect rect = new Rect(left, top, right, bottom);
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return new BitmapDrawable(null, output);
    }

    public static Drawable getDrawable(String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;
        try {
            FileInputStream fis = new FileInputStream(filePath);
            BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inPreferredConfig = Bitmap.Config.ARGB_8888;
            ops.inSampleSize = 1;
            ops.inPurgeable = true;
            ops.inInputShareable = true;
            return new BitmapDrawable(null, BitmapFactory.decodeStream(fis, null, ops));
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
