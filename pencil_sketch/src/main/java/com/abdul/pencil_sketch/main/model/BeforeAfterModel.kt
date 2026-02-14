package com.abdul.pencil_sketch.main.model

import androidx.annotation.Keep

@Keep
data class BeforeAfterModel(
    var before: String = "",
    var after: String = "",
    var price: String = "",
    var position: Int = -1,
    var thumbType: String = ""
)