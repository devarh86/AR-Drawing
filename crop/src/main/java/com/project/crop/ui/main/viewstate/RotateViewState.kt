package com.project.crop.ui.main.viewstate

sealed class RotateViewState {
    object Idle : RotateViewState()
    object UpdateUi : RotateViewState()
    class UpdateRotation(val rotation: Float) : RotateViewState()
    class ResetRotation() : RotateViewState()
    class UpdateRotationFromIcon(val rotation: Float) : RotateViewState()
    object Back : RotateViewState()
    object Tick : RotateViewState()
}
