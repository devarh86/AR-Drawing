package com.project.sticker.ui.viewstate

import com.fahad.newtruelovebyfahad.GetStickersQuery

sealed class StickersUpdateViewState {
    object Idle : StickersUpdateViewState()
    object Tick : StickersUpdateViewState()
    object Back : StickersUpdateViewState()
//    class Success(var list: MutableList<StickersPackByCategories>) : StickersUpdateViewState()
    class UpdateStickerObject(var obj: GetStickersQuery.Sticker) : StickersUpdateViewState()
//    class Error(var message: String) : StickersUpdateViewState()
}
