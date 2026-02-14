package com.project.common.repo.api.apollo.helper

import androidx.annotation.Keep

@Keep
sealed class Response<T>(val data: T? = null, var errorMessage: String? = null) {
    class Loading<T> : Response<T>()
    class Success<T>(data: T?) : Response<T>(data = data)
    class Error<T>(errorMessage: String) : Response<T>(errorMessage = errorMessage)
    class ShowSlowInternet<T>(message: String) : Response<T>(errorMessage = message)
}