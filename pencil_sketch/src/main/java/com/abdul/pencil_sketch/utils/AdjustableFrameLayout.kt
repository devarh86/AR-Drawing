package com.abdul.pencil_sketch.utils

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import com.project.common.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.roundToInt

class AdjustableFrameLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    var userImage: ZoomableImageView? = null
    var effectImage: ZoomableImageView? = null
    private var fgImage: View? = null
    var fromAdjustAndFilter: Boolean = false
    var childVisible: Boolean = true
    private var lastHeight: Int = 0
    private var lastWidth: Int = 0
    private var job: Job? = null
    private val imageViewList: CopyOnWriteArrayList<ZoomableImageView> = CopyOnWriteArrayList()
    var fromEdit = false
    var bgImage: ImageView? = null
    var fromFragment = false
    var isDragEnable = false
    var isInChildBounds = false
    var listener: FrameClicks? = null
    private var isListenerSet = false

    interface FrameClicks {
        fun onFrameClick()
    }

//    override fun dispatchDraw(canvas: Canvas?) {
//        super.dispatchDraw(canvas)
//
//        userImage?.let { bgImage ->
//            effectImage?.let { fgImage ->
//                val saveCount = canvas?.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
//
//                // Draw background image
//                canvas?.let { bgImage.draw(it) }
//
//                // Apply blending mode
//                val paint = Paint()
//                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
//
//                // Get the transformation matrix for the foreground image
////                val matrix = fgImage.imageMatrix
////                canvas?.concat(matrix)
//
//                // Draw foreground image
//                fgImage.draw
//                canvas?.let { fgImage.draw(it, paint) }
//
//                // Restore the canvas
//                saveCount?.let { canvas.restoreToCount(it) }
//            }
//        }
//    }

//    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        super.onLayout(changed, left, top, right, bottom)
//
//        if (!isListenerSet) {
//
//            try {
//                val lastChild = getChildAt(childCount - 1)
//
//                if (lastChild.tag != "fg_image")
//                    return
//
//                fgImage = lastChild
//
//                if (lastChild is ImageView && !isListenerSet) {
//                    lastChild.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
//                        val width = right - left
//                        val height = bottom - top
//                        if (lastWidth != width || lastHeight != height) {
//                            lastWidth = width
//                            lastHeight = height
//                            if (!isDragEnable) changeDimensions()
//                        }
//                    }
//
//                    isListenerSet = true
//                }
//            } catch (ex: Exception) {
//                Log.e("error", "onLayout: ", ex)
//            }
//        }
//    }

    fun setFrameListener(listener: FrameClicks) {
        this.listener = listener
    }

//    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//        super.onSizeChanged(w, h, oldw, oldh)
//        lastHeight = h
//        lastWidth = w
////        if (fromEdit) {
//        changeDimensions()
//        }
//    }

//    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
//        ev?.let {
//            if (ev.action == MotionEvent.ACTION_DOWN || ev.action == MotionEvent.ACTION_POINTER_DOWN) {
//                isInChildBounds = false
//                imageViewList.forEachIndexed { index, zoomableImageView ->
//                    if (ev.x in zoomableImageView.x..zoomableImageView.width.toFloat() + zoomableImageView.x &&
//                        ev.y in zoomableImageView.y..zoomableImageView.height.toFloat() + zoomableImageView.y
//                    ) {
//                        isInChildBounds = true
//                        return@forEachIndexed
//                    }
//                }
//            }
//        }
//        if (!isInChildBounds) {
//            listener?.onFrameClick()
//        }
//        return !isInChildBounds
//    }

    fun addImgView(img: ZoomableImageView, index: Int) {
        imageViewList.add(img)
        bgImage = img
        this.addView(img, index)
    }

    fun removeImgView(img: ZoomableImageView) {
        imageViewList.remove(img)
        removeView(img)
    }

