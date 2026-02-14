package com.project.common.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ImageResponseModelColorize(
    @SerializedName("url")val url: String?
)
