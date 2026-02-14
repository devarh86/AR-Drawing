package com.example.ads.model

import androidx.annotation.Keep
import com.google.android.gms.ads.interstitial.InterstitialAd

@Keep
data class AdConfigModel(
    var idHigh: String = "",
    var idMedium: String = "",
    var idBackUp: String = "",
    var reloadLimit: Int = 2,
    var enable: Boolean = true,
    var isMetaLayout: Boolean = false,
    var metaLayoutId: Int = -1,
    var adMobLayoutId: Int = -1,
    var isAboveCtr: Boolean = false,
    var whichAd: AdsClassification = AdsClassification.NATIVE,
    var currentActivityOrFragment: String = "",
    var interstitialAdModel: InterstitialAdModel = InterstitialAdModel(),
)



@Keep
data class InterstitialAdModel(
    var interstitialAd: InterstitialAd? = null,
    var isAlreadyLoading: Boolean = false,
    var interstitialAdFailedCounter: Int = 0,
    var interstitialAdFirstShow: Boolean = false,
    var interstitialAdAlwaysShow: Boolean = false,
    var interstitialAdFirstShowCount: Int = 0,
    var interstitialAdAfterFirstShowSteps: Int = 5,
    var interstitialAdCurrentCounter: Int = 0,
    var interstitialImpression: Boolean = false,
    var waitTime: Long = 0L,
)
