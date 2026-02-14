package com.abdul.pencil_sketch.main.viewstate

sealed class SketchImageActionViewState {

    object Idle : SketchImageActionViewState()
    object Loading : SketchImageActionViewState()
    object Success : SketchImageActionViewState()
    class Error(val message: String) : SketchImageActionViewState()
    object ShowLoadingState:SketchImageActionViewState()

    class SetUserImage(var path: String = "") : SketchImageActionViewState()
    class ImageEnhanceRequestComplete(var path: String = "", var width: Int = 0, var height: Int = 0):SketchImageActionViewState()
    object SaveLoading : SketchImageActionViewState()
    class SaveComplete(var fromEditorBitmap:Boolean = false) : SketchImageActionViewState()
    class UpdateProgress(var progress: Int, var value: String) : SketchImageActionViewState()

    class UpdateImage(
        var x: Float = 0f,
        var y: Float = 0f,
        var width: Int = 0,
        var height: Int = 0,
        var rotation: Float = 0f,
        var imageIndex: Int = 0,
        var isLast: Boolean = false,
        var percentX: Float = 0f,
        var percentY: Float = 0f,
        var percentHeight: Float = 0f,
        var percentWidth: Float = 0f,
        var mask: Any = ""
    ) : SketchImageActionViewState()

    class UpdateImagePathsWithEnhancement(
        var index: Int,
        var path: String,
        var isLast: Boolean,
        var mask: Any? = "",
        var fromCrop: Boolean = false,
    ) : SketchImageActionViewState()

}