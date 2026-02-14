package com.example.ads.admobs.scripts

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.ads.Constants.OTHER_AD_ON_DISPLAY
import com.example.ads.Constants.firebaseAnalytics
import com.example.ads.R
import com.example.ads.admobs.utils.loadRewarded
import com.example.ads.admobs.utils.logRevenue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import kotlin.time.Duration.Companion.seconds

class Rewarded
{

    var timeHandler: Handler? = null
    private var rewardedAd: RewardedAd? = null
    private var isGranted = false

    fun loadRewarded(activity: Activity,adUnitId: String?, adLoaded: () -> Unit, failedAction: () -> Unit) {
        if (rewardedAd == null) {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                activity.applicationContext,
                adUnitId ?: activity.getString(R.string.reward_low),
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e("REASONFAILED", adError.toString())
                        rewardedAd = null
                        failedAction.invoke()
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        rewardedAd?.onPaidEventListener =
                            OnPaidEventListener { adValue ->
                                kotlin.runCatching {
                                    val loadedAdapterResponseInfo = ad.responseInfo.loadedAdapterResponseInfo
                                    val adSourceName = loadedAdapterResponseInfo?.adSourceName
                                    val adSourceId = loadedAdapterResponseInfo?.adSourceId

                                    val bundle = Bundle().apply {
                                        putString("ad_source", "AdMob")
                                        putString(
                                            "source_activity",
                                            "general_reward_ad"
                                        )
                                        putString("ad_format", "rewarded")
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
                })
        }
    }

    fun showRewarded(
        activity: Activity,
        rewardGrantedAction: () -> Unit,
        failedAction: () -> Unit
    ) {

        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                firebaseAnalytics?.logEvent("reward_ad_clicked", null)
                Log.i("new_event", "reward_ad_clicked")

            }

            override fun onAdDismissedFullScreenContent() {
                OTHER_AD_ON_DISPLAY = false
                rewardedAd = null
                if(isGranted) {
                    firebaseAnalytics?.logEvent("rewarded_video", null)
                    firebaseAnalytics?.logEvent("reward_ads_both", null)
                    Log.i("new_event", "rewarded_video")
                    Log.i("new_event", "reward_ads_both")
                    rewardGrantedAction.invoke()
                } else {
                    failedAction.invoke()
                }
                timeHandler = Handler(Looper.getMainLooper())
                timeHandler?.postDelayed({
                    activity.loadRewarded( {}, {})
                }, 3.seconds.inWholeMilliseconds)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                OTHER_AD_ON_DISPLAY = false
                rewardedAd = null
                firebaseAnalytics?.logEvent("reward_ad_failed", null)
                Log.i("new_event", "reward_ad_failed")
                timeHandler = Handler(Looper.getMainLooper())
                timeHandler?.postDelayed({
                    activity.loadRewarded( {}, {})
                }, 3.seconds.inWholeMilliseconds)
            }

            override fun onAdImpression() {
                firebaseAnalytics?.logEvent("reward_ad_impression", null)
                Log.i("new_event", "reward_ad_impression")
            }

            override fun onAdShowedFullScreenContent() {
                firebaseAnalytics?.logEvent("reward_ad_show", null)
                Log.i("new_event", "reward_ad_show")
                OTHER_AD_ON_DISPLAY = true
            }
        }

        rewardedAd?.let { ad ->
            ad.show(activity) {
                isGranted = true
            }
        } ?: run {
            failedAction.invoke()
        }
    }
}