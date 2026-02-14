package com.xan.event_notifications.data

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Retrieve the notification ID
        kotlin.runCatching {

            Log.i("showNotification", "onReceive: NotificationReceiver")

            val notificationId = intent.getIntExtra("notification_id", 0)

            // Cancel the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)  // Pass the notification ID to cancel the correct notification
        }
    }
}

