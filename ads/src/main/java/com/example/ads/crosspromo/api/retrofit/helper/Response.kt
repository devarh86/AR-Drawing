package com.example.ads.crosspromo.api.retrofit.helper

import com.google.errorprone.annotations.Keep

@Keep
sealed class Response<T>(val data: T? = null, var errorMessage: String? = null) {
    class Loading<T> : Response<T>()
    class Success<T>(data: T?) : Response<T>(data = data)
    class Error<T>(errorMessage: String) : Response<T>(errorMessage = errorMessage)
}