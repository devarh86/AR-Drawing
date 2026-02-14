package com.project.common.states

sealed class EyeDropperState {
    data class Brightness(val brightness: Float) : EyeDropperState()
    data class Alpha(val alpha: Float) : EyeDropperState()
}