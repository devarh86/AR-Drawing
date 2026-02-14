package com.project.core.customView.stickerView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.GestureDetector
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import com.project.sticker.R
import com.project.sticker.stickerView.AutoResizeTextView
import com.project.sticker.utils.setOnSingleClickListener
import kotlin.math.*


abstract class StickerView : FrameLayout {
    private var midPoint: PointF = PointF()
    private var ivBorder: BorderView? = null
    private var ivScale: ImageView? = null
    private var ivRotate: ImageView? = null
    var forTouch: View? = null
    private var oldRotation = 0f
    private var ivDelete: ImageView? = null
    var ivFlip: ImageView? = null
    private var gestureDetector: GestureDetector? = null
    private val moveMatrix = Matrix()

    // For scaling
    private var thisOrgX = -1f
    private var thisOrgY = -1f
    private var scaleOrgX = -1f
    private var scaleOrgY = -1f
    private var scaleOrgWidth = -1.0
    private var scaleOrgHeight = -1.0

    // For rotating
    private var rotateOrgX = -1f
    private var rotateOrgY = -1f
    private var rotateNewX = -1f
    private var rotateNewY = -1f

    // For moving
    private var moveOrgX = -1f
    private var moveOrgY = -1f
    private var centerX = 0.0
    private var centerY = 0.0

    private var listener: IStickerOperation? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context)
    }

    fun setListener(myListener: IStickerOperation) {

        listener = myListener
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init(context: Context) {
        ivBorder = BorderView(context)
        ivScale = ImageView(context)
        ivRotate = ImageView(context)
        forTouch = View(context)
        ivDelete = ImageView(context)
        ivFlip = ImageView(context)
        ivScale?.setImageResource(R.drawable.ic_expand)
        ivDelete?.setImageResource(R.drawable.ic_cancel)
        ivFlip?.setImageResource(R.drawable.ic_rotate)
        ivRotate?.setImageResource(R.drawable.ic_rotate)
        this.tag = "DraggableViewGroup"
        forTouch?.tag = "DraggableViewGroup"
        ivBorder?.tag = "iv_border"
        ivScale?.tag = "iv_scale"
        ivDelete?.tag = "iv_delete"
        ivFlip?.tag = "iv_flip"
        ivRotate?.tag = "iv_rotate"
        val margin = convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext()) / 2
        val size = convertDpToPixel(SELF_SIZE_DP.toFloat(), getContext())
        val thisParams = LayoutParams(
            size,
            size
        )
        thisParams.gravity = Gravity.CENTER
        val ivMainParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        ivMainParams.setMargins(margin, margin, margin, margin)
        val ivBorderParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        ivBorderParams.setMargins(margin, margin, margin, margin)
        val ivScaleParams = LayoutParams(
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext()),
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext())
        )
        ivScaleParams.gravity = Gravity.BOTTOM or Gravity.END
        val ivDeleteParams = LayoutParams(
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext()),
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext())
        )
        ivDeleteParams.gravity = Gravity.TOP or Gravity.END
        val ivFlipParams = LayoutParams(
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext()),
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext())
        )
        ivFlipParams.gravity = Gravity.TOP or Gravity.START

        val ivRotateParams = LayoutParams(
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext()),
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext())
        )
        ivRotateParams.gravity = Gravity.BOTTOM or Gravity.START
        this.layoutParams = thisParams
        forTouch?.layoutParams = thisParams
        this.addView(mainView, ivMainParams)
        this.addView(forTouch)
        this.addView(ivBorder, ivBorderParams)
        this.addView(ivScale, ivScaleParams)
        this.addView(ivDelete, ivDeleteParams)
        this.addView(ivFlip, ivFlipParams)
        this.addView(ivRotate, ivRotateParams)
        setOnTouchListener(mTouchListener)
        forTouch?.setOnTouchListener(mTouchListener)
        ivScale?.setOnTouchListener(mTouchListener)
        ivRotate?.setOnTouchListener(mTouchListener)
        ivDelete?.setOnSingleClickListener {
            removeSticker()
        }

        ivFlip?.setOnSingleClickListener {
            Log.v(TAG, "flip the view")
            val mainView = mainView
            mainView?.rotationY = if (mainView?.rotationY == -180f) 0f else -180f
            mainView?.invalidate()
            requestLayout()
        }
        gestureDetector = GestureDetector(this.context, object :
            GestureDetector.SimpleOnGestureListener() {

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {

                mainView?.let {
                    if (it is AutoResizeTextView) {
                        forTouch?.visibility = View.GONE
                        listener?.onSingleTap(this@StickerView)
                    }
                }

                return super.onSingleTapConfirmed(e)
            }

//            override fun onDoubleTap(e: MotionEvent): Boolean {
//
//                return super.onDoubleTap(e)
//            }
        })
    }

