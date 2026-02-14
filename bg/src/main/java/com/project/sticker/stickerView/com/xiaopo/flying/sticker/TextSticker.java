package com.project.sticker.stickerView.com.xiaopo.flying.sticker;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.project.sticker.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Customize your sticker with text and image background.
 * You can place some text into a given region, however,
 * you can also add a plain text sticker. To support text
 * auto resizing , I take most of the code from AutoResizeTextView.
 * See https://adilatwork.blogspot.com/2014/08/android-textview-which-resizes-its-text.html
 * Notice: It's not efficient to add long text due to too much of
 * StaticLayout object allocation.
 * Created by liutao on 30/11/2016.
 */

public class TextSticker extends Sticker {

    /**
     * Our ellipsis string.
     */
    private static final String mEllipsis = "\u2026";

    private final Context context;
    private final Rect realBounds;
    private Rect textRect;
    private final TextPaint textPaint;
    private Drawable drawable;
    private StaticLayout staticLayout;
    private int textReactTop = 0;
    private int textReactBottom = 0;
    private int textReactLeft = 0;
    private int textReactRight = 0;
    private Layout.Alignment alignment;

    public String text = "";

    public Boolean firstTime = true;

    public int colorWithoutTrans = Color.parseColor("#000000");

    public int myBackgroundColor;

    public int fontPosition = -1;

    /**
     * Upper bounds for text size.
     * This acts as a starting point for resizing.
     */
    private float maxTextSizePixels;

    /**
     * Lower bounds for text size.
     */
    private float minTextSizePixels;

    public int myWidth = 0;
    public int myHeight = 0;
    public int myTextSize = 0;

    public String tag = "";

    /**
     * Line spacing multiplier.
     */
    private float lineSpacingMultiplier = 1.0f;

    /**
     * Additional line spacing.
     */
    private float lineSpacingExtra = 0.0f;

    public TextSticker(@NonNull Context context) {
        this(context, null, 0, 0, 0);
    }

    public TextSticker(@NonNull Context context, int width, int height, int size) {
        this(context, null, width, height, size);
    }

    public TextSticker(@NonNull Context context, @Nullable Drawable drawable, int width, int height, int size) {
        this.context = context;
        this.drawable = drawable;
        if (drawable == null) {
            this.drawable = ContextCompat.getDrawable(context, R.drawable.sticker_transparent_background);
        }
        textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        if (width != 0 && height != 0) {
            myTextSize = size;
            myWidth = width;
            myHeight = height;
        }

        textRect = new Rect(0, 0, getWidth(), getHeight());
        realBounds = new Rect(0, 0, getWidth(), getHeight());

        minTextSizePixels = convertSpToPx(6);
        maxTextSizePixels = convertSpToPx(32);
        alignment = Layout.Alignment.ALIGN_CENTER;
        textPaint.setTextSize(maxTextSizePixels);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Matrix matrix = getMatrix();
        canvas.save();
        canvas.setMatrix(matrix);
        if (drawable != null) {
            drawable.setBounds(realBounds);
            drawable.draw(canvas);
        }
        canvas.restore();

        canvas.save();
        canvas.setMatrix(matrix);
        if (textRect.width() == getWidth()) {
            int dy = getHeight() / 2 - staticLayout.getHeight() / 2;
            // center vertical
            canvas.translate(0, dy);
        } else {
            int dx = textRect.left;
            int dy = textRect.top + textRect.height() / 2 - staticLayout.getHeight() / 2;
            canvas.translate(dx, dy);
        }
//        Paint strokePaint = new Paint();
//        strokePaint.setARGB(255, 0, 0, 0);
//        strokePaint.setTextAlign(Paint.Align.CENTER);
//        strokePaint.setTextSize(16);
//        strokePaint.setTypeface(Typeface.DEFAULT_BOLD);
//        strokePaint.setStyle(Paint.Style.STROKE);
//        strokePaint.setStrokeWidth(3);
//        canvas.drawText("", 100, 100, textPaint);
        staticLayout.draw(canvas);
        canvas.restore();
//
    }

    @Override
    public int getWidth() {
        if (myWidth == 0) {
            return drawable.getIntrinsicWidth();
        } else {
            return myWidth;
        }
    }

    @Override
    public int getHeight() {
        if (myWidth == 0) {
            return drawable.getIntrinsicHeight();
        } else {
            return myHeight;
        }
    }

    @Override
    public void release() {
        super.release();
        if (drawable != null) {
            drawable = null;
        }
    }

