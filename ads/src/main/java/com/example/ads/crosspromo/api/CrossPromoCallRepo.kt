package com.example.ads.crosspromo.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ads.crosspromo.api.retrofit.CrossPromoService
import com.example.ads.crosspromo.api.retrofit.MultiPartRequestBody
import com.example.ads.crosspromo.api.retrofit.helper.Response
import com.example.ads.crosspromo.api.retrofit.model.CrossPromo
import com.google.errorprone.annotations.Keep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Keep
@Singleton
class CrossPromoCallRepo @Inject constructor(
    private val apiService: CrossPromoService,
    private val multiPartRequestBody: MultiPartRequestBody
) {

    private val _crossPromoBody = MutableLiveData<Response<List<CrossPromo>>>()
    val crossPromoBody: LiveData<Response<List<CrossPromo>>> get() = _crossPromoBody

    suspend fun getCrossPromoBody(appPackage: String) = withContext(Dispatchers.IO) {
        try {
            _crossPromoBody.postValue(Response.Loading())
            val response =
                apiService.getCrossPromoBody(
                    multiPartRequestBody.getCrossPromoBodyParameters(appPackage)
                )
            if (response.isSuccessful) {
                _crossPromoBody.postValue(Response.Success(response.body()))
            } else {
                _crossPromoBody.postValue(Response.Error(response.message()))
            }
        } catch (ex: Exception) {
            _crossPromoBody.postValue(Response.Error(ex.message.toString()))
        }
    }
}