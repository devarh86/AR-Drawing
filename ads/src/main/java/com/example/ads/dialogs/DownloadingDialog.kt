package com.example.ads.dialogs
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.view.View
import android.view.Window
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.R
import com.project.common.databinding.DownloadDialogBinding


fun Activity.createDownloadingDialog(
    baseUrl: String?,
    thumb: String?,
    thumbtype: String?,
) = BottomSheetDialog(this, R.style.BottomSheetDialogNew).apply {
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    setCancelable(false)
    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val binding = DownloadDialogBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.apply {
        loadingView.show()
        loadingView.startShimmer()
        try {
            Glide
                .with(this@createDownloadingDialog)
                .asBitmap()
                .override(500)
                .load("${baseUrl}${thumb}")
                .into(object : CustomTarget<Bitmap>() {
                    override fun onLoadStarted(placeholder: Drawable?) {
                        thumbIv.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@createDownloadingDialog,
                                when (thumbtype?.lowercase()) {
                                    FrameThumbType.PORTRAIT.type.lowercase() -> R.drawable.frame_placeholder_portrait
                                    FrameThumbType.LANDSCAPE.type.lowercase() -> R.drawable.frame_placeholder_landscape
                                    FrameThumbType.SQUARE.type.lowercase() -> R.drawable.frame_placeholder_squre
                                    else -> R.drawable.frame_placeholder_portrait
                                }
                            )
                        )
                    }

                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap>?
                    ) {
                        loadingView.hide()
                        loadingView.stopShimmer()
                        thumbIv.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }catch (_: Exception){}
    }
    if (!this@createDownloadingDialog.isFinishing && !this@createDownloadingDialog.isDestroyed && !isShowing) {
        show()
    }
}

fun BottomSheetDialog.onDismissDialog(timeInMilli: Long, onFinishDownload: () -> Unit) {
    val countDownTimer = object : CountDownTimer(timeInMilli, timeInMilli) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            if (isShowing) dismiss()
            onFinishDownload.invoke()
        }
    }
    countDownTimer.start()
}



@Keep
enum class FrameThumbType(val type: String) {
    PORTRAIT("portrait"),
    SQUARE("square"),
    LANDSCAPE("horizontal")
}


fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}