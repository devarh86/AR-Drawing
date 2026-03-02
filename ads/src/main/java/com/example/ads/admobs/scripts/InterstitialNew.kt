package com.example.ads.admobs.scripts

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.ads.Constants.OTHER_AD_ON_DISPLAY
import com.example.ads.Constants.firebaseAnalytics
import com.example.ads.Constants.immediateInterstitial
import com.example.ads.Constants.mNewInterstitial
import com.example.ads.admobs.utils.logRevenue
import com.example.ads.model.AdConfigModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.project.common.utils.ConstantsCommon.showInterstitialAd
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val loggerName = "INTMobifyAds"

class InterstitialNew {

    private var mInterstitialAdsList: MutableList<AdConfigModel> = mutableListOf()

    /**

     * Checks if a list of [AdConfigModel] already contains an object with the same
     * `currentActivityOrFragment` as the given [adConfig].
     *
     * If an object with a matching `currentActivityOrFragment` is found, that object is returned.
     * Otherwise, the original [adConfig] is returned, indicating that no match was found.
     *
     * Note: This function uses a temporary list for iteration to avoid potential issues with modifying the original list
     * during iteration (although it does not modify it here). It focuses only on the `currentActivityOrFragment` property for comparison.
     *
     * @param adConfig The [AdConfigModel] to check for in the list.
     * @return The existing [AdConfigModel] from the list if one with a matching `currentActivityOrFragment` is found,
     *         or the original [adConfig] if no match is found.
     */

    private fun MutableList<AdConfigModel>.containsObject(adConfig: AdConfigModel): AdConfigModel {
        val tempList: MutableList<AdConfigModel> = mutableListOf()
        tempList.addAll(this)
        tempList.forEach {
            if (it.currentActivityOrFragment == adConfig.currentActivityOrFragment) {
                return it
            }
        }

        return adConfig
    }

