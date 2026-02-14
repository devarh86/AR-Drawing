package com.example.effect.ui.intent

import android.content.Context

sealed class StickerIntent {
    class GetStickers(val context: Context?): StickerIntent()
//    class ImageEnhancementAndPlacing(var context: Context) : FilterIntent()
//    class SingleImageEnhancementAndPlacing(var context: Context, var path: String, var index: Int) : FilterIntent()
}