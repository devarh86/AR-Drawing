package com.xan.event_notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.text.intl.Locale
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.project.common.R
import com.project.common.utils.ConstantsCommon.introOnBoardingCompleted
import com.project.common.utils.setString

class NotificationHelper(
    private val context: Context,
    private val firebaseAnalytics: FirebaseAnalytics?,
) {

    companion object {
        private const val CHANNEL_ID = "general_channel_id_for_alerts"
        private const val CHANNEL_NAME = "General Notifications"
        private const val CHANNEL_DESCRIPTION = "Notification for basic alerts"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Notification channels are required for Android 8+ (API 26+)
        kotlin.runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESCRIPTION
//                    enableLights(true)
//                    lightColor = android.graphics.Color.BLUE
                    enableVibration(true)
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun showNotification() {
        kotlin.runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    showNotifications()
                }
            } else {
                showNotifications()
            }
        }
    }

    private fun getContent(fromAvailableLanguages: Boolean): Pair<String, String> {
        return try {
            if (introOnBoardingCompleted) {
                if (fromAvailableLanguages) {
                    Pair(
                        context.setString(R.string.title_general_notification_without_fo),
                        context.setString(R.string.sub_heading_general_notification_without_fo)
                    )
                } else {
                    Pair("Don't lose your In-Progress photo!", "Finish Editing Now")
                }
            } else {
                if (fromAvailableLanguages) {
                    Pair(
                        context.setString(R.string.title_general_notification_with_fo),
                        context.setString(R.string.sub_heading_general_notification_with_fo)
                    )
                } else {
                    Pair(
                        "Don't miss out on photo editing!",
                        "Open the app to continue editing your photos now"
                    )
                }
            }
        } catch (ex: Exception) {
            Pair("Don't lose your In-Progress photo!", "Finish Editing Now")
        }
    }

   private fun getLanguageList(): List<String> {
        return listOf(
            "in", // Indonesian
            "en", // English
            "bn", // Bengali
            "de", // German
            "pt", // Portuguese
            "ar", // Arabic
            "es", // Spanish
            "zh", // Mandarin
            "ru", // Russian
            "ur", // Urdu
            "fr", // French
            "hi", // Hindi
            "ja"  // Japanese
        )
    }

    @SuppressLint("MissingPermission")
    private fun showNotifications() {
        kotlin.runCatching {
            val languageSelected = Locale.current.language
            val listOfAvailableLanguages = getLanguageList()
            val content = try {
                getContent(listOfAvailableLanguages.contains(languageSelected.lowercase()))
            } catch (ex: Exception) {
                getContent(false)
            }
            val intent = Intent().apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            intent.setClassName(
                context,
                "com.fahad.newtruelovebyfahad.ui.activities.SplashActivity"
            )

            intent.putExtra("from_general_notification", true)

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            // Build the notification
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.camera_icon_notification) // Replace with your icon
                .setContentTitle(content.first)
                .setContentText(content.second)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for heads-up notifications
                .setAutoCancel(true) // Dismiss notification on click
                .setContentIntent(pendingIntent)

//            kotlin.runCatching {
//                val icon = Icon.createWithResource(context, R.drawable.app_icon_notification)
//                notificationBuilder.setLargeIcon(icon)
//            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//                notificationBuilder.setStyle(
//                    NotificationCompat.DecoratedCustomViewStyle().
//                )
//            }

//            kotlin.runCatching {
//                notificationBuilder.setColor(ContextCompat.getColor(context, R.color.selected_color))
//            }

            firebaseAnalytics?.logEvent("noti_hide_app_view", null)

            // Show the notification
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, notificationBuilder.build())
            }
        }
    }
}
