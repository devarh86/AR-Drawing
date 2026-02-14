package com.project.common.utils.enums

import androidx.annotation.Keep

@Keep
enum class EditorType(val editorName: String) {
    SINGLE("Single"),
    DOUBLE("Double"),
    GREETING("Greet"),
    COLLAGE("Collage"),
    SHAPES("Shape"),
    PIPS("Pip");
}