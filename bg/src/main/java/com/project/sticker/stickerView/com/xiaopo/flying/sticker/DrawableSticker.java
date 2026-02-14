package com.project.sticker.stickerView.com.xiaopo.flying.sticker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

/**
 * @author wupanjie
 */
public class DrawableSticker extends Sticker {

    private Drawable drawable;
    private Rect realBounds;

    public Float currentBlur = 1f;

    public String tag = "";
    public Float currentAlpha = 255f;
    public Bitmap originalBitmap = null;

//    public DrawableSticker(Drawable drawable, Matrix matrix) {
//        this.drawable = drawable;
//        if(matrix != null) {
//            this.getMatrix().reset();
//            this.getMatrix().set(matrix);
//            realBounds = new Rect(0, 0, (int) (getWidth()*this.getMatrixValue(matrix, Matrix.MSCALE_X)), (int) (getHeight()*this.getMatrixValue(matrix, Matrix.MSCALE_Y)));
//        }
//    }

    public DrawableSticker(Drawable drawable) {
        this.drawable = drawable;
        realBounds = new Rect(0, 0, getWidth(), getHeight());
    }

    @NonNull
    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public DrawableSticker setDrawable(@NonNull Drawable drawable) {
        this.drawable = drawable;
        return this;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.save();
        canvas.setMatrix(getMatrix());
        drawable.setBounds(realBounds);
        drawable.draw(canvas);
        canvas.restore();
    }

    @NonNull
    @Override
    public DrawableSticker setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        drawable.setAlpha(alpha);
        return this;
    }

    @Override
    public int getWidth() {
        return drawable.getIntrinsicWidth();
    }

    @Override
    public int getHeight() {
        return drawable.getIntrinsicHeight();
    }

    @Override
    public void release() {
        super.release();
        if (drawable != null) {
            drawable = null;
        }
    }
}
