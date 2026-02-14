package com.project.sticker.data.model

import androidx.annotation.Keep

@Keep
data class StickersSpecificCategoryDataModel(
    val stickerId: Int = 0,
    var file: String = ""
)