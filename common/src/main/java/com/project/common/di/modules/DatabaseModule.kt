package com.project.common.di.modules

import android.content.Context
import androidx.room.Room
import com.project.common.data_source.RecentSearchDao
import com.project.common.repo.room.helper.AppDatabase
import com.project.common.repo.room.helper.FavouriteDao
import com.project.common.repo.room.helper.RecentsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "CrslDB"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideFavouriteDao(appDatabase: AppDatabase): FavouriteDao {
        return appDatabase.favouriteDao()
    }

    @Singleton
    @Provides
    fun provideRecentsDao(appDatabase: AppDatabase): RecentsDao {
        return appDatabase.recentsDao()
    }

    @Singleton
    @Provides
    fun provideRecentSearchDao(appDatabase: AppDatabase): RecentSearchDao {
        return appDatabase.recentSearchDao()
    }
}