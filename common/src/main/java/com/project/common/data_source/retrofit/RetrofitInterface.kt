package com.project.common.data_source.retrofit

import com.project.common.model.ImageResponseModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RetrofitInterface {
    @Multipart
    @POST("api/remove-bg/")
    fun eraseImageBackgroundCall(
        @Part img: MultipartBody.Part,
        @Part("rembg") rembg: RequestBody,
        @Part("enhance") enhance: RequestBody,
        @Part("adjustment") adjustment: RequestBody
    ): Call<ImageResponseModel>




}
