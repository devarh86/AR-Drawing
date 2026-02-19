package com.project.common.remote_config

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import com.project.common.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext

@Keep
class RemoteConfigRepo(@ApplicationContext private val context: Context) {
    private val remoteConfig: FirebaseRemoteConfig by lazy { FirebaseRemoteConfig.getInstance() }

    fun getFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val configSetting = FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0).build()
        remoteConfig.setConfigSettingsAsync(configSetting)

        remoteConfig.setDefaultsAsync(
            mapOf(
                "carousal_ad_settings" to Gson().toJson(getDefaultRemoteAdSettings()),
//                context.getString(com.remotex.R.string.notification_topic) to Gson().toJson(getDefaultNotificationSettings())
            )
        )
        return remoteConfig
    }


    fun fetchRemoteString(key: String, onComplete: (String?) -> Unit) {
        try {
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(remoteConfig.getString(key))
                    } else {
                        Log.e("RemoteConfigRepo", "Failed to fetch key: $key", task.exception)
                        onComplete(null)
                    }
                }
        } catch (e: Exception) {
            Log.e("RemoteConfigRepo", "Error fetching key: $key", e)
            onComplete(null)
        }
    }

    fun fetchRemoteBoolean(key: String, onComplete: (Boolean?) -> Unit) {
        try {
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(remoteConfig.getBoolean(key))
                    } else {
                        Log.e("RemoteConfigRepo", "Failed to fetch boolean key: $key", task.exception)
                        onComplete(null)
                    }
                }
        } catch (e: Exception) {
            Log.e("RemoteConfigRepo", "Error fetching boolean key: $key", e)
            onComplete(null)
        }
    }

    fun getDefaultRemoteAdSettings(): AdConfigModel {
//       val fileName = if (BuildConfig.DEBUG) "ad_config_release.json" else "ad_config_release.json"
        val fileName = if (BuildConfig.DEBUG) "ad_config_test.json" else "ad_config_release.json"

        val inputStream = context.assets.open(fileName)

        val json = inputStream.bufferedReader().use { it.readText() }
        val type = object : TypeToken<AdConfigModel>() {}.type
        return try {
            Gson().fromJson(json, type)
        } catch (_: Exception) {
            AdConfigModel()
        }
    }
/*
    fun getDefaultNotificationSettings(): NotificationSettings {
        val inputStream = context.assets.open("notification_settings.json")
        val json = inputStream.bufferedReader().use { it.readText() }
        val type = object : TypeToken<NotificationSettings>() {}.type
        return try {
            Gson().fromJson(json, type)
        } catch (_: Exception) {
            NotificationSettings(true, 9, 9, "1,2", listOf(3, 5, 1), listOf(4, 6, 7), false)
        }
    }*/
}
