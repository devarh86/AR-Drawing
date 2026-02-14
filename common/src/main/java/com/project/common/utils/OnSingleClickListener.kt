package com.project.common.utils

import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.View
import com.example.inapp.helpers.Constants.isProVersion
import com.project.common.utils.Constants.showProDialogClick8th
import com.project.common.utils.ConstantsCommon.showInterstitialAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.project.common.utils.Constants.isProWithInterOn

class OnSingleClickListener(private val block: () -> Unit) : View.OnClickListener {
    companion object {

        private var lastClickTime = 0L
        private var clickCounter  = 0L

    }

    override fun onClick(view: View) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 300) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        clickCounter++
        Log.i("CURENTCOUNTER", "onClick:$clickCounter ")
       // block()
        if (!isProVersion() &&(clickCounter % 8).toInt() == 0&& isProWithInterOn) {
//            view?.context?.let {
                try {
                    showProDialogClick8th.value = true

                } catch (ex: Exception) {
                    Log.e("error", "onCreate: ", ex)
                }
//            }
        }else{
            block()
        }
        if (!showInterstitialAd) {
            CoroutineScope(IO).launch {
                delay(1000)
                showInterstitialAd = true
            }
        }
    }
}

fun View.setOnSingleClickListener(block: () -> Unit) {
    setOnClickListener(OnSingleClickListener(block))
}