//    fun replaceImgView(img: ZoomableImageView, index: Int) {
//        this.removeView(img)
//        this.addView(img, index)
//    }

    fun hideViews() {
        try {
            if (!childVisible)
                return

            childVisible = false
//            alpha = 0.95f
            imageViewList.forEach {
                it.isVisible = false
            }
        } catch (ex: Exception) {
            Log.e("error", "hideViews: ", ex)
        }
    }

    fun hideViewsPipAndShape() {
        try {
            childVisible = false
            imageViewList.forEach {
                it.isVisible = false
            }
        } catch (ex: Exception) {
            Log.e("error", "hideViews: ", ex)
        }
    }

    fun invisibleViewsPipAndShape() {
        try {
            if (childVisible && imageViewList.isNotEmpty()) {
                childVisible = false
                imageViewList.forEach {
                    it.visibility = INVISIBLE
                }
            }
        } catch (ex: Exception) {
            Log.e("error", "hideViews: ", ex)
        }
    }

    fun invisibleViews() {
        try {
            Log.i("TAG", "invisibleViews: $childVisible")
            if (childVisible && imageViewList.isNotEmpty()) {
                childVisible = false
//                alpha = 0.95f
                imageViewList.forEach {
                    it.visibility = INVISIBLE
                }
            }
        } catch (ex: Exception) {
            Log.e("error", "hideViews: ", ex)
        }
    }

    fun showViews() {
        try {
            Log.i("TAG", "showViews: $childVisible")
            if (!childVisible) {
                childVisible = true
                alpha = 1f
                postOnAnimation {
                    imageViewList.forEach {
                        it.isVisible = true
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("error", "hideViews: ", ex)
        }
    }

    fun stopSizeUpdates() {
        imageViewList.forEach {
            it.frameChangingState = true
        }
    }

    private fun changeDimensions() {
        job?.cancel()
//        hideViews()
        if (fromFragment) {
            fromFragment = false
            invisibleViews()
        }

//        if (fromAdjustAndFilter) {
//            hideViews()
//        }
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                imageViewList.forEach { child ->
                    if (isActive && !child.frameChangingState) {

                        val currentWidth = lastWidth
                        val currentHeight = lastHeight

//                        val x =
//                            percentX.times((currentWidth))+(Constants.paddingHorizontal/2)
//                        Log.i("calculateFrameValues", "calculateFrameValues: $x")
//                        val width = percentWidth.times((currentWidth - (Constants.paddingHorizontal+Constants.paddingHorizontal))).toInt()
//                        val y =
//                            percentY.times((currentHeight))+(Constants.paddingVertical/2)
//                        val height =
//                            percentHeight.times((currentHeight - (Constants.paddingVertical+Constants.paddingVertical))).toInt()

                        val layoutParams = child.layoutParams
                        layoutParams.width =
                            child.percentWidth.times((currentWidth - (Constants.paddingHorizontal + Constants.paddingHorizontal)))
                                .roundToInt()
                        layoutParams.height =
                            child.percentHeight.times((currentHeight - (Constants.paddingVertical + Constants.paddingVertical)))
                                .roundToInt()
                        val x =
                            child.percentX.times((currentWidth)) + (Constants.paddingHorizontal / 2)
                        val y =
                            child.percentY.times((currentHeight)) + (Constants.paddingVertical / 2)

                        postOnAnimation {

                            child.x = x
                            child.y = y
                            child.layoutParams = layoutParams

//                            if (fromAdjustAndFilter) {
//                                fromAdjustAndFilter = false
//                                showViews()
//                            }
                        }
                        if (fromEdit)
                            fromEdit = false
                    } else {
                        return@forEach
                    }
                }
//                postOnAnimation {
//                    if (fromFragment) {
//                        fromFragment = false
//                        showViews()
//                    }
//                }
//                if (isActive) {
//                    bgImage?.layoutParams?.width = lastWidth
//                    bgImage?.layoutParams?.height = lastHeight
//                }
            } catch (ex: Exception) {
                Log.e("error", "onSizeChanged: ", ex)
            }
        }
    }
}