package com.abdul.pencil_sketch.main.intents

import android.content.Context
import android.graphics.Bitmap

sealed class SketchIntent {

    class GenerateToken(var context: Context) : SketchIntent()
    object SetImage : SketchIntent()
    object SetFrame : SketchIntent()

    class SaveImages(var context: Context) : SketchIntent()
    class SaveImageForEditor(var context: Context, var editorBitmap: Bitmap) : SketchIntent()
    class AddCroppedImage(var index: Int, var path: String) : SketchIntent()
    class ImageEnhancementAndPlacing(var context: Context) : SketchIntent()
    class SingleImageEnhancementAndPlacing(var path: String, var index: Int) : SketchIntent()


}