    /**
     * Loads an interstitial ad based on the provided ad configuration.
     *
     * @param context The context in which the ad will be loaded.
     * @param adConfigModel The ad configuration model containing ad details.
     * @param callback The callback to be invoked after the ad is loaded or fails to load.
     * @param reload A flag indicating whether the ad should be reloaded.
     */
    fun loadInterstitial(
        context: Context,
        adConfigModel: AdConfigModel,
        callback: () -> Unit,
        reload: Boolean = false,
    ) {
        kotlin.runCatching {
            // Retrieve the ad configuration from the list or use the provided one
            val adConfig = mInterstitialAdsList.containsObject(adConfigModel)

            adConfig.apply {
                Log.i(
                    loggerName,
                    "loadInterstitial: ${this.currentActivityOrFragment} and ad is = ${interstitialAdModel.interstitialAd}"
                )

                // Check if the ad is not already loaded and not currently loading
                if (interstitialAdModel.interstitialAd == null && !interstitialAdModel.isAlreadyLoading) {
                    interstitialAdModel.isAlreadyLoading = true

                    // Determine the ad ID to use based on the reload flag and reload limit
                    val adId = when {
                        !reload && idHigh.isNotBlank() && reloadLimit >= 2 -> {
                            Log.i(loggerName, "loadInterstitial: config.idHigh $idHigh")
                            idHigh
                        }

                        reload && reloadLimit == 2 || (idMedium.isNotBlank() && reloadLimit == 1 && !reload) -> {
                            Log.i(loggerName, "loadInterstitial: config.idMedium $idMedium")
                            idMedium
                        }

                        else -> {
                            Log.i(loggerName, "loadInterstitial: config.idBackUp $idBackUp")
                            idBackUp
                        }
                    }

                    val adRequest = AdRequest.Builder().build()

                    // Load the interstitial ad with the determined ad ID
                    InterstitialAd.load(
                        context.applicationContext,
                        adId,
                        adRequest,
                        object : InterstitialAdLoadCallback() {
                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                Log.e(
                                    loggerName,
                                    "loadInterstitial: onAdFailedToLoad ${adError.message}"
                                )
                                interstitialAdModel.interstitialAdFailedCounter += 1
                                interstitialAdModel.isAlreadyLoading = false

                                // Retry loading the ad if the failure count is within the limit
                                if (interstitialAdModel.interstitialAdFailedCounter <= reloadLimit) {
                                    if (reloadLimit > 2) interstitialAdModel.interstitialAdFailedCounter =
                                        reloadLimit + 1
                                    loadInterstitial(context, adConfigModel, callback, true)
                                } else {
                                    interstitialAdModel.interstitialAdFailedCounter = 0
                                    interstitialAdModel.interstitialAd = null
                                    callback.invoke()
                                }
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                Log.e(loggerName, "loadInterstitial: onAdLoaded")
                                interstitialAdModel.interstitialAdFailedCounter = 0
                                interstitialAdModel.interstitialAd = interstitialAd

                                // Add the ad configuration to the list if not already present
                                if (!mInterstitialAdsList.contains(this@apply)) {
                                    Log.e(
                                        loggerName,
                                        "loadInterstitial: onAdLoaded adding config into list"
                                    )
                                    mInterstitialAdsList.add(this@apply)
                                    interstitialAd.onPaidEventListener =
                                        OnPaidEventListener { adValue ->
                                            val newInterstitialAdModel = this@apply
                                            kotlin.runCatching {
                                                val loadedAdapterResponseInfo = interstitialAd.responseInfo?.loadedAdapterResponseInfo
                                                val adSourceName = loadedAdapterResponseInfo?.adSourceName
                                                val adSourceId = loadedAdapterResponseInfo?.adSourceId

                                                val bundle = Bundle().apply {
                                                    putString("ad_source", "AdMob")
                                                    putString(
                                                        "source_activity",
                                                        "${newInterstitialAdModel.currentActivityOrFragment.lowercase()}_inter_ad"
                                                    )
                                                    putString("ad_format", "interstitial")
                                                    putString("currency_code", adValue.currencyCode)
                                                    putLong(
                                                        "value_micros",
                                                        adValue.valueMicros
                                                    )

                                                    putDouble(
                                                        "ad_value",
                                                        adValue.valueMicros / 1000000.0
                                                    )
                                                    adSourceName?.let {
                                                        putString("ad_source_name", adSourceName)
                                                    }
                                                    adSourceId?.let {
                                                        putString("ad_source_id", adSourceId)
                                                    }
                                                }
                                                firebaseAnalytics?.logEvent(
                                                    "ad_revenue_event", bundle
                                                )
                                                Log.i(loggerName, "onAdLoaded: ad_revenue_event, $bundle")
                                                logRevenue(adValue.valueMicros / 1000000.0, context)
                                            }
                                            val data = SingularAdData(
                                                "AdMob",
                                                adValue.currencyCode,
                                                adValue.valueMicros / 1000000.0
                                            )

                                            Singular.adRevenue(data)

                                            interstitialAd.onPaidEventListener = null
                                        }
                                } else {

                                    if (interstitialAd.onPaidEventListener == null) {
                                        interstitialAd.onPaidEventListener =
                                            OnPaidEventListener { adValue ->
                                                val newInterstitialAdModel = this@apply
                                                kotlin.runCatching {
                                                    val loadedAdapterResponseInfo = interstitialAd.responseInfo?.loadedAdapterResponseInfo
                                                    val adSourceName = loadedAdapterResponseInfo?.adSourceName
                                                    val adSourceId = loadedAdapterResponseInfo?.adSourceId

                                                    val bundle = Bundle().apply {
                                                        putString("ad_source", "AdMob")
                                                        putString(
                                                            "source_activity",
                                                            "${newInterstitialAdModel.currentActivityOrFragment.lowercase()}_inter_ad"
                                                        )
                                                        putString("ad_format", "interstitial")
                                                        putString("currency_code", adValue.currencyCode)
                                                        putLong(
                                                            "value_micros",
                                                            adValue.valueMicros
                                                        )

                                                        putDouble(
                                                            "ad_value",
                                                            adValue.valueMicros / 1000000.0
                                                        )
                                                        adSourceName?.let {
                                                            putString("ad_source_name", adSourceName)
                                                        }
                                                        adSourceId?.let {
                                                            putString("ad_source_id", adSourceId)
                                                        }
                                                    }
                                                    firebaseAnalytics?.logEvent(
                                                        "ad_revenue_event", bundle
                                                    )
                                                    Log.i(loggerName, "onAdLoaded: ad_revenue_event, $bundle")
                                                    logRevenue(adValue.valueMicros / 1000000.0, context)
                                                }
                                                val data = SingularAdData(
                                                    "AdMob",
                                                    adValue.currencyCode,
                                                    adValue.valueMicros / 1000000.0
                                                )

                                                Singular.adRevenue(data)

                                                interstitialAd.onPaidEventListener = null
                                            }
                                    }
                                    Log.e(
                                        loggerName,
                                        "loadInterstitial: onAdLoaded already having this config"
                                    )
                                }

                                interstitialAdModel.isAlreadyLoading = false
                                callback.invoke()
                            }
                        })
                }
            }
        }.onFailure {
            Log.d(loggerName, "loadInterstitial crash")
        }
    }

    /**
     * Shows an interstitial ad if it is loaded and meets the display conditions.
     *
     * @param activity The activity in which the ad will be shown.
     * @param adConfigModel The ad configuration model containing ad details.
     * @param nextAction The action to be performed after the ad is shown or fails to show.
     */
    fun showInterstitial(
        activity: Activity,
        adConfigModel: AdConfigModel,
        nextAction: () -> Unit,
    ) {
        kotlin.runCatching {
            // Retrieve the ad configuration from the list or use the provided one
            val adConfig = mInterstitialAdsList.containsObject(adConfigModel)

            adConfig.apply {
                interstitialAdModel.interstitialAd?.let {
                    Log.i(loggerName, "showInterstitial ad loaded")

                    // Check if the ad should be shown based on the first show count and current counter
                    if (!interstitialAdModel.interstitialAdFirstShow && interstitialAdModel.interstitialAdFirstShowCount > 0) {
                        kotlin.runCatching {
                            interstitialAdModel.interstitialAdFirstShow = ((interstitialAdModel.interstitialAdCurrentCounter + 1) % interstitialAdModel.interstitialAdFirstShowCount) == 0
                            if (interstitialAdModel.interstitialAdFirstShow) {
                                interstitialAdModel.interstitialAdFirstShowCount = 0
                                interstitialAdModel.interstitialAdCurrentCounter = 0
                            } else {
                                interstitialAdModel.interstitialAdCurrentCounter += 1
                                nextAction.invoke()
                                Log.i(loggerName, "showInterstitial first step check not Match yet")
                                return@apply
                            }
                        }.onFailure {
                            Log.e(loggerName, "showInterstitial error ${it.message}")
                        }
                    }

                    Log.i(
                        loggerName,
                        "showInterstitial counter ${interstitialAdModel.interstitialAdCurrentCounter}"
                    )

                    // Show the ad if it meets the display conditions
                    if (interstitialAdModel.interstitialAdAlwaysShow || interstitialAdModel.interstitialAdFirstShow || interstitialAdModel.interstitialAdCurrentCounter % interstitialAdModel.interstitialAdAfterFirstShowSteps == 0) {
                        Log.i(loggerName, "showInterstitial check match successfully")
                        interstitialAdModel.interstitialAdFirstShow = false
                        interstitialAdModel.interstitialAdCurrentCounter += 1

                        it.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                Log.i(loggerName, "showInterstitial onAdClicked")
                            }

                            override fun onAdDismissedFullScreenContent() {
                                OTHER_AD_ON_DISPLAY = false
                                var isPangle = false

                                kotlin.runCatching {
                                    interstitialAdModel.interstitialAd?.responseInfo?.adapterResponses?.forEach { adapterResponse ->
                                        if (adapterResponse.adSourceName.contains(
                                                "Pangle",
                                                ignoreCase = true
                                            ) || adapterResponse.adSourceName.contains(
                                                "Liftoff",
                                                ignoreCase = true
                                            )
                                        ) {
                                            isPangle = true
                                            println("Pangle served the interstitial ad")
                                            return@forEach
                                        }
                                    }

                                    interstitialAdModel.interstitialAd?.fullScreenContentCallback =
                                        null
                                    interstitialAdModel.interstitialAd = null

                                    if (isPangle) {
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            Log.i(loggerName, "showInterstitial next Action")
                                            nextAction.invoke()
                                        }, 500)
                                    } else {
                                        Log.i(loggerName, "showInterstitial next Action")
                                        nextAction.invoke()
                                    }
                                }
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                OTHER_AD_ON_DISPLAY = false
                                Log.e(
                                    loggerName,
                                    "showInterstitial onAdFailedToShowFullScreenContent"
                                )
                                interstitialAdModel.interstitialAd?.fullScreenContentCallback = null
                                interstitialAdModel.interstitialAd = null
                                nextAction.invoke()
                            }

                            override fun onAdImpression() {
                                Log.i(loggerName, "showInterstitial onAdImpression")
                                interstitialAdModel.interstitialImpression = true
                            }

                            override fun onAdShowedFullScreenContent() {

                                if (currentActivityOrFragment.isNotBlank()) {
                                    firebaseAnalytics?.logEvent(
                                        "$currentActivityOrFragment inter_ad",
                                        null
                                    )
                                    Log.i(
                                        loggerName,
                                        "showInterstitial Which activity/fragment is $currentActivityOrFragment"
                                    )
                                }

                                OTHER_AD_ON_DISPLAY = true
                                if (!immediateInterstitial) {
                                    showInterstitialAd = false
                                }
                                Log.i(loggerName, "showInterstitial onAdShowedFullScreenContent")
                            }
                        }

                        it.show(activity)
                    } else {
                        interstitialAdModel.interstitialAdCurrentCounter += 1
                        Log.d(loggerName, "showInterstitial check match unSuccessfully")
                        nextAction.invoke()
                    }
                } ?: run {
                    Log.d(loggerName, "showInterstitial The interstitial ad wasn't ready yet.")
                    nextAction.invoke()
                }
            }
        }.onFailure {
            Log.d(loggerName, "showInterstitial crash")
            Log.d(loggerName, "showInterstitial $it")
            nextAction.invoke()
        }
    }
}