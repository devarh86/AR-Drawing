package com.fahad.newtruelovebyfahad.ui.fragments.learning.customview.drawing_action

import android.graphics.Paint

sealed class DrawingAction {
    data class Path(val path: android.graphics.Path, val paint: Paint, val isEraser: Boolean = false) : DrawingAction()
}