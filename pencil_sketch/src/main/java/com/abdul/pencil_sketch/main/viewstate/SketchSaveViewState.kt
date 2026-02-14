package com.abdul.pencil_sketch.main.viewstate

import com.project.common.enum_classes.SaveQuality

sealed class SketchSaveViewState {
    object Idle : SketchSaveViewState()
    object Cancel : SketchSaveViewState()
    object Back : SketchSaveViewState()
    class Success(var path: String) : SketchSaveViewState()
    class Error(val message: String) : SketchSaveViewState()
    class UpdateProgress(val progress: Int) : SketchSaveViewState()
    class UpdateProgressText(val text: String) : SketchSaveViewState()
    class SaveClick(val resolution: SaveQuality) : SketchSaveViewState()
}
