package com.example.ads.admobs.scripts

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.example.ads.Constants.OTHER_AD_ON_DISPLAY
import com.example.ads.Constants.appIsForeground
import com.example.ads.Constants.firebaseAnalytics
import com.example.ads.Constants.needToLoadAppOpen
import com.example.ads.admobs.utils.logRevenue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
class AppOpen {

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
        fun adDismiss()
        fun onAdFailed()
    }

    val LOG_TAG = "CRS_APP_OPEN"

    // var timeHandler: Handler? = null

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }

    fun loadAd(context: Context, adUnitId: String?, onAdLoaded: () -> Unit, onAdFailed: () -> Unit) {
        needToLoadAppOpen = false
        if (isLoadingAd || isAdAvailable()) {
            onAdFailed()
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()
        if (appIsForeground) {
            AppOpenAd.load(
                context, adUnitId ?: context.getString(com.example.ads.R.string.app_open), request,
                object : AppOpenAd.AppOpenAdLoadCallback() {

                    override fun onAdLoaded(ad: AppOpenAd) {
                        Log.d(LOG_TAG, "Ad was loaded.")
                        appOpenAd = ad
                        isLoadingAd = false
                        ad.onPaidEventListener =
                            OnPaidEventListener { adValue ->
                                val impressionData: AdValue = adValue
                                val data = SingularAdData(
                                    "AdMob",
                                    impressionData.currencyCode,
                                    impressionData.valueMicros / 1000000.0
                                )
                                Singular.adRevenue(data)
                            }

                        onAdLoaded()
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(LOG_TAG, loadAdError.message)
                        isLoadingAd = false
                        needToLoadAppOpen = true
                        onAdFailed()
                    }
                })
        }
    }

    /** Shows the ad if one isn't already showing. */
    fun showAdIfAvailable(
        activity: Activity,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        if (OTHER_AD_ON_DISPLAY) {
            Log.d(LOG_TAG, "The other ad is already showing.")
            return
        }

        if (isShowingAd) {
            Log.d(LOG_TAG, "The app open ad is already showing.")
            return
        }

        if (isLoadingAd) {
            Log.d(LOG_TAG, "The app open ad is loading ad.")
            onShowAdCompleteListener.onAdFailed()
            return
        }

        if (!isAdAvailable()) {
            Log.d(LOG_TAG, "The app open ad is not ready yet.")
            onShowAdCompleteListener.onAdFailed()
            return
        }
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdDismissedFullScreenContent() {
                Log.d(LOG_TAG, "Ad dismissed fullscreen content.")
                OTHER_AD_ON_DISPLAY = false
                appOpenAd = null
                isShowingAd = false
                onShowAdCompleteListener.adDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(LOG_TAG, adError.message)
                OTHER_AD_ON_DISPLAY = false
                appOpenAd = null
                isShowingAd = false
                onShowAdCompleteListener.onAdFailed()
            }

            override fun onAdShowedFullScreenContent() {
                OTHER_AD_ON_DISPLAY = true
                isShowingAd = true
                Log.d(LOG_TAG, "Ad showed fullscreen content.")
            }
        }
        // isShowingAd = true
        appOpenAd?.show(activity) ?: run {
            onShowAdCompleteListener.onAdFailed()
        }
    }
}
