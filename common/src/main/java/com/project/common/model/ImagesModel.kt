package com.project.common.model

import android.graphics.ColorMatrix
import android.graphics.Matrix
import androidx.annotation.Keep

@Keep
data class ImagesModel(
    var originalPath: String = "",
    var croppedPath: String = "",
    var bgOriginalPath: String = "",
    var bgCroppedPath: String = "",
    var matrix: Matrix? = null, // For transformations
    var colorMatrix: ColorMatrix? = null,
    var replace: Boolean = false,
    var imgPerScaleX: Float = 0f,
    var imgPerScaleY: Float = 0f,
    var imgPerSkewX: Float = 0f,
    var imgPerSkewY: Float = 0f,
    var percentageX: Float = 0f,
    var percentageY: Float = 0f,
    var flipHorizontal: Boolean = false,
    var flipVertical: Boolean = false,
    var zoomFactor: Float = 1.0f,
    var width: Int = 0, // Add this to store image width dynamically
    var imageWidth: Int = 0,
    var imageHeight: Int = 0,
)