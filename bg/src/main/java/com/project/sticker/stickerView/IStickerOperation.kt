package com.project.core.customView.stickerView

interface IStickerOperation {
    fun onSelect(stickerView: StickerView)
    fun onDelete(stickerView: StickerView)
    fun onSingleTap(stickerView: StickerView)
}