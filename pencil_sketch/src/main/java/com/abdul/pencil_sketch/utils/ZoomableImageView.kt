package com.abdul.pencil_sketch.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.setBlendMode
import androidx.core.graphics.values
import com.abdul.pencil_sketch.R
import com.project.common.utils.setColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

class ZoomableImageView : AppCompatImageView {

    var opacity: Float = 255f
    var opacityFilter: Float = 255f

    var maskPaint: Paint? = null
    var maskPaintFilter: Paint? = null

    var paint: Paint? = null
    private val TAG = this.javaClass.simpleName
    private var resizingJob: Job? = null
    private var rotationAngle: Float = 0f
    var frameChangingState: Boolean = false
    var flipHorizontal = false
    var flipVertical = false
    var touchDisable = true
    var percentageRotation: Float = 0f
    var imgPerScaleX: Float = 0f
    var imgPerScaleY: Float = 0f
    var imgPerSkewX: Float = 0f
    var imgPerSkewY: Float = 0f
    var imageInCenter: Boolean = false
    var prevMatrix: Matrix? = null
    var myListener: ZoomImgEvents? = null
    var percentageX = 0f
    var percentageY = 0f
    var lastEvent: FloatArray? = FloatArray(4)
    var d = 0f
    var newRot = 0f
    private val savedMatrix: Matrix = Matrix()
    private var scale = 0f
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private var mode = NONE
    private val start = PointF()
    private val mid = PointF()
    var oldDist = 1f
    private var m: FloatArray? = null
    var percentX = 0f
    var percentY = 0f
    var percentHeight = 0f
    var percentWidth = 0f
    private var context: Context? = null
    var effectMatrix = Matrix()
    var userImageMatrix = Matrix()
    var imagePopulated = false
    private var viewWidth = 0
    private var viewHeight = 0
    private var mGestureDetector: GestureDetector? = null
    private var doubleTapListener: GestureDetector.OnDoubleTapListener? = null
    private var zoomTouchListener: ZoomTouchListener? = null

    private var zoomTouchListenerUserImage: ZoomTouchListenerUserImage? = null
    var imgPerScaleXImg: Float = 0f
    var imgPerScaleYImg: Float = 0f
    var imgPerSkewXImg: Float = 0f
    var imgPerSkewYImg: Float = 0f

    var percentageXImg = 0f
    var percentageYImg = 0f
    private var zoomForUerImg = false
    var sizeChanges: Boolean = false

    private var isSyncing = false

