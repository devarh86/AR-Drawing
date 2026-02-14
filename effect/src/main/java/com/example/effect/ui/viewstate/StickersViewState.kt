package com.example.effect.ui.viewstate

import com.project.sticker.data.model.StickersPackByCategories

sealed class StickersViewState {
    object Idle : StickersViewState()
    object Loading : StickersViewState()
    class Success(var list: MutableList<StickersPackByCategories>) : StickersViewState()
    class UpdateStickerObject(var obj: StickersPackByCategories) : StickersViewState()
    class Error(var message: String) : StickersViewState()
}
