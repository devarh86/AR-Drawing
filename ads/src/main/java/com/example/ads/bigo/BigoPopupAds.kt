package com.example.ads.bigo

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import com.example.ads.R
import com.google.android.gms.ads.AdValue
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData



fun Activity?.loadBigoPopup(onAdClosed: () -> Unit) {

   /* showProgress()

    kotlin.runCatching {
        val popupAdLoader = PopupAdLoader.Builder()
            .withAdLoadListener(object : AdLoadListener<PopupAd> {
                override fun onError(error: AdError) {
                    dismissProgress(this@loadBigoPopup)
                    this@loadBigoPopup?.findViewById<FrameLayout>(R.id.pop_up_overlay)?.let {
                        it.visibility = View.GONE
                    }
//                onAdClosed.invoke()
                    Log.d("adsTag", "onError code: ${error.code}, ${error.message} ")

                    onAdClosed.invoke()
                }

                override fun onAdLoaded(ad: PopupAd) {

                    dismissProgress(this@loadBigoPopup)
                    handlePopupAd(ad) {
                        this@loadBigoPopup?.findViewById<FrameLayout>(R.id.pop_up_overlay)?.let {
                            it.visibility = View.GONE
                        }
                        onAdClosed.invoke()
//                    Log.d("adsTag", "backToHomeBtn: Unit Function")
                    }
                }
            }).build()
        val popupAdRequest =
            PopupAdRequest.Builder().withSlotId(this?.getString(R.string.bigo_popup)).build()
        popupAdLoader.loadAd(popupAdRequest)
    }.onFailure {
        dismissProgress(this@loadBigoPopup)
        this@loadBigoPopup?.findViewById<FrameLayout>(R.id.pop_up_overlay)?.let {
            it.visibility = View.GONE
        }
        onAdClosed.invoke()
    }*/
}

private fun Activity?.showProgress() {

    this?.let {

        kotlin.runCatching {
            findViewById<FrameLayout>(R.id.pop_up_overlay)?.let {
                it.visibility = View.VISIBLE
                findViewById<TextView>(R.id.pop_up_overlay_loading_ad)?.let {
                    it.visibility = View.VISIBLE
                }
            } ?: run {
                try {
                    val layoutParamsNew = LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val frameLayout = FrameLayout(it)
                    frameLayout.layoutParams = layoutParamsNew
                    frameLayout.setBackgroundColor(Color.parseColor("#E6000000"))
                    frameLayout.id = R.id.pop_up_overlay
                    val textView = TextView(it)
                    textView.layoutParams = LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER
                    )
                    textView.setTextColor(ContextCompat.getColor(this@showProgress, R.color.white))
                    textView.textSize = 18f
                    textView.text = "Loading Ad"
                    textView.id = R.id.pop_up_overlay_loading_ad
                    frameLayout.addView(textView)
                    addContentView(frameLayout, layoutParamsNew)

                    if (!frameLayout.hasOnClickListeners()) {
                        frameLayout.setOnClickListener { }
                    } else {
                    }

                } catch (ex: Exception) {
                    Log.e("TAG", "showProgress: ", ex)
                }
            }
        }

//        mProgressDialog = ProgressDialog(this)
//        if (!mProgressDialog.isShowing) {
//            mProgressDialog.show()
//        }
    }
}

//lateinit var mProgressDialog: ProgressDialog


private fun dismissProgress(activity: Activity?) {
    activity?.findViewById<TextView>(R.id.pop_up_overlay_loading_ad)?.let {
        it.visibility = View.GONE
    }
}


/*
fun handlePopupAd(ad: PopupAd, onAdClosed: () -> Unit) {
    val listener: AdInteractionListener = object : AdInteractionListener {
        override fun onAdError(@NonNull error: AdError) {
            // There's something wrong when using this ad.
            Log.d("adsTag", "onAdError: $error")
            onAdClosed.invoke()
        }

        override fun onAdImpression() {
            // When the ad appears on the screen.
            Log.d("adsTag", "onAdImpression")
//            ad?.
//            ad.bid?.price?.let {
//                Log.d("adsTag", "onAdImpression $it")
//                kotlin.runCatching {
//                    val impressionData: AdValue = adValue
//                    val data = SingularAdData(
//                        "AdMob",
//                        impressionData.currencyCode,
//                        impressionData.valueMicros / 1000000.0
//                    )
//                    Singular.adRevenue(data)
//                }
//            }
        }

        override fun onAdClicked() {
            // When the user clicks on the ad.
            Log.d("adsTag", "onAdClicked")
        }

        override fun onAdOpened() {
            // When the fullsceen ad covers the screen.
            Log.d("adsTag", "onAdOpened")
        }

        override fun onAdClosed() {
            // When the fullsceen ad closes.
            Log.d("adsTag", "onAdClosed: ")
            onAdClosed.invoke()
        }
    }
    ad.setAdInteractionListener(listener)
    ad.show()
}*/
