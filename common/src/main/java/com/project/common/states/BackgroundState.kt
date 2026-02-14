package com.project.common.states

import android.graphics.PorterDuff
import com.fahad.newtruelovebyfahad.GetStickersQuery
import com.project.common.model.ImagesModel


sealed class BackgroundState {
    data class Color(val color: Int) : BackgroundState()
    data class EyeDropper(val color: Int) : BackgroundState()
    data class Transparent(val color: Int, val clear: PorterDuff.Mode) : BackgroundState()
    data class Gradient(val gradientList: List<Int>) : BackgroundState()
//    data class Image(val images: List<GalleryChildModel>) : BackgroundState()
//    data class Image(val images: MutableList<ImagesModel>) : BackgroundState()
    data class Image(val images: ImagesModel?) : BackgroundState()
    class ApiBG(val images: GetStickersQuery.Sticker?) : BackgroundState()
    data object None : BackgroundState()
}

