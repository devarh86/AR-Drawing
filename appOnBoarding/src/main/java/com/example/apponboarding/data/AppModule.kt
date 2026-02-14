package com.example.apponboarding.data

import android.content.Context
import android.content.SharedPreferences
import com.example.apponboarding.domain.LanguageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideLanguageRepository(
        sharedPreferences: SharedPreferences,@ApplicationContext context: Context
    ): LanguageRepository {
        return LanguageRepositoryImpl(sharedPreferences,context)
    }
}
