package com.project.common.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.common.repo.api.apollo.NetworkCallRepo
import com.project.common.repo.offline.OfflineDataRepo
import com.project.common.repo.room.FavouriteRepo
import com.project.common.repo.room.RecentRepo
import com.project.common.repo.room.model.FavouriteModel
import com.project.common.repo.room.model.RecentsModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ApiViewModel @Inject public constructor(
    private val offlineDataRepo: OfflineDataRepo,
    private val networkCallRepo: NetworkCallRepo,
    private val favouriteRepo: FavouriteRepo,
    private val recentRepo: RecentRepo
) : ViewModel() {

    /*init {
        getAuthToken()
    }*/

    val token = networkCallRepo.token
    fun getAuthToken(networkState: Boolean) = viewModelScope.launch(IO) {
        networkCallRepo.getToken(networkState)
    }

    fun clearToken() {
        networkCallRepo.clearToken()
    }

    val featureScreen = networkCallRepo.featureScreen
    val offlineFeatureScreen = offlineDataRepo.featureScreen
    fun getFeatureScreen(networkState: Boolean) = CoroutineScope(IO).launch {
        if (networkState) {
            networkCallRepo.getFeatureScreen()
        } else {
            offlineDataRepo.getFeatureScreen()
        }
    }

    val mainScreen = networkCallRepo.mainScreen
    val mainFromMainScreen = networkCallRepo.mainFromMainScreen
    fun getMainScreen() = viewModelScope.launch {
        networkCallRepo.getMainScreen()
    }

    val searchTags = networkCallRepo.searchTags
    fun getSearchTags() = viewModelScope.launch {
        networkCallRepo.getSearchTags()
    }

    val frame = networkCallRepo.frame
    val offlineFrame = offlineDataRepo.frame
    fun getFrame(id: Int, networkState: Boolean = true) = viewModelScope.launch {
        if (networkState) networkCallRepo.getFrame(id) else offlineDataRepo.getFrame(id)
    }

    fun clearFrame(networkState: Boolean = true) =
        if (networkState) networkCallRepo.clearFrame() else offlineDataRepo.clearFrame()

    val stickers = networkCallRepo.stickers
    val offlineStickers = offlineDataRepo.stickers
    fun getStickers(networkState: Boolean) = viewModelScope.launch {
        if (networkState) networkCallRepo.getStickers() else offlineDataRepo.getStickers()
    }

    val backgrounds = networkCallRepo.backgrounds

    fun getBackgrounds() = viewModelScope.launch {
        networkCallRepo.getBackgrounds()
    }

    val filters = networkCallRepo.filters
    val offlineFilters = offlineDataRepo.filters

    fun getFilters(networkState: Boolean) = viewModelScope.launch {
        if (networkState) networkCallRepo.getFilters() else offlineDataRepo.getFilters()
    }

    val effects = networkCallRepo.effects
    fun getEffects() = viewModelScope.launch {
        networkCallRepo.getEffects()
    }

    // favourites
    val favouriteFrames: LiveData<List<FavouriteModel>> get() = favouriteRepo.allFavouriteFrames

    fun favourite(frame: FavouriteModel) {
        if (frame.isFavourite) addToFavourite(frame) else removeFromFavourite(frame)
    }

    private fun addToFavourite(frame: FavouriteModel) = viewModelScope.launch {
        withContext(IO) {
            favouriteRepo.insertFavouriteFrame(frame)
        }
    }

    private fun removeFromFavourite(frame: FavouriteModel) = viewModelScope.launch {
        withContext(IO) {
            favouriteRepo.deleteFavouriteFrame(frame)
        }
    }

    // recents
    val recentsFrames: LiveData<List<RecentsModel>> get() = recentRepo.allRecentFrames

    fun addToRecent(frame: RecentsModel) = viewModelScope.launch {
        withContext(IO) {
            recentRepo.insertRecentFrame(frame)
        }
    }

    /*fun favourite(frame: RecentsModel) {
        if (frame.isFavourite) addToFavourite(frame) else removeFromFavourite(frame)
    }

    private fun addToFavourite(frame: FavouriteModel) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            favouriteRepo.insertFavouriteFrame(frame)
        }
    }

    private fun removeFromFavourite(frame: FavouriteModel) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            favouriteRepo.deleteFavouriteFrame(frame)
        }
    }*/
}
