package com.fahad.newtruelovebyfahad.utils.fonts

import androidx.annotation.Keep

@Keep
data class Frame(
    val category: Category,
    val editor: String,
    val files: List<File>,
    val hashtag: List<Hashtag>,
    val tags: List<Tag>,
    val thumb: String,
    val title: String
)