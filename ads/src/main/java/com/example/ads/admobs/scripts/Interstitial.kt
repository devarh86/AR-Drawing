package com.example.ads.admobs.scripts

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.ads.Constants.OTHER_AD_ON_DISPLAY
import com.example.ads.Constants.firebaseAnalytics
import com.example.ads.Constants.immediateInterstitial
import com.example.ads.Constants.saveInterAdId
import com.example.ads.Constants.splashInterAdId
import com.example.ads.Constants.splashTimeOut
import com.example.ads.R
import com.example.ads.admobs.utils.logRevenue
import com.example.ads.model.AdConfigModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.ResponseInfo
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
import kotlin.time.Duration.Companion.seconds

class Interstitial {

    var timeHandler: Handler? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var adLoadFailed = false
    private var onEventLoaded: ((Boolean) -> Unit)? = null
    private var adReloadCounter = 0
    private var isMedium = false

    var adConfig: AdConfigModel? = null

    fun loadInterstitial(
        context: Context,
        adLoaded: () -> Unit,
        adFailed: () -> Unit,
        forSplash: Boolean = false,
        forSave: Boolean = false,
        reloadSplash: Boolean = false,
        reloadSave: Boolean = false
    ) {
        if (mInterstitialAd == null) {
            adLoadFailed = false


            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context.applicationContext,
                if (forSplash) {
                    Log.i("SPLASHISSUE","loadInterstitial--For Splash")
                    adConfig?.let { config ->
                        Log.i("SPLASHISSUE","loadInterstitial--For Splash inside let")
                        when {
                            reloadSplash && isMedium -> {
                                isMedium = false
                                config.idMedium
                            }
                            reloadSplash -> {
                                config.idBackUp
                            }
                            else -> {
//                                isMedium = true
                                splashInterAdId
                            }
                        }
                    } ?: kotlin.run {
                        Log.i("SPLASHISSUE","loadInterstitial---RUN")
                        context.getString(R.string.splash_inter_backup) // Fallback if `adConfig` is null  check this case
                    }
                }else if(forSave){
                    Log.i("SPLASHISSUE","loadInterstitial--For Save")
                    adConfig?.let { config ->
                        Log.i("SPLASHISSUE","loadInterstitial--For Save inside let")
                        when {
                            reloadSave && isMedium -> {
                                isMedium = false
                                config.idMedium
                            }
                            reloadSave -> {
                                config.idBackUp
                            }
                            else -> {
                                isMedium = true
                                saveInterAdId
                            }
                        }
                    } ?: kotlin.run {
                        Log.i("SPLASHISSUE","loadInterstitial---RUN")
                        context.getString(R.string.save_inter_backup) // Fallback if `adConfig` is null  check this case
                    }
                } else {
                    Log.i("SPLASHISSUE","loadInterstitial--For ELSE")
                    context.getString(R.string.interstitial)
                },
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {

                        Log.e("REASONFAILEDInter", adError.toString())

                        adReloadCounter += 1
                        if (forSplash && adReloadCounter < 2) {
                            loadInterstitial(context, adLoaded, adFailed, true, reloadSplash = true)
                        } else if (forSave && adReloadCounter <= 2) {
                            loadInterstitial(context, adLoaded, adFailed, forSave = true, reloadSave = true)

                        } else {
                            adReloadCounter = 0
                            adFailed.invoke()
                            adLoadFailed = true
                            mInterstitialAd = null
                            if (forSplash) {
                                Log.i(
                                    "SPLASHISSUE",
                                    "loadInterstitial---onAdFailedToLoad--FORSPLASH"
                                )
                                onEventLoaded?.invoke(false)
                            }
                        }
                    }
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d("TAG", "interstitial ads Ad was loaded.")
                        adReloadCounter = 0
                        adLoadFailed = false
                        mInterstitialAd = interstitialAd
                        interstitialAd.onPaidEventListener =
                            OnPaidEventListener { adValue ->

                                kotlin.runCatching {
                                    val loadedAdapterResponseInfo = interstitialAd.responseInfo?.loadedAdapterResponseInfo
                                    val adSourceName = loadedAdapterResponseInfo?.adSourceName
                                    val adSourceId = loadedAdapterResponseInfo?.adSourceId

                                    val bundle = Bundle().apply {
                                        putString("ad_source", "AdMob")
                                        putString(
                                            "source_activity",
                                            "general_interstitial_ad"
                                        )
                                        putString("ad_format", "interstitial")
                                        putString("currency_code", adValue.currencyCode)
                                        putLong(
                                            "value_micros",
                                            adValue.valueMicros
                                        )
                                        adSourceName?.let {
                                            putString("ad_source_name", adSourceName)
                                        }
                                        adSourceId?.let {
                                            putString("ad_source_id", adSourceId)
                                        }
                                        putDouble(
                                            "ad_value",
                                            adValue.valueMicros / 1000000.0
                                        )
                                    }

                                    firebaseAnalytics?.logEvent(
                                        "ad_revenue_event", bundle
                                    )

                                    Log.i("logger", "onAdLoaded: ad_revenue_event, $bundle")
                                    logRevenue(adValue.valueMicros / 1000000.0, context)
                                }
                                val impressionData: AdValue = adValue
                                val data = SingularAdData(
                                    "AdMob",
                                    impressionData.currencyCode,
                                    impressionData.valueMicros / 1000000.0
                                )
                                Singular.adRevenue(data)
                            }

                        if (forSplash) {
                            onEventLoaded?.invoke(true)
                        }
                        adLoaded.invoke()
                    }
                })
        }
    }

    fun showInterstitial(
        activity: Activity,
        loadedAction: () -> Unit,
        failedAction: () -> Unit,
        reload: Boolean = true,
        forSave:Boolean = false
    ) {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d("TAG", "interstitial ads Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                OTHER_AD_ON_DISPLAY = false

                var isPangle = false

                kotlin.runCatching {
                    val ad = mInterstitialAd
                    val responseInfo: ResponseInfo? =
                        ad?.responseInfo
                    responseInfo?.adapterResponses?.forEach { adapterResponse ->
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

                }
                if (isPangle) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        loadedAction.invoke()
                    }, 200)
                } else {
                    loadedAction.invoke()
                }
                Log.d("TAG", "interstitial ads Ad dismissed fullscreen content.")
                mInterstitialAd = null

                if (reload) {
                    loadInterstitial(activity.applicationContext, {}, {},forSave = true)

                }else if(forSave){

                    loadInterstitial(activity.applicationContext, {}, {},forSave = true)
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                OTHER_AD_ON_DISPLAY = false
                Log.e("TAG", "interstitial ads Ad failed to show fullscreen content.")
                mInterstitialAd = null
                failedAction.invoke()

                if (reload) {
                    loadInterstitial(activity.applicationContext, {}, {},forSave = true)
                }else if(forSave){

                    loadInterstitial(activity.applicationContext, {}, {},forSave = true)
                }
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d("TAG", "interstitial ads Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                OTHER_AD_ON_DISPLAY = true
                if(!immediateInterstitial){
                    showInterstitialAd = false
                }
                Log.d("TAG", "interstitial ads Ad showed fullscreen content.")
            }
        }
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(activity)
        } else if (adLoadFailed) {
            loadInterstitial(activity.applicationContext, {}, {})
            failedAction.invoke()
        } else {
            Log.d("TAG", "interstitial ads The interstitial ad wasn't ready yet.")
            failedAction.invoke()
        }
    }


    fun showSplashInterstitial(
        activity: Activity,
        loadedAction: () -> Unit,
        failedAction: () -> Unit,
        reload: Boolean = true
    ) {
        Log.i("SPLASHISSUE", "showSplashInterstitial: start ")
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d("TAG", "interstitial ads Ad was clicked.")

                Log.i("SPLASHISSUE", "showSplashInterstitial: onAdClicked ")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                OTHER_AD_ON_DISPLAY = false
                loadedAction.invoke()
                Log.d("TAG", "interstitial ads Ad dismissed fullscreen content.")
                Log.i("SPLASHISSUE", "showSplashInterstitial: onAdDismissedFullScreenContent ")
                mInterstitialAd = null

//                if (reload) {
//                    Log.i("SPLASHISSUE", "showSplashInterstitial: onAdDismissedFullScreenContent--reload ")
//                    timeHandler = Handler(Looper.getMainLooper())
//                    timeHandler?.postDelayed({
//                        loadInterstitial(activity.applicationContext, {}, {},true)
//                    }, 2.seconds.inWholeMilliseconds)
//                }
                loadInterstitial(activity.applicationContext, {}, {},true)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                OTHER_AD_ON_DISPLAY = false
                Log.e("TAG", "interstitial ads Ad failed to show fullscreen content.")
                mInterstitialAd = null
                Log.i("SPLASHISSUE", "showSplashInterstitial: onAdFailedToShowFullScreenContent ")
                failedAction.invoke()

//                if (reload) {
//                    timeHandler = Handler(Looper.getMainLooper())
//                    timeHandler?.postDelayed({
//                    }, 2.seconds.inWholeMilliseconds)
//                }
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d("TAG", "interstitial ads Ad recorded an impression.")

                Log.i("SPLASHISSUE", "showSplashInterstitial: onAdImpression ")
            }

            override fun onAdShowedFullScreenContent() {
                OTHER_AD_ON_DISPLAY = true
                Log.d("TAG", "interstitial ads Ad showed fullscreen content.")
            }
        }
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(activity)
            Log.i("SPLASHISSUE", "showSplashInterstitial:  mInterstitialAd?.show ")
        } else if (adLoadFailed) {
            loadInterstitial(activity.applicationContext, {}, {})
            Log.i("SPLASHISSUE", "showSplashInterstitial:  adLoadFailed ")
            failedAction.invoke()
        } else {
            Log.d("TAG", "interstitial ads The interstitial ad wasn't ready yet.")
            startCountdown(CoroutineScope(Default), failedAction)
            onEventLoaded = {

                Log.i("SPLASHISSUE", "showSplashInterstitial:  ----onEventLoaded ")
                Log.i("TAG", "showSplashInterstitial: $it")
                if (it) {
                    stopCountdown()
                    showSplashInterstitial(
                        activity,
                        loadedAction,
                        failedAction,
                        reload
                    )
                    Log.i("SPLASHISSUE", "showSplashInterstitial:  LAST IF")
                    onEventLoaded = null
                } else {

                    Log.i("SPLASHISSUE", "showSplashInterstitial:  LAST ELSE")
                    stopCountdown()
                    failedAction.invoke()
                    onEventLoaded = null
                }
            }
        }
    }

    private var job: Job? = null

    private fun startCountdown(scope: CoroutineScope, failedAction: () -> Unit) {
        job = scope.launch {
            var remainingTime = splashTimeOut // 10 seconds
            val interval = 1000L // 1 second

            while (remainingTime > 0) {
                delay(interval)
                remainingTime -= interval
                Log.i("TAG", "startCountdown: $remainingTime")
                // Update UI
            }
            withContext(Main) {
                onEventLoaded = null
                if (job?.isActive == true) {
                    failedAction.invoke()
                }
            }
        }
    }

    private fun stopCountdown() {
        job?.cancel()
    }
}

