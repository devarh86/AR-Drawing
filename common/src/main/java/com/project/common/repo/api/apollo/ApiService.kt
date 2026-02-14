package com.project.common.repo.api.apollo

import androidx.annotation.Keep
import com.fahad.newtruelovebyfahad.GetHomeAndTemplateScreenDataQuery
import com.fahad.newtruelovebyfahad.GetMainScreenQuery
import com.fahad.newtruelovebyfahad.GetSearchFramesQuery
import com.project.common.repo.api.apollo.helper.Response
import kotlinx.coroutines.flow.Flow

@Keep
interface ApiService {
    suspend fun getToken(networkState: Boolean)
    suspend fun getFeatureScreen()
    suspend fun getMainScreen()
    suspend fun getStickers()
    suspend fun getBackgrounds()
    suspend fun getFilters()
    suspend fun getEffects()
    suspend fun getFrame(id: Int)
    suspend fun getSearchTags()
    suspend fun getHomeAndTemplateScreen(): Flow<Response<GetHomeAndTemplateScreenDataQuery.Data?>>
    suspend fun getSearchFrames(tag: String): Flow<Response<GetSearchFramesQuery.Data?>>

}