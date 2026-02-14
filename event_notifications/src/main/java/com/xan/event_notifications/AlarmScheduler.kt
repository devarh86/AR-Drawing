package com.xan.event_notifications

import android.app.PendingIntent
import com.xan.event_notifications.model.ReminderItem

interface AlarmScheduler {
    fun createPendingIntent(reminderItem: ReminderItem): PendingIntent

    fun schedule(reminderItem: ReminderItem)

    fun cancel(reminderItem: ReminderItem)
}