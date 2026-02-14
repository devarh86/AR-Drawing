package com.example.ads.crosspromo.api.retrofit.helper

import androidx.annotation.Keep

@Keep
object CrossPromoApiConstants {
    const val BASE_URL = "https://utm.mobify.games/api/"
    const val ACCEPT = "Accept"
    const val ACCEPT_JSON = "application/json"
    const val AUTHORIZATION = "Authorization"
    const val BEARER = "Bearer "
    const val TOKEN = "2|MzY2clIe9jpXZSIk27AnNlX1MXJFdSGhcybA4EWC"

    const val TIMEOUT = 60
    const val CACHE_DURATION_MIN = 60
    const val CACHE_DURATION_SEC: Long = 600
    const val STALE_DURATION_DAYS = 7
    const val HTTP_CACHE_MAX_SIZE = 50 * 1024 * 1024
    const val HTTP_CACHE_DIR = "ads_module_cache"
}