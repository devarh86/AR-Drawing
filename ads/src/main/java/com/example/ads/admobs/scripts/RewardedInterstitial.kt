package com.example.ads.admobs.scripts

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.ads.Constants.OTHER_AD_ON_DISPLAY
import com.example.ads.Constants.firebaseAnalytics
import com.example.ads.R
import com.example.ads.admobs.utils.logRevenue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import kotlin.time.Duration.Companion.seconds

class RewardedInterstitial {

    var timeHandler: Handler? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var TAG = "FAHAD_REWARDED_INTERSTITIAL"
    private var isGranted = false

    fun loadRewardedInterstitial(
        activity: Activity,
        adLoaded: () -> Unit,
        failedAction: () -> Unit
    ) {
        if (rewardedInterstitialAd == null) {
            RewardedInterstitialAd.load(activity.applicationContext,
                activity.getString(R.string.rewarded_interstitial),
                AdRequest.Builder().build(),
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        Log.d(TAG, "Ad was loaded.")
                        rewardedInterstitialAd = ad
                        ad?.onPaidEventListener =
                            OnPaidEventListener { adValue ->
                                kotlin.runCatching {
                                    val loadedAdapterResponseInfo = ad.responseInfo?.loadedAdapterResponseInfo
                                    val adSourceName = loadedAdapterResponseInfo?.adSourceName
                                    val adSourceId = loadedAdapterResponseInfo?.adSourceId

                                    val bundle = Bundle().apply {
                                        putString("ad_source", "AdMob")
                                        putString(
                                            "source_activity",
                                            "general_rewarded_interstitial_ad"
                                        )
                                        putString("currency_code", adValue.currencyCode)
                                        putString("ad_format", "rewarded_interstitial")
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
                                    logRevenue(adValue.valueMicros / 1000000.0, activity)
                                }
                                val impressionData: AdValue = adValue
                                val data = SingularAdData(
                                    "AdMob",
                                    impressionData.currencyCode,
                                    impressionData.valueMicros / 1000000.0)
                                Singular.adRevenue(data)
                            }
                        adLoaded.invoke()
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e("REASONFAILED", adError.toString())
                        Log.d(TAG, adError.toString())
                        rewardedInterstitialAd = null
                        failedAction.invoke()
                    }
                })
        }
    }

    fun showRewardedInterstitial(
        activity: Activity,
        rewardGrantedAction: () -> Unit,
        failedAction: () -> Unit
    ) {
        rewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                firebaseAnalytics?.logEvent("reward_ad_clicked", null)
                Log.i("new_event", "reward_ad_clicked")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed fullscreen content.")
                OTHER_AD_ON_DISPLAY = false
                rewardedInterstitialAd = null
                if (isGranted){
                    firebaseAnalytics?.logEvent("rewarded_inters_video", null)
                    firebaseAnalytics?.logEvent("reward_ads_both", null)
                    Log.i("new_event", "rewarded_inters_video")
                    Log.i("new_event", "reward_ads_both")
                    rewardGrantedAction.invoke()
                } else failedAction.invoke()

                 timeHandler = Handler(Looper.getMainLooper())
                 timeHandler?.postDelayed({
                     loadRewardedInterstitial(activity, {}, { })
                 }, 3.seconds.inWholeMilliseconds)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show fullscreen content.")
                OTHER_AD_ON_DISPLAY = false
                rewardedInterstitialAd = null
                firebaseAnalytics?.logEvent("reward_ad_failed", null)
                Log.i("new_event", "reward_ad_failed")
                timeHandler = Handler(Looper.getMainLooper())
                timeHandler?.postDelayed({
                    loadRewardedInterstitial(activity, {}, { })
                }, 1.seconds.inWholeMilliseconds)
            }

            override fun onAdImpression() {
                firebaseAnalytics?.logEvent("reward_ad_impression", null)
                Log.i("new_event", "reward_ad_impression")
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                firebaseAnalytics?.logEvent("reward_ad_show", null)
                Log.i("new_event", "reward_ad_show")
                Log.d(TAG, "Ad showed fullscreen content.")
                OTHER_AD_ON_DISPLAY = true
            }
        }

        rewardedInterstitialAd?.let { ad ->
            ad.show(activity) {
                isGranted = true
            }
        } ?: run {
            failedAction.invoke()
        }
    }
}