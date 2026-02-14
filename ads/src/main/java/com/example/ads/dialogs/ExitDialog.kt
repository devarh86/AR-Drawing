package com.example.ads.dialogs

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.ads.admobs.utils.loadAndShowNativeOnBoarding
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.show
import com.example.ads.utils.nativeExitConfig
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.R
import com.project.common.databinding.ExitDialogBinding


fun Activity.createExitDialog(
): ExitModel? {
    BottomSheetDialog(this, R.style.BottomSheetDialogNew).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val binding = ExitDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        runCatching {
         //   binding.nativeContainer.hide()
                    loadAndShowNativeOnBoarding(loadedAction = {
                        binding.nativeContainer.show()
                        binding.mediumNativeLayout.adContainer.show()
                        binding.mediumNativeLayout.shimmerViewContainer.hide()
                        binding.mediumNativeLayout.adContainer.removeAllViews()
                        if (it?.parent != null) {
                            (it.parent as ViewGroup).removeView(it)
                        }
                        binding.mediumNativeLayout.adContainer.addView(it)

                    }, failedAction = {
                        binding.nativeContainer.hide()
                    },
                        nativeExitConfig()
                    )


        }

        binding.apply {
            discardBtn.setOnClickListener {
                onDismissDialog()
            }
            exitBtn.setOnClickListener {
                onDismissDialog()
                kotlin.runCatching {
                    Log.i("REMTASK", "createExitDialog:--finishAndRemoveTask ")
                    finishAndRemoveTask()
                }.onFailure {
                    Log.i("REMTASK", "createExitDialog:--Failure ")
                }
            }
        }
        if (!this@createExitDialog.isFinishing && !this@createExitDialog.isDestroyed && !isShowing) {
            show()
        }
        return ExitModel(this, binding.nativeContainer)
    }

    return null
}
@Keep
data class ExitModel(
    var dialog: BottomSheetDialog? = null,
    var nativeContainer: ConstraintLayout? = null
)

fun BottomSheetDialog.onDismissDialog() {
    if (isShowing) dismiss()
}

/*
fun Activity.createExitDialog(
): ExitModel? {
    BottomSheetDialog(this, R.style.BottomSheetDialog).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val binding = ExitDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        runCatching {
            if (isProVersion()) {
                binding.nativeContainer.hide()
            } else {
                if (Constants.showNativeExit) {
                    loadAndShowNative(com.example.ads.R.layout.medium_native_ad, {
                        binding.nativeContainer.show()
                        binding.mediumNativeLayout.adContainer.show()
                        binding.mediumNativeLayout.shimmerViewContainer.hide()
                        binding.mediumNativeLayout.adContainer.removeAllViews()
                        if (it?.parent != null) {
                            (it.parent as ViewGroup).removeView(it)
                        }
                        binding.mediumNativeLayout.adContainer.addView(it)
                    }, {
                        binding.nativeContainer.hide()
                    })
                } else {
                    binding.nativeContainer.hide()
                }
            }
        }

        binding.apply {
            discardBtn.setOnClickListener {
                onDismissDialog()
            }
            exitBtn.setOnClickListener {
                onDismissDialog()
                kotlin.runCatching {
                    finishAndRemoveTask()
                }
            }
        }
        if (!this@createExitDialog.isFinishing && !this@createExitDialog.isDestroyed && !isShowing) {
            show()
        }
        return ExitModel(this, binding.nativeContainer)
    }

    return null
}

data class ExitModel(
    var dialog: BottomSheetDialog? = null,
    var nativeContainer: ConstraintLayout? = null
)

fun BottomSheetDialog.onDismissDialog() {
    if (isShowing) dismiss()
}*/
