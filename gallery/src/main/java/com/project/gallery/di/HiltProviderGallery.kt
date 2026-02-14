package com.project.gallery.di

import android.content.Context
import com.project.gallery.data.repository.GalleryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class HiltProviderGallery {

    @Singleton
    @Provides
    fun provideGalleryRepo(@ApplicationContext appContext: Context): GalleryRepository =
        GalleryRepository(appContext)
}