package com.project.common.utils

import android.os.SystemClock
import android.view.View

class OnSaveSingleClickListener(private val block: () -> Unit) : View.OnClickListener {
        companion object {
            private var lastClickTime = 0L
        }

        override fun onClick(view: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < 300) {
                return
            }
            lastClickTime = SystemClock.elapsedRealtime()

            block()
        }
    }

fun View.setOnSaveSingleClickListener(block: () -> Unit) {
        setOnClickListener(OnSaveSingleClickListener(block))
}
