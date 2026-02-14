package com.project.common.utils

import android.content.Context
import android.util.Log
import java.util.Locale


private const val LANGUAGE_ENGLISH = "en"
private const val TAG = "LocalizationManager"
fun Context.setLocale(localeCode: String?) {

    kotlin.runCatching {
        val locale = Locale(
            if (localeCode.isNullOrEmpty()) LANGUAGE_ENGLISH else localeCode
        )

        Locale.setDefault(locale)
        val config = this.resources.configuration
        Log.i(TAG, "setLocale: $locale")
        config.setLocale(locale)
        this.resources.updateConfiguration(config, this.resources.displayMetrics)
    }.onFailure {
        Log.e(TAG, "setLocale: ${it.message}")
    }
}
