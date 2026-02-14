package com.example.ads.crosspromo.api.retrofit.model

import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PanelItems(
    @SerializedName("GIF") val gif: List<CrossPromoItem>?,
    @SerializedName("ICON") val icon: List<CrossPromoItem>?,
    @SerializedName("INTERSTITIAL") val interstitial: List<CrossPromoItem>?,
    @SerializedName("NATIVE") val native: List<CrossPromoItem>?,
    @SerializedName("NATIVE_VIDEO") val nativeVideo: List<CrossPromoItem>?,
    @SerializedName("RECTANGULAR_BANNER") val rectangularBanner: List<CrossPromoItem>?,
    @SerializedName("SMALL_BANNER") val smallBanner: List<CrossPromoItem>?,
    @SerializedName("SMART_BANNER") val smartBanner: List<CrossPromoItem>?,
    @SerializedName("VIDEO") val video: List<CrossPromoItem>?
)