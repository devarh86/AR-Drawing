package com.project.sticker.stickerView.com.xiaopo.flying.sticker;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Math.round;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import com.project.sticker.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author wupanjie
 */
public class StickerUtils {
    private static final String TAG = "StickerView";
    private static Context context;

    public static File saveImageToGallery(@NonNull Context mContext, @NonNull File file
            , @NonNull Bitmap bmp) {
//        if (bmp == null) {
//            throw new IllegalArgumentException("bmp should not be null");
//        }
        context = mContext;

        if (bmp != null) {
//            Bitmap bitmap;
            //////Checking waterMark inApp
//            if (getPrefForInAPPWaterMarkPurchase("inAppWaterMark") == false
//                    && TimerForRewardedAD.TimeCount == 0)
//                bitmap = addWatermark(bmp);
//            else
//                bitmap = bmp;

            try {
                FileOutputStream fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.WEBP, 100, fos);
                fos.flush();
                fos.close();

                ///change - Custom Function be Jasim
                context.sendBroadcast(new Intent
                        (Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                Log.e(TAG, "saveImageToGallery: the path of bmp is " + file.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (OutOfMemoryError outOfMemoryError) {
                outOfMemoryError.printStackTrace();
            }
        }
        return file;
    }

    //* Adds a watermark on the given image.
    //
    public static Bitmap addWatermark(Context context, Bitmap source) {
        Bitmap bmp = null;
        try {
            int w, h;
            Canvas c;
            Paint paint;
            Bitmap watermark;
            Matrix matrix;
            float scale;
            RectF r;
            w = source.getWidth();
            h = source.getHeight();
            // Create the new bitmap
            bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
            // Copy the original bitmap into the new one
            c = new Canvas(bmp);
            c.drawBitmap(source, 0, 0, paint);
            // Load the watermark
            watermark = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_expand);
            // Scale the watermark to be approximately 40% of the source image height
            scale = (float) (((float) h * 0.15) / (float) watermark.getHeight()); //original
//            scale = (float) (((float) h * 0.20) / (float) watermark.getHeight());
            // Create the matrix
            matrix = new Matrix();
            matrix.postScale(scale, scale);
            // Determine the post-scaled size of the watermark
            r = new RectF(0, 0, watermark.getWidth(), watermark.getHeight());
            matrix.mapRect(r);
            // Move the watermark to the bottom right corner // edited by jasim
            int bottomRightMargin = 20;
            int bottomMargin = 20;
            matrix.postTranslate((w - (r.width() + bottomRightMargin))
                    , (h - (r.height() + bottomMargin)));
            // Draw the watermark
            c.drawBitmap(watermark, matrix, paint);
            // Free up the bitmap memory
            watermark.recycle();
        } catch (OutOfMemoryError outOfMemoryError) {
        } catch (RuntimeException e) {
        }

        return bmp;
    }

    public static void notifySystemGallery(@NonNull Context context, @NonNull File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("bmp should not be null");
        }

        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(),
                    file.getName(), null);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("File couldn't be found");
        }
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }

    @NonNull
    public static RectF trapToRect(@NonNull float[] array) {
        RectF r = new RectF();
        trapToRect(r, array);
        return r;
    }

    public static void trapToRect(@NonNull RectF r, @NonNull float[] array) {
        r.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                Float.NEGATIVE_INFINITY);
        for (int i = 1; i < array.length; i += 2) {
            float x = round(array[i - 1] * 10) / 10.f;
            float y = round(array[i] * 10) / 10.f;
            r.left = (x < r.left) ? x : r.left;
            r.top = (y < r.top) ? y : r.top;
            r.right = (x > r.right) ? x : r.right;
            r.bottom = (y > r.bottom) ? y : r.bottom;
        }
        r.sort();
    }
    ///////////////////////////Shared Preferences
    ////////InAPP Purchase

    private static boolean getPrefForInAPPWaterMarkPurchase(String key) {
        SharedPreferences preferences = context.getSharedPreferences("inAppWaterMark", MODE_PRIVATE);
        return preferences.getBoolean(key, false);
    }
}
