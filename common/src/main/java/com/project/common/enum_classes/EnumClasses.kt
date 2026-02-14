package com.project.common.enum_classes

import androidx.annotation.Keep

@Keep
enum class EditorBottomTypes {

    NONE, ADD,FRAME,LAYOUT,BG, TEXT, FILTERS, STICKER, REPLACE, CLOSE, CROP, ROTATE, VERTICAL, HORIZONTAL, ADJUSTMENT, IMAGE_EDITOR, SAVE, AI_ENHANCE, RATIO
}

@Keep
enum class Progress {
    DONE
}
@Keep
enum class BgResultEnum(val value: String) {
    BACKGROUND_GALLERY("BACKGROUND_GALLERY"),
    ADD_BUTTON("ADD_BUTTON")
}

@Keep
enum class SaveQuality {
    LOW, MEDIUM, HIGH
}

@Keep
enum class FrameFetching {
    NONE, ALLCATEGORIES, CATEGORY, ALLFRAMES, FRAME, ALLTAGS, FETCHSPECIFICCATEGORYDATA
}