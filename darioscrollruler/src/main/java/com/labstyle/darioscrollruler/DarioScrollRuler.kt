package com.labstyle.darioscrollruler

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

class DarioScrollRuler @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyle, defStyleRes) {
    companion object {
        private const val MINOR_DIVISIONS_PER_UNIT = 10f
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val baselinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val overScroller = OverScroller(context)
    private val viewConfig = ViewConfiguration.get(context)

    private var velocityTracker: VelocityTracker? = null
    private var lastTouchX = 0f
    private var isFlinging = false
    private var isSnapping = false
    private var lastBroadcastValue = Float.NaN

    var scrollListener: ScrollRulerListener? = null
    var minValue = 0f
        private set
    var maxValue = 100f
        private set
    var initialValue = (minValue + maxValue) / 2f
        private set
    var currentPositionValue: Float = 0f
        private set

    private var tickColor: Int
    private var indicatorColor: Int
    private var rulerBackgroundColor: Int
    private var edgeColor: Int

    private var majorTickSpacingPx: Float
    private var majorTickHeightPx: Float
    private var mediumTickHeightPx: Float
    private var minorTickHeightPx: Float
    private var indicatorWidthPx: Float
    private var indicatorHeightPx: Float
    private var baselinePaddingBottomPx: Float
    private var stepValue: Float

    init {
        val defaultTickColor = ContextCompat.getColor(context, R.color.ruler_marker_small_medium)
        val defaultIndicatorColor = ContextCompat.getColor(context, R.color.ruler_indicator_bar)
        val defaultBackgroundColor = ContextCompat.getColor(context, R.color.ruler_background_panel)
        val defaultEdgeColor = ContextCompat.getColor(context, R.color.ruler_edge_blue)

        val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.DarioScrollRuler)
        val startMinValue = attrsArray.getFloat(R.styleable.DarioScrollRuler_minValue, minValue)
        val startMaxValue = attrsArray.getFloat(R.styleable.DarioScrollRuler_maxValue, maxValue)
        val startInitialValue = attrsArray.getFloat(R.styleable.DarioScrollRuler_initialValue, initialValue)

        tickColor = attrsArray.getColor(R.styleable.DarioScrollRuler_tickColor, defaultTickColor)
        indicatorColor = attrsArray.getColor(R.styleable.DarioScrollRuler_indicatorColor, defaultIndicatorColor)
        rulerBackgroundColor =
            attrsArray.getColor(R.styleable.DarioScrollRuler_rulerBackgroundColor, defaultBackgroundColor)
        edgeColor = defaultEdgeColor

        majorTickSpacingPx = attrsArray.getDimension(R.styleable.DarioScrollRuler_tickSpacing, dpToPx(38f))
        majorTickHeightPx = attrsArray.getDimension(R.styleable.DarioScrollRuler_majorTickHeight, dpToPx(46f))
        mediumTickHeightPx = attrsArray.getDimension(R.styleable.DarioScrollRuler_mediumTickHeight, dpToPx(30f))
        minorTickHeightPx = attrsArray.getDimension(R.styleable.DarioScrollRuler_minorTickHeight, dpToPx(18f))
        indicatorWidthPx = attrsArray.getDimension(R.styleable.DarioScrollRuler_indicatorWidth, dpToPx(2f))
        indicatorHeightPx = attrsArray.getDimension(R.styleable.DarioScrollRuler_indicatorHeight, dpToPx(46f))
        stepValue = attrsArray.getFloat(R.styleable.DarioScrollRuler_valueStep, 1f).coerceAtLeast(0.1f)
        attrsArray.recycle()

        baselinePaddingBottomPx = dpToPx(8f)

        tickPaint.color = tickColor
        tickPaint.strokeWidth = dpToPx(1f)
        tickPaint.strokeCap = Paint.Cap.ROUND

        indicatorPaint.color = indicatorColor
        indicatorPaint.strokeWidth = indicatorWidthPx
        indicatorPaint.strokeCap = Paint.Cap.ROUND

        baselinePaint.color = tickColor
        baselinePaint.strokeWidth = dpToPx(1f)
        baselinePaint.alpha = 95

        edgePaint.color = edgeColor
        edgePaint.strokeWidth = dpToPx(1.5f)

        isClickable = true
        reload(startMinValue, startMaxValue, startInitialValue)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = max(
            dpToPx(70f),
            max(majorTickHeightPx, indicatorHeightPx) + baselinePaddingBottomPx * 2f
        ).roundToInt()
        val measuredWidth = resolveSize(suggestedMinimumWidth, widthMeasureSpec)
        val measuredHeight = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val centerX = viewWidth / 2f
        val baselineY = height / 2f
        val pxPerValue = pxPerValue()
        val minorStepValue = 1f / MINOR_DIVISIONS_PER_UNIT

        canvas.drawColor(rulerBackgroundColor)

        val visibleValueHalfRange = centerX / pxPerValue + 2f
        val startValue = currentPositionValue - visibleValueHalfRange
        val endValue = currentPositionValue + visibleValueHalfRange
        val startTickIndex = floor(startValue / minorStepValue).toInt() - 5
        val endTickIndex = ceil(endValue / minorStepValue).toInt() + 5
        var minTickX = Float.MAX_VALUE
        var maxTickX = Float.MIN_VALUE

        for (tickIndex in startTickIndex..endTickIndex) {
            val tickValue = tickIndex * minorStepValue
            if (tickValue < minValue || tickValue > maxValue) continue

            val x = centerX + ((tickValue - currentPositionValue) * pxPerValue)
            if (x < -2f || x > viewWidth + 2f) continue
            if (x < minTickX) minTickX = x
            if (x > maxTickX) maxTickX = x

            val tickHeight = when {
                tickIndex % 10 == 0 -> majorTickHeightPx
                tickIndex % 5 == 0 -> mediumTickHeightPx
                else -> minorTickHeightPx
            }

            val top = baselineY - (tickHeight / 2f)
            val bottom = baselineY + (tickHeight / 2f)
            canvas.drawLine(x, top, x, bottom, tickPaint)
        }
        if (minTickX != Float.MAX_VALUE && maxTickX != Float.MIN_VALUE) {
            canvas.drawLine(minTickX, baselineY, maxTickX, baselineY, baselinePaint)
        }

        val indicatorTopY = baselineY - (indicatorHeightPx / 2f)
        val indicatorBottomY = baselineY + (indicatorHeightPx / 2f)
        canvas.drawLine(centerX, indicatorTopY, centerX, indicatorBottomY, indicatorPaint)

        val edgeInset = dpToPx(8f)
        canvas.drawLine(0f, edgeInset, 0f, height - edgeInset, edgePaint)
        val rightEdgeX = viewWidth - edgePaint.strokeWidth
        canvas.drawLine(rightEdgeX, edgeInset, rightEdgeX, height - edgeInset, edgePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                stopAnimations()
                ensureVelocityTracker()
                velocityTracker?.addMovement(event)
                lastTouchX = event.x
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(event)
                val dx = event.x - lastTouchX
                lastTouchX = event.x
                updateValueByDragDelta(dx)
                return true
            }

            MotionEvent.ACTION_UP -> {
                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000, viewConfig.scaledMaximumFlingVelocity.toFloat())
                val xVelocity = velocityTracker?.xVelocity ?: 0f
                recycleVelocityTracker()

                if (abs(xVelocity) >= viewConfig.scaledMinimumFlingVelocity) {
                    flingWithVelocity(-xVelocity)
                } else {
                    snapToNearestStep(animated = true)
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                recycleVelocityTracker()
                snapToNearestStep(animated = true)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun computeScroll() {
        super.computeScroll()
        if (overScroller.computeScrollOffset()) {
            val value = scrollXToValue(overScroller.currX.toFloat())
            setCurrentValue(value, notifyListener = true)
            postInvalidateOnAnimation()
            return
        }

        if (isFlinging) {
            isFlinging = false
            snapToNearestStep(animated = true)
            return
        }

        if (isSnapping) {
            isSnapping = false
            val snapped = quantizeToStep(currentPositionValue)
            setCurrentValue(snapped, notifyListener = true)
        }
    }

    fun scrollToValue(value: Float) {
        stopAnimations()
        val snappedTarget = quantizeToStep(clampValue(value))
        setCurrentValue(snappedTarget, notifyListener = true)
        invalidate()
    }

    fun reload(min: Float, max: Float, initValue: Float) {
        if (min <= max) {
            minValue = min
            maxValue = max
        } else {
            minValue = max
            maxValue = min
        }

        initialValue = clampValue(initValue)
        lastBroadcastValue = Float.NaN
        setCurrentValue(initialValue, notifyListener = true)
        invalidate()
    }

    private fun updateValueByDragDelta(dx: Float) {
        if (dx == 0f) return
        val deltaValue = -dx / pxPerValue()
        setCurrentValue(currentPositionValue + deltaValue, notifyListener = true)
        postInvalidateOnAnimation()
    }

    private fun flingWithVelocity(xVelocity: Float) {
        val startX = valueToScrollX(currentPositionValue).roundToInt()
        val minX = valueToScrollX(minValue).roundToInt()
        val maxX = valueToScrollX(maxValue).roundToInt()

        overScroller.fling(startX, 0, xVelocity.roundToInt(), 0, minX, maxX, 0, 0)
        isFlinging = true
        isSnapping = false
        postInvalidateOnAnimation()
    }

    private fun snapToNearestStep(animated: Boolean) {
        val snappedValue = quantizeToStep(currentPositionValue)
        if (abs(snappedValue - currentPositionValue) < 0.001f) {
            setCurrentValue(snappedValue, notifyListener = true)
            invalidate()
            return
        }

        if (!animated) {
            setCurrentValue(snappedValue, notifyListener = true)
            invalidate()
            return
        }

        val startX = valueToScrollX(currentPositionValue).roundToInt()
        val targetX = valueToScrollX(snappedValue).roundToInt()
        overScroller.startScroll(startX, 0, targetX - startX, 0, 160)
        isFlinging = false
        isSnapping = true
        postInvalidateOnAnimation()
    }

    private fun setCurrentValue(value: Float, notifyListener: Boolean) {
        val clamped = clampValue(value)
        if (abs(clamped - currentPositionValue) < 0.0001f) {
            if (notifyListener) broadcastIfNeeded()
            return
        }
        currentPositionValue = clamped
        if (notifyListener) broadcastIfNeeded()
    }

    private fun broadcastIfNeeded() {
        val callbackValue = quantizeToStep(currentPositionValue)
        if (lastBroadcastValue.isNaN() || abs(callbackValue - lastBroadcastValue) >= 0.0001f) {
            lastBroadcastValue = callbackValue
            scrollListener?.onRulerScrolled(callbackValue)
        }
    }

    private fun clampValue(value: Float): Float = value.coerceIn(minValue, maxValue)

    private fun quantizeToStep(value: Float): Float {
        val safeStep = stepValue.coerceAtLeast(0.1f)
        val steps = ((value - minValue) / safeStep).roundToInt()
        return clampValue((steps * safeStep) + minValue)
    }

    private fun pxPerValue(): Float = majorTickSpacingPx.coerceAtLeast(dpToPx(12f))

    private fun valueToScrollX(value: Float): Float = (clampValue(value) - minValue) * pxPerValue()

    private fun scrollXToValue(scrollX: Float): Float = minValue + (scrollX / pxPerValue())

    private fun stopAnimations() {
        if (!overScroller.isFinished) {
            overScroller.abortAnimation()
        }
        isFlinging = false
        isSnapping = false
    }

    private fun ensureVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        } else {
            velocityTracker?.clear()
        }
    }

    private fun recycleVelocityTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density
}
