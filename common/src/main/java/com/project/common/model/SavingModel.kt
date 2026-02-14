package com.project.common.model

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import androidx.annotation.Keep

@Keep
data class SavingModel(
    var percentWidth: Float = 0f,
    var percentHeight: Float = 0f,
    var percentX: Float = 0f,
    var percentY: Float = 0f,
    var rotation: Float = 0f,
    var drawable: Drawable? = null,
    var floatArray: FloatArray = floatArrayOf(),
    var colorFilter: ColorFilter? = null,
    var overlayBitmap: Bitmap? = null,
    var effectBitmap: Bitmap? = null,
    var effectFloatArray: FloatArray = floatArrayOf(),
    var userImageBitmap: Bitmap? = null,
    var maskBitmap: Bitmap? = null,

)
