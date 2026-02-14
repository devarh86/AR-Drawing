package com.example.ads.dialogs

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Window
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.ads.Constants.rewardTime
import com.example.ads.Constants.showAllReward
import com.example.ads.R
import com.example.ads.admobs.utils.loadRewarded
import com.example.ads.admobs.utils.loadRewardedInterstitial
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.show
import com.example.ads.databinding.RewardedIntersitialDialogBinding
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.example.inapp.helpers.showToast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.api.ResourceProto.resource
import java.lang.Exception

fun Activity.createProFramesDialog(
    showRewardAd: Boolean = false,
    thumb: String,
    thumbType: Drawable?,
    action: () -> Unit,
    goProAction: () -> Unit,
    dismissAction: () -> Unit,
    isPro: Boolean = false
) {
    if (isPro) {
        if (!isProVersion()) goProAction.invoke() else showToast("Already Purchased")
    } else {
        BottomSheetDialog(this, com.project.common.R.style.BottomSheetDialogNew).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val binding = RewardedIntersitialDialogBinding.inflate(layoutInflater)
            setContentView(binding.root)
            binding.loadingView.show()
            binding.loadingView.startShimmer()
            try {
                if(thumb.isNotEmpty()) {
                    Glide
                        .with(this@createProFramesDialog.applicationContext)
                        .asBitmap()
                        .override(500)
                        .skipMemoryCache(false)
                        .load(thumb)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onLoadStarted(placeholder: Drawable?) {
                                binding.thumbIv.setImageDrawable(
                                    thumbType
                                )
                            }

                            override fun onResourceReady(
                                resource: Bitmap, transition: Transition<in Bitmap>?
                            ) {
                                binding.loadingView.hide()
                                binding.loadingView.stopShimmer()
                                binding.thumbIv.setImageBitmap(resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })
                }else{
                    binding.loadingView.hide()
                    binding.thumbIv.hide()
                    binding.loadingView.stopShimmer()
                }
            } catch (_: Exception) {
            }

            var countDownTimer: CountDownTimer? = null

            if (showRewardAd || showAllReward) {
                binding.watchAdTimerTv.text = "Watch Ad to unlock"
                binding.watchAd.setOnClickListener {
                    if (!this@createProFramesDialog.isFinishing && !this@createProFramesDialog.isDestroyed && isShowing)
                        dismiss()
                    action.invoke()
                }
                loadRewarded({}, {})
            } else {
                if (!this@createProFramesDialog.isFinishing && !this@createProFramesDialog.isDestroyed && isShowing)
                    dismiss()
                action.invoke()
             //   loadRewardedInterstitial({}, {})
             /*   var timeCounter = rewardTime
                countDownTimer = object : CountDownTimer(rewardTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        timeCounter -= 1000
                        binding.watchAdTimerTv.text = "Ad Starting in... ${timeCounter / 1000}s"
                    }

                    override fun onFinish() {
                        dismiss()
                        action.invoke()
                    }
                }
                countDownTimer.start()*/
            }
            binding.crossImg.setOnClickListener {
                runCatching {
                    countDownTimer?.cancel()
                }
                dismiss()
            }
            binding.pro.setOnClickListener {
                dismiss()
                if (!isProVersion()) goProAction.invoke() else showToast("Already Purchased")
            }

            setOnDismissListener {
                runCatching {
                    countDownTimer?.cancel()
                }
                dismissAction.invoke()
            }
            if (!this@createProFramesDialog.isFinishing && !this@createProFramesDialog.isDestroyed && !isShowing) {
                show()
            }
        }
    }
}