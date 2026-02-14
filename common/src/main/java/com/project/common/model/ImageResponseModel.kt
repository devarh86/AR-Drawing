package com.project.common.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ImageResponseModel(
    @SerializedName("image_url")val url: String?
)
