package com.project.sticker.data.model

import androidx.annotation.Keep
import com.fahad.newtruelovebyfahad.GetStickersQuery
@Keep
data class StickersPackByCategories(

    var catName: String = "",
    var tag: String = "",
    var id: Int = 0,
    val packList: List<GetStickersQuery.Sticker> = mutableListOf(),
    var fromAssets: Boolean = false
)
