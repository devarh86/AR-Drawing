package com.project.common.utils

import android.app.Activity
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.databinding.DialogPermissionsLayoutBinding

fun Activity.createPermissionsDialog(
    acceptAction: () -> Unit,
    declineAction: () -> Unit,
) = BottomSheetDialog(this).apply {
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    setCancelable(false)
    with(DialogPermissionsLayoutBinding.inflate(LayoutInflater.from(this@createPermissionsDialog))) {
        setContentView(root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.70).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
        crossImg.setOnSingleClickListener {
            if (!isDestroyed && !isFinishing && isShowing) dismiss()
            declineAction.invoke()
        }

        doneLayout.setOnSingleClickListener {
            if (!isDestroyed && !isFinishing && isShowing) dismiss()
            acceptAction.invoke()
        }

        cancelTxt.setOnSingleClickListener {
            if (!isDestroyed && !isFinishing && isShowing) dismiss()
            declineAction.invoke()
        }

        if (!isDestroyed && !isFinishing && !isShowing) {
            show()
        }
    }
}