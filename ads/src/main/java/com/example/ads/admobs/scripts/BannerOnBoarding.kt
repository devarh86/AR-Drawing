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
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.example.ads.Constants.firebaseAnalytics
import com.example.ads.Constants.loadBannerOnResume
import com.example.ads.R
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
import androidx.core.view.isEmpty
import com.example.ads.admobs.utils.logRevenue

class BannerOnBoarding {
    private var bannerView: AdView? = null
    private var previousBanner: AdView? = null
    private var newAdLoad = true
    var mAdSize = AdSize.MEDIUM_RECTANGLE
    var obIndex = 0
    private var crossBanner: WeakReference<ImageView>? = null
    private var frameLayout: WeakReference<FrameLayout>? = null
    private var shimmerFrameLayout: WeakReference<ShimmerFrameLayout>? = null
    private var container: WeakReference<ConstraintLayout>? = null

    fun preLoadBanner(activity: Activity) {
        if (bannerView == null)
            loadAdaptiveBanner(activity, true)
    }

    private fun loadAdaptiveBanner(
        activity: Activity,
        preLoad: Boolean = false
    ) {

        kotlin.runCatching {
            newAdLoad = false
            previousBanner = bannerView
            bannerView = AdView(activity.applicationContext)
            bannerView?.apply {
                adUnitId =
                    if (obIndex == 0) {
                        ContextCompat.getString(activity, R.string.banner_onboarding_all)
                    } else if (obIndex == 1) {
                        ContextCompat.getString(activity, R.string.banner_onboarding_medium)
                    } else {
                        ContextCompat.getString(activity, R.string.banner_onboarding_high)
                    }
                setAdSize(
                    if (mAdSize == AdSize.MEDIUM_RECTANGLE) AdSize.MEDIUM_RECTANGLE else adSizeBanner(
                        activity
                    )
                )
                val adRequest = AdRequest.Builder().build()
                loadAd(adRequest)
                adListener = object : AdListener() {
                    override fun onAdClicked() {}
                    override fun onAdClosed() {}
                    override fun onAdOpened() {}
                    override fun onAdImpression() {}
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e("REASONFAILED", adError.toString())
                        Log.d("FAHAD_BANNER", "onAdFailedToLoad: ${adError.message}")
                        bannerView = null
                        newAdLoad = true
                        if (!activity.isFinishing && !activity.isDestroyed && !preLoad) {
                            container?.get()?.visibility = View.GONE
                            frameLayout?.get()?.visibility = View.GONE
                            shimmerFrameLayout?.get()?.visibility = View.GONE
                        }
                    }

                    override fun onAdLoaded() {
                        Log.d("FAHAD_BANNER", "onAdLoaded")

                        newAdLoad = true
                        previousBanner = null
                        if (!activity.isFinishing && !activity.isDestroyed && !preLoad) {
                            container?.get()?.visibility = View.VISIBLE
                            frameLayout?.get()?.visibility = View.VISIBLE
                            crossBanner?.get()?.visibility = View.INVISIBLE
                            shimmerFrameLayout?.get()?.visibility = View.GONE
                            frameLayout?.get()?.removeAllViews()
                            frameLayout?.get()?.addView(this@apply)
                        }
                        bannerView?.onPaidEventListener =
                            OnPaidEventListener { adValue ->
                                kotlin.runCatching {
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
    }

    fun showAdaptiveBannerAd(
        activity: Activity,
        container: ConstraintLayout,
        crossBanner: ImageView?,
        frameLayout: FrameLayout,
        shimmerFrameLayout: ShimmerFrameLayout
    ) {

        kotlin.runCatching {
            this.container = WeakReference(container)
            this.crossBanner = WeakReference(crossBanner)
            this.frameLayout = WeakReference(frameLayout)
            this.shimmerFrameLayout = WeakReference(shimmerFrameLayout)

            previousBanner?.let {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    this.frameLayout?.get()?.visibility = View.VISIBLE
                    this.container?.get()?.visibility = View.VISIBLE
                    this.shimmerFrameLayout?.get()?.visibility = View.GONE
                    runCatching {
                        val list = frameLayout.children
                        if (list.count() == 0 || list.first() != it) {
                            frameLayout.removeAllViews()
                            if (it.parent != null) {
                                (it.parent as ViewGroup).removeView(it)
                            }
                            frameLayout.addView(it)
                        }
                    }
                }
                if (loadBannerOnResume && newAdLoad) {
                    loadAdaptiveBanner(
                        activity,
                        false
                    )
                }
            } ?: run {
                bannerView?.let {
                    if (!activity.isFinishing && !activity.isDestroyed) {
                        this.frameLayout?.get()?.visibility = View.VISIBLE
                        this.container?.get()?.visibility = View.VISIBLE
                        this.shimmerFrameLayout?.get()?.visibility = View.GONE
                        runCatching {
                            val list = frameLayout.children
                            if (list.count() == 0 || list.first() != it) {
                                frameLayout.removeAllViews()
                                if (it.parent != null) {
                                    (it.parent as ViewGroup).removeView(it)
                                }
                                frameLayout.addView(it)
                            }
                        }
                    }
                    if (loadBannerOnResume && newAdLoad) {
                        loadAdaptiveBanner(
                            activity,
                            false
                        )
                    }
                } ?: run {
                    loadAdaptiveBanner(
                        activity,
                        false
                    )
                }
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