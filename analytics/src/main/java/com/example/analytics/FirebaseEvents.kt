package com.example.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics


object FirebaseEvents {
    private var fbAnalytics: FirebaseAnalytics? = null
    const val TAG = "FIREBASE_EVENT"

    fun initializeFB(context: Context?) {
        fbAnalytics = context?.let { FirebaseAnalytics.getInstance(it) }
    }

    fun logEvent(eventName: String?) {
        /////////Firebase onCreate Event
        try {
            val params = Bundle()
            eventName?.let {
                fbAnalytics?.logEvent(eventName, params)
                Log.d(TAG, "logEvent: $eventName")
            }
        } catch (_: Exception) {
        }
    }

    fun logEventParams(eventName: String?) {
        try {
            val params = Bundle()
            params.putString("ctr", "ctr")
            eventName?.let {
                fbAnalytics?.logEvent(eventName, params)
                Log.d("Firebase_Event", "logEvent: ${eventName}_ctr " + "${eventName}_ctr".count())
            }
        } catch (_: Exception) {
        }
    }
}