package com.project.common.repo.room.helper

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.project.common.repo.room.model.RecentsModel
@Keep
@Dao
interface RecentsDao {
    @Query("SELECT * FROM recents_frames")
    fun getAll(): LiveData<List<RecentsModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(frame: RecentsModel)

    @Query("DELETE FROM recents_frames WHERE frame = :frame")
    fun delete(frame: String)
}