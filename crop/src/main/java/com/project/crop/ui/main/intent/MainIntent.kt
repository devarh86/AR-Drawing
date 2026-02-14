package com.project.crop.ui.main.intent

sealed class MainIntent {
    class RotateImage(var rotaion: Float) : MainIntent()

    class RotateImageFromIcon(var rotaion: Float) : MainIntent()

    class ResetRotation() : MainIntent()
}