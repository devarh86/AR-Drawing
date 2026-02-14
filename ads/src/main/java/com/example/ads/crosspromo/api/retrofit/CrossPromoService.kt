package com.example.ads.crosspromo.api.retrofit

import com.example.ads.crosspromo.api.retrofit.model.CrossPromo
import com.google.errorprone.annotations.Keep
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

@Keep
interface CrossPromoService {
    @POST("getAdsList")
    suspend fun getCrossPromoBody(@Body body: RequestBody): Response<List<CrossPromo>>
}