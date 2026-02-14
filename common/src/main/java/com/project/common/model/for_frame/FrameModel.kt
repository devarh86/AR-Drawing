package com.project.common.model.for_frame

import androidx.annotation.Dimension
import androidx.annotation.Keep

@Keep
data class FrameModel(
    val frames: MutableList<FrameAllDataModel> = mutableListOf(),
    val title: String
)

@Keep
data class FrameAllDataModel(
    var dividerPosition: Int = -1,
    val fileLink: String = "",
    val tabPosition: Int = 0,
    var frameTitle: String = "",
    var id: Long = -1L,
    var obj: Any? = null,
    var tag_title: String = "",
    var placeholder: Int = 0
)