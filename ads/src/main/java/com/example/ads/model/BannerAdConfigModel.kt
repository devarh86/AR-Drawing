package com.example.ads.model

import androidx.annotation.Keep
import com.example.ads.admobs.scripts.Interstitial
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import java.lang.ref.WeakReference

@Keep
data class BannerAdConfigModel(
    var idHigh: String = "",
    var idMedium: String = "",
    var idBackUp: String = "",
    var reloadLimit: Int = 2,
    var enable: Boolean = true,
    var isAboveCtr: Boolean = false,
    var whichAd: AdsClassification = AdsClassification.BANNER,
    var currentActivityOrFragment: String = "",
    var fromWhere: String = "",
    var bannerView: WeakReference<AdView?> ?= null,
    var isAlreadyLoading: Boolean = false,
)



