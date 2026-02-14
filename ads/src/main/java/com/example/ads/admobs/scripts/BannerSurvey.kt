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
import androidx.core.view.contains
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
import java.lang.ref.WeakReference

class BannerSurvey {
    private var bannerView: AdView? = null
    private var alreadyLoading = false

    private var bannerFailed = 0

    private var isMedium = false
    private var crossBanner: WeakReference<ImageView>? = null
    private var frameLayout: WeakReference<FrameLayout>? = null
    private var shimmerFrameLayout: WeakReference<ShimmerFrameLayout>? = null
    private var container: WeakReference<ConstraintLayout>? = null

    private var lastLoadedTime = 0L

    private fun loadAdaptiveBanner(
        activity: Activity,
        bnrLoadingFailed: Boolean = false,
    ) {
        if (alreadyLoading)
            return

        alreadyLoading = true
        bannerView = AdView(activity.applicationContext)
        bannerView?.apply {
            adUnitId =
                if (!bnrLoadingFailed) {
                    activity.applicationContext.getString(R.string.banner_bottom_medium)
                } else {
                    Log.e("BANNER_SEC", "loadAdaptiveBanner: second banner id  ")
                    activity.applicationContext.getString(R.string.banner_bottom_backup)
                }
            setAdSize(AdSize.MEDIUM_RECTANGLE)
            val adRequest = AdRequest.Builder().build()
            loadAd(adRequest)
            adListener = object : AdListener() {

                override fun onAdClicked() {}

                override fun onAdClosed() {}

                override fun onAdOpened() {}

                override fun onAdImpression() {}

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("FAHAD_BANNER", "onAdFailedToLoad: ${adError.message}")
                    bannerView = null
                    alreadyLoading = false
                    if (!activity.isDestroyed && !activity.isFinishing) {
                        bannerFailed += 1
                        if (bannerFailed < 2) {
                            loadAdaptiveBanner(
                                activity
                            )
                        } else {
                            container?.get()?.visibility = View.GONE
                            frameLayout?.get()?.visibility = View.GONE
                            shimmerFrameLayout?.get()?.visibility = View.GONE
                            bannerFailed = 0
                        }
                    }
                }

                override fun onAdLoaded() {
                    bannerFailed = 0
                    Log.d("FAHAD_BANNER", "onAdLoaded")

                    Log.d(
                        "showAdaptiveBannerAd",
                        "onAdLoaded ${(!activity.isDestroyed && !activity.isFinishing)}"
                    )

                    if (!activity.isDestroyed && !activity.isFinishing) {
                        container?.get()?.visibility = View.VISIBLE
                        frameLayout?.get()?.visibility = View.VISIBLE
                        crossBanner?.get()?.visibility = View.INVISIBLE
                        shimmerFrameLayout?.get()?.visibility = View.GONE
                        frameLayout?.get()?.removeAllViews()
                        Log.i("TAG", "showAdaptiveBannerAd: addviewNew")
                        frameLayout?.get()?.addView(this@apply)
                    }

                    lastLoadedTime = System.currentTimeMillis()

                    alreadyLoading = false

                    bannerView?.onPaidEventListener =
                        OnPaidEventListener { adValue ->
                            runCatching {
                                val loadedAdapterResponseInfo =
                                    bannerView?.responseInfo?.loadedAdapterResponseInfo
                                val adSourceName = loadedAdapterResponseInfo?.adSourceName
                                val adSourceId = loadedAdapterResponseInfo?.adSourceId

                                val bundle = Bundle().apply {
                                    putString("ad_source", "AdMob")
                                    putString(
                                        "source_activity",
                                        "general_banner_ad"
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
                }
            }
        }
    }

    fun showAdaptiveBannerAd(
        activity: Activity,
        container: ConstraintLayout,
        crossBanner: ImageView?,
        frameLayout: FrameLayout,
        shimmerFrameLayout: ShimmerFrameLayout
    ) {

        this.container = WeakReference(container)
        this.crossBanner = WeakReference(crossBanner)
        this.frameLayout = WeakReference(frameLayout)
        this.shimmerFrameLayout = WeakReference(shimmerFrameLayout)

        Log.i("TAG", "showAdaptiveBannerAd: $alreadyLoading")

        runCatching {
            bannerView?.let { bannerView ->
                if (!alreadyLoading) {
                    this.frameLayout?.get()?.let {
                        if (!it.contains(bannerView)) {
                            runCatching {
                                Log.i("TAG", "showAdaptiveBannerAd containsCheck")
                                this.frameLayout?.get()?.visibility = View.VISIBLE
                                this.container?.get()?.visibility = View.VISIBLE
                                this.shimmerFrameLayout?.get()?.visibility = View.GONE
                                this.frameLayout?.get()?.removeAllViews()
                                if (bannerView.parent != null) {
                                    (bannerView.parent as ViewGroup).removeView(bannerView)
                                }
                                this.frameLayout?.get()?.addView(bannerView)
                            }.onFailure {
                                Log.i("TAG", "showAdaptiveBannerAd: $it")
                            }
                        } else {
                            this.frameLayout?.get()?.visibility = View.VISIBLE
                            this.container?.get()?.visibility = View.VISIBLE
                            this.shimmerFrameLayout?.get()?.visibility = View.GONE
                        }
                    }
                }
                loadAdaptiveBanner(
                    activity
                )
            } ?: run {
                loadAdaptiveBanner(
                    activity
                )
            }
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