package com.example.apponboarding.domain

data class Language(
    val name: String,
    val flagResId: Int,
    val languageCode: String,  // Drawable resource ID for flag
    var isSelected: Boolean = false
)