/*old loading
*
    fun loadInterstitial(
        context: Context,
        adLoaded: () -> Unit,
        adFailed: () -> Unit,
        forSplash: Boolean = false,
        reloadSplash: Boolean = false
    ) {
        if (mInterstitialAd == null) {
            adLoadFailed = false
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context.applicationContext,
                if (forSplash) {
                    if (reloadSplash && isMedium) {
                        isMedium = false
                        ContextCompat.getString(
                            context,
                            R.string.interstitial_splash_medium
                        )
                    } else if (reloadSplash) {
                        ContextCompat.getString(
                            context,
                            R.string.interstitial_splash_back_up
                        )
                    } else {
                        isMedium = true
                        splashInterAdId
                    }
                } else {
                    context.getString(R.string.interstitial)
                },
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        adReloadCounter += 1
                        if (adReloadCounter <= 2) {
                            loadInterstitial(context, adLoaded, adFailed, true, reloadSplash = true)
                        } else {
                            adReloadCounter = 0
                            adFailed.invoke()
                            adLoadFailed = true
                            Log.d("TAG", "adinterstitial ${adError.message}")
                            mInterstitialAd = null
                            if (forSplash) {
                                onEventLoaded?.invoke(false)
                            }
                        }
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d("TAG", "interstitial ads Ad was loaded.")
                        adReloadCounter = 0
                        adLoadFailed = false
                        mInterstitialAd = interstitialAd
                        interstitialAd.onPaidEventListener =
                            OnPaidEventListener { adValue ->
                                val impressionData: AdValue = adValue
                                val data = SingularAdData(
                                    "AdMob",
                                    impressionData.currencyCode,
                                    impressionData.valueMicros / 1000000.0
                                )
                                Singular.adRevenue(data)
                            }

                        if (forSplash) {
                            onEventLoaded?.invoke(true)
                        }

                        adLoaded.invoke()
                    }
                })
        }
    }
*
*
*
*
* */