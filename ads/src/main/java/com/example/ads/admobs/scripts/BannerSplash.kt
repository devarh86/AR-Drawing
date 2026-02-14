package com.example.ads.admobs.scripts

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowMetrics
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.ads.Constants.firebaseAnalytics
import com.example.ads.R
import com.example.ads.admobs.utils.logRevenue
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData

class BannerSplash {

    private var bannerView: AdView? = null
    private var bannerFailed = 0
    private var isMedium = false

    private fun loadAdaptiveBanner(
        activity: Activity,
        container: ConstraintLayout,
        crossBanner: ImageView?,
        frameLayout: FrameLayout,
        shimmerFrameLayout: ShimmerFrameLayout,
        bnrLoadingFailed: Boolean = false
    ) {

        bannerView = AdView(activity.applicationContext)
        bannerView?.apply {
            //  adUnitId = activity.applicationContext.getString(R.string.banner)
            adUnitId = if (!bnrLoadingFailed) {
                isMedium = true
                activity.applicationContext.getString(R.string.banner_splash_high)
            } else if (bnrLoadingFailed && isMedium) {
                isMedium = false
                activity.applicationContext.getString(R.string.banner_splash_medium)
            } else {
                isMedium = false
                Log.e("BANNER_SEC", "loadAdaptiveBanner: second banner id ")
                activity.applicationContext.getString(R.string.banner_splash_all)
            }
            setAdSize(adSizeBanner(activity))
            val adRequest = AdRequest.Builder().build()
            loadAd(adRequest)
            adListener = object : AdListener() {
                override fun onAdClicked() {}
                override fun onAdClosed() {}
                override fun onAdOpened() {}
                override fun onAdImpression() {}
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("REASONFAILEDBanner", adError.toString())
                    Log.d("FAHAD_BANNER", "onAdFailedToLoad: ${adError.message}")
                    bannerView = null
                    bannerFailed += 1

                    //  crossBanner?.visibility = View.VISIBLE
                    if (bannerFailed <= 2) {
                        loadAdaptiveBanner(
                            activity,
                            container,
                            crossBanner,
                            frameLayout,
                            shimmerFrameLayout,
                            true
                        )
                    } else {
                        container.visibility = View.GONE
                        frameLayout.visibility = View.GONE
                        shimmerFrameLayout.visibility = View.GONE
                        bannerFailed = 0
                    }
                }

                override fun onAdLoaded() {
                    bannerFailed = 0
                    Log.d("FAHAD_BANNER", "onAdLoaded")
                    container.visibility = View.VISIBLE
                    frameLayout.visibility = View.VISIBLE
                    crossBanner?.visibility = View.INVISIBLE
                    shimmerFrameLayout.visibility = View.GONE
                    bannerView?.onPaidEventListener =
                        OnPaidEventListener { adValue ->

                            kotlin.runCatching {
                                val loadedAdapterResponseInfo = bannerView?.responseInfo?.loadedAdapterResponseInfo
                                val adSourceName = loadedAdapterResponseInfo?.adSourceName
                                val adSourceId = loadedAdapterResponseInfo?.adSourceId

                                val bundle = Bundle().apply {
                                    putString("ad_source", "AdMob")
                                    putString(
                                        "source_activity",
                                        "general_splash_banner_ad"
                                    )
                                    putString("ad_format", "banner")
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
                    frameLayout.removeAllViews()
                    frameLayout.addView(this@apply)
                }
            }
        }
    }

    fun showAdaptiveBannerAd(
        activity: Activity,
        container: ConstraintLayout,
        crossBanner: ImageView?,
        frameLayout: FrameLayout,
        shimmerFrameLayout: ShimmerFrameLayout,
        loadNewAd: Boolean = false
    ) {
        bannerView?.let {
            frameLayout.visibility = View.VISIBLE
            container.visibility = View.VISIBLE
            shimmerFrameLayout.visibility = View.GONE
            frameLayout.removeAllViews()
            if (it.parent != null) {
                (it.parent as ViewGroup).removeView(it)
            }
            frameLayout.addView(it)
            if (loadNewAd) {
                loadAdaptiveBanner(
                    activity,
                    container,
                    crossBanner,
                    frameLayout,
                    shimmerFrameLayout
                )
            }
        } ?: run {
            loadAdaptiveBanner(
                activity,
                container,
                crossBanner,
                frameLayout,
                shimmerFrameLayout
            )
        }

    }

    fun onResume() {
        bannerView?.resume()
    }

    fun onPause() {
        bannerView?.pause()
    }

    private fun adSizeBanner(activity: Activity): AdSize {
        val adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics =
                activity.windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            bounds.width().toFloat()
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(
                displayMetrics
            )
            displayMetrics.widthPixels.toFloat()
        }
        val density = activity.applicationContext.resources.displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            activity.applicationContext,
            adWidth
        )
    }
}