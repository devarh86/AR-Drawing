/*
 * Copyright (c) 2015 Naver Corp.
 * @Author Ohkyun Kim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.project.crop.ui.main.custom_views.imagecrop.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.ViewConfiguration
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.project.crop.R
import com.project.crop.ui.main.custom_views.imagecrop.model.CropInfo
import com.project.crop.ui.main.custom_views.imagecrop.model.ViewState
import com.project.crop.ui.main.custom_views.imagecrop.util.BitmapLoadUtils.decode
import com.project.crop.ui.main.custom_views.imagecrop.util.Cubic
import com.project.crop.ui.main.custom_views.imagecrop.util.Easing
import com.project.crop.ui.main.custom_views.imagecrop.view.graphics.FastBitmapDrawable
import java.io.File
import kotlin.math.abs

open class ImageCropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {
    protected var mEasing: Easing = Cubic()
    private var mBaseMatrix = Matrix()
    private var mSuppMatrix = Matrix()
    private val mDisplayMatrix = Matrix()
    private var mHandler = Handler()
    private var mLayoutRunnable: Runnable? = null
    protected var mUserScaled = false
    private var mMaxZoom = ZOOM_INVALID
    private var mMinZoom = ZOOM_INVALID

    // true when min and max zoom are explicitly defined
    private var mMaxZoomDefined = false
    private var mMinZoomDefined = false
    private val mMatrixValues = FloatArray(9)
    private var mThisWidth = -1
    private var mThisHeight = -1
    protected val center = PointF()
    private var mBitmapChanged = false
    private var mRestoreRequest = false
    protected val defaultAnimationTime = 200
    private var mBitmapRect = RectF()
    private var mCenterRect = RectF()
    private var mScrollRect = RectF()
    var croppedRect = RectF()
        protected set
    private var mOutsideLayerPaint: Paint? = null
    private var mAspectRatioWidth = DEFAULT_ASPECT_RATIO_WIDTH
    private var mAspectRatioHeight = DEFAULT_ASPECT_RATIO_HEIGHT
    private var mTargetAspectRatio = (mAspectRatioHeight / mAspectRatioWidth).toFloat()
    private var mPts: FloatArray = floatArrayOf()
    private val gridRowCount = 3
    private val gridColumnCount = 3
    private var mGridInnerLinePaint: Paint? = null
    private var mGridOuterLinePaint: Paint? = null
    private var gridInnerMode = 0
    private var gridOuterMode = 0
    private var gridLeftRightMargin = 0f
    private var gridTopBottomMargin = 0f
    private var imageFilePath: String? = null
    protected var mScaleDetector: ScaleGestureDetector? = null
    private var mGestureDetector: GestureDetector? = null
    private var mTouchSlop = 0
    private var mScaleFactor = 0f
    protected var mDoubleTapDirection = 0
    private var mGestureListener: GestureDetector.OnGestureListener? = null
    private var mScaleListener: OnScaleGestureListener? = null
    var doubleTapEnabled = true
    protected var mScaleEnabled = true
    protected var mScrollEnabled = true
    private var mDoubleTapListener: OnImageViewTouchDoubleTapListener? = null
    private var mSingleTapListener: OnImageViewTouchSingleTapListener? = null
    var isChangingScale = false
        private set
    private val suppMatrixValues = FloatArray(9)
    private var flipVertical = false
    private var flipHorizontal = false
    private fun init(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ImageCropView)
        mOutsideLayerPaint = Paint()
        val outsideLayerColor = a.getColor(
            R.styleable.ImageCropView_outsideLayerColor, Color.parseColor(
                DEFAULT_OUTSIDE_LAYER_COLOR_ID
            )
        )
        mOutsideLayerPaint?.color = outsideLayerColor
        scaleType = ScaleType.MATRIX
        mGridInnerLinePaint = Paint()
        val gridInnerStrokeWidth = a.getDimension(R.styleable.ImageCropView_gridInnerStroke, 1f)
        mGridInnerLinePaint?.strokeWidth = gridInnerStrokeWidth
        val gridInnerColor = a.getColor(R.styleable.ImageCropView_gridInnerColor, Color.WHITE)
        mGridInnerLinePaint?.color = gridInnerColor
        mGridOuterLinePaint = Paint()
        val gridOuterStrokeWidth = a.getDimension(R.styleable.ImageCropView_gridOuterStroke, 2f)
        mGridOuterLinePaint?.strokeWidth = gridOuterStrokeWidth
        val gridOuterColor = a.getColor(R.styleable.ImageCropView_gridOuterColor, Color.WHITE)
        mGridOuterLinePaint?.color = gridOuterColor
        mGridOuterLinePaint?.style = Paint.Style.STROKE
        gridInnerMode = a.getInt(R.styleable.ImageCropView_setInnerGridMode, GRID_OFF)
        gridOuterMode = a.getInt(R.styleable.ImageCropView_setOuterGridMode, GRID_OFF)
        gridLeftRightMargin = a.getDimension(R.styleable.ImageCropView_gridLeftRightMargin, 32f)
        gridTopBottomMargin = a.getDimension(R.styleable.ImageCropView_gridTopBottomMargin, 0f)
        val rowLineCount = (gridRowCount - 1) * 4
        val columnLineCount = (gridColumnCount - 1) * 4
        mPts = FloatArray(rowLineCount + columnLineCount)
        a.recycle()
        mTouchSlop = ViewConfiguration.get(getContext()).scaledTouchSlop
        mGestureListener = GestureListener()
        mScaleListener = ScaleListener()
        mScaleListener?.let {
            mScaleDetector = ScaleGestureDetector(getContext(), it)
        }
        mGestureListener?.let {
            mGestureDetector = GestureDetector(getContext(), it, null, true)
        }
        mDoubleTapDirection = 1
        mBitmapChanged = false
        mRestoreRequest = false
    }

    override fun setScaleType(scaleType: ScaleType) {
        if (scaleType == ScaleType.MATRIX) {
            super.setScaleType(scaleType)
        } else {
            throw IllegalArgumentException("Unsupported scaleType. Only ScaleType.MATRIX can be used")
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawTransparentLayer(canvas)
        drawGrid(canvas)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (LOG_ENABLED) {
            Log.d(LOG_TAG, "onLayout: $changed, bitmapChanged: $mBitmapChanged")
        }
        super.onLayout(changed, left, top, right, bottom)
        var deltaX = 0
        var deltaY = 0
        if (changed) {
            val oldW = mThisWidth
            val oldH = mThisHeight
            mThisWidth = right - left
            mThisHeight = bottom - top
            deltaX = mThisWidth - oldW
            deltaY = mThisHeight - oldH

            // update center point
            center.x = mThisWidth / 2f
            center.y = mThisHeight / 2f
        }
        var height = (mThisWidth * mTargetAspectRatio).toInt()
        if (height > mThisHeight) {
            val width = (mThisHeight - gridTopBottomMargin * 2).div(mTargetAspectRatio).toInt()
            val halfDiff = (mThisWidth - width) / 2
            croppedRect[(left + halfDiff).toFloat(), top + gridTopBottomMargin, (right - halfDiff).toFloat()] =
                bottom - gridTopBottomMargin
        } else {
            height = (mThisWidth - (gridLeftRightMargin * 2)).times(mTargetAspectRatio).toInt()
            val halfDiff = (mThisHeight - height) / 2
            croppedRect[left + gridLeftRightMargin, (halfDiff - top).toFloat(), right - gridLeftRightMargin] =
                (height + halfDiff).toFloat()
        }
        val r = mLayoutRunnable
        if (r != null) {
            mLayoutRunnable = null
            r.run()
        }
        val drawable = drawable
        if (drawable != null) {
            if (changed || mBitmapChanged) {
                if (mBitmapChanged) {
                    mBaseMatrix.reset()
                    if (!mMinZoomDefined) mMinZoom = ZOOM_INVALID
                    if (!mMaxZoomDefined) mMaxZoom = ZOOM_INVALID
                }
                var scale = 1f

                // retrieve the old values
                val oldMatrixScale = getScale(mBaseMatrix)
                val oldScale = this.scale
                val oldMinScale = 1f.coerceAtMost(1f / oldMatrixScale)
                getProperBaseMatrix(drawable, mBaseMatrix)
                val newMatrixScale = getScale(mBaseMatrix)
                if (LOG_ENABLED) {
                    Log.d(LOG_TAG, "old matrix scale: $oldMatrixScale")
                    Log.d(LOG_TAG, "new matrix scale: $newMatrixScale")
                    Log.d(LOG_TAG, "old min scale: $oldMinScale")
                    Log.d(LOG_TAG, "old scale: $oldScale")
                }

                // 1. bitmap changed or scaleType changed
                if (mBitmapChanged) {
                    imageMatrix = imageViewMatrix
                } else if (changed) {

                    // 2. layout size changed
                    if (!mMinZoomDefined) mMinZoom = ZOOM_INVALID
                    if (!mMaxZoomDefined) mMaxZoom = ZOOM_INVALID
                    imageMatrix = imageViewMatrix
                    postTranslate(-deltaX.toFloat(), -deltaY.toFloat())
                    if (!mUserScaled) {
                        zoomTo(scale)
                    } else {
                        if (abs(oldScale - oldMinScale) > 0.001) {
                            scale = oldMatrixScale / newMatrixScale * oldScale
                        }
                        if (LOG_ENABLED) {
                            Log.v(LOG_TAG, "userScaled. scale=$scale")
                        }
                        zoomTo(scale)
                    }
                    if (LOG_ENABLED) {
                        Log.d(LOG_TAG, "old scale: $oldScale")
                        Log.d(LOG_TAG, "new scale: $scale")
                    }
                }
                mUserScaled = false
                if (scale > maxScale || scale < minScale) {
                    // if current scale if outside the min/max bounds
                    // then restore the correct scale
                    zoomTo(scale)
                }
                if (!mRestoreRequest) {
                    center(horizontal = true, vertical = true)
                }
                if (mBitmapChanged) mBitmapChanged = false
                if (mRestoreRequest) mRestoreRequest = false
                if (LOG_ENABLED) {
                    Log.d(LOG_TAG, "new scale: " + this.scale)
                }
            }
        } else {
            if (mBitmapChanged) mBitmapChanged = false
            if (mRestoreRequest) mRestoreRequest = false
        }
    }

    private fun resetDisplay() {
        mBitmapChanged = true
        resetMatrix()
        requestLayout()
    }

    private fun resetMatrix() {
        if (LOG_ENABLED) {
            Log.i(LOG_TAG, "resetMatrix")
        }
        mSuppMatrix = Matrix()
        imageMatrix = imageViewMatrix
        zoomTo(1f)
        postInvalidate()
    }

    private fun drawTransparentLayer(canvas: Canvas) {
        /*-
          -------------------------------------
          |                top                |
          -------------------------------------
          |      |                    |       |
          |      |                    |       |
          | left |      mCropRect     | right |
          |      |                    |       |
          |      |                    |       |
          -------------------------------------
          |              bottom               |
          -------------------------------------
         */
        val r = Rect()
        getLocalVisibleRect(r)
        mOutsideLayerPaint?.let {
            canvas.drawRect(
                r.left.toFloat(),
                r.top.toFloat(),
                r.right.toFloat(),
                croppedRect.top,
                it
            ) // top
            canvas.drawRect(
                r.left.toFloat(),
                croppedRect.bottom,
                r.right.toFloat(),
                r.bottom.toFloat(),
                it
            ) // bottom
            canvas.drawRect(
                r.left.toFloat(),
                croppedRect.top,
                croppedRect.left,
                croppedRect.bottom,
                it
            ) // left
            canvas.drawRect(
                croppedRect.right,
                croppedRect.top,
                r.right.toFloat(),
                croppedRect.bottom,
                it
            ) // right
        }
    }

    private fun drawGrid(canvas: Canvas) {
        var index = 0
        for (i in 0 until gridRowCount - 1) {
            mPts[index++] = croppedRect.left //start Xi
            mPts[index++] =
                croppedRect.height() * ((i.toFloat() + 1.0f) / gridRowCount.toFloat()) + croppedRect.top //start Yi
            mPts[index++] = croppedRect.right //stop  Xi
            mPts[index++] =
                croppedRect.height() * ((i.toFloat() + 1.0f) / gridRowCount.toFloat()) + croppedRect.top //stop  Yi
        }
        for (i in 0 until gridColumnCount - 1) {
            mPts[index++] =
                croppedRect.width() * ((i.toFloat() + 1.0f) / gridColumnCount.toFloat()) + croppedRect.left //start Xi
            mPts[index++] = croppedRect.top //start Yi
            mPts[index++] =
                croppedRect.width() * ((i.toFloat() + 1.0f) / gridColumnCount.toFloat()) + croppedRect.left //stop  Xi
            mPts[index++] = croppedRect.bottom //stop  Yi
        }

        if (gridInnerMode == GRID_ON) {
            mGridInnerLinePaint?.let { canvas.drawLines(mPts, it) }
        }
        if (gridOuterMode == GRID_ON) {
            val halfLineWidth = mGridOuterLinePaint?.strokeWidth?.times(0.5f)
            halfLineWidth?.let {
                mGridOuterLinePaint?.let {
                    canvas.drawRect(
                        croppedRect.left + halfLineWidth,
                        croppedRect.top + halfLineWidth,
                        croppedRect.right - halfLineWidth,
                        croppedRect.bottom - halfLineWidth,
                        it
                    )
                }
            }
        }
    }

    override fun setImageResource(resId: Int) {
        setImageDrawable(ContextCompat.getDrawable(context, resId))
    }

    fun setAspectRatio(aspectRatioWidth: Int, aspectRatioHeight: Int) {
        require(!(aspectRatioWidth <= 0 || aspectRatioHeight <= 0)) { "Cannot set aspect ratio value to a number less than or equal to 0." }
        mAspectRatioWidth = aspectRatioWidth
        mAspectRatioHeight = aspectRatioHeight
        mTargetAspectRatio = mAspectRatioHeight.toFloat() / mAspectRatioWidth.toFloat()
        resetDisplay()
    }

    fun setImageRotation(rotation: Float) {
        mSuppMatrix.postRotate(rotation, width / 2f, height / 2f)
        imageMatrix = imageViewMatrix
    }

    fun setImageFilePath(imageFilePath: String?, flipVertical: Boolean, flipHorizontal: Boolean) {
        val imageFile = imageFilePath?.let { File(it) }
        imageFile?.let {
            if (!imageFile.exists()) {
//                "Image file does not exist"
                return
            }
            this.imageFilePath = imageFilePath
            val reqSize = 1000
            this.flipVertical = flipVertical
            this.flipHorizontal = flipHorizontal
            val bitmap = decode(imageFilePath, reqSize, reqSize, true, flipVertical, flipHorizontal)
            bitmap?.let { it1 -> setImageBitmap(it1) }
        }
    }

    override fun setImageBitmap(bitmap: Bitmap) {
        val minScale = 1f
        val maxScale = 8f
        setImageBitmap(bitmap, minScale, maxScale)
    }

    fun setImageBitmap(bitmap: Bitmap?, minZoom: Float, maxZoom: Float) {
        val viewWidth = width
        if (viewWidth <= 0) {
            mLayoutRunnable = Runnable { setImageBitmap(bitmap, minZoom, maxZoom) }
            return
        }
        if (bitmap != null) {
            setImageDrawable(FastBitmapDrawable(bitmap), minZoom, maxZoom)
        } else {
            setImageDrawable(null, minZoom, maxZoom)
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        val minScale = 1f
        val maxScale = 8f
        setImageDrawable(drawable, minScale, maxScale)
    }

    fun setImageDrawable(drawable: Drawable?, minZoom: Float, maxZoom: Float) {
        val viewWidth = width
        if (viewWidth <= 0) {
            mLayoutRunnable = Runnable { setImageDrawable(drawable, minZoom, maxZoom) }
            return
        }
        setImageDrawableNew(drawable, minZoom, maxZoom)
    }

    private fun setImageDrawableNew(drawable: Drawable?, minZoom: Float, max_zoom: Float) {
        var minZoomNew = minZoom
        var maxZoom = max_zoom
        if (LOG_ENABLED) {
            Log.i(LOG_TAG, "_setImageDrawable")
        }
        mBaseMatrix.reset()
        if (drawable != null) {
            if (LOG_ENABLED) {
                Log.d(LOG_TAG, "size: " + drawable.intrinsicWidth + "x" + drawable.intrinsicHeight)
            }
            super.setImageDrawable(drawable)
        } else {
            super.setImageDrawable(null)
        }
        if (minZoomNew != ZOOM_INVALID && maxZoom != ZOOM_INVALID) {
            minZoomNew = minZoomNew.coerceAtMost(maxZoom)
            maxZoom = minZoomNew.coerceAtLeast(maxZoom)
            mMinZoom = minZoomNew
            mMaxZoom = maxZoom
            mMinZoomDefined = true
            mMaxZoomDefined = true
        } else {
            mMinZoom = ZOOM_INVALID
            mMaxZoom = ZOOM_INVALID
            mMinZoomDefined = false
            mMaxZoomDefined = false
        }
        if (LOG_ENABLED) {
            Log.v(LOG_TAG, "mMinZoom: $mMinZoom, mMaxZoom: $mMaxZoom")
        }
        mBitmapChanged = true
        mScaleFactor = maxScale / 3
        requestLayout()
    }

    private fun computeMaxZoom(): Float {
        val drawable = drawable ?: return 1f
        val fw = drawable.intrinsicWidth.toFloat() / mThisWidth.toFloat()
        val fh = drawable.intrinsicHeight.toFloat() / mThisHeight.toFloat()
        val scale = fw.coerceAtLeast(fh) * 8
        if (LOG_ENABLED) {
            Log.i(LOG_TAG, "computeMaxZoom: $scale")
        }
        return scale
    }

    private fun computeMinZoom(): Float {
        if (LOG_ENABLED) {
            Log.i(LOG_TAG, "computeMinZoom")
        }
        drawable ?: return 1f
        var scale = getScale(mBaseMatrix)
        scale = 1f.coerceAtMost(1f / scale)
        if (LOG_ENABLED) {
            Log.i(LOG_TAG, "computeMinZoom: $scale")
        }
        return scale
    }

    val maxScale: Float
        get() {
            if (mMaxZoom == ZOOM_INVALID) {
                mMaxZoom = computeMaxZoom()
            }
            return mMaxZoom
        }
    val minScale: Float
        get() {
            if (LOG_ENABLED) {
                Log.i(LOG_TAG, "getMinScale, mMinZoom: $mMinZoom")
            }
            if (mMinZoom == ZOOM_INVALID) {
                mMinZoom = computeMinZoom()
            }
            if (LOG_ENABLED) {
                Log.v(LOG_TAG, "mMinZoom: $mMinZoom")
            }
            return mMinZoom
        }

    private val imageViewMatrix: Matrix
        get() = getImageViewMatrix(mSuppMatrix)

    private fun getImageViewMatrix(supportMatrix: Matrix?): Matrix {
        mDisplayMatrix.set(mBaseMatrix)
        mDisplayMatrix.postConcat(supportMatrix)
        return mDisplayMatrix
    }

    private var baseScale = 1f

    init {
        init(context, attrs)
    }

    private fun getProperBaseMatrix(drawable: Drawable, matrix: Matrix) {
        val viewWidth = croppedRect.width()
        val viewHeight = croppedRect.height()
        if (LOG_ENABLED) {
            Log.d(LOG_TAG, "getProperBaseMatrix. view: " + viewWidth + "x" + viewHeight)
        }
        val w = drawable.intrinsicWidth.toFloat()
        val h = drawable.intrinsicHeight.toFloat()
        val widthScale: Float
        val heightScale: Float
        matrix.reset()
        if (w > viewWidth || h > viewHeight) {
            widthScale = viewWidth / w
            heightScale = viewHeight / h
            baseScale = widthScale.coerceAtLeast(heightScale)
            matrix.postScale(baseScale, baseScale)
            val tw = (viewWidth - w * baseScale) / 2.0f
            val th = (viewHeight - h * baseScale) / 2.0f
            matrix.postTranslate(tw, th)
        } else {
            widthScale = viewWidth / w
            heightScale = viewHeight / h
            baseScale = widthScale.coerceAtLeast(heightScale)
            matrix.postScale(baseScale, baseScale)
            val tw = (viewWidth - w * baseScale) / 2.0f
            val th = (viewHeight - h * baseScale) / 2.0f
            matrix.postTranslate(tw, th)
        }
        if (LOG_ENABLED) {
            printMatrix(matrix)
        }
    }

    private fun getValue(matrix: Matrix, whichValue: Int): Float {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[whichValue]
    }

    private fun printMatrix(matrix: Matrix) {
        val scalex = getValue(matrix, Matrix.MSCALE_X)
        val scaley = getValue(matrix, Matrix.MSCALE_Y)
        val tx = getValue(matrix, Matrix.MTRANS_X)
        val ty = getValue(matrix, Matrix.MTRANS_Y)
        Log.d(LOG_TAG, "matrix: { x: $tx, y: $ty, scalex: $scalex, scaley: $scaley }")
    }

    private val bitmapRect: RectF?
        get() = getBitmapRect(mSuppMatrix)

    private fun getBitmapRect(supportMatrix: Matrix?): RectF? {
        val drawable = drawable ?: return null
        val m = getImageViewMatrix(supportMatrix)
        mBitmapRect[0f, 0f, drawable.intrinsicWidth.toFloat()] = drawable.intrinsicHeight.toFloat()
        m.mapRect(mBitmapRect)
        return mBitmapRect
    }

    private fun getScale(matrix: Matrix): Float {
        return getValue(matrix, Matrix.MSCALE_X)
    }

    @SuppressLint("Override")
    override fun getRotation(): Float {
        return 0f
    }

    val scale: Float
        get() = getScale(mSuppMatrix)

//    fun getBaseScale(): Float {
//        return getScale(mBaseMatrix)
//    }

    protected fun center(horizontal: Boolean, vertical: Boolean) {
        drawable ?: return
        val rect = getCenter(mSuppMatrix, horizontal, vertical)
        if (rect.left != 0f || rect.top != 0f) {
            if (LOG_ENABLED) {
                Log.i(LOG_TAG, "center")
            }
            postTranslate(rect.left, rect.top)
        }
    }

    private fun getCenter(supportMatrix: Matrix?, horizontal: Boolean, vertical: Boolean): RectF {
        drawable ?: return RectF(0f, 0f, 0f, 0f)
        mCenterRect[0f, 0f, 0f] = 0f
        val rect = getBitmapRect(supportMatrix)
        val height = rect!!.height()
        val width = rect.width()
        var deltaX = 0f
        var deltaY = 0f
        if (vertical) {
            val viewHeight = mThisHeight
            if (height < viewHeight) {
                deltaY = (viewHeight - height) / 2 - rect.top
            } else if (rect.top > 0) {
                deltaY = -rect.top
            } else if (rect.bottom < viewHeight) {
                deltaY = mThisHeight - rect.bottom
            }
        }
        if (horizontal) {
            val viewWidth = mThisWidth
            if (width < viewWidth) {
                deltaX = (viewWidth - width) / 2 - rect.left
            } else if (rect.left > 0) {
                deltaX = -rect.left
            } else if (rect.right < viewWidth) {
                deltaX = viewWidth - rect.right
            }
        }
        mCenterRect[deltaX, deltaY, 0f] = 0f
        return mCenterRect
    }

    private fun postTranslate(deltaX: Float, deltaY: Float) {
        if (deltaX != 0f || deltaY != 0f) {
            if (LOG_ENABLED) {
                Log.i(LOG_TAG, "postTranslate: " + deltaX + "x" + deltaY)
            }
            mSuppMatrix.postTranslate(deltaX, deltaY)
            imageMatrix = imageViewMatrix
        }
    }

    private fun postScale(scale: Float, centerX: Float, centerY: Float) {
        if (LOG_ENABLED) {
            Log.i(LOG_TAG, "postScale: " + scale + ", center: " + centerX + "x" + centerY)
        }
        mSuppMatrix.postScale(scale, scale, centerX, centerY)
        imageMatrix = imageViewMatrix
    }

    private fun zoomTo(scale: Float) {
        var myScale = scale

        if (LOG_ENABLED) {
            Log.i(LOG_TAG, "zoomTo: $myScale")
        }
        if (myScale > maxScale) myScale = maxScale
        if (myScale < minScale) myScale = minScale
        if (LOG_ENABLED) {
            Log.d(LOG_TAG, "sanitized scale: $myScale")
        }
        val center = center
        zoomTo(myScale, center.x, center.y)
    }

    private fun zoomTo(scale: Float, durationMs: Float) {
        val center = center
        zoomTo(scale, center.x, center.y, durationMs)
    }

    protected fun zoomTo(scale: Float, centerX: Float, centerY: Float) {
        var myScale = scale
        if (myScale > maxScale) myScale = maxScale
        val oldScale = this.scale
        val deltaScale = myScale / oldScale
        postScale(deltaScale, centerX, centerY)
        //        center(true, true);
    }

    protected fun onZoomAnimationCompleted(scale: Float) {
        if (LOG_ENABLED) {
            Log.d(LOG_TAG, "onZoomAnimationCompleted. scale: $scale , minZoom: $minScale")
        }
        if (scale < minScale) {
            zoomTo(minScale, 50f)
        }
    }

    private fun scrollBy(x: Float, y: Float) {
        panBy(x.toDouble(), y.toDouble())
    }

    protected fun panBy(dx: Double, dy: Double) {
        mScrollRect[dx.toFloat(), dy.toFloat(), 0f] = 0f
        postTranslate(mScrollRect.left, mScrollRect.top)
        adjustCropAreaImage()
    }

    private fun adjustCropAreaImage() {
        drawable ?: return
        val rect = getAdjust(mSuppMatrix)
        if (rect.left != 0f || rect.top != 0f) {
            if (LOG_ENABLED) {
                Log.i(LOG_TAG, "center")
            }
            postTranslate(rect.left, rect.top)
        }
    }

    private fun getAdjust(supportMatrix: Matrix): RectF {
        drawable ?: return RectF(0f, 0f, 0f, 0f)
        mCenterRect[0f, 0f, 0f] = 0f
        val rect = getBitmapRect(supportMatrix)
        var deltaX = 0f
        var deltaY = 0f

        //Y
        rect?.let {
            if (rect.top > croppedRect.top) {
                deltaY = croppedRect.top - rect.top
            } else if (rect.bottom < croppedRect.bottom) {
                deltaY = croppedRect.bottom - rect.bottom
            }
        }
        //X
        rect?.let {
            if (rect.left > croppedRect.left) {
                deltaX = croppedRect.left - rect.left
            } else if (rect.right < croppedRect.right) {
                deltaX = croppedRect.right - rect.right
            }
        }
        mCenterRect[deltaX, deltaY, 0f] = 0f
        return mCenterRect
    }

    private fun scrollBy(distanceX: Float, distanceY: Float, durationMs: Double) {
        val dx = distanceX.toDouble()
        val dy = distanceY.toDouble()
        val startTime = System.currentTimeMillis()
        mHandler.post(
            object : Runnable {
                var old_x = 0.0
                var old_y = 0.0
                override fun run() {
                    val now = System.currentTimeMillis()
                    val currentMs = durationMs.coerceAtMost((now - startTime).toDouble())
                    val x = mEasing.easeOut(currentMs, 0.0, dx, durationMs)
                    val y = mEasing.easeOut(currentMs, 0.0, dy, durationMs)
                    panBy(x - old_x, y - old_y)
                    old_x = x
                    old_y = y
                    if (currentMs < durationMs) {
                        mHandler.post(this)
                    }
                }
            }
        )
    }

    protected fun zoomTo(scale: Float, centerX: Float, centerY: Float, durationMs: Float) {
        var myScale = scale
        if (myScale > maxScale) myScale = maxScale
        val startTime = System.currentTimeMillis()
        val oldScale = this.scale
        val deltaScale = myScale - oldScale
        val m = Matrix(mSuppMatrix)
        m.postScale(myScale, myScale, centerX, centerY)
        val rect = getCenter(m, horizontal = true, vertical = true)
        val destX = centerX + rect.left * myScale
        val destY = centerY + rect.top * myScale
        mHandler.post(
            object : Runnable {
                override fun run() {
                    val now = System.currentTimeMillis()
                    val currentMs = durationMs.coerceAtMost((now - startTime).toFloat())
                    val newScale = mEasing.easeInOut(
                        currentMs.toDouble(),
                        0.0,
                        deltaScale.toDouble(),
                        durationMs.toDouble()
                    ).toFloat()
                    zoomTo(oldScale + newScale, destX, destY)
                    if (currentMs < durationMs) {
                        mHandler.post(this)
                    } else {
                        onZoomAnimationCompleted(this@ImageCropView.scale)
                        center(horizontal = true, vertical = true)
                    }
                }
            }
        )
    }

    val croppedImage: Bitmap?
        get() {
            val cropInfo = cropInfo ?: return null
            var bitmap: Bitmap?
            if (imageFilePath != null) {
                bitmap = cropInfo.getCroppedImage(imageFilePath, flipVertical, flipHorizontal)
            } else {
                bitmap = viewBitmap
                if (bitmap != null) {
                    bitmap = cropInfo.getCroppedImage(bitmap)
                }
            }
            return bitmap
        }
    private val cropInfo: CropInfo?
        get() {
            val viewBitmap = viewBitmap ?: return null
            val scale = baseScale * scale
            val viewImageRect = bitmapRect
            return viewImageRect?.top?.let {
                CropInfo(
                    scale,
                    viewBitmap.width.toFloat(),
                    it,
                    viewImageRect.left,
                    croppedRect.top,
                    croppedRect.left,
                    croppedRect.width(),
                    croppedRect.height()
                )
            }
        }

    private val viewBitmap: Bitmap?
        get() {
            val drawable = drawable
            return if (drawable != null) {
                BitmapDrawable(
                    resources,
                    drawable.toBitmap(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )
                ).bitmap
            } else {
                Log.e(LOG_TAG, "drawable is null")
                null
            }
        }

//    fun setGridInnerMode(gridInnerMode: Int) {
//        this.gridInnerMode = gridInnerMode
//        invalidate()
//    }
//
//    fun setGridOuterMode(gridOuterMode: Int) {
//        this.gridOuterMode = gridOuterMode
//        invalidate()
//    }

//    fun setGridLeftRightMargin(marginDP: Int) {
//        gridLeftRightMargin = dpToPixel(marginDP).toFloat()
//        requestLayout()
//    }
//
//    fun setGridTopBottomMargin(marginDP: Int) {
//        gridTopBottomMargin = dpToPixel(marginDP).toFloat()
//        requestLayout()
//    }

//    private fun dpToPixel(dp: Int): Int {
//        return TypedValue.applyDimension(
//            TypedValue.COMPLEX_UNIT_DIP,
//            dp.toFloat(),
//            resources.displayMetrics
//        ).toInt()
//    }

    fun saveState(): ViewState {
        mSuppMatrix.getValues(suppMatrixValues)
        return ViewState(imageViewMatrix, suppMatrixValues)
    }

//    fun restoreState(viewState: ViewState) {
//        mBitmapChanged = true
//        mRestoreRequest = true
//        mSuppMatrix = Matrix()
//        mSuppMatrix.setValues(viewState.suppMatrixValues)
//        imageMatrix = viewState.matrix
//        postInvalidate()
//        requestLayout()
//    }
//
//    fun setDoubleTapListener(listener: OnImageViewTouchDoubleTapListener?) {
//        mDoubleTapListener = listener
//    }
//
//    fun setSingleTapListener(listener: OnImageViewTouchSingleTapListener?) {
//        mSingleTapListener = listener
//    }
//
//    fun setScaleEnabled(value: Boolean) {
//        mScaleEnabled = value
//    }
//
//    fun setScrollEnabled(value: Boolean) {
//        mScrollEnabled = value
//    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mBitmapChanged) return false
        mScaleDetector!!.onTouchEvent(event)
        if (!mScaleDetector!!.isInProgress) {
            mGestureDetector!!.onTouchEvent(event)
        }
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_UP -> return onUp(event)
        }
        return true
    }

    protected fun onDoubleTapPost(scale: Float, maxZoom: Float): Float {
        return if (mDoubleTapDirection == 1) {
            if (scale + mScaleFactor * 2 <= maxZoom) {
                scale + mScaleFactor
            } else {
                mDoubleTapDirection = -1
                maxZoom
            }
        } else {
            mDoubleTapDirection = 1
            1f
        }
    }

    fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return true
    }

    fun myOnScroll(distanceX: Float, distanceY: Float): Boolean {
        mUserScaled = true
        scrollBy(-distanceX, -distanceY)
        invalidate()
        return true
    }

    fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val diffX = e2.x - e1!!.x
        val diffY = e2.y - e1.y
        if (abs(velocityX) > 800 || abs(velocityY) > 800) {
            mUserScaled = true
            scrollBy(diffX / 2, diffY / 2, 300.0)
            invalidate()
            return true
        }
        return false
    }

    fun onDown(e: MotionEvent?): Boolean {
        return !mBitmapChanged
    }

    private fun onUp(e: MotionEvent?): Boolean {
        if (mBitmapChanged) return false
        if (scale < minScale) {
            zoomTo(minScale, 50f)
        }
        return true
    }

    fun onSingleTapUp(e: MotionEvent?): Boolean {
        return !mBitmapChanged
    }

    inner class GestureListener : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (null != mSingleTapListener) {
                mSingleTapListener!!.onSingleTapConfirmed()
            }
            return this@ImageCropView.onSingleTapConfirmed(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (LOG_ENABLED) {
                Log.d(LOG_TAG, "onDoubleTap. double tap enabled? $doubleTapEnabled")
            }
            if (doubleTapEnabled) {
                mUserScaled = true
                val scale: Float = scale
                var targetScale = onDoubleTapPost(scale, maxScale)
                targetScale = maxScale.coerceAtMost(targetScale.coerceAtLeast(minScale))
                zoomTo(targetScale, e.x, e.y, defaultAnimationTime.toFloat())
                invalidate()
            }
            if (null != mDoubleTapListener) {
                mDoubleTapListener!!.onDoubleTap()
            }
            return super.onDoubleTap(e)
        }

        override fun onLongPress(e: MotionEvent) {
            if (isLongClickable) {
                if (!mScaleDetector!!.isInProgress) {
                    isPressed = true
                    performLongClick()
                }
            }
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (!mScrollEnabled) return false
            if (e1 == null) return false
            if (e1.pointerCount > 1 || e2.pointerCount > 1) return false
            return if (mScaleDetector?.isInProgress == true) false else this@ImageCropView.myOnScroll(
                distanceX,
                distanceY
            )
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (!mScrollEnabled) return false
            e1?.let {
                if (e1.pointerCount > 1 || e2.pointerCount > 1) return false
                return if (mScaleDetector?.isInProgress == true) false else this@ImageCropView.onFling(
                    e1,
                    e2,
                    velocityX,
                    velocityY
                )
            } ?: return false
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return this@ImageCropView.onSingleTapUp(e)
        }

        override fun onDown(e: MotionEvent): Boolean {
            return this@ImageCropView.onDown(e)
        }
    }

    inner class ScaleListener : SimpleOnScaleGestureListener() {
        private var mScaled = false
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isChangingScale = true
            return super.onScaleBegin(detector)
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val span = detector.currentSpan - detector.previousSpan
            var targetScale: Float = scale * detector.scaleFactor
            if (mScaleEnabled) {
                if (mScaled && span != 0f) {
                    mUserScaled = true
                    targetScale = maxScale.coerceAtMost(targetScale.coerceAtLeast(minScale - 0.1f))
                    zoomTo(targetScale, detector.focusX, detector.focusY)
                    mDoubleTapDirection = 1
                    invalidate()
                    return true
                }

                // This is to prevent a glitch the first time
                // image is scaled.
                if (!mScaled) mScaled = true
            }
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            isChangingScale = false
            super.onScaleEnd(detector)
        }
    }

    interface OnImageViewTouchDoubleTapListener {
        fun onDoubleTap()
    }

    interface OnImageViewTouchSingleTapListener {
        fun onSingleTapConfirmed()
    }

//    val positionInfo: FloatArray
//        get() {
//            val vals = FloatArray(9)
//            mSuppMatrix.getValues(vals)
//            return vals
//        }
//
//    fun applyPositionInfo(values: FloatArray) {
//        mBitmapChanged = true
//        applyValues(values)
//        requestLayout()
//    }

//    private fun applyValues(values: FloatArray) {
//        if (LOG_ENABLED) {
//            Log.i(LOG_TAG, "Matrix updated based on previous position info")
//        }
//        mSuppMatrix = Matrix()
//        mSuppMatrix.setValues(values)
//        imageMatrix = imageViewMatrix
//        postInvalidate()
//    }

    companion object {
        const val LOG_TAG = "ImageCropView"
        protected const val LOG_ENABLED = true
        const val ZOOM_INVALID = -1f
        const val DEFAULT_ASPECT_RATIO_WIDTH = 1
        const val DEFAULT_ASPECT_RATIO_HEIGHT = 1
        const val GRID_OFF = 0
        const val GRID_ON = 1
        private const val DEFAULT_OUTSIDE_LAYER_COLOR_ID = "#99000000"
    }
}