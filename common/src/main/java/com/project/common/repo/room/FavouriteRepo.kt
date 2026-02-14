package com.project.common.repo.room

import androidx.annotation.Keep
import com.project.common.repo.room.helper.FavouriteDao
import com.project.common.repo.room.model.FavouriteModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Keep
class FavouriteRepo @Inject constructor(
    private val favouriteDao: FavouriteDao
) {

    val allFavouriteFrames = favouriteDao.getAll()

    fun insertFavouriteFrame(frame: FavouriteModel) {
        favouriteDao.insert(frame)
    }

    fun deleteFavouriteFrame(frame: FavouriteModel) {
        frame.frame?.let { favouriteDao.delete(it) }
    }

    fun checkIsFav(frame: FavouriteModel): Boolean {
        return frame.frame?.let { favouriteDao.checkExist(it) } ?: false
    }
}