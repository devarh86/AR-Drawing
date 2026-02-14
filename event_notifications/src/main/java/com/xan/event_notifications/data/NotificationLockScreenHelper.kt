package com.xan.event_notifications.data

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.ads.Constants.languageCode
import com.example.event_notifications.R
import com.xan.event_notifications.data.constants.Constants.notiLockscreenCountry
import com.xan.event_notifications.model.NotificationData
import com.xan.event_notifications.model.ReminderItem
import java.util.Calendar
import java.util.Locale

class NotificationLockScreenHelper(private var context: Context) {

    private val notificationChannelId: String = "runner_channel_id"
    private val notificationChannelName: String = "Running Notification"
    private val notificationId: Int = 200

    fun scheduleAlarmForConditions(
        notiLockscreenCountry: String,
        timePushNotiLockscreen1: Long?,
        timePushNotiLockscreen2: Long?,
    ) {
        /*||currentDay == Calendar.SATURDAY*/
        kotlin.runCatching {
            val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val notificationLockScreen1Day =
                currentDay ==  Calendar.MONDAY || currentDay == Calendar.TUESDAY || currentDay == Calendar.WEDNESDAY || currentDay == Calendar.THURSDAY || currentDay == Calendar.FRIDAY|| currentDay == Calendar.SATURDAY||  currentDay == Calendar.SUNDAY
            val notificationLockScreen2Day =
                currentDay ==  Calendar.MONDAY || currentDay == Calendar.TUESDAY || currentDay == Calendar.WEDNESDAY || currentDay == Calendar.THURSDAY || currentDay == Calendar.FRIDAY|| currentDay == Calendar.SATURDAY||  currentDay == Calendar.SUNDAY

            val isFromUS = isUserInUS()
                  //  && isFromUS
            if (notiLockscreenCountry == "1" ) {

                Log.d("showNotification", "country condition 1")
                timePushNotiLockscreen1?.let { timePushNotiLockscreen ->
                    checkForDay(
                        timePushNotiLockscreen,
                        null,
                        notificationLockScreen1Day,
                        notificationLockScreen2Day
                    )
                }
            } else if (notiLockscreenCountry == "2") {

                timePushNotiLockscreen2?.let { timePushNotiLockscreen ->
                    checkForDay(
                        null,
                        timePushNotiLockscreen,
                        notificationLockScreen1Day,
                        notificationLockScreen2Day
                    )
                }
            } else if (notiLockscreenCountry == "1,2") {
                timePushNotiLockscreen1?.let { timePushNotiLockscreen ->
                    checkForDay(
                        timePushNotiLockscreen,
                        null,
                        notificationLockScreen1Day,
                        notificationLockScreen2Day
                    )
                }
                timePushNotiLockscreen2?.let { timePushNotiLockscreen ->
                    checkForDay(
                        null,
                        timePushNotiLockscreen,
                        notificationLockScreen1Day,
                        notificationLockScreen2Day
                    )
                }
            } else {

            }
        }
    }

    private fun checkForDay(
        timePushNotiLockscreen1: Long?,
        timePushNotiLockscreen2: Long?,
        notificationLockScreen1Day: Boolean,
        notificationLockScreen2Day: Boolean,
    ) {
        kotlin.runCatching {
            timePushNotiLockscreen1?.let { timePushNotiLockscreen ->
                if (notificationLockScreen1Day) {
                    val timeInMillis = getTimeInMillisForNotification(timePushNotiLockscreen)
                    timeInMillis.let { time ->
                        val reminderItem = ReminderItem(
                            1,
                            id = 1,
                           time =time
                           // time =System.currentTimeMillis()+2000
                                //
                        )
                        schedule(reminderItem)
                    }
                }
            }
            timePushNotiLockscreen2?.let { timePushNotiLockscreen ->
                if (notificationLockScreen2Day) {
                    val timeInMillis = getTimeInMillisForNotification(timePushNotiLockscreen)
                    timeInMillis.let { time ->
                        val reminderItem = ReminderItem(
                            2,
                            id = 1,
                            time = time

                        )
                        schedule(reminderItem)
                    }
                }
            }
        }

    }

    private fun isUserInUS(): Boolean {
        try {
            val cCode: String
            val deviceLocales: Locale = Resources.getSystem().configuration.getLocales().get(0)
            cCode = deviceLocales.country
            return cCode == "US"
        } catch (ex: Exception) {
            return false
        }
    }

