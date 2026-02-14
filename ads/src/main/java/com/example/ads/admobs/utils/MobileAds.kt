package com.example.ads.admobs.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.ads.Constants.ADS_SDK_INITIALIZE
import com.example.ads.Constants.ADS_SDK_INITIALIZE_BIGO
import com.example.ads.Constants.CAN_LOAD_ADS
import com.example.ads.Constants.appIsActive
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MobileAds {

    private lateinit var consentInformation: ConsentInformation
    fun initialize(context: Context, onCompletion: () -> Unit) {
        runCatching {
            if (!ADS_SDK_INITIALIZE.get() && CAN_LOAD_ADS) {
                CoroutineScope(IO).launch {
                    MobileAds.initialize(context) {
                        kotlin.runCatching {
                            CoroutineScope(Main).launch {
                                if (appIsActive) {
                                    ADS_SDK_INITIALIZE.set(true)
                                    onCompletion.invoke()
                                }
                                //  }
                            }
                        }
                    }
                }
            }
        }
    }


    fun checkAdmobConsent(
        activity: Activity,
        onCompletion: () -> Unit,
        consentCompletionCallback: () -> Unit
    ) {
        runCatching {
            val params = ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()
            /*val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("85A9659B8FCE4D151569A4070D033196")
                .build()

            val params = ConsentRequestParameters.Builder()
                .setConsentDebugSettings(debugSettings)
                .build()*/

            consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { loadAndShowError ->
                        Log.w(
                            "FAHAD_CONSENT_GDPR",
                            String.format(
                                "%s: %s",
                                loadAndShowError?.errorCode,
                                loadAndShowError?.message
                            )
                        )

                        if (consentInformation.canRequestAds()) {
                            Log.d("FAHAD_IMRAN", "checkAdmobConsent: CAN LOAD ADS")
                            CAN_LOAD_ADS = true
                            initialize(activity.applicationContext) {
                                Log.d("FAHAD_IMRAN", "checkAdmobConsent: onCompletion")
                                onCompletion.invoke()
                            }
                        }
                        Log.d("FAHAD_IMRAN", "checkAdmobConsent: consentCompletionCallback")
                        consentCompletionCallback.invoke()
                    }
                },
                { requestConsentError ->
                    Log.w(
                        "FAHAD_CONSENT_GDPR",
                        String.format(
                            "%s: %s",
                            requestConsentError.errorCode,
                            requestConsentError.message
                        )
                    )
                    Log.d(
                        "FAHAD_IMRAN",
                        "checkAdmobConsent: requestConsentError consentCompletionCallback"
                    )
                    if (!consentInformation.canRequestAds()) {
                        Log.d("FAHAD_IMRAN", "checkAdmobConsent: onCompletion from Error")
                        onCompletion.invoke()
                    }
                    consentCompletionCallback.invoke()
                }
            )

            if (consentInformation.canRequestAds()) {
                Log.d("FAHAD_IMRAN", "checkAdmobConsent: canRequestAds CAN LOAD ADS")
                CAN_LOAD_ADS = true
                initialize(activity.applicationContext) {
                    Log.d("FAHAD_IMRAN", "checkAdmobConsent: onCompletion")
                    onCompletion.invoke()
                }
            }
        }.onFailure {
            Log.d("FAHAD_IMRAN", "checkAdmobConsent: onFailure consentCompletionCallback")
            consentCompletionCallback.invoke()
        }
    }
}