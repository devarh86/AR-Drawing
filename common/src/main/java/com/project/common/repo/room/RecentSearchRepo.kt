package com.project.common.repo.room

import androidx.annotation.Keep
import com.project.common.db_table.RecentSearchTable
import com.project.common.data_source.RecentSearchDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Keep
class RecentSearchRepo @Inject constructor(
    private val recentDao: RecentSearchDao
) {

    fun getAllData(): List<RecentSearchTable> {
        return recentDao.getAll()
    }

    suspend fun insertRecentSearch(searchQuery: RecentSearchTable) {
        kotlin.runCatching {
            if (!recentDao.isChipExists(searchQuery.searchValue)) {
                recentDao.insert(searchQuery)
            } else {
                recentDao.readSpecificData(searchQuery.searchValue)?.let {
                    recentDao.updateTime(it.id, System.currentTimeMillis())
                }
            }
        }
    }

    fun deleteAllRecentSearch() {

        recentDao.deleteAllRecentSearches()
    }

    fun deleteSpecificRecentSearch(query: String) {
        recentDao.deleteSpecificRecent(query)
    }

    fun deleteOldestChip() {
        recentDao.deleteOldestChip()
    }

    fun getRecentSearchCount(): Int {
        return recentDao.getChipCount()
    }

    fun getLatestRecent(): Flow<RecentSearchTable> {
        return recentDao.getLatestChip()
    }

//    fun checkIsFav(frame: FavouriteModel): Boolean {
//        return frame.frame?.let { favouriteDao.checkExist(it) } ?: false
//    }
}