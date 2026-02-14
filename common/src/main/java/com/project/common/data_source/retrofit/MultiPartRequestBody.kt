package com.project.common.data_source.retrofit

import com.google.errorprone.annotations.Keep
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Keep
@Singleton
class MultiPartRequestBody @Inject constructor(){

    fun getBgRemovedBody(): RequestBody?{
        return  MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("rembg","True")
            .addFormDataPart("enhance", "False")
            .addFormDataPart("adjustment", "False")
            .build()
    }

}