package com.xan.event_notifications.data

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService

private const val TAG = "AlarmReceiver"

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        kotlin.runCatching {
            context?.let {
                Log.i("showNotification", "onReceive: AlarmReceiver")
//                if (!isPhoneLocked(context)) {

                kotlin.runCatching {
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(200)
                    notificationManager.cancel(1001)
                }

                val runnerNotifier = NotificationLockScreenHelper(it)
                val id = intent?.getIntExtra("id", 0)
                runnerNotifier.showNotification(context, id)
//                }
//                else{
//                    val serviceIntent = Intent(context, MyService::class.java)
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                        context.startForegroundService(serviceIntent) // Use startForegroundService for Android 8+
//                    } else {
//                        context.startService(serviceIntent)
//                    }
//                }
            }
        }.onFailure {
            Log.i("showNotification", "onReceive: $it")
        }
    }

    private fun isPhoneLocked(context: Context): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isKeyguardLocked
    }

}