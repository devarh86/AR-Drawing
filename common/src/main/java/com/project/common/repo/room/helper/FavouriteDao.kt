package com.project.common.repo.room.helper

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.common.repo.room.model.FavouriteModel

@Keep
@Dao
interface FavouriteDao {
    @Query("SELECT * FROM favourite_frames")
    fun getAll(): LiveData<List<FavouriteModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(frame: FavouriteModel)

    @Query("DELETE FROM favourite_frames WHERE frame = :frame")
    fun delete(frame: String)

    @Query("SELECT EXISTS (SELECT * FROM favourite_frames WHERE frame = :frame)")
    fun checkExist(frame: String): Boolean
}