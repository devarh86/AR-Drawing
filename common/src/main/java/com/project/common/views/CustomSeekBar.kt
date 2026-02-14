package com.project.common.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.widget.SeekBar


class CustomSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatSeekBar(context, attrs, defStyleAttr) {

    private var baseColor: Int = Color.RED
    private lateinit var opacityGradient: Shader
    private lateinit var brightnessGradient: Shader
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val thumbStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4A90E2")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    // Make track height smaller relative to the view height
    private val trackHeightRatio = 4f // Track takes 1/4th of the view height
    // Make thumb larger than track height
    private val thumbTrackRatio = 1.8f // Thumb is 1.8 times the track height

    private var isOpacityBar = true
    private var colorChangeListener: OnColorChangeListener? = null

    interface OnColorChangeListener {
        fun onColorChanged(progress: Int, color: Int)
    }

    init {
        thumb = null // Remove default thumb
        progressDrawable = null // Remove default progress drawable

        // Add padding for the larger thumb
        val thumbRadius = (height / trackHeightRatio * thumbTrackRatio / 2f).toInt()
        setPadding(
            paddingLeft + thumbRadius,
            paddingTop,
            paddingRight + thumbRadius,
            paddingBottom
        )

        setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val newColor = calculateNewColor(progress)
                colorChangeListener?.onColorChanged(progress, newColor)
                invalidate()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    fun setOnColorChangeListener(listener: OnColorChangeListener) {
        colorChangeListener = listener
    }

    fun setAsOpacityBar(color: Int?) {
        color?.let {
            baseColor = it
        }
        isOpacityBar = true
        createOpacityGradient()
        invalidate()
    }

    fun setAsBrightnessBar(color: Int?) {
        color?.let {
            baseColor = it
        }
        isOpacityBar = false
        createBrightnessGradient()
        invalidate()
    }

    private fun calculateNewColor(progress: Int): Int {
        return if (isOpacityBar) {
            Color.argb(
                progress,
                Color.red(baseColor),
                Color.green(baseColor),
                Color.blue(baseColor)
            )
        } else {
            val factor = progress / 255f
            val red = (Color.red(baseColor) * factor).toInt().coerceIn(0, 255)
            val green = (Color.green(baseColor) * factor).toInt().coerceIn(0, 255)
            val blue = (Color.blue(baseColor) * factor).toInt().coerceIn(0, 255)
            Color.rgb(red, green, blue)
        }
    }

    private fun createOpacityGradient() {
        val transparentColor = Color.argb(0, Color.red(baseColor),
            Color.green(baseColor), Color.blue(baseColor))
        opacityGradient = LinearGradient(
            paddingLeft.toFloat(), 0f,
            (width - paddingRight).toFloat(), 0f,
            transparentColor, baseColor,
            Shader.TileMode.CLAMP
        )
    }

    private fun createBrightnessGradient() {
        val darkColor = Color.BLACK
        brightnessGradient = LinearGradient(
            paddingLeft.toFloat(), 0f,
            (width - paddingRight).toFloat(), 0f,
            darkColor, baseColor,
            Shader.TileMode.CLAMP
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (isOpacityBar) {
            createOpacityGradient()
        } else {
            createBrightnessGradient()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val trackHeight = height / trackHeightRatio
        val trackRadius = trackHeight / 2
        val thumbRadius = trackHeight * thumbTrackRatio / 2

        // Draw background track with gradient
        paint.shader = if (isOpacityBar) opacityGradient else brightnessGradient
        canvas.drawRoundRect(
            paddingLeft.toFloat(),
            (height / 2 - trackHeight / 2),
            (width - paddingRight).toFloat(),
            (height / 2 + trackHeight / 2),
            trackRadius,
            trackRadius,
            paint
        )

        // Calculate and draw thumb
        val thumbX = paddingLeft + ((width - paddingLeft - paddingRight) * (progress / max.toFloat()))
        val thumbY = height / 2f

        // Draw thumb shadow
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.setShadowLayer(6f, 0f, 3f, Color.parseColor("#30000000"))
        canvas.drawCircle(thumbX, thumbY, thumbRadius, paint)
        paint.clearShadowLayer()

        // Draw thumb
        canvas.drawCircle(thumbX, thumbY, thumbRadius, thumbPaint)
        canvas.drawCircle(thumbX, thumbY, thumbRadius - 2f, thumbStrokePaint)
    }
}