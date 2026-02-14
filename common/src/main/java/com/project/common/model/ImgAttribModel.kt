package com.project.common.model

import android.widget.ImageView
import androidx.annotation.Keep

@Keep
data class ImgAttributeModel(
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 0f,
    var height: Float = 0f,
    var rotation: Float = 0f,
    var drawable: Int = 0,
    var img: ImageView? = null,
    var mask: Any = "",
    var frameSliderCount: Any = ""
)
