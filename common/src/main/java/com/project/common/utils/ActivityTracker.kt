package com.project.common.utils

import android.app.Activity
import java.lang.ref.WeakReference

object ActivityTracker {

    private var currentActivity: WeakReference<Activity>? = null

    fun setCurrentActivity(activity: Activity?) {
        currentActivity = if (activity != null) WeakReference(activity) else null
    }

    fun getCurrentActivity(): Activity? = currentActivity?.get()

    fun isActivityActive(): Boolean {
        val activity = getCurrentActivity()
        return activity != null && !activity.isFinishing && !activity.isDestroyed
    }
}