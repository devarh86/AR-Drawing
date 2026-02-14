package com.project.common.repo.room.helper

import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.common.data_source.RecentSearchDao
import com.project.common.db_table.RecentSearchTable
import com.project.common.repo.room.model.FavouriteModel
import com.project.common.repo.room.model.RecentsModel

@Keep
@Database(
    entities = [FavouriteModel::class, RecentsModel::class, RecentSearchTable::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favouriteDao(): FavouriteDao
    abstract fun recentsDao(): RecentsDao
    abstract fun recentSearchDao(): RecentSearchDao
}