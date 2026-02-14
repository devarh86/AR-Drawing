package com.project.sticker.ui.viewstate

import android.widget.FrameLayout
import com.project.sticker.data.model.StickersPackByCategories
import com.project.sticker.data.model.StickersSpecificCategoryDataModel

sealed class StickersViewState {
    object Idle : StickersViewState()
    object Loading : StickersViewState()
    class UpdateUi(var frameLayout: FrameLayout) : StickersViewState()
    class Success(var list: MutableList<StickersPackByCategories>) : StickersViewState()
    class UpdateStickerObject(var obj: StickersPackByCategories) : StickersViewState()
    class Error(var message: String) : StickersViewState()
}
