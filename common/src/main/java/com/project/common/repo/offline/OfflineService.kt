package com.project.common.repo.offline

import androidx.annotation.Keep

@Keep
interface OfflineService {
    suspend fun getFeatureScreen()
    suspend fun getFrame(id: Int)
    suspend fun getStickers()
    suspend fun getFilters()
}