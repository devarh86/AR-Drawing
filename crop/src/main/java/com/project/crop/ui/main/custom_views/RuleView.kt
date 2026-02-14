package com.project.crop.ui.main.custom_views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import com.project.crop.R

class RuleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var fromUser: Boolean = true
    private val TOUCH_SLOP: Int
    private val MIN_FLING_VELOCITY: Int
    private val MAX_FLING_VELOCITY: Int
    private var bgColor = 0
    private var gradationColor = 0
    private var shortLineWidth = 0f
    private var longLineWidth = 0f
    private var shortGradationLen = 0f
    private var longGradationLen = 0f
    private var textColor = 0
    private var textSize = 0f
    private var indicatorLineColor = 0
    private var indicatorLineWidth = 0f
    private var indicatorLineLen = 0f
    var minValue = 0f
        private set
    var maxValue = 0f
        private set
    private var currentValue = 0f
    private var gradationUnit = 0f
    private var numberPerCount = 0
    private var gradationGap = 0f
    private var gradationNumberGap = 0f
    private var mMinNumber = 0
    private var mMaxNumber = 0
    private var mCurrentNumber = 0
    private var mNumberRangeDistance = 0f
    private var mNumberUnit = 0
    private var mCurrentDistance = 0f
    private var mWidthRangeNumber = 0
    private var mPaint: Paint? = null
    private var mTextPaint: TextPaint? = null
    private var mScroller: Scroller? = null
    private var mVelocityTracker: VelocityTracker? = null
    private var mWidth = 0
    private var mHalfWidth = 0
    private var mHeight = 0
    private var mDownX = 0
    private var mLastX = 0
    private var mLastY = 0
    private var isMoved = false
    private var mValueChangedListener: OnValueChangedListener? = null

    interface OnValueChangedListener {
        fun onValueChanged(value: Float)
        fun onValueChangedComplete(value: Float)
    }

    enum class Theme {
        LIGHT, DARK
    }

    private var currentTheme: Theme = Theme.LIGHT


    init {
        initAttrs(context, attrs)

        val viewConfiguration = ViewConfiguration.get(context)
        TOUCH_SLOP = viewConfiguration.scaledTouchSlop
        MIN_FLING_VELOCITY = viewConfiguration.scaledMinimumFlingVelocity
        MAX_FLING_VELOCITY = viewConfiguration.scaledMaximumFlingVelocity
        convertValue2Number()
        init(context)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.RuleView)
        bgColor = ta.getColor(R.styleable.RuleView_zjun_bgColor, Color.parseColor("#f5f8f5"))
        gradationColor = ta.getColor(R.styleable.RuleView_zjun_gradationColor, Color.LTGRAY)
        shortLineWidth =
            ta.getDimension(R.styleable.RuleView_gv_shortLineWidth, dp2px(1f).toFloat())
        shortGradationLen =
            ta.getDimension(R.styleable.RuleView_gv_shortGradationLen, dp2px(16f).toFloat())
        longGradationLen =
            ta.getDimension(R.styleable.RuleView_gv_longGradationLen, shortGradationLen * 2)
        longLineWidth = ta.getDimension(R.styleable.RuleView_gv_longLineWidth, shortLineWidth * 2)
        textColor = ta.getColor(R.styleable.RuleView_zjun_textColor, Color.BLACK)
        textSize = ta.getDimension(R.styleable.RuleView_zjun_textSize, sp2px(14f).toFloat())
        indicatorLineColor =
            ta.getColor(R.styleable.RuleView_zjun_indicatorLineColor, Color.parseColor("#48b975"))
        indicatorLineWidth =
            ta.getDimension(R.styleable.RuleView_zjun_indicatorLineWidth, dp2px(3f).toFloat())
        indicatorLineLen =
            ta.getDimension(R.styleable.RuleView_gv_indicatorLineLen, dp2px(35f).toFloat())
        minValue = ta.getFloat(R.styleable.RuleView_gv_minValue, 0f)
        maxValue = ta.getFloat(R.styleable.RuleView_gv_maxValue, 100f)
        currentValue = ta.getFloat(R.styleable.RuleView_gv_currentValue, 0f)
        gradationUnit = ta.getFloat(R.styleable.RuleView_gv_gradationUnit, .1f)
        numberPerCount = ta.getInt(R.styleable.RuleView_gv_numberPerCount, 10)
        gradationGap = ta.getDimension(R.styleable.RuleView_gv_gradationGap, dp2px(10f).toFloat())
        gradationNumberGap =
            ta.getDimension(R.styleable.RuleView_gv_gradationNumberGap, dp2px(8f).toFloat())
        ta.recycle()

        applyTheme(currentTheme)
    }

    fun toggleTheme() {
        val newTheme = when (currentTheme) {
            Theme.LIGHT -> Theme.DARK
            Theme.DARK -> Theme.LIGHT
        }
        applyTheme(newTheme)
    }

    private fun applyTheme(theme: Theme) {
        kotlin.runCatching {
            currentTheme = theme
            when (theme) {

                Theme.LIGHT -> {
                    kotlin.runCatching {
                        bgColor = Color.parseColor("#F6F6F6")
                        gradationColor = Color.parseColor("#343F56")
                        textColor = Color.parseColor("#343F56")
                        indicatorLineColor = Color.parseColor("#3A55FF")
                    }
                }

                Theme.DARK -> {
                    kotlin.runCatching {
                        bgColor = Color.parseColor("#000000")
                        gradationColor = Color.parseColor("#ffffff")
                        textColor = Color.parseColor("#ffffff")
                        indicatorLineColor = Color.parseColor("#3A55FF")
                    }
                }
            }

            mPaint?.color = gradationColor
            mTextPaint?.color = textColor

            invalidate()  // Redraw the view with the new theme
        }
    }


    /**
     * 初始化
     */
    private fun init(context: Context) {
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint?.strokeWidth = shortLineWidth
        mTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint?.textSize = textSize
        mTextPaint?.color = textColor
        mScroller = Scroller(context)
    }

    /**
     * 把真实数值转换成绘制数值
     * 为了防止float的精度丢失，把minValue、maxValue、currentValue、gradationUnit都放大10倍
     */
    private fun convertValue2Number() {
        mMinNumber = (minValue * 10).toInt()
        mMaxNumber = (maxValue * 10).toInt()
        mCurrentNumber = (currentValue * 10).toInt()
        mNumberUnit = (gradationUnit * 10).toInt()
        mCurrentDistance = (mCurrentNumber - mMinNumber) / mNumberUnit * gradationGap
        mNumberRangeDistance = (mMaxNumber - mMinNumber) / mNumberUnit * gradationGap
        if (mWidth != 0) {
            mWidthRangeNumber = (mWidth / gradationGap * mNumberUnit).toInt()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        kotlin.runCatching {
            mWidth = calculateSize(true, widthMeasureSpec)
            mHeight = calculateSize(false, heightMeasureSpec)
            mHalfWidth = mWidth shr 1
            if (mWidthRangeNumber == 0) {
                mWidthRangeNumber = (mWidth / gradationGap * mNumberUnit).toInt()
            }
            setMeasuredDimension(mWidth, mHeight)
        }

    }

    private fun calculateSize(isWidth: Boolean, spec: Int): Int {
        val mode = MeasureSpec.getMode(spec)
        val size = MeasureSpec.getSize(spec)
        var realSize = size
        when (mode) {
            MeasureSpec.EXACTLY -> {}
            MeasureSpec.AT_MOST -> if (!isWidth) {
                val defaultContentSize = dp2px(30f)
                realSize = Math.min(realSize, defaultContentSize)
            }

            MeasureSpec.UNSPECIFIED -> {}
            else -> {}
        }
        logD("isWidth=%b, mode=%d, size=%d, realSize=%d", isWidth, mode, size, realSize)
        return realSize
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        val x = event.x.toInt()
        val y = event.y.toInt()
        logD("onTouchEvent: action=%d", action)
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker?.addMovement(event)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                fromUser = true
                mScroller?.forceFinished(true)
                mDownX = x
                isMoved = false
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = x - mLastX
                if (!isMoved) {
                    val dy = y - mLastY
                    if (Math.abs(dx) < Math.abs(dy) || Math.abs(x - mDownX) < TOUCH_SLOP) {
                        return false
                    }
                    isMoved = true
                }
                mCurrentDistance += 1
                mCurrentDistance += -dx.toFloat()
                Log.i("onTouchEvent", "onTouchEvent: $dx")
                calculateValue()
            }

            MotionEvent.ACTION_UP -> {
                mVelocityTracker?.computeCurrentVelocity(1000, MAX_FLING_VELOCITY.toFloat())
                val xVelocity = mVelocityTracker?.xVelocity?.toInt()
                xVelocity?.let {
                    if (Math.abs(xVelocity) >= MIN_FLING_VELOCITY) {
                        mScroller?.fling(
                            mCurrentDistance.toInt(), 0, -xVelocity, 0,
                            0, mNumberRangeDistance.toInt(), 0, 0
                        )
                        invalidate()
                    } else {
                        scrollToGradation()
                    }
                }
                calculateValueFinalized()
            }

            else -> {}
        }
        mLastX = x
        mLastY = y
        return true
    }

    private fun calculateValue() {
        mCurrentDistance = Math.min(Math.max(mCurrentDistance, 0f), mNumberRangeDistance)
        mCurrentNumber = mMinNumber + (mCurrentDistance / gradationGap).toInt() * mNumberUnit
        currentValue = mCurrentNumber / 10f
        logD(
            "calculateValue: mCurrentDistance=%f, mCurrentNumber=%d, currentValue=%f",
            mCurrentDistance, mCurrentNumber, currentValue
        )
        if (fromUser)
            mValueChangedListener?.onValueChanged(currentValue)
        invalidate()
    }

    private fun calculateValueFinalized() {
        mCurrentDistance = Math.min(Math.max(mCurrentDistance, 0f), mNumberRangeDistance)
        mCurrentNumber = mMinNumber + (mCurrentDistance / gradationGap).toInt() * mNumberUnit
        currentValue = mCurrentNumber / 10f
        if (fromUser)
            mValueChangedListener?.onValueChangedComplete(currentValue)
    }

    private fun scrollToGradation() {
        mCurrentNumber = mMinNumber + Math.round(mCurrentDistance / gradationGap) * mNumberUnit
        mCurrentNumber = Math.min(Math.max(mCurrentNumber, mMinNumber), mMaxNumber)
        mCurrentDistance = (mCurrentNumber - mMinNumber) / mNumberUnit * gradationGap
        currentValue = mCurrentNumber / 10f
        logD(
            "scrollToGradation: mCurrentDistance=%f, mCurrentNumber=%d, currentValue=%f",
            mCurrentDistance, mCurrentNumber, currentValue
        )
        if (fromUser)
            mValueChangedListener?.onValueChanged(currentValue)
        invalidate()
    }

    override fun computeScroll() {
        if (mScroller?.computeScrollOffset() == true) {
            if (mScroller?.currX != mScroller?.finalX) {
                mCurrentDistance = mScroller?.currX?.toFloat() ?: 0f
                calculateValue()
            } else {
                scrollToGradation()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(bgColor)
        drawGradation(canvas)
        drawIndicator(canvas)
    }

    private fun drawGradation(canvas: Canvas) {
        mPaint?.color = gradationColor
        mPaint?.strokeWidth = shortLineWidth
        mPaint?.let { canvas.drawLine(0f, shortLineWidth * .5f, mWidth.toFloat(), 0f, it) }
        var startNum =
            (mCurrentDistance.toInt() - mHalfWidth) / gradationGap.toInt() * mNumberUnit + mMinNumber
        val expendUnit = mNumberUnit shl 1
        startNum -= expendUnit
        if (startNum < mMinNumber) {
            startNum = mMinNumber
        }
        var rightMaxNum = startNum + expendUnit + mWidthRangeNumber + expendUnit
        if (rightMaxNum > mMaxNumber) {
            rightMaxNum = mMaxNumber
        }
        var distance =
            mHalfWidth - (mCurrentDistance - (startNum - mMinNumber) / mNumberUnit * gradationGap)
        val perUnitCount = mNumberUnit * numberPerCount
        logD(
            "drawGradation: startNum=%d, rightNum=%d, perUnitCount=%d",
            startNum, rightMaxNum, perUnitCount
        )
        while (startNum <= rightMaxNum) {
            logD("drawGradation: startNum=%d", startNum)
            if (startNum % perUnitCount == 0) {
                mPaint?.strokeWidth = longLineWidth
                mPaint?.let {
                    canvas.drawLine(distance, 0f, distance, longGradationLen, it)
                }
                val fNum = startNum / 10f
                var text = fNum.toString()
                logD("drawGradation: text=%s", text)
                if (text.endsWith(".0")) {
                    text = text.substring(0, text.length - 2)
                }
                val textWidth = mTextPaint?.measureText(text)
                textWidth?.let {
                    mTextPaint?.let { it1 ->
                        canvas.drawText(
                            text,
                            distance - it * .5f,
                            longGradationLen + gradationNumberGap + textSize,
                            it1
                        )
                    }
                }
            } else {
                mPaint?.strokeWidth = shortLineWidth
                mPaint?.let {
                    canvas.drawLine(distance, 0f, distance, shortGradationLen, it)
                }
            }
            startNum += mNumberUnit
            distance += gradationGap
        }
    }

    private fun drawIndicator(canvas: Canvas) {
        mPaint?.color = indicatorLineColor
        mPaint?.strokeWidth = indicatorLineWidth
        mPaint?.strokeCap = Paint.Cap.ROUND
        mPaint?.let {
            canvas.drawLine(
                mHalfWidth.toFloat(), 0f, mHalfWidth.toFloat(), indicatorLineLen,
                it
            )
        }
        mPaint?.strokeCap = Paint.Cap.BUTT
    }

    private fun dp2px(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
            .toInt()
    }

    private fun sp2px(sp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
            .toInt()
    }

    private fun logD(format: String, vararg args: Any) {
        if (LOG_ENABLE) {
            Log.d("GradationView", String.format("zjun@$format", *args))
        }
    }

    fun setCurrentValue(currentValue: Float, fromUser: Boolean) {
        this.fromUser = fromUser
        require(!(currentValue < minValue || currentValue > maxValue)) {
            String.format(
                "The currentValue of %f is out of range: [%f, %f]",
                currentValue, minValue, maxValue
            )
        }
        if (mScroller?.isFinished == false) {
            mScroller?.forceFinished(true)
        }
        this.currentValue = currentValue
        mCurrentNumber = (this.currentValue * 10).toInt()
        val newDistance = (mCurrentNumber - mMinNumber) / mNumberUnit * gradationGap
        val dx = (newDistance - mCurrentDistance).toInt()
        val duration = dx * 2000 / mNumberRangeDistance.toInt()
        mScroller?.startScroll(mCurrentDistance.toInt(), 0, dx, duration)
        postInvalidate()
    }

    fun getCurrentValue(): Float {
        return currentValue
    }

    @SuppressLint("DefaultLocale")
    fun setValue(minValue: Float, maxValue: Float, curValue: Float, unit: Float, perCount: Int) {
        if (minValue > maxValue || curValue < minValue || curValue <= maxValue) {
            String.format(
                "The given values are invalid, check firstly: " +
                        "minValue=%f, maxValue=%f, curValue=%s", minValue, maxValue, curValue
            )
        }
        if (mScroller?.isFinished != true) {
            mScroller?.forceFinished(true)
        }
        this.minValue = minValue
        this.maxValue = maxValue
        currentValue = curValue
        gradationUnit = unit
        numberPerCount = perCount
        convertValue2Number()
        if (fromUser)
            mValueChangedListener?.onValueChanged(currentValue)
        postInvalidate()
    }

    fun setOnValueChangedListener(listener: OnValueChangedListener?) {
        mValueChangedListener = listener
    }

    companion object {
        private const val LOG_ENABLE = true
    }
}