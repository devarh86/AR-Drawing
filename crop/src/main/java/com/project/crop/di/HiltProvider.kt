package com.project.crop.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class HiltProvider {

//    @Singleton
//    @Provides
//    fun provideApiService(@ApplicationContext appContext: Context): APIUserInterFaceFilters =
//        RetrofitClient(appContext).retrofitClient.create(
//            APIUserInterFaceFilters::class.java
//        )
}