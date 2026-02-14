package com.example.ads.admobs.scripts

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.example.ads.Constants.firebaseAnalytics
import com.example.ads.R
import com.example.ads.admobs.utils.logRevenue
import com.example.ads.model.AdConfigModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import kotlin.math.abs

class Native {
    var nativeAd: NativeAd? = null
    var adConfig: AdConfigModel? = null
    var nextAdConfig: AdConfigModel? = null
    var previousAdConfig: AdConfigModel? = null
    var currentFragmentOrActivity: MutableList<String> = mutableListOf()
    var lastLoadTime = 0L
    private var timeOut = 0L
    private var loadedAction: ((NativeAdView?) -> Unit)? = null
    private var failedAction: (() -> Unit)? = null
    private var adImpression: (Boolean) -> Unit = {}
    private var nativeAdLayout: Int = R.layout.meta_native_layout_onboarding
    private var adFailedCounter = 0
    private var alreadyLoading = false
    private var isMedium = false

    fun loadAndShowNative(
        activity: Activity,
        nativeAdLayout: Int,
        adId: String,
        loadedAction: (nativeAdView: NativeAdView?) -> Unit,
        failedAction: () -> Unit,
    ) {
        val videoOptions = VideoOptions.Builder().build()
        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(activity, adId)
            .forNativeAd { ad: NativeAd ->
                nativeAd = ad
                ad.setOnPaidEventListener { adValue ->

                    kotlin.runCatching {
                        val loadedAdapterResponseInfo = ad.responseInfo?.loadedAdapterResponseInfo
                        val adSourceName = loadedAdapterResponseInfo?.adSourceName
                        val adSourceId = loadedAdapterResponseInfo?.adSourceId

                        val bundle = Bundle().apply {
                            putString("ad_source", "AdMob")
                            putString(
                                "source_activity",
                                "general_native_ad"
                            )
                            putString("currency_code", adValue.currencyCode)
                            putString("ad_format", "native")
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

                        Log.i(logger, "onAdLoaded: ad_revenue_event, $bundle")
                        logRevenue(adValue.valueMicros / 1000000.0, activity)
                    }

                    val impressionData: AdValue = adValue
                    val data = SingularAdData(
                        "AdMob",
                        impressionData.currencyCode,
                        impressionData.valueMicros / 1000000.0
                    )
                    Singular.adRevenue(data)


                }
                populateNativeAdView(activity, nativeAdLayout, ad, loadedAction, failedAction)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failedAction.invoke()
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                }
            })
            .withNativeAdOptions(
                adOptions
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private var logger = "MobifyAds"

    fun loadAndShowNativeOnBoarding(
        activity: Activity,
        loadedAction: (nativeAdView: NativeAdView?) -> Unit,
        failedAction: () -> Unit,
        nextConfig: AdConfigModel?,
        fromReload: Boolean = false,
        adImpression: (Boolean) -> Unit = {}
    ) {

        Log.i(logger, "loadAndShowNativeOnBoarding: calling")

        this.loadedAction = loadedAction
        this.failedAction = failedAction
        this.adImpression = adImpression
        adConfig?.let { config ->
            this.nativeAdLayout =
                if (config.isMetaLayout && config.metaLayoutId != -1) config.metaLayoutId
                else if (!config.isMetaLayout && config.adMobLayoutId != -1) {
                    config.adMobLayoutId
                } else return
        } ?: return

        Log.i(logger, "loadAndShowNativeOnBoarding: $nativeAd")

        if (nativeAd == null) {
            timeOut = 0L
            nextAdConfig = nextConfig
            loadNextNative(
                activity
            )
        } else {
            previousAdConfig?.let { config ->
                kotlin.runCatching {
                    var event =
                        if (currentFragmentOrActivity.isNotEmpty()) currentFragmentOrActivity.last()
                            .lowercase() else "EMPTY"
                    event = event.plus("_" + config.currentActivityOrFragment.lowercase())
                    firebaseAnalytics?.logEvent(event, null)
                    Log.i(
                        "TAG",
                        "loadAndShowNativeOnBoarding: $event"
                    )
                }
            }

            adConfig?.let { config ->
                this.nativeAdLayout = if (nativeAd?.isMeta() == true) {
                    if (config.metaLayoutId != -1) config.metaLayoutId
                    else if (config.adMobLayoutId != -1) {
                        config.adMobLayoutId
                    } else return
                } else {
                    if (config.adMobLayoutId != -1) {
                        config.adMobLayoutId
                    } else return
                }
            } ?: return

            populateNativeAdView(activity, nativeAdLayout, nativeAd, loadedAction = {
                loadedAction.invoke(it)
                Log.i(logger, "loadAndShowNativeOnBoarding: populateNativeAdView called")
                adConfig = nextConfig
                if (fromReload && abs(System.currentTimeMillis() - lastLoadTime) > 2000L) {

                    Log.i(
                        logger,
                        "loadAndShowNativeOnBoarding: loadNextNative called with time check"
                    )

                    loadNextNative(
                        activity
                    )
                } else if (!fromReload) {

                    Log.i(logger, "loadAndShowNativeOnBoarding: loadNextNative called fromReload")

                    loadNextNative(
                        activity
                    )
                }
            }, failedAction)
        }
    }


    private fun NativeAd.isMeta(): Boolean {
        responseInfo?.mediationAdapterClassName?.let { adapterName ->
            Log.i("TAG", "isMeta: $adapterName")
            if (adapterName.contains("facebook", ignoreCase = true) || adapterName.contains(
                    "meta",
                    ignoreCase = true
                )
            ) {
                return true
            } else {
                return false
            }
        } ?: return false
    }

    fun loadNextNative(
        activity: Activity,
        reload: Boolean = false,
    ) {

        Log.i(logger, "loadNextNative: alreadyLoading $alreadyLoading")

        Log.i(logger, "loadNextNative: forFlooring $reload")

        if (alreadyLoading) {
            return
        }

        alreadyLoading = true

        Log.i(logger, "loadNextNative: adConfig $adConfig")
        Log.i(logger, "loadNextNative: nextAdConfig $nextAdConfig")

        adConfig?.let { config ->
            nextAdConfig.let { nextConfig ->

                val videoOptions = VideoOptions.Builder().build()
                val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
                val adLoader = AdLoader.Builder(
                    activity,
                    if (!reload && config.idHigh.isNotBlank() && config.reloadLimit >= 2) {
                        Log.i(logger, "loadNextNative: idHigh ${config.idHigh}")
                        isMedium = config.reloadLimit == 2
                        config.idHigh

                    } else if (reload && isMedium || (config.idMedium.isNotBlank() && config.reloadLimit == 1 && !reload)) {
                        Log.i(logger, "loadNextNative: idHigh ${config.idMedium}")
                        isMedium = false
                        config.idMedium

                    } else {
                        Log.i(logger, "loadNextNative: idHigh ${config.idBackUp}")
                        isMedium = false
                        config.idBackUp
                    }
                ).forNativeAd { ad: NativeAd ->

                    Log.i(logger, "loadNextNative: loaded")

                    nativeAd = ad

                    val newNativeConfig = config

                    ad.setOnPaidEventListener { adValue ->

                        val loadedAdapterResponseInfo = ad.responseInfo?.loadedAdapterResponseInfo
                        val adSourceName = loadedAdapterResponseInfo?.adSourceName
                        val adSourceId = loadedAdapterResponseInfo?.adSourceId

                        val bundle = Bundle().apply {
                            putString("ad_source", "AdMob")
                            putString(
                                "source_activity",
                                "${(newNativeConfig.currentActivityOrFragment.lowercase())}_native_ad"
                            )
                            putString("ad_format", "native")
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

                        Log.i(logger, "onAdLoaded: ad_revenue_event, $bundle")
                        logRevenue(adValue.valueMicros / 1000000.0, activity)
                        val impressionData: AdValue = adValue
                        val data = SingularAdData(
                            "AdMob",
                            impressionData.currencyCode,
                            impressionData.valueMicros / 1000000.0
                        )
                        Singular.adRevenue(data)

                        ad.setOnPaidEventListener(null)
                    }

                    lastLoadTime = System.currentTimeMillis()

                    alreadyLoading = false
                    previousAdConfig = config
                    adFailedCounter = 0

                    Log.i(
                        logger,
                        "loadNextNative: loaded config Name: ${config.currentActivityOrFragment}"
                    )

                    Log.i(logger, "loadNextNative: loaded current Name: $currentFragmentOrActivity")

                    if (currentFragmentOrActivity.contains(config.currentActivityOrFragment)) {

                        kotlin.runCatching {
                            adConfig?.let { config ->
                                var event =
                                    if (currentFragmentOrActivity.isNotEmpty()) currentFragmentOrActivity.last()
                                        .lowercase() else "EMPTY"
                                event =
                                    event.plus("_" + config.currentActivityOrFragment.lowercase())
                                firebaseAnalytics?.logEvent(event, null)
                                Log.i(
                                    "TAG",
                                    "loadAndShowNativeOnBoarding: $event"
                                )
                            }
                        }

                        loadedAction?.let {
                            failedAction?.let { it1 ->

                                this.nativeAdLayout = if (nativeAd?.isMeta() == true) {
                                    if (config.metaLayoutId != -1) config.metaLayoutId
                                    else if (config.adMobLayoutId != -1) {
                                        config.adMobLayoutId
                                    } else return@forNativeAd
                                } else {
                                    if (config.adMobLayoutId != -1) {
                                        config.adMobLayoutId
                                    } else return@forNativeAd
                                }

                                populateNativeAdView(
                                    activity, nativeAdLayout, ad,
                                    {
                                        if (currentFragmentOrActivity.contains(config.currentActivityOrFragment)) {
                                            if (nextConfig != null && nextConfig.currentActivityOrFragment != config.currentActivityOrFragment) {
                                                adConfig = nextConfig
                                                nextAdConfig = null
                                                loadNextNative(activity)
                                            }
                                            nextAdConfig = null
                                            loadedAction?.invoke(it)
                                        }
                                    }, it1
                                )
                            }
                        }
                    }
//                    else {
//                        adConfig?.let {
//                            if (it.currentActivityOrFragment == "UNINSTALL_NATIVE_OTHER") {
//                                loadedAction?.let {
//                                    failedAction?.let { it1 ->
//                                        populateNativeAdView(
//                                            activity, nativeAdLayout, ad,
//                                            {
//                                                nextAdConfig = null
//                                                loadedAction?.invoke(it)
//                                            }, it1
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
                }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {

                            Log.i(logger, "loadNextNative: failed $adError")

                            adFailedCounter += 1
                            alreadyLoading = false
                            previousAdConfig = config
                            if (adFailedCounter <= config.reloadLimit) {

                                if (config.reloadLimit > 2) {
                                    adFailedCounter = config.reloadLimit + 1
                                }

                                loadNextNative(
                                    activity,
                                    true
                                )
                            } else {
                                Log.e("TAG", "onAdFailedToLoadNative: nativeAd")
                                nativeAd = null
                                adFailedCounter = 0
                                failedAction?.invoke()
                            }
                        }

                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            previousAdConfig = config
                            adFailedCounter = 0
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                            adImpression.invoke(true)
                        }
                    })
                    .withNativeAdOptions(
                        adOptions
                    ).build()

                adLoader.loadAd(AdRequest.Builder().build())
            }
        } ?: run {
            alreadyLoading = false
            return
        }
    }

    fun loadNative(
        activity: Activity,
        loadedAction: (nativeAd: NativeAd?) -> Unit,
        failedAction: () -> Unit,
    ) {
        try {
            val adLoader = AdLoader.Builder(
                activity,
                activity.applicationContext.getString(R.string.native_language_back_up)
            ).forNativeAd { ad: NativeAd ->
                nativeAd = ad
                loadedAction.invoke(nativeAd)
                ad.setOnPaidEventListener { adValue ->
                    val impressionData: AdValue = adValue
                    val data = SingularAdData(
                        "AdMob",
                        impressionData.currencyCode,
                        impressionData.valueMicros / 1000000.0
                    )
                    Singular.adRevenue(data)

                    kotlin.runCatching {
                        val loadedAdapterResponseInfo = ad.responseInfo?.loadedAdapterResponseInfo
                        val adSourceName = loadedAdapterResponseInfo?.adSourceName
                        val adSourceId = loadedAdapterResponseInfo?.adSourceId

                        val bundle = Bundle().apply {
                            putString("ad_source", "AdMob")
                            putString(
                                "source_activity",
                                "general_native_ad"
                            )
                            putString("ad_format", "native")
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

                        Log.i(logger, "onAdLoaded: ad_revenue_event, $bundle")
                        logRevenue(adValue.valueMicros / 1000000.0, activity)
                    }
                }
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failedAction.invoke()
                }
            })
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        } catch (_: Exception) {
        }
    }

    fun populateNativeAdView(
        activity: Activity,
        nativeAdLayout: Int,
        nativeAd: NativeAd?,
        loadedAction: (nativeAdView: NativeAdView?) -> Unit,
        failedAction: () -> Unit,
    ) {
        try {
            Log.d(
                "ActivityState",
                "isFinishing 0 : ${activity.isFinishing}, isDestroyed: ${activity.isDestroyed}"
            )
            nativeAd?.let {
                val inflater =
                    activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val adView = try {
                    inflater.inflate(nativeAdLayout, null) as NativeAdView
                } catch (ex: ClassCastException) {
                    return
                }

                adView.mediaView = adView.findViewById(R.id.ad_media)
                adView.headlineView = adView.findViewById(R.id.ad_headline)
                adView.bodyView = adView.findViewById(R.id.ad_body)
                adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
                adView.iconView = adView.findViewById(R.id.ad_app_icon)
                adView.priceView = adView.findViewById(R.id.ad_price)
                adView.starRatingView = adView.findViewById(R.id.ad_stars)
                adView.storeView = adView.findViewById(R.id.ad_store)
                adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

                if (adView.advertiserView == null) {
                    adView.advertiserView = adView.findViewById(R.id.ad_sponsored_label)
                }

                (adView.headlineView as? TextView)?.text = nativeAd.headline
                nativeAd.mediaContent?.let { adView.mediaView?.setMediaContent(it) }

                if (nativeAd.body == null) {
                    adView.bodyView?.visibility = View.INVISIBLE
                } else {
                    adView.bodyView?.visibility = View.VISIBLE
                    (adView.bodyView as? TextView)?.text = nativeAd.body
                }

                if (nativeAd.callToAction == null) {
                    adView.callToActionView?.visibility = View.INVISIBLE
                } else {
                    adView.callToActionView?.visibility = View.VISIBLE
                    (adView.callToActionView as? Button)?.text = nativeAd.callToAction
                }

                if (nativeAd.icon == null) {
                    adView.iconView?.visibility = View.GONE
                } else {
                    (adView.iconView as? ImageView)?.setImageDrawable(
                        nativeAd.icon?.drawable
                    )
                    adView.iconView?.visibility = View.VISIBLE
                }

                if (nativeAd.price == null) {
                    adView.priceView?.visibility = View.INVISIBLE
                } else {
                    adView.priceView?.visibility = View.VISIBLE
                    (adView.priceView as? TextView)?.text = nativeAd.price
                }

                if (nativeAd.store == null) {
                    adView.storeView?.visibility = View.INVISIBLE
                } else {
                    adView.storeView?.visibility = View.VISIBLE
                    (adView.storeView as? TextView)?.text = nativeAd.store
                }

                if (nativeAd.starRating == null) {
                    adView.starRatingView?.visibility = View.INVISIBLE
                } else {
                    (adView.starRatingView as? RatingBar)?.rating = nativeAd.starRating!!.toFloat()
                    adView.starRatingView?.visibility = View.VISIBLE
                }

                if (nativeAd.advertiser == null) {
                    adView.advertiserView?.visibility = View.INVISIBLE
                } else {
                    (adView.advertiserView as? TextView)?.text = nativeAd.advertiser
                    adView.advertiserView?.visibility = View.VISIBLE
                }

                if (!activity.isFinishing && !activity.isDestroyed) {
                    adView.setNativeAd(nativeAd)
                    loadedAction.invoke(adView)
                }
            } ?: run {
                failedAction.invoke()
            }
        } catch (ex: java.lang.Exception) {
            Log.i("TAG", "populateNativeAdView: $ex")
            failedAction.invoke()
        }
    }
}