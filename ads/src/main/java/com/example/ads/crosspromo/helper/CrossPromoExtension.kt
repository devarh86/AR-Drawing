package com.example.ads.crosspromo.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.TransactionTooLargeException
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import com.bumptech.glide.Glide
import com.example.ads.Constants
import com.example.ads.crosspromo.api.retrofit.model.CrossPromoItem
import com.example.ads.crosspromo.scripts.CrossPromoInterstitialAdsActivity
import com.example.inapp.helpers.Constants.isProVersion
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.analytics.FirebaseAnalytics

fun Context?.isNetworkAvailable(): Boolean {
    this?.let {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val isAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = cm?.activeNetwork ?: return false
            val actNw = cm.getNetworkCapabilities(networkCapabilities) ?: return false
            when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> true
            }
        } else {
            cm?.let { it.activeNetworkInfo?.isConnected } ?: run { false }
        }
        return isAvailable
    }
    return false
}

fun Activity.openUrl(appUri: Uri) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, appUri))
    } catch (e: TransactionTooLargeException) {
        e.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Activity.setAnimation(
    firebase: FirebaseAnalytics?,
    imageView: ImageView,
    placement: String,
    list: List<CrossPromoItem>,
    onInitializationCompleted: (() -> Unit)? = null
) {
    var currentLink = ""
    var currentAdType = ""
    var currentAppName = ""
    var currentAdAppName = ""
    ExtendedAnimationDrawable(this, list,
        onFrameChanged = { link, adtype, appName, adAppName ->
            currentLink = link
            currentAdType = adtype
            currentAppName = appName
            currentAdAppName = adAppName
            firebase?.logEvent(
                "${adAppName}_${placement}_${adtype}".replace(
                    ".",
                    "_"
                ), null
            )
            Log.d("FAHAD_CROSS", "${adAppName}_${placement}_${adtype}".replace(".", "_"))
        },
        onInitializationCompleted = {
            imageView.apply {
                post {
                    setImageDrawable(it)
                    it.start()
                    setOnClickListener {
                        if (currentLink.isNotBlank()) {
                            firebase?.logEvent(
                                "${currentAdAppName}_${placement}_${currentAdType}".replace(
                                    ".",
                                    "_"
                                ), Bundle().apply { putString("ctr", "ctr") }
                            )
                            Log.d(
                                "FAHAD_CROSS",
                                "${currentAdAppName}_${placement}_${currentAdType}_ctr".replace(
                                    ".",
                                    "_"
                                )
                            )
                            openUrl(Uri.parse(currentLink))
                        }
                    }
                }
                onInitializationCompleted?.invoke()
            }
        }
    )
}

fun Activity?.showNativeCrossPromo(
    firebase: FirebaseAnalytics,
    shimmerFrameLayout: ShimmerFrameLayout,
    frameLayout: FrameLayout,
    placement: String,
    crossPromoAdList: List<CrossPromoItem>,
    width: Int,
    height: Int
) {
    if (!isProVersion()) {
        this?.let {
            crossPromoAdList.let { crossPromoAdList ->
                val adView = ImageView(it)
                adView.layoutParams = ViewGroup.LayoutParams(
                    if (width != Int.MAX_VALUE) resources.getDimensionPixelSize(width) else MATCH_PARENT,
                    if (height != Int.MAX_VALUE) resources.getDimensionPixelSize(height) else (resources.displayMetrics.widthPixels * 0.6).toInt()
                )
                adView.scaleType = ImageView.ScaleType.FIT_XY
                setAnimation(firebase, adView, placement, crossPromoAdList) {
                    frameLayout.removeAllViews()
                    if (adView.parent != null) {
                        (adView.parent as ViewGroup).removeView(adView)
                    }
                    frameLayout.addView(adView)
                    frameLayout.show()
                    shimmerFrameLayout.hide()
                }
            }
        }
    } else {
        frameLayout.hide()
        shimmerFrameLayout.hide()
    }
}

fun Activity?.setInterstitialCrossPromo(
    placement: String,
    crossPromoAdList: List<CrossPromoItem>,
) {
    Constants.placement = placement
    Constants.crossPromoAdsList = crossPromoAdList
    this?.let {
        crossPromoAdList.forEach {
            Glide.with(applicationContext)
                .load(it.link)
                .preload()
        }
    }
}

fun Activity?.showInterstitialCrossPromo(
    crossPromoAction: () -> Unit,
    activityResultLauncher: ActivityResultLauncher<Intent>
) {
    if (Constants.crossPromoAdsList.isNotEmpty()) {
        this?.apply {
            try {
                activityResultLauncher.launch(
                    Intent(
                        this@showInterstitialCrossPromo,
                        CrossPromoInterstitialAdsActivity::class.java
                    )
                )
            } catch (_: Exception) {
                crossPromoAction.invoke()
            }
        } ?: run {
            crossPromoAction.invoke()
        }
    } else {
        crossPromoAction.invoke()
    }
}

fun Activity?.showInterstitialCrossPromo(
    firebase: FirebaseAnalytics?,
    frameLayout: FrameLayout,
    placement: String,
    crossPromoAdList: List<CrossPromoItem>,
    width: Int = Int.MAX_VALUE,
    height: Int = Int.MAX_VALUE,
) {
    if (!isProVersion()) {
        this?.let {
            crossPromoAdList.let { crossPromoAdList ->
                val adView = ImageView(it)
                adView.layoutParams = ViewGroup.LayoutParams(
                    if (width != Int.MAX_VALUE) resources.getDimensionPixelSize(width) else MATCH_PARENT,
                    if (height != Int.MAX_VALUE) resources.getDimensionPixelSize(height) else MATCH_PARENT
                )
                adView.scaleType = ImageView.ScaleType.FIT_XY
                setAnimation(firebase, adView, placement, crossPromoAdList) {
                    frameLayout.removeAllViews()
                    if (adView.parent != null) {
                        (adView.parent as ViewGroup).removeView(adView)
                    }
                    frameLayout.addView(adView)
                    frameLayout.show()
                }
            }
        }
    } else {
        frameLayout.hide()
    }
}


fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun Int.isOdd() = this % 2 == 1
fun Int.isEven() = this % 2 == 0