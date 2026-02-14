package com.project.common.remote_config

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class NotificationSettings(
    @SerializedName("is_lock_screen_noti_enabled") var isLockScreenNotiEnabled: Boolean,
    @SerializedName("time_push_noti_lockscreen_1") var timePushNotiLockscreen1: Long,
    @SerializedName("time_push_noti_lockscreen_2") var timePushNotiLockscreen2: Long,
    @SerializedName("noti_lockscreen_country") var notiLockscreenCountry: String,
    @SerializedName("lockscreen_days_1") var lockscreenDays1: List<Int>,
    @SerializedName("lockscreen_days_2") var lockscreenDays2: List<Int>,
    @SerializedName("is_exit_notification_enable") var isExitNotificationEnable: Boolean,
)
