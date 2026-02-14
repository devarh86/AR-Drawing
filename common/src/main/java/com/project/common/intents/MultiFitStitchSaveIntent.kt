package com.project.common.intents

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.drawable.Drawable
import com.project.common.enum_classes.SaveQuality
import com.project.common.model.SavingModel

sealed class MultiFitStitchSaveIntent {
    class SaveClick(var resolution: SaveQuality) : MultiFitStitchSaveIntent()
    class Saving(
        var isProVersion: Boolean = false,
        var zoomLevels: Map<Int, Float> = HashMap(),
    ) : MultiFitStitchSaveIntent()

    class SavingStitch(
        var isProVersion: Boolean = false, var colorMatrix: ColorMatrix, var spacing: Int,
    ) : MultiFitStitchSaveIntent()

    class SavingStitchVertical(
        var isProVersion: Boolean = false, var colorMatrix: ColorMatrix, var spacing: Int,
    ) : MultiFitStitchSaveIntent()

    class SavingCollage(
        var context: Context,
        var mainBitmap: Bitmap,
        var overlayBitmap: Bitmap,
    ) : MultiFitStitchSaveIntent()

    class SavingGreeting(
        var context: Context,
        var overlayBitmap: Bitmap,
    ) : MultiFitStitchSaveIntent()

    class SavingPip(
        var context: Context,
        var savingModelList: MutableList<SavingModel>,
        var maskedBitmap: Bitmap,
        var cutBitmap: Bitmap,
        var alpha: Float,
        var cutDrawableBlur: Drawable,
        var maskedDrawableBlur: Drawable,
    ) : MultiFitStitchSaveIntent()
}