    @NonNull
    @Override
    public TextSticker setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        textPaint.setAlpha(alpha);
        return this;
    }

    @NonNull
    @Override
    public Drawable getDrawable() {

        return drawable;
    }

    @Override
    public TextSticker setDrawable(@NonNull Drawable drawable) {
        this.drawable = drawable;
        realBounds.set(0, 0, getWidth(), getHeight());
//        textRect.set(0, 0, getWidth(), getHeight());
        textRect = new Rect(textReactLeft, textReactTop, getWidth() - textReactRight, getHeight() - textReactBottom);
        return this;
    }

    @NonNull
    public TextSticker setDrawable(@NonNull Drawable drawable, @Nullable Rect region) {
        this.drawable = drawable;
        realBounds.set(0, 0, getWidth(), getHeight());
        if (region == null) {
            textRect.set(0, 0, getWidth(), getHeight());
        } else {
            textRect.set(region.left, region.top, region.right, region.bottom);
        }
        return this;
    }

    @NonNull
    public TextSticker setTypeface(@Nullable Typeface typeface) {
        textPaint.setTypeface(typeface);
        return this;
    }

    public void setFontPosition(int position) {
        fontPosition = position;
    }

    @NonNull
    public TextSticker setTextColor(@ColorInt int color) {
        textPaint.setColor(color);
        return this;
    }

    @NonNull
    public int getTextColor() {
        return textPaint.getColor();
    }


    /////////////Function created beside library built in
    @NonNull
    public TextSticker setShadowLayer(float radius, float dx, float dy, int color) {
        textPaint.setShadowLayer(radius, dx, dy, color);
        return this;
    }

    @NonNull
    public TextSticker setTextAlign(@NonNull Layout.Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    @NonNull
    public TextSticker setMaxTextSize(@Dimension(unit = Dimension.SP) float size) {
        textPaint.setTextSize(convertSpToPx(size));
        maxTextSizePixels = textPaint.getTextSize();
        return this;
    }

    @NonNull
    public TextSticker setTextSizePx(float size) {
        textPaint.setTextSize(size);
        return this;
    }

    /**
     * Sets the lower text size limit
     *
     * @param minTextSizeScaledPixels the minimum size to use for text in this view,
     *                                in scaled pixels.
     */
    @NonNull
    public TextSticker setMinTextSize(float minTextSizeScaledPixels) {
        minTextSizePixels = convertSpToPx(minTextSizeScaledPixels);
        return this;
    }

    @NonNull
    public TextSticker setMinTextSizeInPx(float minTextSizeScaledPixels) {
        minTextSizePixels = minTextSizeScaledPixels;
        return this;
    }

    @NonNull
    public TextSticker setLineSpacing(float add, float multiplier) {
        lineSpacingMultiplier = multiplier;
        lineSpacingExtra = add;
        return this;
    }

    @NonNull
    public TextSticker setText(@Nullable String text) {
        this.text = text;
        return this;
    }

    @NonNull
    public TextSticker assignTextview(@Nullable TextView textView) {
        return this;
    }


    @NonNull
    public TextSticker setOutline() {
        return this;
    }

    @NonNull
    public TextPaint getPaint() {
        return textPaint;
    }

    @Nullable
    public String getText() {
        return text;
    }

    /**
     * Resize this view's text size with respect to its width and height
     * (minus padding). You should always call this method after the initialization.
     */
    @NonNull
    public TextSticker resizeText() {
        final int availableHeightPixels = textRect.height();

        final int availableWidthPixels = textRect.width();

        final CharSequence text = getText();

        // Safety check
        // (Do not resize if the view does not have dimensions or if there is no text)
        if (text == null
                || text.length() <= 0
                || availableHeightPixels <= 0
                || availableWidthPixels <= 0
                || maxTextSizePixels <= 0) {
            return this;
        }

        float targetTextSizePixels = maxTextSizePixels;
        int targetTextHeightPixels =
                getTextHeightPixels(text, availableWidthPixels, targetTextSizePixels);

        // Until we either fit within our TextView
        // or we have reached our minimum text size,
        // incrementally try smaller sizes
        while (targetTextHeightPixels > availableHeightPixels
                && targetTextSizePixels > minTextSizePixels) {
            targetTextSizePixels = Math.max(targetTextSizePixels - 2, minTextSizePixels);

            targetTextHeightPixels =
                    getTextHeightPixels(text, availableWidthPixels, targetTextSizePixels);
        }

        // If we have reached our minimum text size and the text still doesn't fit,
        // append an ellipsis
        // (NOTE: Auto-ellipsize doesn't work hence why we have to do it here)
        if (targetTextSizePixels == minTextSizePixels
                && targetTextHeightPixels > availableHeightPixels) {
            // Make a copy of the original TextPaint object for measuring
            TextPaint textPaintCopy = new TextPaint(textPaint);
            textPaintCopy.setTextSize(targetTextSizePixels);

            // Measure using a StaticLayout instance
            StaticLayout staticLayout =
                    new StaticLayout(text, textPaintCopy, availableWidthPixels, Layout.Alignment.ALIGN_NORMAL,
                            lineSpacingMultiplier, lineSpacingExtra, false);

            // Check that we have a least one line of rendered text
            if (staticLayout.getLineCount() > 0) {
                // Since the line at the specific vertical position would be cut off,
                // we must trim up to the previous line and add an ellipsis
                int lastLine = staticLayout.getLineForVertical(availableHeightPixels) - 1;

                if (lastLine >= 0) {
                    int startOffset = staticLayout.getLineStart(lastLine);
                    int endOffset = staticLayout.getLineEnd(lastLine);
                    float lineWidthPixels = staticLayout.getLineWidth(lastLine);
                    float ellipseWidth = textPaintCopy.measureText(mEllipsis);

                    // Trim characters off until we have enough room to draw the ellipsis
                    while (availableWidthPixels < lineWidthPixels + ellipseWidth) {
                        endOffset--;
                        lineWidthPixels =
                                textPaintCopy.measureText(text.subSequence(startOffset, endOffset + 1).toString());
                    }

                    setText(text.subSequence(0, endOffset) + mEllipsis);
                }
            }
        }
//        if (myTextSize != 0) {
//
//            textPaint.setTextSize(myTextSize);
//        } else {
        textPaint.setTextSize(targetTextSizePixels);
//        }
        staticLayout =
                new StaticLayout(this.text, textPaint, textRect.width(), alignment, lineSpacingMultiplier,
                        lineSpacingExtra, true);
        return this;
    }

    @NonNull
    public TextSticker setTextLeft(@Nullable int left) {
        this.textReactLeft = left;
        return this;
    }

    @NonNull
    public TextSticker setTextTop(@Nullable int top) {
        this.textReactTop = top;
        return this;
    }

    @NonNull
    public TextSticker setTextRight(@Nullable int right) {
        this.textReactRight = right;
        return this;
    }

    @NonNull
    public TextSticker setTextBottom(@Nullable int bottom) {
        this.textReactBottom = bottom;
        return this;
    }

    /**
     * @return lower text size limit, in pixels.
     */
    public float getMinTextSizePixels() {
        return minTextSizePixels;
    }

    /**
     * Sets the text size of a clone of the view's {@link TextPaint} object
     * and uses a {@link StaticLayout} instance to measure the height of the text.
     *
     * @return the height of the text when placed in a view
     * with the specified width
     * and when the text has the specified size.
     */
    protected int getTextHeightPixels(@NonNull CharSequence source, int availableWidthPixels,
                                      float textSizePixels) {
        textPaint.setTextSize(textSizePixels);
        // It's not efficient to create a StaticLayout instance
        // every time when measuring, we can use StaticLayout.Builder
        // since api 23.
        StaticLayout staticLayout =
                new StaticLayout(source, textPaint, availableWidthPixels, Layout.Alignment.ALIGN_NORMAL,
                        lineSpacingMultiplier, lineSpacingExtra, true);
        return staticLayout.getHeight();
    }

    /**
     * @return the number of pixels which scaledPixels corresponds to on the device.
     */
    private float convertSpToPx(float scaledPixels) {
        return scaledPixels * context.getResources().getDisplayMetrics().scaledDensity;
    }


    // parse hex color to RGB color code
    public static int[] parseColor(final String color) {
        final Matcher mx = Pattern.compile("^#([0-9a-z]{2})([0-9a-z]{2})([0-9a-z]{2})$", CASE_INSENSITIVE).matcher(color);
        if (!mx.find())
            throw new IllegalArgumentException("invalid color value");
        final int R = Integer.parseInt(mx.group(1), 16);
        final int G = Integer.parseInt(mx.group(2), 16);
        final int B = Integer.parseInt(mx.group(3), 16);

        return new int[]{R, G, B};
    }

    // set Text Color And Opacity
    public void setTextColorOpacity(String color, int opacity) {
        textPaint.setColor(Color.argb(opacity, parseColor(color)[0], parseColor(color)[1], parseColor(color)[2]));
    }

    // set background color and opacity
    public GradientDrawable setBackgroundColorOpacity(int[] bgGradientColors, int bgColor, int bgOpacity, int stickerWidth, int stickerHeight) {
        GradientDrawable gradientDrawable = new GradientDrawable();

        // Set the color array to draw gradient
//    gradientDrawable.setColors(bgGradientColors);

        // set the color
        gradientDrawable.setColor(bgColor);

        // Set the GradientDrawable gradient type linear gradient
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        // Set GradientDrawable shape is a rectangle
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);

        //Where the INT ranges from 0 (fully transparent) to 255 (fully opaque).
        gradientDrawable.setAlpha(bgOpacity);

        // Set GradientDrawable width and in pixels
        gradientDrawable.setSize(stickerWidth, stickerHeight);

        // Set 3 pixels width solid blue color border
        //gradientDrawable.setStroke(5, Color.BLUE);

        // corner radius
        gradientDrawable.setCornerRadius(15f);

        return gradientDrawable;
    }

    // set shadow color, opacity and size
    public void setTextShadowOpacity(float shadowRadius, float shadowDx, float shadowDy, String shadowColor) {

        textPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy,
                Color.parseColor(shadowColor));
    }

}
