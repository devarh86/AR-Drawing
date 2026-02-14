package com.project.common.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ImageResponse(
    @SerializedName("image_url") val resultUrl:String?
)


