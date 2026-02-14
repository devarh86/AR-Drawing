package com.project.common.utils

import android.content.Context
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import java.lang.Exception

object DeviceCheck {

    fun isLowEndDevice(context: Context): Boolean {
        // Get available memory in megabytes
      try {
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            activityManager.getMemoryInfo(memoryInfo)
            val availableMemoryMB = (memoryInfo.availMem / (1024 * 1024)) // Convert to megabytes

            // Check for low-end conditions
            val isLowRAMDevice = availableMemoryMB <= 1024
            val isLowScreenDensity = context.resources.displayMetrics.densityDpi <= 240
            val isOldApiLevel = SDK_INT < Build.VERSION_CODES.O
//        val isLowEndProcessor = Build.BOARD.contains("mt") || Build.BOARD.contains("sc")

            return isLowRAMDevice || isLowScreenDensity || isOldApiLevel
        }catch(ex: Exception){
          return true
        }
    }
}