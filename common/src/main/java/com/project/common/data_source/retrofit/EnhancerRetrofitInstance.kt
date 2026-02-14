package com.project.common.data_source.retrofit

import com.project.common.model.ImageResponseModel
import com.project.common.model.TokenResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface EnhancerRetrofitInstance {
    @Multipart
    @POST("generate-token")
    fun getTokenForEnhancer(@Part("package_name") appPackage: RequestBody): Call<TokenResponse>

    @Multipart
    @POST("AI-Enhancer")
     fun uploadImageAndGetEnhanceImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Call<ImageResponseModel>
}

//  @Body appPackage: RequestBody