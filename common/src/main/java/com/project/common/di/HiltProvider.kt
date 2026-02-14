package com.project.common.di

import android.content.Context
import androidx.room.Room
import com.project.common.data_source.MyDatabase
import com.project.common.data_source.retrofit.BgRemoverRetrofitInterface
import com.project.common.data_source.retrofit.EnhancerRetrofitInstance
import com.project.common.data_source.retrofit.RetrofitInterface
import com.project.common.data_source.retrofit.SketchRetrofitInterface
import com.project.common.remote_config.RemoteConfigRepo
import com.project.common.repository.DraftRepository
import com.project.common.repository.EditorRepository
import com.project.common.utils.ConstantsCommon
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class HiltProvider {

//    @Singleton
//    @Provides
//    fun provideApiService(@ApplicationContext appContext: Context): APIUserInterFaceFramePlacer =
//        RetrofitClientForFrames(appContext).retrofitClient.create(
//            APIUserInterFaceFramePlacer::class.java
//        )
//
//    @Singleton
//    @Provides
//    fun provideAppContext(@ApplicationContext appContext: ApplicationContext): ApplicationContext {
//        return appContext
//    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): MyDatabase {
        return Room
            .databaseBuilder(appContext, MyDatabase::class.java, "Frame_Placer_Database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideDraftRepository(db: MyDatabase): DraftRepository =
        DraftRepository(db = db)

    @Provides
    @Singleton
    fun getRetrofitInstance(retrofit: Retrofit): RetrofitInterface {
        return retrofit.create(RetrofitInterface::class.java)
    }

    @Provides
    @Singleton
    fun getRetroInstance(): Retrofit {
        val httpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        httpClientBuilder.addInterceptor { chain ->
            val request: Request = chain.request().newBuilder()
                .header("Accept", "application/json")
                .header("Authorization", "token " + "96809bf914d0101fb5c901d19f9aac1dc6b802a7")
                .build()
            chain.proceed(request)
        }

        val httpClient = httpClientBuilder.build()
        return Retrofit.Builder()
            .baseUrl("https://tools.framme.online/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun getEnhanceRetrofitInstance(retrofit: Retrofit): EnhancerRetrofitInstance {
        return retrofit.create(EnhancerRetrofitInstance::class.java)
    }

    @Provides
    @Singleton
    fun getEnhanceRetroInstance(): Retrofit {
        val httpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(50, TimeUnit.SECONDS)
            .readTimeout(50, TimeUnit.SECONDS)
            .writeTimeout(50, TimeUnit.SECONDS)

        httpClientBuilder.addInterceptor { chain ->
            val request: Request = chain.request().newBuilder()
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }

        val httpClient = httpClientBuilder.build()
        return Retrofit.Builder()
            .baseUrl(ConstantsCommon.BASE_URL_Enhancer)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun getBgRemoverRetrofitInstance(retrofit: Retrofit): BgRemoverRetrofitInterface {
        return retrofit.create(BgRemoverRetrofitInterface::class.java)
    }

    @Provides
    @Singleton
    fun getBgRemoverRetroInstance(): Retrofit {
        val httpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        httpClientBuilder.addInterceptor { chain ->
            val request: Request = chain.request().newBuilder()
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }

        val httpClient = httpClientBuilder.build()
        return Retrofit.Builder()
            .baseUrl(ConstantsCommon.BASE_URL_BG_REMOVER)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideConfigRepo(
        @ApplicationContext context: Context,
    ): RemoteConfigRepo = RemoteConfigRepo(
        context
    )

    @Provides
    @Singleton
    fun getSketchRetrofitInstance(retrofit: Retrofit): SketchRetrofitInterface {
        return retrofit.create(SketchRetrofitInterface::class.java)
    }

    @Provides
    @Singleton
    fun getSketchRetroInstance(): Retrofit {
        val httpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(50, TimeUnit.SECONDS)
            .readTimeout(50, TimeUnit.SECONDS)
            .writeTimeout(50, TimeUnit.SECONDS)

        httpClientBuilder.addInterceptor { chain ->
            val request: Request = chain.request().newBuilder()
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }

        val httpClient = httpClientBuilder.build()
        return Retrofit.Builder()
            .baseUrl(ConstantsCommon.BASE_URL_SKETCH)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideEditorRepo(
        db: MyDatabase, @ApplicationContext context: Context,
    ): EditorRepository = EditorRepository(
        db,
        getEnhanceRetrofitInstance(getEnhanceRetroInstance()),
        getBgRemoverRetrofitInstance(getBgRemoverRetroInstance()),
        getSketchRetrofitInstance(getSketchRetroInstance()),
        context
    )
}