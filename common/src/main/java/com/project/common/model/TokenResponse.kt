package com.project.common.model

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName

@Keep
data class TokenResponse(
    @SerializedName("Authorization") val token:String?
)


