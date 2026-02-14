package com.project.gallery.data.model

import androidx.annotation.Keep

@Keep
data class GalleryModel(

    var path: String? = "",
    var video: Boolean? = false,
    var folderName: String? ="",
    var folderImagesVideoPaths: MutableList<GalleryChildModel> = mutableListOf(),
    var isSelected: Boolean? = false,
)