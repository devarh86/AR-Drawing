package com.example.ads.crosspromo.api.retrofit.model

import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CrossPromoItem(
    @SerializedName("ad_file") val adFile: String?,
    @SerializedName("ad_frequency") val adFrequency: String?,
    @SerializedName("ad_status") val adStatus: String?,
    @SerializedName("ad_type") val adType: String?,
    @SerializedName("id") val id: Int?,
    @SerializedName("index") val index: Int?,
    @SerializedName("link") val link: String?,
    @SerializedName("package") val appPackage: String?,
    @SerializedName("title") val title: String?
)