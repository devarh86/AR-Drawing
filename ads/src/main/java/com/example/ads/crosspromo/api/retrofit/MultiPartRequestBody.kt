package com.example.ads.crosspromo.api.retrofit

import com.google.errorprone.annotations.Keep
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Keep
@Singleton
class MultiPartRequestBody @Inject constructor() {

    fun getCrossPromoBodyParameters(
        appPackage: String,
        header: String = "0",
        utmFormat: String = "1"
    ): RequestBody {
        return MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("package", appPackage)
            .addFormDataPart("header", header)
            .addFormDataPart("utm_format", utmFormat)
            .build()
    }
}