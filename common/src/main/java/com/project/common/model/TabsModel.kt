package com.project.common.model

import androidx.annotation.Keep
import com.project.common.enum_classes.EditorBottomTypes

@Keep
data class TabsModel(
    var text: String = "",
    var icon: Int = 0,
    var type: EditorBottomTypes = EditorBottomTypes.NONE
)