    private fun getTimeInMillisForNotification(timePushNotification: Long): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timePushNotification.toInt())  //for build
          // add(Calendar.HOUR_OF_DAY, 0) //for testing
            set(Calendar.MINUTE, 0) //for build
           // add(Calendar.MINUTE, 1) //for testing
            set(Calendar.SECOND, 0) //for build
        }
        return calendar.timeInMillis
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(
        importance: Int = NotificationManager.IMPORTANCE_HIGH,
    ): NotificationChannel {
        return NotificationChannel(
            notificationChannelId,
            notificationChannelName,
            importance
        ).apply {
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
    }

    private fun schedule(reminderItem: ReminderItem) {

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        kotlin.runCatching {
//            alarmManager.setExact() also comment interval day
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                reminderItem.time,
//                AlarmManager.INTERVAL_DAY,
                createPendingIntent(reminderItem)
            )
        }.onFailure {
            Log.i("TAG", "scheduleAlarm: $it")
        }
    }

    private fun createPendingIntent(reminderItem: ReminderItem): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("id", reminderItem.timePushNotiLockscreen)

        val customId = System.currentTimeMillis().toInt()

        return PendingIntent.getBroadcast(
            context,
            customId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }


    private fun Context.getLocalizedContext(): Context {
        return runCatching {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val config = Configuration(this.resources.configuration)
            config.setLocale(locale)
            return this.createConfigurationContext(config)
        }.getOrElse {
            Log.e("LocaleError", "Failed to get localized context: ${it.message}")
            this
        }
    }

    fun showNotification(context: Context, id: Int?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        kotlin.runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = createNotificationChannel()
                notificationManager.createNotificationChannel(channel)
            }
            val localizedContext = context.getLocalizedContext()
            if (id == 1) {
                val notificationData1 = NotificationData(
                    localizedContext.getString(com.project.common.R.string.notification_title_1),
                    localizedContext.getString(com.project.common.R.string.notification_description_1),
                    R.drawable.notification_image_one,
                    1
                )
                val notification = buildNotification(notificationData1)
                notification?.let {
                    notificationManager.notify(
                        notificationId,
                        it
                    )
                }
            } else if (id == 2) {
                val notificationData2 = NotificationData(
                    localizedContext.getString(com.project.common.R.string.notification_title_2),
                    localizedContext.getString(com.project.common.R.string.notification_description_2),
                    R.drawable.notification_image_two,
                    2
                )
                val notification = buildNotification(notificationData2)
                notification?.let {
                    notificationManager.notify(
                        notificationId,
                        it
                    )
                }
            } else {
            }
        }
    }



    private fun buildNotification(notification: NotificationData): Notification? {

        try{
        val remoteViews = RemoteViews(context.packageName, R.layout.custom_notification_layout_with_button)

//        val remoteViewsSmall =
//            RemoteViews(context.packageName, R.layout.custom_notification_layout_small)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val fullScreenIntent = createFullScreenIntent() ?: return null

            remoteViews.apply {
                setTextViewText(R.id.notification_title, notification.title)
                setTextViewText(R.id.notification_body, notification.description)
                setImageViewResource(R.id.notification_image, notification.image)
//                setOnClickPendingIntent(R.id.openNotificationBtn, fullScreenIntent)
//                closeNotificationIntent()?.let {
//                    setOnClickPendingIntent(
//                        R.id.closeNotificationBtn,
//                        it
//                    )
//                }
            }

//            remoteViewsSmall.apply {
//                setTextViewText(R.id.notification_title, notification.title)
//                setTextViewText(R.id.notification_body, notification.description)
//            }

            val builder = NotificationCompat.Builder(context, notificationChannelId)
                .setContentIntent(fullScreenIntent)
                .setSmallIcon(com.project.common.R.drawable.camera_icon_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    notificationManager.canUseFullScreenIntent()
                } else {
                    true
                }
            ) {
                if (isPhoneLocked()) {
                    createFullScreenPendingIntent(notification)?.let {
                        builder.setOngoing(true)
                        builder.setFullScreenIntent(it, true)
                    } ?: run {
                        builder.setOngoing(false)
                        builder.setContentTitle(notification.title)
                        builder.setContentText(notification.description)
                    }
                } else {
                    builder.setOngoing(false)
                    builder.setCustomBigContentView(remoteViews)
                    builder.setContentTitle(notification.title)
                    builder.setContentText(notification.description)
                }
            } else {
                builder.setOngoing(false)
                builder.setContentTitle(notification.title)
                builder.setContentText(notification.description)
                builder.setCustomBigContentView(remoteViews)
            }

            notiLockscreenCountry.let {
             /*   firebaseAnalytics?.eventWithParam(
                    AnalyticsConstants.EventName.NOTI_LOCKSCREEN_VIEW,
                    it
                )*/
            }

            return builder.build()
        } catch (ex: java.lang.Exception) {
            return null
        }
    }


    private fun isPhoneLocked(): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isKeyguardLocked
    }

    private fun closeNotificationIntent(): PendingIntent? {
        try {
            val cancelIntent = Intent(context, NotificationReceiver::class.java)
            cancelIntent.putExtra(
                "notification_id",
                notificationId
            )
            return PendingIntent.getBroadcast(
                context,
                0,
                cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } catch (ex: java.lang.Exception) {
            return null
        }
    }

    private fun createFullScreenPendingIntent(notification: NotificationData): PendingIntent? {

        try {

            val fullScreenIntent = Intent().apply {
                setClassName(
                    context,
                    "com.fahad.newtruelovebyfahad.ui.activities.FullScreenIntentActivity"
                )
            }
            fullScreenIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )

            fullScreenIntent.putExtra("title", notification.title)
            fullScreenIntent.putExtra("body", notification.description)
            fullScreenIntent.putExtra("path", notification.image)
            fullScreenIntent.putExtra("id", notification.id)

            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                0,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            return fullScreenPendingIntent
        } catch (ex: java.lang.Exception) {
            return null
        }
    }

    private fun createFullScreenIntent(): PendingIntent? {
        try {
            val intent = Intent()
            intent.setClassName(
                context,
                "com.fahad.newtruelovebyfahad.ui.activities.SplashActivity"
            )
            intent.putExtra("noti_event", true)

            return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } catch (ex: java.lang.Exception) {
            return null
        }
    }
}