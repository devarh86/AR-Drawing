package com.fahad.newtruelovebyfahad.ui.fragments.home

import androidx.annotation.Keep

@Keep
data class ButtonItem(
    val id: Int,
    val iconRes: Int,
    val text: String,
    val isEnabled: Boolean = true
)
