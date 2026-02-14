package com.project.common.remote_config

import android.app.Activity
import android.content.Context
import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.common.BuildConfig

import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@Keep
@HiltViewModel
class RemoteConfigViewModel @Inject constructor(private val repository: RemoteConfigRepo) : ViewModel() {
    var adConfig: MutableLiveData<AdConfigModel> = MutableLiveData()

    fun getRemoteConfigSplash(context: Activity) {
        if (BuildConfig.DEBUG) {
            adConfig.value = repository.getDefaultRemoteAdSettings()
//            Logger.log(getLog("getRemoteConfigSplash_adConfig", "${adConfig.value}"))
            return
        }

        val rc = repository.getFirebaseRemoteConfig()
        rc.fetchAndActivate()
            .addOnCompleteListener(context) { task ->
                if (task.isSuccessful) {
                    val adJson = rc.getString("carousal_ad_settings")
//                    val notificationJson = rc.getString(context.getString(com.remotex.R.string.notification_topic))
//                    Logger.log(adJson)
//                    Logger.log(notificationJson)
                    adConfig.value = if (adJson.isEmpty()) {
                        repository.getDefaultRemoteAdSettings()
                    } else {
                        Gson().fromJson(adJson, AdConfigModel::class.java) ?: repository.getDefaultRemoteAdSettings()
                    }
                }
            }
    }

    fun fetchRemoteString(key: String, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            repository.fetchRemoteString(key) { value ->
//                Log.d("TAG", getLog("fetchRemoteValue", "key: $key, value: $value"))
                onComplete(value)
            }
        }
    }

    fun fetchRemoteBoolean(key: String, onComplete: (Boolean?) -> Unit) {
        viewModelScope.launch {
            repository.fetchRemoteBoolean(key) { value ->
//                Log.d("TAG", getLog("fetchRemoteValue", "key: $key, value: $value"))
                onComplete(value)
            }
        }
    }

    fun getAdConfig(context: Context): AdConfigModel {

        if (BuildConfig.DEBUG) {
            return repository.getDefaultRemoteAdSettings()
        }

        val jsonString = repository.getFirebaseRemoteConfig().getString("carousal_ad_settings")
        return if (jsonString.isEmpty()) {
            repository.getDefaultRemoteAdSettings()
        } else {
            Gson().fromJson(jsonString, AdConfigModel::class.java) ?: repository.getDefaultRemoteAdSettings()
        }
    }

//    fun getNotificationConfig(context: Context): NotificationSettings {
//
////        if (BuildConfig.DEBUG) {
////            return repository.getDefaultNotificationSettings()
////        }
//
//        val jsonString = repository.getFirebaseRemoteConfig().getString(context.getString(com.remotex.R.string.notification_topic))
//        return if (jsonString.isEmpty()) {
//            repository.getDefaultNotificationSettings()
//        } else {
//            Gson().fromJson(jsonString, NotificationSettings::class.java) ?: repository.getDefaultNotificationSettings()
//        }
//    }
}