//    val isFlip: Boolean
//        get() = mainView?.rotationY == -180f

    protected abstract val mainView: View?

    fun removeSticker() {
        if (this@StickerView.parent != null) {
            val myCanvas = this@StickerView.parent as ViewGroup
            myCanvas.removeView(this@StickerView)
            listener?.onDelete(this)
        }
    }

    @SuppressLint("ClickableViewAccessibility", "InternalInsetResource")
    private val mTouchListener = OnTouchListener { view, event ->

        if (view.tag == "DraggableViewGroup") {

            gestureDetector?.onTouchEvent(event)

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    Log.v(TAG, "sticker view action down")
                    moveOrgX = event.rawX
                    moveOrgY = event.rawY
                    listener?.onSelect(this)
                }
                MotionEvent.ACTION_MOVE -> {
                    Log.v(TAG, "sticker view action move")
                    val offsetX = event.rawX - moveOrgX
                    val offsetY = event.rawY - moveOrgY
                    this@StickerView.x = this@StickerView.x + offsetX
                    this@StickerView.y = this@StickerView.y + offsetY
                    moveOrgX = event.rawX
                    moveOrgY = event.rawY
                }
                MotionEvent.ACTION_UP -> Log.v(TAG, "sticker view action up")
            }
        } else if (view.tag == "iv_scale") {

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.v(TAG, "iv_scale action down")

                    thisOrgX = this@StickerView.x
                    thisOrgY = this@StickerView.y

                    scaleOrgX = event.rawX
                    scaleOrgY = event.rawY
                    scaleOrgWidth = this@StickerView.layoutParams.width.toDouble()
                    scaleOrgHeight = this@StickerView.layoutParams.height.toDouble()

                    centerX = (this@StickerView.x +
                            (this@StickerView.parent as View).x + this@StickerView.width.toFloat() / 2).toDouble()

                    calculateCenterY()
                    listener?.onSelect(this)
                }
                MotionEvent.ACTION_MOVE -> {

                    Log.v(TAG, "iv_scale action move")

                    val angleDiff = abs(
                        atan2(
                            (event.rawY - scaleOrgY).toDouble(),
                            (event.rawX - scaleOrgX).toDouble()
                        )
                                - atan2(scaleOrgY - centerY, scaleOrgX - centerX)
                    ) * 180 / Math.PI

                    Log.v(TAG, "angle_diff: $angleDiff")

                    val length1 = getLength(
                        centerX, centerY,
                        scaleOrgX.toDouble(), scaleOrgY.toDouble()
                    )
                    val length2 = getLength(
                        centerX, centerY,
                        event.rawX.toDouble(), event.rawY.toDouble()
                    )

                    val size = convertDpToPixel(
                        SELF_SIZE_DP.toFloat(),
                        context
                    )
                    if ((length2 > length1
                                && (angleDiff < 25 || abs(angleDiff - 180) < 25))
                    ) {
                        //scale up
                        val offsetX = abs(event.rawX - scaleOrgX).toDouble()
                        val offsetY = abs(event.rawY - scaleOrgY).toDouble()
                        var offset = offsetX.coerceAtLeast(offsetY)
                        offset = offset.roundToInt().toDouble()
                        this@StickerView.layoutParams.width += offset.toInt()
                        this@StickerView.layoutParams.height += offset.toInt()
                        //DraggableViewGroup.this.setX((float) (getX() - offset / 2))
                        //DraggableViewGroup.this.setY((float) (getY() - offset / 2))
                    } else if (((length2 < length1
                                ) && (angleDiff < 25 || abs(angleDiff - 180) < 25)
                                && (this@StickerView.layoutParams.width > size / 2
                                ) && (this@StickerView.layoutParams.height > size / 2))
                    ) {
                        //scale down
                        val offsetX = abs(event.rawX - scaleOrgX).toDouble()
                        val offsetY = abs(event.rawY - scaleOrgY).toDouble()
                        var offset = offsetX.coerceAtLeast(offsetY)
                        offset = offset.roundToInt().toDouble()
                        this@StickerView.layoutParams.width -= offset.toInt()
                        this@StickerView.layoutParams.height -= offset.toInt()
                    }

//                    val angle = Math.atan2(event.getRawY() - centerY, event.getRawX() - centerX) * 180 / Math.PI
//                    Log.v(TAG, "log angle: " + angle)
//
//                    //setRotation((float) angle - 45)
//                    rotation = (angle-50).toFloat()
//                    Log.v(TAG, "getRotation(): " + rotation)
//
//                    rotateOrgX = rotateNewX
//                    rotateOrgY = rotateNewY
//
                    scaleOrgX = event.rawX
                    scaleOrgY = event.rawY

                    postInvalidate()
                    requestLayout()
                }
                MotionEvent.ACTION_UP -> {
                    Log.v(TAG, "iv_scale action up")
                }
            }
        } else if (view.tag == "iv_rotate") {
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    centerX = (this@StickerView.x +
                            (this@StickerView.parent as View).x + this@StickerView.width.toFloat() / 2).toDouble()

                    calculateCenterY()

                }
                MotionEvent.ACTION_MOVE -> {

                    val angle = Math.atan2(
                        event.getRawY() - centerY,
                        event.getRawX() - centerX
                    ) * 180 / Math.PI
                    setRotation((angle - 45f).toFloat())

                    rotateOrgX = rotateNewX
                    rotateOrgY = rotateNewX

                    scaleOrgX = event.getRawX()
                    scaleOrgY = event.getRawY()

                    postInvalidate()
                    requestLayout()
                }
                MotionEvent.ACTION_UP -> {

                }
            }
        }
        true
    }

    open fun zoomAndRotateSticker(event: MotionEvent): Float {

        val newRotation = calculateRotation(midPoint.x, midPoint.y, event.rawX, event.rawY)
        return (newRotation - oldRotation)
    }

    protected open fun calculateMidPoint(event: MotionEvent?): PointF {
//        if (event == null || event.pointerCount < 2) {
//            midPoint.set(0f, 0f)
//            return midPoint
//        }
        val x = (event?.getRawX()?.plus(this.x))?.div(2)
        val y = (event?.getRawX()?.plus(this.y))?.div(2)
        x?.let { y?.let { it1 -> midPoint.set(it, it1) } }
        return midPoint
    }

    protected open fun calculateRotation(event: MotionEvent?): Float {
        return event?.rawX?.let { calculateRotation(it, event.rawY, this.x, this.y) }!!
    }

    protected open fun calculateRotation(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val x = (x1 - x2).toDouble()
        val y = (y1 - y2).toDouble()
        val radians = Math.atan2(y, x)
        return Math.toDegrees(radians).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    private fun calculateCenterY() {
        //double statusBarHeight = Math.ceil(25 * getContext().getResources().getDisplayMetrics().density)
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        val statusBarHeight = result.toDouble()
        centerY = this@StickerView.y +
                (this@StickerView.parent as View).y +
                statusBarHeight + this@StickerView.height.toFloat() / 2
    }

    private fun getLength(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return sqrt((y2 - y1).pow(2.0) + (x2 - x1).pow(2.0))
    }

//    private fun getRelativePos(absX: Float, absY: Float): FloatArray {
//        Log.v("ken", "getRelativePos getX:" + (this.parent as View).x)
//        Log.v("ken", "getRelativePos getY:" + (this.parent as View).y)
//        val pos = floatArrayOf(
//            absX - (this.parent as View).x,
//            absY - (this.parent as View).y
//        )
//        Log.v(TAG, "getRelativePos absY:$absY")
//        Log.v(TAG, "getRelativePos relativeY:" + pos[1])
//        return pos
//    }

//    fun setControlItemsHidden(isHidden: Boolean) {
//        if (isHidden) {
//            iv_border?.visibility = INVISIBLE
//            iv_scale?.visibility = INVISIBLE
//            iv_delete?.visibility = INVISIBLE
//            iv_flip?.visibility = INVISIBLE
//        } else {
//            iv_border?.visibility = VISIBLE
//            iv_scale?.visibility = VISIBLE
//            iv_delete?.visibility = VISIBLE
//            iv_flip?.visibility = VISIBLE
//        }
//    }

    protected val imageViewFlip: View?
        get() = ivFlip

//    private fun onScaling(scaleUp: Boolean) {}

//    private fun onRotating() {}

    private inner class BorderView : View {
        constructor(context: Context?) : super(context)
        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
        constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
            context,
            attrs,
            defStyle
        )

        val border = Rect()
        val borderPaint = Paint()

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            // Draw sticker border
            val params = this.layoutParams as LayoutParams
            Log.v(TAG, "params.leftMargin: " + params.leftMargin)

            border.left = this.left - params.leftMargin
            border.top = this.top - params.topMargin
            border.right = this.right - params.rightMargin
            border.bottom = this.bottom - params.bottomMargin
            borderPaint.strokeWidth = 0f
            borderPaint.color = Color.parseColor("#B3000000")
            borderPaint.style = Paint.Style.STROKE
            canvas.drawRect(border, borderPaint)
        }
    }

    fun hideOrShowBorderAndIcons(visibility: Boolean) {

        ivBorder?.isVisible = visibility
        ivFlip?.isVisible = visibility
        ivDelete?.isVisible = visibility
        ivScale?.isVisible = visibility
        ivRotate?.isVisible = visibility
    }

    companion object {
        const val TAG = "com.knef.stickerView"
        private const val BUTTON_SIZE_DP = 30
        private const val SELF_SIZE_DP = 100
        private fun convertDpToPixel(dp: Float, context: Context): Int {
            val resources = context.resources
            val metrics = resources.displayMetrics
            val px = dp * (metrics.densityDpi / 160f)
            return px.toInt()
        }
    }
}