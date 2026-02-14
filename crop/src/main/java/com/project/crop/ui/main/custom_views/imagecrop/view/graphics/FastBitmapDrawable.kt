package com.project.crop.ui.main.custom_views.imagecrop.view.graphics

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import java.io.InputStream

/**
 * Fast bitmap drawable. Does not support states. it only
 * support alpha and colormatrix
 *
 * @author alessandro
 */
class FastBitmapDrawable(var bitmap: Bitmap?) : Drawable() {
    var paint: Paint
        protected set
    protected var mIntrinsicWidth = 0
    protected var mIntrinsicHeight = 0

    constructor(res: Resources?, `is`: InputStream?) : this(BitmapFactory.decodeStream(`is`)) {}

    override fun draw(canvas: Canvas) {
        if (null != bitmap && !bitmap!!.isRecycled) {
            canvas.drawBitmap(bitmap!!, 0.0f, 0.0f, paint)
        }
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(p0: ColorFilter?) {
        p0?.let {
            paint.colorFilter = it
        }
    }

    override fun getIntrinsicWidth(): Int {
        return mIntrinsicWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mIntrinsicHeight
    }

    override fun getMinimumWidth(): Int {
        return mIntrinsicWidth
    }

    override fun getMinimumHeight(): Int {
        return mIntrinsicHeight
    }

    fun setAntiAlias(value: Boolean) {
        paint.isAntiAlias = value
        invalidateSelf()
    }

    init {
        if (null != bitmap) {
            mIntrinsicWidth = bitmap!!.width
            mIntrinsicHeight = bitmap!!.height
        } else {
            mIntrinsicWidth = 0
            mIntrinsicHeight = 0
        }
        paint = Paint()
        paint.isDither = true
        paint.isFilterBitmap = true
    }
}