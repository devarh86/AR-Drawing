package com.project.crop.ui.main.custom_views.imagecrop.model

import android.graphics.Bitmap
import com.project.crop.ui.main.custom_views.imagecrop.util.BitmapLoadUtils

/**
 * Created by helloyako on 2016. 5. 10..
 */
class CropInfo(
    val scale: Float,
    val viewBitmapWidth: Float,
    val viewImageTop: Float,
    val viewImageLeft: Float,
    val cropTop: Float,
    val cropLeft: Float,
    val cropWidth: Float,
    val cropHeight: Float
) {
    fun getCroppedImage(path: String?, flipVertical: Boolean, flipHorizontal: Boolean): Bitmap? {
        return getCroppedImage(
            path,
            800,
            flipHorizontal = flipHorizontal,
            flipVertical = flipVertical
        )
    }

    /**
     * @param reqSize for image sampling
     */
    fun getCroppedImage(
        path: String?,
        reqSize: Int,
        flipVertical: Boolean,
        flipHorizontal: Boolean
    ): Bitmap? {
        val bitmap: Bitmap? = BitmapLoadUtils.decode(
            path,
            reqSize,
            reqSize,
            flipVertical = flipVertical,
            flipHorizontal = flipHorizontal
        )
        return bitmap?.let { getCroppedImage(it) }
    }

    fun getCroppedImage(bitmap: Bitmap): Bitmap {
        val scale = scale * (viewBitmapWidth / bitmap.width.toFloat())
        var x = Math.abs(viewImageLeft - cropLeft) / scale
        var y = Math.abs(viewImageTop - cropTop) / scale
        var actualCropWidth = cropWidth / scale
        var actualCropHeight = cropHeight / scale
        if (x < 0) {
            x = 0f
        }
        if (y < 0) {
            y = 0f
        }
        if (y + actualCropHeight > bitmap.height) {
            actualCropHeight = bitmap.height - y
        }
        if (x + actualCropWidth > bitmap.width) {
            actualCropWidth = bitmap.width - x
        }
        return Bitmap.createBitmap(
            bitmap,
            x.toInt(),
            y.toInt(),
            actualCropWidth.toInt(),
            actualCropHeight.toInt()
        )
    }
}