package com.project.common.data_source

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.common.db_table.RecentSearchTable
import kotlinx.coroutines.flow.Flow

@Keep
@Dao
interface RecentSearchDao {
    @Query("SELECT * FROM recent_search_model ORDER BY timestamp DESC")
    fun getAll(): List<RecentSearchTable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(frame: RecentSearchTable)

    @Query("UPDATE recent_search_model SET timestamp = :timeStamp WHERE id = :id")
    fun updateTime(id: Long, timeStamp: Long)

    @Query("DELETE FROM recent_search_model")
    fun deleteAllRecentSearches()

    @Query("DELETE FROM recent_search_model WHERE searchValue = :query")
    fun deleteSpecificRecent(query: String)

    @Query("SELECT * FROM recent_search_model ORDER BY timestamp DESC LIMIT 1")
    fun getLatestChip(): Flow<RecentSearchTable>

    // Delete the oldest chip based on timestamp or ID
    @Query("DELETE FROM recent_search_model WHERE id = (SELECT id FROM recent_search_model ORDER BY id ASC LIMIT 1)")
    fun deleteOldestChip()

    // Get the number of chips in the table
    @Query("SELECT COUNT(*) FROM recent_search_model")
    fun getChipCount(): Int

    @Query("SELECT COUNT(*) FROM recent_search_model WHERE searchValue = :chipName")
    suspend fun isChipExists(chipName: String): Boolean

    @Query("SELECT * FROM recent_search_model WHERE searchValue = :chipName LIMIT 1")
    suspend fun readSpecificData(chipName: String): RecentSearchTable?
}