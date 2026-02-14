package com.project.gallery.ui.main.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.gallery.data.model.GalleryChildModel
import com.project.gallery.data.model.GalleryModel
import com.project.gallery.data.repository.GalleryRepository
import com.project.gallery.ui.main.intent.MainIntent
import com.project.gallery.ui.main.viewstate.MainViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repo: GalleryRepository
) : ViewModel() {

    fun initializing(){
        // handleIntent()
    }

    var categoryName: String = ""

    private var galleryJob: Job? = null

    val galleryIntent: Channel<MainIntent> = Channel(Channel.UNLIMITED)
//
//    private val _state = MutableStateFlow<MainViewState>(MainViewState.Idle)
//    val state: StateFlow<MainViewState> get() = _state

    private val _state = MutableLiveData<MainViewState>(MainViewState.Idle)
    val state: LiveData<MainViewState> get() = _state

    @set:Synchronized
    var galleryFoldersWithImages: MutableList<GalleryModel> = mutableListOf()

    var selectedImages: MutableList<GalleryChildModel> = mutableListOf()

    var selectedImagesForMaintainingState: MutableList<GalleryChildModel> = mutableListOf()

    var selectedCounterForMaintainingState: MutableList<Int> = mutableListOf()

    var selectedImagesState: MutableState<List<String>> = mutableStateOf(emptyList())



    override fun onCleared() {
        super.onCleared()
        galleryJob?.cancel()
    }

    fun startLoadingImages() {
            _state.value = (MainViewState.Loading)
    }

    fun updateTickIcon(showPrimaryTick: Boolean){
        _state.value = MainViewState.UpdateTickIcon(showPrimaryTick)
    }

    suspend fun updateFolder(obj: GalleryModel) {
        galleryFoldersWithImages.add(obj)
    }

    fun imageLoadedCompleted() {
        _state.value = MainViewState.Success
    }

    val allMediaList: MutableList<GalleryChildModel> = mutableListOf()
    var id = 0
    suspend fun updateImage(obj: GalleryChildModel) {
        if (obj.parentIndex + 1 < galleryFoldersWithImages.size && obj.parentIndex + 1 >= 0) {
            galleryFoldersWithImages[obj.parentIndex + 1].folderImagesVideoPaths.add(
                obj
            )
            obj.id = id
            id++
            allMediaList.add(obj)
        }
    }

    fun resetGalleryState() {
        _state.value = MainViewState.Idle
    }
}