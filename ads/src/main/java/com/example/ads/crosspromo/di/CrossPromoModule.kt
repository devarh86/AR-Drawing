package com.example.ads.crosspromo.di

import android.content.Context
import com.example.ads.crosspromo.api.retrofit.CrossPromoService
import com.example.ads.crosspromo.api.retrofit.helper.CrossPromoApiConstants
import com.example.ads.crosspromo.api.retrofit.helper.CrossPromoApiConstants.ACCEPT
import com.example.ads.crosspromo.api.retrofit.helper.CrossPromoApiConstants.ACCEPT_JSON
import com.example.ads.crosspromo.api.retrofit.helper.CrossPromoApiConstants.AUTHORIZATION
import com.example.ads.crosspromo.api.retrofit.helper.CrossPromoApiConstants.BASE_URL
import com.example.ads.crosspromo.api.retrofit.helper.CrossPromoApiConstants.BEARER
import com.example.ads.crosspromo.api.retrofit.helper.CrossPromoApiConstants.TOKEN
import com.example.ads.crosspromo.api.retrofit.helper.CrossPromoQualifier
import com.example.ads.crosspromo.helper.isNetworkAvailable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class CrossPromoModule {
    @Provides
    @Singleton
    fun provideCrossPromoApiInterface(@Named("CrossPromoRetrofit") retrofit: Retrofit): CrossPromoService {
        return retrofit.create(CrossPromoService::class.java)
    }


    @Provides
    @Singleton
    @Named("CrossPromoRetrofit")
    fun getCrossPromoRetrofit(@CrossPromoQualifier okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @CrossPromoQualifier
    fun getCrossPromoOkHttpClient(
        @ApplicationContext context: Context,
        @Named("CrossPromoCache") myCache: Cache
    ): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
        okHttpClientBuilder.addInterceptor { chain ->
            val request: Request = chain.request().newBuilder()
                .header(ACCEPT, ACCEPT_JSON)
                .header(AUTHORIZATION, "$BEARER$TOKEN")
                .build()
            chain.proceed(request)
        }.addInterceptor { chain ->
            var request = chain.request()
            if (request.cacheControl.noCache) {
                return@addInterceptor chain.proceed(request)
            }
            request = if (context.isNetworkAvailable()) {
                request.newBuilder()
                    .cacheControl(
                        CacheControl.Builder().maxStale(
                            CrossPromoApiConstants.CACHE_DURATION_MIN,
                            TimeUnit.MINUTES
                        ).build()
                    )
                    .build()
            } else {
                request.newBuilder()
                    .cacheControl(
                        CacheControl.Builder().onlyIfCached()
                            .maxStale(
                                CrossPromoApiConstants.STALE_DURATION_DAYS,
                                TimeUnit.DAYS
                            )
                            .build()
                    )
                    .build()
            }
            chain.proceed(request)
        }.connectTimeout(CrossPromoApiConstants.TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(CrossPromoApiConstants.TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(CrossPromoApiConstants.TIMEOUT.toLong(), TimeUnit.SECONDS)
            .cache(myCache)
            .addNetworkInterceptor { chain ->
                val originalResponse = chain.proceed(chain.request())
                val maxAge = originalResponse.cacheControl.maxAgeSeconds
                if (maxAge <= 0) {
                    originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Expires")
                        .removeHeader("Cache-Control")
                        .header(
                            "Cache-Control",
                            String.format(
                                Locale.ENGLISH,
                                "max-age=%d, only-if-cached, max-stale=%d",
                                CrossPromoApiConstants.CACHE_DURATION_SEC,
                                0
                            )
                        )
                        .build()
                } else {
                    originalResponse
                }
            }

        return okHttpClientBuilder.build()
    }

    @Provides
    @Named("CrossPromoCache")
    fun getCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, CrossPromoApiConstants.HTTP_CACHE_DIR)
        return Cache(cacheDir, CrossPromoApiConstants.HTTP_CACHE_MAX_SIZE.toLong())
    }
}