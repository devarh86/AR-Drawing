package com.fahad.newtruelovebyfahad

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.asLiveData
import com.example.ads.Constants.appIsActive
import com.example.ads.Constants.appOpen
import com.example.ads.Constants.generalNotificationHideAppEnable
import com.example.analytics.Constants.firebaseAnalytics
import com.example.inapp.core.GoogleBilling
import com.example.inapp.helpers.Constants
import com.example.inapp.repo.datastore.BillingDataStore
import com.fahad.newtruelovebyfahad.utils.AppObserver
import com.fahad.newtruelovebyfahad.utils.createProDialog
import com.farimarwat.grizzly.GrizzlyMonitorBuilder
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.project.common.utils.ActivityTracker
import com.project.common.utils.Constants.showProDialogClick8th
import com.project.common.utils.ConstantsCommon.isOpenCVSuccess
import com.project.common.utils.GlobalApp
import com.xan.event_notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application() {

    @set:Inject
    lateinit var billing: GoogleBilling

    @set:Inject
    lateinit var billingDataStore: BillingDataStore
    private var appObserver: AppObserver? = null

    private var notification: NotificationHelper? = null


    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        appIsActive = true
        GlobalApp.instance = this
        appObserver = AppObserver(appOpen = appOpen, this)
        appObserver?.let {
            ProcessLifecycleOwner.get().lifecycle.addObserver(it)
            this.registerActivityLifecycleCallbacks(it)
        }

        runCatching {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    if (!OpenCVLoader.initLocal()) {
                        isOpenCVSuccess = false
                    } else {
                        isOpenCVSuccess = true
                    }
                } catch (e: Exception) {
                    Log.e("MyApp", "Error initializing OpenCV", e)
                }
            }
        }.onFailure { e ->
            Log.e("MyApp", "Error initializing OpenCV", e)
            isOpenCVSuccess = false
        }

        try {
            firebaseAnalytics = Firebase.analytics
            if (::billing.isInitialized) {
                billing.firebaseAnalytics = firebaseAnalytics
            }

            registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
                override fun onActivityResumed(activity: Activity) {
                    ActivityTracker.setCurrentActivity(activity)
                }

                override fun onActivityPaused(activity: Activity) {
                    // Keep reference until next activity comes
                }

                override fun onActivityDestroyed(activity: Activity) {
                    if (ActivityTracker.getCurrentActivity() == activity) {
                        ActivityTracker.setCurrentActivity(null)
                    }
                }

                // Other methods can be empty
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            })
            com.example.ads.Constants.firebaseAnalytics = firebaseAnalytics
//             Initialize Firebase Crashlytics
            val firebaseCrashlytics = FirebaseCrashlytics.getInstance()
            //      Initialize and start GrizzlyMonitor with custom settings
            GrizzlyMonitorBuilder(this)
                .withTicker(200L)
                .withThreshold(4500L)
                .withTitle("Error Detected")
                .withMessage("Please restart the application to resolve the issue.")
                .withFirebaseCrashLytics(firebaseCrashlytics)
                .build()
                .start()
        } catch (ex: Exception) {
            Log.e("error", "onCreate: ", ex)
        }
        showProDialogClick8th.observeForever {
            if (it) {
                kotlin.runCatching {
                    val currentActivity = ActivityTracker.getCurrentActivity()
                    if (currentActivity != null && ActivityTracker.isActivityActive()) {
                        // Safe to use the activity
                        currentActivity.createProDialog({}, {})
//                        currentActivity.runOnUiThread {
//                        }
                    }
                    // ActivityTracker.getCurrentActivity()?.createProDialog({},{})
                }
            }
        }

        Constants.isProVersion.observeForever {
            GlobalScope.launch(Dispatchers.IO) {
                billingDataStore.writeIsPro(it)
            }
        }
        billingDataStore.readIsPro().asLiveData().observeForever {
//            Constants.isProVersion.value = true
//            Constants.isProVersion.value = false
            Constants.isProVersion.value = it
        }
    }

    fun initNotificationHelper() {
        kotlin.runCatching {
            if (generalNotificationHideAppEnable) {
                notification = NotificationHelper(this, firebaseAnalytics)
                appObserver?.setNotificationHelper(notification)
            }
        }
    }
}