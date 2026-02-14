package com.project.common.model

data class RatioItem(
    val imageResId: Int,
    val ratioText: String,
    var isSelected: Boolean = false // Track selection state
)

