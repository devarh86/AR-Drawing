package com.example.ads.crosspromo.api.retrofit.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CrossPromo(
    @SerializedName("ads") val ads: PanelItems?,
    @SerializedName("placement") val placement: String?
)