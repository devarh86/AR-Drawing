package com.project.common.repo.room

import androidx.annotation.Keep
import com.project.common.repo.room.helper.RecentsDao
import com.project.common.repo.room.model.RecentsModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Keep
class RecentRepo @Inject constructor(
    private val recentDao: RecentsDao
) {

    val allRecentFrames = recentDao.getAll()

    fun insertRecentFrame(frame: RecentsModel) {
        try {
            deleteRecentFrame(frame)
            recentDao.insert(frame)
        } catch (_: Exception) {
        }
    }

    private fun deleteRecentFrame(frame: RecentsModel) {
        frame.frame?.let { recentDao.delete(it) }
    }
}