package com.fahad.newtruelovebyfahad.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.ads.Constants.appIsActive
import com.example.ads.Constants.appIsForeground
import com.example.ads.Constants.showAllAppOpenAd
import com.example.ads.Constants.showAppOpen
import com.example.ads.admobs.scripts.AppOpen
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Events
import com.example.questions_intro.ui.activity.BlendOnBoardingActivity
import com.example.questions_intro.ui.activity.IntroActivity
import com.example.questions_intro.ui.activity.QuestionsActivity
import com.example.questions_intro.ui.activity.SurveyActivity
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.activities.save.SaveAndShareActivity
import com.project.common.utils.ConstantsCommon
import com.xan.event_notifications.NotificationHelper

class AppObserver(
    private val appOpen: AppOpen,
    private val context: Context
) : LifecycleEventObserver,
    Application.ActivityLifecycleCallbacks {


    private var mCurrentActivity: Activity? = null
    private var appStart: Boolean = true
    private val ramThresholdMb = 250


    private var notificationHelper: NotificationHelper? = null

    fun setNotificationHelper(notificationHelper: NotificationHelper?) {
        this.notificationHelper = notificationHelper
    }

    private fun onBackgroundEntered() {
        appIsForeground = false
    }

    private fun getCurrentRamUsageMb(context: Context): Long? {
        try {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            return (memoryInfo.availMem) / 1024L / 1024L

        } catch (ex: java.lang.Exception) {
            return null
        }
    }

    private fun onForegroundEntered() {

        appIsForeground = true

        val currentRamUsageMb = try {
            getCurrentRamUsageMb(context = context)?.let {
                it
            } ?: -1L
        } catch (ex: Exception) {
            -1L
        }

        Log.i("RamCheck", "onForegroundEntered: $currentRamUsageMb")

        val showAd = if (currentRamUsageMb != -1L && currentRamUsageMb > ramThresholdMb) {
            true
        } else if (currentRamUsageMb == -1L)
            true
        else {
            false
        }

        if (!com.example.inapp.helpers.Constants.isProVersion() && showAd && showAllAppOpenAd && showAppOpen) {//showAppOpenAd
            mCurrentActivity?.let {
                if (!appStart) {
                    when (it) {
                        is MainActivity -> {
                            if (!it.getSplashVisible()) {
                                it.showAppOpen = true
                                it.showAppOpenAd()
                            }
                        }

                        is BlendOnBoardingActivity -> {
                            it.showAppOpen = true
                            it.showAppOpenAd()
                        }

                        is SaveAndShareActivity -> {
                            it.showAppOpen = true
                            it.showAppOpenAd()
                        }

                        is SurveyActivity -> {
                            it.showAppOpen = true
                            it.showAppOpen()
                        }

                        is IntroActivity -> {
                            it.showAppOpen = true
                            it.showAppOpen()
                        }

                        is QuestionsActivity -> {
                            it.showAppOpen = true
                            it.showAppOpen()
                        }

                        else -> {}
                    }
                } else appStart = false
            }
        } else {
            showAppOpen = true
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                this.onBackgroundEntered()
                if (ConstantsCommon.enableGeneralNotification) {
                    notificationHelper?.showNotification()
                } else {
                    ConstantsCommon.enableGeneralNotification = true
                }
            }

            Lifecycle.Event.ON_START -> this.onForegroundEntered()
            Lifecycle.Event.ON_CREATE -> {
                firebaseAnalytics?.logEvent(
                    Events.ApplicationKeys.APPLICATION,
                    Bundle().apply {
                        putString(
                            Events.ApplicationParams.APPLICATION_STATE,
                            Events.ApplicationState.CREATE
                        )
                    })
            }

            Lifecycle.Event.ON_RESUME -> {
                firebaseAnalytics?.logEvent(
                    Events.ApplicationKeys.APPLICATION,
                    Bundle().apply {
                        putString(
                            Events.ApplicationParams.APPLICATION_STATE,
                            Events.ApplicationState.RESUME
                        )
                    })
            }

            Lifecycle.Event.ON_PAUSE -> {
                firebaseAnalytics?.logEvent(
                    Events.ApplicationKeys.APPLICATION,
                    Bundle().apply {
                        putString(
                            Events.ApplicationParams.APPLICATION_STATE,
                            Events.ApplicationState.PAUSE
                        )
                    })
            }

            Lifecycle.Event.ON_DESTROY -> {
                appIsActive = false
                firebaseAnalytics?.logEvent(
                    Events.ApplicationKeys.APPLICATION,
                    Bundle().apply {
                        putString(
                            Events.ApplicationParams.APPLICATION_STATE,
                            Events.ApplicationState.DESTROY
                        )
                    })
            }

            else -> {}
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        if (!appOpen.isShowingAd) {
            mCurrentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {
        mCurrentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
    }
}