package com.project.gallery.data.model

import androidx.annotation.Keep
import androidx.constraintlayout.helper.widget.Carousel

@Keep
data class GalleryChildModel(

    var path: String = "",
    var isSelected: Boolean = false,
    var parentIndex: Int = 0,
    var id: Int = 0,
    var sampleImagePath: Int = 0,
    var fromCollage: Boolean = false,
    var fromCarousel: Boolean = false,
    var selectedImageCounter:Int = 0,
    var globalSelectionNumber: Int = 0,
    var indexInAdapter: Int = -1,
    var sample:Int = 0
)
