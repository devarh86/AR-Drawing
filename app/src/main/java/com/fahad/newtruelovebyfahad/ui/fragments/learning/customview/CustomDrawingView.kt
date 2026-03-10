package com.fahad.newtruelovebyfahad.ui.fragments.learning.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.fahad.newtruelovebyfahad.ui.fragments.learning.customview.drawing_action.DrawingAction
import com.fahad.newtruelovebyfahad.ui.fragments.learning.customview.enums.ToolType
import com.fahad.newtruelovebyfahad.ui.fragments.learning.customview.interfaces.DrawingStateCallback
import kotlin.math.abs
import kotlin.math.min

private const val TAG = "CustomDrawingView"

class CustomDrawingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var drawingStateCallback: DrawingStateCallback? = null

    var currentTool: ToolType = ToolType.NONE

    private var imageBitmap: Bitmap? = null

    private val alpha = 150
    private val red = 65
    private val green = 12
    private val blue = 218
    private val brushColor = Color.argb(alpha, red, green, blue)

    private val path = Path()
    private val bitmapPaint = Paint(Paint.DITHER_FLAG)
    private var drawBitmap: Bitmap? = null
    private var drawCanvas: Canvas? = null

    private var lastX = 0f
    private var lastY = 0f
    private var touchX = 0f
    private var touchY = 0f
    private var showIndicator = false
    private var isRequestMade = false
    private var indicatorRadius = 25f

    private val undoStack = mutableListOf<DrawingAction>()
    private val redoStack = mutableListOf<DrawingAction>()
    private val MAX_UNDO = 50

    private val imageMatrix = Matrix()
    private val inverseMatrix = Matrix()
    private val imageDrawRect = RectF() // to track where the image is drawn

    private var magnifierEnabled = false
    private var magnifierBitmap: Bitmap? = null
    private var magnifierCanvas: Canvas? = null
    private var imageTouchX = 0f
    private var imageTouchY = 0f
    private var offsetY = 0f // set this from your slider
    private var magnifierOnLeft = true

    private val touchDotPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val brushPaint = Paint().apply {
        color = brushColor
        style = Paint.Style.STROKE
        strokeWidth = 50f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
    }

    private val eraserPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        color = Color.TRANSPARENT
        style = Paint.Style.STROKE
        strokeWidth = 50f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val fillPaint = Paint().apply {
        color = brushColor
        style = Paint.Style.FILL
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
    }

    private val whiteCirclePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun setImageBitmap(bitmap: Bitmap?) {
        imageBitmap = bitmap
        invalidate()
    }

    fun setTool(tool: ToolType) {
        currentTool = tool
        showIndicator = tool == ToolType.BRUSH || tool == ToolType.ERASER

        if (touchX == 0f && touchY == 0f) {
            touchX = width / 2f
            touchY = height / 2f
        }

        invalidate()
    }

    fun setBrushSize(size: Float) {
        brushPaint.strokeWidth = size * 2f
        fillPaint.strokeWidth = size * 2f
        invalidate()
    }

    fun setEraserSize(size: Float) {
        eraserPaint.strokeWidth = size * 2f
        invalidate()
    }

    fun setCircleSize(size: Float) {
        indicatorRadius = size
        invalidate()
    }

    fun setRequestMade(request: Boolean) {
        isRequestMade = request
        invalidate()
    }

    fun setOffsetY(value: Float) {
        offsetY = value
        invalidate()
    }

    fun setBrushAlpha(alpha: Int) {
        val newColor = Color.argb(alpha, red, green, blue)
        brushPaint.color = newColor
        fillPaint.color = newColor

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (drawBitmap == null || drawBitmap?.isRecycled == true) {
            drawBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            drawCanvas = Canvas(drawBitmap!!)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        imageBitmap?.let { bitmap ->
            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()
            val imageWidth = bitmap.width.toFloat()
            val imageHeight = bitmap.height.toFloat()

            val scale = min(viewWidth / imageWidth, viewHeight / imageHeight)
            val dx = (viewWidth - imageWidth * scale) / 2f
            val dy = (viewHeight - imageHeight * scale) / 2f

            imageMatrix.reset()
            imageMatrix.postScale(scale, scale)
            imageMatrix.postTranslate(dx, dy)

            imageDrawRect.set(0f, 0f, imageWidth, imageHeight)
            imageMatrix.mapRect(imageDrawRect)

            canvas.drawBitmap(bitmap, imageMatrix, bitmapPaint)
        }

        drawBitmap?.takeIf { !it.isRecycled }?.let {
            canvas.drawBitmap(it, 0f, 0f, bitmapPaint)
        }

        when (currentTool) {
            ToolType.BRUSH, ToolType.ERASER -> {
                val paint = if (currentTool == ToolType.BRUSH) brushPaint else eraserPaint
                canvas.drawPath(path, paint)
            }

            else -> {}
        }

        if (offsetY > 0f && showIndicator && (currentTool == ToolType.BRUSH || currentTool == ToolType.ERASER)) {
            canvas.drawCircle(touchX, touchY, 15f, touchDotPaint)
        }

        if (showIndicator && (currentTool == ToolType.BRUSH || currentTool == ToolType.ERASER)) {
            canvas.drawCircle(touchX, touchY - offsetY, indicatorRadius, whiteCirclePaint)
        }

        if (magnifierEnabled && imageTouchX >= 0 && imageTouchY >= 0) {
            drawCustomMagnifier(canvas, imageTouchX, imageTouchY)
        }
    }

    private fun drawCustomMagnifier(canvas: Canvas, imageTouchX: Float, imageTouchY: Float) {
        val magnifierSize = 300
        val sourceSize = magnifierSize * 1.3f // Increased source size to show more context

        if (magnifierBitmap == null || magnifierBitmap?.width != sourceSize.toInt()) {
            magnifierBitmap?.recycle()
            magnifierBitmap = Bitmap.createBitmap(sourceSize.toInt(), sourceSize.toInt(), Bitmap.Config.ARGB_8888)
            magnifierCanvas = Canvas(magnifierBitmap!!)
            magnifierCanvas?.drawColor(Color.WHITE) // Set a white background
        }

        magnifierBitmap?.let { bitmap ->
            magnifierCanvas?.apply {
                drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR) // Clear previous content
                imageBitmap?.let { originalBitmap ->
                    // Ensure touch coordinates are within bounds
                    val safeTouchX = imageTouchX.coerceIn(0f, originalBitmap.width - 1f)
                    val safeTouchY = imageTouchY.coerceIn(0f, originalBitmap.height - 1f)

                    // Calculate the source rectangle centered around the touch point with extra context
                    val halfSource = sourceSize / 2
                    val srcRectF = RectF(
                        safeTouchX - halfSource,
                        safeTouchY - halfSource,
                        safeTouchX + halfSource,
                        safeTouchY + halfSource
                    ).let {
                        it.left = it.left.coerceIn(0f, originalBitmap.width.toFloat())
                        it.top = it.top.coerceIn(0f, originalBitmap.height.toFloat())
                        it.right = it.right.coerceIn(0f, originalBitmap.width.toFloat())
                        it.bottom = it.bottom.coerceIn(0f, originalBitmap.height.toFloat())
                        it
                    }

                    // Convert RectF to Rect for drawBitmap
                    val srcRect = Rect(
                        srcRectF.left.toInt(),
                        srcRectF.top.toInt(),
                        srcRectF.right.toInt(),
                        srcRectF.bottom.toInt()
                    )
                    val dstRect = Rect(0, 0, sourceSize.toInt(), sourceSize.toInt())

                    // Draw the original bitmap portion
                    drawBitmap(originalBitmap, srcRect, dstRect, bitmapPaint)

                    // Draw the drawBitmap (with the path) on top with adjusted coordinates
                    drawBitmap?.let { drawnBitmap ->
                        // Adjust drawSrcRect based on the imageMatrix scale
                        val scaleX = originalBitmap.width / drawnBitmap.width.toFloat()
                        val scaleY = originalBitmap.height / drawnBitmap.height.toFloat()
                        val drawSrcRectF = RectF(
                            (srcRectF.left / scaleX),
                            (srcRectF.top / scaleY),
                            (srcRectF.right / scaleX),
                            (srcRectF.bottom / scaleY)
                        ).let {
                            it.left = it.left.coerceIn(0f, drawnBitmap.width.toFloat())
                            it.top = it.top.coerceIn(0f, drawnBitmap.height.toFloat())
                            it.right = it.right.coerceIn(0f, drawnBitmap.width.toFloat())
                            it.bottom = it.bottom.coerceIn(0f, drawnBitmap.height.toFloat())
                            it
                        }
                        val drawSrcRect = Rect(
                            drawSrcRectF.left.toInt(),
                            drawSrcRectF.top.toInt(),
                            drawSrcRectF.right.toInt(),
                            drawSrcRectF.bottom.toInt()
                        )
                        drawBitmap(drawnBitmap, drawSrcRect, dstRect, bitmapPaint)
                    }
                }
            }

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, magnifierSize, magnifierSize, true)

            val screenWidth = width.toFloat()
            val margin = 20f

            val magnifierRect = if (magnifierOnLeft) {
                RectF(margin, margin, margin + magnifierSize, margin + magnifierSize)
            } else {
                RectF(
                    screenWidth - margin - magnifierSize, margin,
                    screenWidth - margin, margin + magnifierSize
                )
            }

            val saveCount = canvas.save()
            canvas.clipPath(Path().apply {
                addRoundRect(magnifierRect, 20f, 20f, Path.Direction.CW)
            })
            scaledBitmap.let {
                canvas.drawBitmap(it, null, magnifierRect, null)
            }
            canvas.restoreToCount(saveCount)

            canvas.drawRoundRect(magnifierRect, 30f, 30f, Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 8f
                isAntiAlias = true
            })
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isRequestMade) return false
        if (currentTool == ToolType.NONE) return false

        // Drawing
        val x = event.x
        val y = event.y

        touchX = event.x
        touchY = event.y

        val drawX = touchX
        val drawY = touchY - offsetY

        // Magnifier
        val drawPoint = floatArrayOf(drawX, drawY)
        imageMatrix.invert(inverseMatrix)
        inverseMatrix.mapPoints(drawPoint)
        imageTouchX = drawPoint[0]
        imageTouchY = drawPoint[1]

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                magnifierEnabled = true

                // Magnifier to move left and right
                val touchPoint = RectF(drawX - 1f, drawY - 1f, drawX + 1f, drawY + 1f)
                val currentMagnifierRect = if (magnifierOnLeft) {
                    RectF(20f, 20f, 20f + 300f, 20f + 300f)
                } else {
                    RectF(width - 20f - 300f, 20f, width - 20f, 20f + 300f)
                }

                if (RectF.intersects(touchPoint, currentMagnifierRect)) {
                    magnifierOnLeft = !magnifierOnLeft
                }

                // magnifier path
                if (currentTool == ToolType.BRUSH || currentTool == ToolType.ERASER) {
                    val paint = if (currentTool == ToolType.BRUSH) brushPaint else eraserPaint
                    drawCanvas?.drawPath(path, paint) // Draw path in real-time
                    invalidate() // Force immediate redraw
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                magnifierEnabled = false
                if (currentTool == ToolType.BRUSH || currentTool == ToolType.ERASER) {
                    val paint = if (currentTool == ToolType.BRUSH) brushPaint else eraserPaint
                    drawCanvas?.drawPath(path, paint) // Finalize the path
                    // saveDrawingAction()
                }
            }
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = drawX
                lastY = drawY
                showIndicator = currentTool == ToolType.BRUSH || currentTool == ToolType.ERASER

                drawingStateCallback?.onDrawingStateChanged(false, hasVisibleContent())

                when (currentTool) {
                    ToolType.BRUSH, ToolType.ERASER -> {
                        path.reset()
                        path.moveTo(drawX, drawY)
                    }

                    else -> {}
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when (currentTool) {
                    ToolType.BRUSH, ToolType.ERASER -> {
                        val dx = abs(drawX - lastX)
                        val dy = abs(drawY - lastY)
                        if (dx >= 8 || dy >= 8) {
                            path.quadTo(lastX, lastY, (drawX + lastX) / 2, (drawY + lastY) / 2)
                            lastX = drawX
                            lastY = drawY
                            val paint = if (currentTool == ToolType.BRUSH) brushPaint else eraserPaint
                            drawCanvas?.drawPath(path, paint) // Update canvas in real-time
                            invalidate() // Ensure magnifier updates immediately
                        }
                    }

                    else -> {}
                }
            }

            MotionEvent.ACTION_UP -> {
                Log.d(TAG, "ACTION_UP: currentTool = $currentTool")

                when (currentTool) {
                    ToolType.ERASER -> {
                        val before = drawBitmap?.copy(Bitmap.Config.ARGB_8888, true)
                        drawCanvas?.drawPath(path, eraserPaint)

                        val after = drawBitmap
                        val erased = didBitmapChange(before, after)

                        if (erased) {
                            val eraserAction = DrawingAction.Path(Path(path), Paint(eraserPaint))
                            undoStack.add(eraserAction)
                            if (undoStack.size > MAX_UNDO) undoStack.removeAt(0)
                            redoStack.clear()
                            Log.d(TAG, "Eraser action added to undo stack")
                        } else {
                            Log.d(TAG, "Eraser made no visible change — skipped adding to undo stack")
                        }

                        path.reset()
                        before?.recycle()
                    }

                    ToolType.BRUSH -> {
                        if (!path.isEmpty) {
                            drawCanvas?.drawPath(path, brushPaint)
                            val brushAction = DrawingAction.Path(Path(path), Paint(brushPaint))
                            undoStack.add(brushAction)
                            if (undoStack.size > MAX_UNDO) undoStack.removeAt(0)
                            redoStack.clear()
                            Log.d(TAG, "Brush action added to undo stack")
                        } else {
                            Log.d(TAG, "Empty brush path — skipped")
                        }
                        path.reset()
                    }

                    else -> {}
                }

                // Delayed state update for eraser to allow canvas update
                if (currentTool == ToolType.ERASER) {
                    post { notifyDrawingStateChanged() }
                } else {
                    notifyDrawingStateChanged()
                }
            }
        }

        invalidate()
        return true
    }

    private fun didBitmapChange(before: Bitmap?, after: Bitmap?): Boolean {
        if (before == null || after == null) return false
        if (before.width != after.width || before.height != after.height) return true

        val step = 20 // every 20th pixel to reduce cost
        for (x in 0 until before.width step step) {
            for (y in 0 until before.height step step) {
                if (before.getPixel(x, y) != after.getPixel(x, y)) {
                    return true
                }
            }
        }
        return false
    }

    private fun saveDrawingAction() {
        when (currentTool) {
            ToolType.ERASER -> {
                val eraserAction = DrawingAction.Path(Path(path), Paint(eraserPaint))
                undoStack.add(eraserAction)
                if (undoStack.size > MAX_UNDO) undoStack.removeAt(0)
                redoStack.clear()
                Log.d(TAG, "Eraser action added to undo stack")
            }

            ToolType.BRUSH -> {
                if (!path.isEmpty) {
                    val brushAction = DrawingAction.Path(Path(path), Paint(brushPaint))
                    undoStack.add(brushAction)
                    if (undoStack.size > MAX_UNDO) undoStack.removeAt(0)
                    redoStack.clear()
                    Log.d(TAG, "Brush action added to undo stack")
                }
            }

            else -> {}
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(undoStack.removeAt(undoStack.size - 1))
            redrawCanvas()

            // Notify that drawing state has changed after undo
            notifyDrawingStateChanged()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(redoStack.removeAt(redoStack.size - 1))
            redrawCanvas()

            // Notify that drawing state has changed after undo
            notifyDrawingStateChanged()
        }
    }

    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun canUndo(): Boolean = undoStack.isNotEmpty()

    private fun redrawCanvas() {
        drawBitmap?.takeIf { !it.isRecycled }?.apply {
            eraseColor(Color.TRANSPARENT)
            drawCanvas = Canvas(this)
            for (action in undoStack) {
                when (action) {
                    is DrawingAction.Path -> drawCanvas?.drawPath(action.path, action.paint)
                }
            }
            invalidate()
        }
    }

    fun createMaskBitmap(): Bitmap {
        // Use imageBitmap dimensions if available, otherwise fall back to view dimensions
        val width = imageBitmap?.width ?: drawBitmap?.width ?: width
        val height = imageBitmap?.height ?: drawBitmap?.height ?: height

        // Create a new bitmap for the mask with the same dimensions as imageBitmap
        val maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Set the background to black
        maskBitmap.eraseColor(Color.BLACK)

        // Process drawBitmap if it exists
        drawBitmap?.let { bitmap ->
            // Ensure drawBitmap dimensions match the mask dimensions
            if (bitmap.width == width && bitmap.height == height) {
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

                // Create mask pixels array
                val maskPixels = IntArray(width * height)

                // Set pixels to white where drawings exist (non-transparent), black otherwise
                for (i in pixels.indices) {
                    val alpha = Color.alpha(pixels[i])
                    maskPixels[i] = if (alpha > 0) Color.WHITE else Color.BLACK
                }

                // Set the mask pixels
                maskBitmap.setPixels(maskPixels, 0, width, 0, 0, width, height)
            } else {
                // Handle dimension mismatch (e.g., scale or crop drawBitmap to match imageBitmap)
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
                val pixels = IntArray(width * height)
                scaledBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

                // Create mask pixels array
                val maskPixels = IntArray(width * height)

                // Set pixels to white where drawings exist (non-transparent), black otherwise
                for (i in pixels.indices) {
                    val alpha = Color.alpha(pixels[i])
                    maskPixels[i] = if (alpha > 0) Color.WHITE else Color.BLACK
                }

                // Set the mask pixels
                maskBitmap.setPixels(maskPixels, 0, width, 0, 0, width, height)
                scaledBitmap.recycle() // Clean up scaled bitmap
            }
        } ?: run {
            // Fallback if drawBitmap is null
            maskBitmap.eraseColor(Color.BLACK)
        }

        return maskBitmap
    }

    fun setDrawingStateCallback(callback: DrawingStateCallback?) {
        drawingStateCallback = callback
    }

    private fun notifyDrawingStateChanged() {
        val hasContent = hasDrawnContent()
        val hasVisible = hasVisibleContent()

        Log.d(TAG, "notifyDrawingStateChanged: hasContent = $hasContent")
        drawingStateCallback?.onDrawingStateChanged(hasContent, hasVisible)
    }

    fun shouldShowRemoveButton(): Boolean {
        return hasDrawnContent()
    }

    fun shouldShowEraserButton(): Boolean {
        return hasVisibleContent()
    }

    private fun hasDrawnContent(): Boolean {
        if (undoStack.isEmpty()) {
            Log.d(TAG, "hasDrawnContent: false - undo stack is empty")
            return false
        }

        // Check if we have any eraser actions
        val hasEraserActions = undoStack.any { action ->
            val isEraser = action is DrawingAction.Path &&
                    action.paint.xfermode != null &&
                    action.paint.xfermode is PorterDuffXfermode
            Log.d(TAG, "Action: ${action.javaClass.simpleName}, isEraser: $isEraser")
            isEraser
        }

        Log.d(TAG, "hasDrawnContent: hasEraserActions = $hasEraserActions")

        return if (hasEraserActions) {
            val hasVisible = hasVisibleContent()
            Log.d(TAG, "hasDrawnContent: checking visible content = $hasVisible")
            hasVisible
        } else {
            Log.d(TAG, "hasDrawnContent: true - no eraser actions")
            true
        }
    }

    // Add this method to check for visible content
    private fun hasVisibleContent(): Boolean {
        drawBitmap?.let { bitmap ->
            if (bitmap.isRecycled) {
                Log.d(TAG, "hasVisibleContent: bitmap is recycled")
                return false
            }

            Log.d(TAG, "hasVisibleContent: checking bitmap ${bitmap.width}x${bitmap.height}")

            // Simple approach - check every 20th pixel
            val width = bitmap.width
            val height = bitmap.height
            val step = 20

            var pixelsChecked = 0
            var visiblePixels = 0

            for (x in 0 until width step step) {
                for (y in 0 until height step step) {
                    pixelsChecked++
                    val pixel = bitmap.getPixel(x, y)
                    val alpha = Color.alpha(pixel)
                    if (alpha > 0) {
                        visiblePixels++
                    }
                }
            }

            Log.d(TAG, "hasVisibleContent: checked $pixelsChecked pixels, found $visiblePixels visible")
            return visiblePixels > 0
        }

        Log.d(TAG, "hasVisibleContent: drawBitmap is null")
        return false
    }

    fun clearDrawing() {
        undoStack.clear()
        redoStack.clear()
        redrawCanvas()
        notifyDrawingStateChanged()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        drawBitmap?.recycle()
        drawBitmap = null
        imageBitmap = null
        magnifierBitmap?.recycle()
        magnifierBitmap = null
    }
}