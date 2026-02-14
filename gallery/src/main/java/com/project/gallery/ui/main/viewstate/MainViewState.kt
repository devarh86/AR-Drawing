package com.project.gallery.ui.main.viewstate

import com.project.gallery.data.model.GalleryChildModel
import com.project.gallery.data.model.GalleryModel

sealed class MainViewState {

    object Idle : MainViewState()
    object Loading : MainViewState()
    object Success : MainViewState()
    class UpdateImage(val obj: GalleryChildModel, val parentIndex: Int = 0) : MainViewState()
    class UpdateFolder(val obj: GalleryModel) : MainViewState()
    class Error(val message: String) : MainViewState()
    class UpdateTickIcon(val showPrimaryTick: Boolean) : MainViewState()
}