    constructor(context: Context) : super(context) {
        sharedConstructing(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        sharedConstructing(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        sharedConstructing(context)
    }

    fun enableUserZoom(boolean: Boolean) {
        if (boolean) {
            zoomForUerImg = true
            zoomTouchListenerUserImage = ZoomTouchListenerUserImage()
            setOnTouchListener(null)
            setOnTouchListener(zoomTouchListenerUserImage)
        } else {
            zoomForUerImg = false
            zoomTouchListener = ZoomTouchListener()
            setOnTouchListener(null)
            //    setOnTouchListener (zoomTouchListener)
        }
    }

    fun syncMatrix(fromMatrix: Matrix, toMatrix: Matrix, targetView: ImageView) {
        if (isSyncing) return

        isSyncing = true
        toMatrix.set(fromMatrix)
        targetView.imageMatrix = toMatrix
        targetView.invalidate()
        isSyncing = false
    }

    fun setListener(listener: ZoomImgEvents?) {
        if (myListener == null)
            myListener = listener
    }

    interface ZoomImgEvents {
        fun onLongPress(zoomableImageView: ZoomableImageView?)
        fun onSinglePress(zoomableImageView: ZoomableImageView?)
        fun actionUpFromDrag(zoomableImageView: ZoomableImageView?)
        fun actionUpFromDragForDragDisable()
        fun updateRotation(img: ZoomableImageView, rotation: Float)
        fun updateRatioAfterRotation(img: ZoomableImageView?, bitmap: Bitmap)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun sharedConstructing(context: Context?) {
        super.setClickable(true)
        this.context = context
        mGestureDetector = GestureDetector(context, GestureListener())
        effectMatrix = Matrix()
        m = FloatArray(9)
        zoomTouchListener = ZoomTouchListener()
        setOnTouchListener(zoomTouchListener)
    }

    var effectMode: BlendModeCompat = BlendModeCompat.SRC
    var filterMode: BlendModeCompat = BlendModeCompat.SRC

    init {
        maskPaint = maskPaint ?: Paint(Paint.ANTI_ALIAS_FLAG)
        maskPaintFilter = maskPaintFilter ?: Paint(Paint.ANTI_ALIAS_FLAG)
        paint = Paint()
        paint?.style = Paint.Style.FILL_AND_STROKE
        paint?.color = context?.setColor(R.color.primary) ?: Color.RED
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        if (drawable == null)
            return

        try {

            drawable?.let {
                // Get the dimensions of the bitmap
                if (viewHeight != 0 && viewWidth != 0 && scaleType == ScaleType.MATRIX && !imageInCenter) {
                    imageInCenter = true
                    val drawable = it
                    val intrinsicWidth = drawable.intrinsicWidth
                    val intrinsicHeight = drawable.intrinsicHeight
                    val matrix = calculateFitScaleMatrix(width, height, intrinsicWidth, intrinsicHeight)

                    val matrixImg = calculateFitScaleMatrixImg(width, height, intrinsicWidth, intrinsicHeight)
                    imgPerSkewXImg =
                        (matrixImg.values()[Matrix.MSKEW_X] * 100).div(viewWidth).div(100)
                    imgPerSkewYImg =
                        (matrixImg.values()[Matrix.MSKEW_Y] * 100).div(viewHeight).div(100)

                    imgPerScaleXImg =
                        (matrixImg.values()[Matrix.MSCALE_X] * 100).div(viewWidth).div(100)
                    imgPerScaleYImg =
                        (matrixImg.values()[Matrix.MSCALE_Y] * 100).div(viewHeight).div(100)

                    val transX = matrixImg.values()[Matrix.MTRANS_X]
                    val transY = matrixImg.values()[Matrix.MTRANS_Y]
                    percentageXImg =
                        (transX * 100).div(viewWidth).div(100)
                    percentageYImg =
                        (transY * 100).div(viewHeight).div(100)

                    Log.i("onGlobalLayout", "onGlobalLayout: ${userImageMatrix}")
                    sizeChanges = true
                    userImageMatrix.reset()
                    userImageMatrix.set(matrix)
                    imageMatrix = userImageMatrix
                } else if (prevMatrix != null) {
                    userImageMatrix.reset()
                    userImageMatrix.set(prevMatrix)
                    imageMatrix = prevMatrix
                    sizeChanges = true
                    prevMatrix = null
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "onResourceReady: ", ex)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            maskPaintFilter?.setBlendMode(filterMode)
            maskPaint?.setBlendMode(effectMode)

        } else {
            maskPaintFilter?.setBlendMode(filterMode)
            maskPaint?.setBlendMode(effectMode)
        }

        maskPaintFilter?.alpha = opacityFilter.roundToInt()
        maskPaint?.alpha = opacity.roundToInt()

        super.onDraw(canvas)

        if (setStroke) {
            paint?.let { paint ->
                canvas.drawRect(0f, 0f, strokeWidth, height.toFloat(), paint)
                canvas.drawRect(strokeWidth, 0f, width.toFloat(), strokeWidth, paint)
                canvas.drawRect(
                    width.toFloat() - strokeWidth,
                    strokeWidth,
                    width.toFloat(),
                    height.toFloat(),
                    paint
                )
                canvas.drawRect(
                    strokeWidth,
                    height.toFloat() - strokeWidth,
                    width.toFloat() - strokeWidth,
                    height.toFloat(),
                    paint
                )
            }
        }
    }

    private var setStroke = false
    private var drawPlus = false
    private val strokeWidth = 8f
    fun setStroke() {
        setStroke = true
        invalidate()
    }

    fun resetStroke() {
        setStroke = false
        invalidate()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {

        resizingJob?.cancel()

        viewWidth = width
        viewHeight = height

        if (frameChangingState || drawable == null) {
            return
        }

        resizingJob = CoroutineScope(IO).launch {

            if (isActive) {

                val result = reCalculateMatrixValuesImg(userImageMatrix.values())
                userImageMatrix.reset()
                userImageMatrix.setValues(result)
                withContext(Main) {
                    sizeChanges = true
                }
            }
        }
        super.onSizeChanged(width, height, oldWidth, oldHeight)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        resizingJob?.cancel()
    }

    private fun reCalculateMatrixValuesImg(values: FloatArray): FloatArray {
        val x = percentageXImg * viewWidth
        val y = percentageYImg * viewHeight
        val scaleX = imgPerScaleXImg * viewWidth
        val scaleY = imgPerScaleYImg * viewHeight
        val skewX = imgPerSkewXImg * viewWidth
        val skewY = imgPerSkewYImg * viewHeight
        values[1] = skewX
        values[3] = skewY
        values[2] = x
        values[5] = y
        values[0] = scaleX
        values[4] = scaleY
        return values
    }


    /**
     * Save the current matrix and view dimensions
     * in the prevMatrix and prevView variables.
     */

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (doubleTapListener != null) {
                return doubleTapListener?.onSingleTapConfirmed(e) == true
            }
//            if (clickable) {
//                myListener?.onSinglePress(this@ZoomableImageView)
//            }
            return true
        }

        override fun onLongPress(e: MotionEvent) {
//            if (myListener != null) {
//                myListener?.onLongPress(this@ZoomableImageView)
//            }
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {

            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            return if (doubleTapListener != null) {
                doubleTapListener!!.onDoubleTapEvent(e)
            } else false
        }
    }

    fun flipHorizontal() {

        runCatching {
            prevMatrix = Matrix()
            prevMatrix?.set(userImageMatrix)
            BitmapDrawable(
                resources,
                drawable?.toBitmap()?.flipHorizontally()
            ).let {
                this.setImageDrawable(it)
            }
        }
    }

    fun flipVertical() {
        runCatching {
            prevMatrix = Matrix()
            prevMatrix?.set(userImageMatrix)
            BitmapDrawable(resources, drawable?.toBitmap()?.flipVertically()).let {
                this.setImageDrawable(it)
            }
        }
    }

    // To flip horizontally:
    private fun Bitmap.flipHorizontally(): Bitmap {
        val matrix = Matrix().apply { postScale(-1f, 1f, width / 2f, height / 2f) }
        flipHorizontal = !flipHorizontal
        //        if (!isRecycled) {
//            recycle()
//        }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    // To flip vertically:
    private fun Bitmap.flipVertically(): Bitmap {
        val matrix = Matrix().apply { postScale(1f, -1f, width / 2f, height / 2f) }
        flipVertical = !flipVertical
        //        if (!isRecycled) {
//            recycle()
//        }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }


    var rotationOnSlowGesture = false
    var resetSpeed = false
    var startRotation = 0f

    inner class ZoomTouchListener : OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {

            val view = v as ImageView

            if (touchDisable) {
                return false
            }

            mGestureDetector?.onTouchEvent(event)

            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    Log.i(TAG, "onTouch: not_pointer_down")
                    savedMatrix.set(effectMatrix)
                    start.x = event.x
                    start.y = event.y
                    mode = DRAG
                    lastEvent = null
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    Log.i(TAG, "onTouch: pointer_down")
                    resetSpeed = false
                    oldDist = spacing(event)
                    if (oldDist > 10f) {
                        savedMatrix.set(effectMatrix)
                        midPoint(mid, event)
                        val radians = atan2(
                            savedMatrix.values()[Matrix.MSKEW_X].toDouble(),
                            savedMatrix.values()[Matrix.MSCALE_X].toDouble()
                        )
                        val degrees = Math.toDegrees(radians).toFloat()
                        startRotation = -degrees
                        mode = ZOOM
                    }
                    lastEvent = FloatArray(4)
                    lastEvent?.let { lastEvent ->
                        lastEvent[0] = event.getX(0)
                        lastEvent[1] = event.getX(1)
                        lastEvent[2] = event.getY(0)
                        lastEvent[3] = event.getY(1)
                    }
                    d = rotation(event)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    rotationOnSlowGesture = false
                    mode = NONE
                    lastEvent = null
                    val matrix = effectMatrix
                    val radians = atan2(
                        matrix.values()[Matrix.MSKEW_X].toDouble(),
                        matrix.values()[Matrix.MSCALE_X].toDouble()
                    )
                    val degrees = Math.toDegrees(radians).toFloat()
                    percentageRotation = -degrees

                    imgPerSkewX =
                        (matrix.values()[Matrix.MSKEW_X] * 100).div(viewWidth).div(100)
                    imgPerSkewY =
                        (matrix.values()[Matrix.MSKEW_Y] * 100).div(viewHeight).div(100)

                    imgPerScaleX =
                        (matrix.values()[Matrix.MSCALE_X] * 100).div(viewWidth).div(100)
                    imgPerScaleY =
                        (matrix.values()[Matrix.MSCALE_Y] * 100).div(viewHeight).div(100)

                    val transX = matrix.values()[Matrix.MTRANS_X]
                    val transY = matrix.values()[Matrix.MTRANS_Y]
                    percentageX =
                        (transX * 100).div(viewWidth).div(100)
                    percentageY =
                        (transY * 100).div(viewHeight).div(100)

//                    val scaleX: Float = matrix.values()[Matrix.MSCALE_X]
//                    val scaleY: Float = matrix.values()[Matrix.MSCALE_Y]
//                    val x: Float = matrix.values()[Matrix.MTRANS_X]
//                    val y: Float = matrix.values()[Matrix.MTRANS_Y]

//                    applyTranslationLimits()

                    drawPlus = false

                    invalidate()

//                    myListener?.updateRotation(this@ZoomableImageView, getImageRotation())
                }

                MotionEvent.ACTION_MOVE -> {
                    if (mode == DRAG) {
                        effectMatrix.set(savedMatrix)

                        val dx = (event.x - start.x)
                        val dy = (event.y - start.y)

                        effectMatrix.postTranslate(dx, dy)

                        if (drawable == null) {
                            return false
                        }

//                    applyTranslationLimits()

                        view.invalidate()

                    } else if (mode == ZOOM && event.pointerCount == 2) {
                        val radians = atan2(
                            effectMatrix.values()[Matrix.MSKEW_X].toDouble(),
                            effectMatrix.values()[Matrix.MSCALE_X].toDouble()
                        )
                        val degrees = Math.toDegrees(radians).toFloat()
                        effectMatrix.set(savedMatrix)
                        val newDist: Float = spacing(event)
                        newRot = rotation(event)

                        if (lastEvent != null) {

                            val speed = (newRot - d)
                            rotationAngle = speed.times(
                                if (!resetSpeed) {
                                    if (-degrees in -4f..4f) {
                                        if (speed > 24 || speed < -24) {
                                            resetSpeed = true
                                            1f
                                        } else {
                                            drawPlus = true
                                            rotationOnSlowGesture = true
                                            0f
                                        }
                                    } else if (-degrees !in -4f..4f) {
                                        drawPlus = false
                                        resetSpeed = true
                                        1f
                                    } else {
                                        0f
                                    }
                                } else {
                                    rotationOnSlowGesture = false
                                    drawPlus = -degrees in -4f..4f
                                    1f
                                }
                            )

                            rotationAngle = if (rotationOnSlowGesture) {
                                rotationOnSlowGesture = false
                                if (startRotation < 0)
                                    -startRotation
                                else {
                                    -startRotation
                                }
                            } else {
                                rotationAngle
                            }

//                        lastValue?.let {
//                            if (firstTimeRotationStart) {
//                                rotationAngle = it
//                                lastValue = null
////                            return false
//                            }
//                        }

                            Log.i(TAG, "rotationDegree: ${-degrees}")
                            Log.i(TAG, "rotationAngle: ${-rotationAngle}")

                            effectMatrix.postRotate(rotationAngle, viewWidth / 2f, viewHeight / 2f)
//                        view.imageMatrix = effectMatrix
                            invalidate()
                        }

                        if (newDist > 10f) {
                            scale = (newDist / oldDist)
                            effectMatrix.postScale(scale, scale, mid.x, mid.y)

//                        val scaleFactor =
//                            Math.sqrt((scaleX * scaleX + scaleY * scaleY).toDouble()).toFloat()
//                        if (scaleFactor in 0.5..3.0) {
                            invalidate()
//                        } else {
//                            effectMatrix.set(savedMatrix)
//                        }
                        }
                    }
                }
            }
            return true
        }
    }


    inner class ZoomTouchListenerUserImage : OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {

            val view = v as ImageView

            if (touchDisable)
                return false

            mGestureDetector?.onTouchEvent(event)

            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    Log.i(TAG, "onTouch: not_pointer_down")
                    savedMatrix.set(imageMatrix)
                    start.x = event.x
                    start.y = event.y
                    mode = DRAG
                    lastEvent = null
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    Log.i(TAG, "onTouch: pointer_down")
                    resetSpeed = false
                    oldDist = spacing(event)
                    if (oldDist > 10f) {
                        savedMatrix.set(imageMatrix)
                        midPoint(mid, event)
                        val radians = atan2(
                            savedMatrix.values()[Matrix.MSKEW_X].toDouble(),
                            savedMatrix.values()[Matrix.MSCALE_X].toDouble()
                        )
                        val degrees = Math.toDegrees(radians).toFloat()
                        startRotation = -degrees
                        mode = ZOOM
                    }
                    lastEvent = FloatArray(4)
                    lastEvent?.let { lastEvent ->
                        lastEvent[0] = event.getX(0)
                        lastEvent[1] = event.getX(1)
                        lastEvent[2] = event.getY(0)
                        lastEvent[3] = event.getY(1)
                    }
                    d = rotation(event)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    rotationOnSlowGesture = false
                    mode = NONE
                    lastEvent = null
                    val matrix = imageMatrix
                    val radians = atan2(
                        matrix.values()[Matrix.MSKEW_X].toDouble(),
                        matrix.values()[Matrix.MSCALE_X].toDouble()
                    )
                    val degrees = Math.toDegrees(radians).toFloat()
                    percentageRotation = -degrees

                    imgPerSkewXImg =
                        (matrix.values()[Matrix.MSKEW_X] * 100).div(viewWidth).div(100)
                    imgPerSkewYImg =
                        (matrix.values()[Matrix.MSKEW_Y] * 100).div(viewHeight).div(100)

                    imgPerScaleXImg =
                        (matrix.values()[Matrix.MSCALE_X] * 100).div(viewWidth).div(100)
                    imgPerScaleYImg =
                        (matrix.values()[Matrix.MSCALE_Y] * 100).div(viewHeight).div(100)

                    val transX = matrix.values()[Matrix.MTRANS_X]
                    val transY = matrix.values()[Matrix.MTRANS_Y]
                    percentageXImg =
                        (transX * 100).div(viewWidth).div(100)
                    percentageYImg =
                        (transY * 100).div(viewHeight).div(100)


                    drawPlus = false

                    imageMatrix = userImageMatrix

                    myListener?.updateRotation(this@ZoomableImageView, getImageRotation())
                }

                MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                    userImageMatrix.set(savedMatrix)

                    val dx = (event.x - start.x)
                    val dy = (event.y - start.y)

                    userImageMatrix.postTranslate(dx, dy)

                    if (drawable == null) {
                        return false
                    }

                    view.imageMatrix = userImageMatrix

                } else if (mode == ZOOM && event.pointerCount == 2) {
                    val radians = atan2(
                        userImageMatrix.values()[Matrix.MSKEW_X].toDouble(),
                        userImageMatrix.values()[Matrix.MSCALE_X].toDouble()
                    )
                    val degrees = Math.toDegrees(radians).toFloat()
                    userImageMatrix.set(savedMatrix)
                    val newDist: Float = spacing(event)
                    newRot = rotation(event)

                    if (lastEvent != null) {

                        val speed = (newRot - d)
                        rotationAngle = speed.times(
                            if (!resetSpeed) {
                                if (-degrees in -4f..4f) {
                                    if (speed > 24 || speed < -24) {
                                        resetSpeed = true
                                        1f
                                    } else {
                                        drawPlus = true
                                        rotationOnSlowGesture = true
                                        0f
                                    }
                                } else if (-degrees !in -4f..4f) {
                                    drawPlus = false
                                    resetSpeed = true
                                    1f
                                } else {
                                    0f
                                }
                            } else {
                                rotationOnSlowGesture = false
                                drawPlus = -degrees in -4f..4f
                                1f
                            }
                        )

                        rotationAngle = if (rotationOnSlowGesture) {
                            rotationOnSlowGesture = false
                            if (startRotation < 0)
                                -startRotation
                            else {
                                -startRotation
                            }
                        } else {
                            rotationAngle
                        }

                        Log.i(TAG, "rotationDegree: ${-degrees}")
                        Log.i(TAG, "rotationAngle: ${-rotationAngle}")

                        userImageMatrix.postRotate(rotationAngle, viewWidth / 2f, viewHeight / 2f)
                        view.imageMatrix = userImageMatrix
                    }

                    if (newDist > 10f) {
                        scale = (newDist / oldDist)
                        userImageMatrix.postScale(scale, scale, mid.x, mid.y)

                        view.imageMatrix = userImageMatrix
                    }
                }
            }
            return true
        }
    }


    fun getImageRotation(): Float {
        return rotation
    }

    fun rotateBitmap(angle: Float): Bitmap? {
        try {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return originalBitmap?.let {
                Bitmap.createBitmap(
                    it,
                    0,
                    0,
                    it.width,
                    it.height,
                    matrix,
                    true
                )
            }
        } catch (ex: java.lang.Exception) {
            Log.e("error", "rotateBitmap: ", ex)
            return null
        }
    }

    var originalBitmap: Bitmap? = null
    var rotatedBitmap: Bitmap? = null

    fun setImageRotationUsingMatrix(rotation: Float) {
        setRotation(rotation)
    }

    fun resetRotation() {
        if (!frameChangingState) {
            CoroutineScope(IO).launch {
                rotationForCrop = 0f
                rotateBitmap(0f)?.let {
                    withContext(Main) {
                        rotation = 0f
                        if (this@ZoomableImageView.isAttachedToWindow) {
                            frameChangingState = true
                            setImageBitmap(null)
                            if (rotatedBitmap != null && rotatedBitmap?.isRecycled == false) {
                                rotatedBitmap?.recycle()
                            }
                            rotatedBitmap = it
                            myListener?.updateRatioAfterRotation(this@ZoomableImageView, it)
                        }
                    }
                } ?: run { frameChangingState = false }
            }
        }
    }

    var rotationForCrop = 0f

    fun setImageRotation(rotation: Float) {

        if (!frameChangingState) {
            rotateBitmap(rotation)?.let {
                frameChangingState = true
                setImageBitmap(null)
                if (rotatedBitmap != null && rotatedBitmap?.isRecycled == false) {
                    rotatedBitmap?.recycle()
                }
                rotatedBitmap = it
                myListener?.updateRatioAfterRotation(this, it)
            } ?: run { frameChangingState = false }
        }
    }

    private fun rotation(event: MotionEvent): Float {
        val delta_x = (event.getX(0) - event.getX(1)).toDouble()
        val delta_y = (event.getY(0) - event.getY(1)).toDouble()
        val radians = atan2(delta_y, delta_x)
        return Math.toDegrees(radians).toFloat()
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }

    fun fillOrCenterImageIntoView(scaleType: ScaleType) {
        if (scaleType == ScaleType.MATRIX) {
            imageInCenter = false
            this.scaleType = ScaleType.MATRIX
            imagePopulated = true
        } else {
            this.scaleType = ScaleType.CENTER_INSIDE
        }
    }

    fun setImageOpacity(opacityValue: Int) {
        // Accept 0..1, 0..100, or 0..255 input and normalize to alpha 0..255.
//        val alphaValue = when {
//            opacityValue <= 1f -> (opacityValue.coerceIn(0f, 1f) * 255f).roundToInt()
//            opacityValue <= 100f -> ((opacityValue.coerceIn(0f, 100f) / 100f) * 255f).roundToInt()
//            else -> opacityValue.roundToInt().coerceIn(0, 255)
//        }
        imageAlpha = opacityValue
        invalidate()
    }

    fun opacity(opacity: Float) {
        this.opacity = opacity
        invalidate()
    }

}

