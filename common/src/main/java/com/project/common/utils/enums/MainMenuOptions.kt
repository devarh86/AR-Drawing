package com.project.common.utils.enums

import androidx.annotation.Keep

@Keep
enum class MainMenuOptions(val title: String) {
    SCRL("scrl"),
    MULTIFIT("MultiFit"),
    STITCH("Stitch"),
    TEMPLATES("Templates"),
    MAIN("Main"),
    SEAMLESS("Seamless"),
    TAPE("Tape"),
    PAPER("Paper"),
    REWIND("Rewind"),
    FILM("Film"),
    PLASTIC("Plastic"),
    STORIES("Stories"),

    SOLO("Solo"),
    DUAL("Dual"),
    MULTIPLEX("Multiplex"),
    PIP("Pip"),
    COLLAGEFRAME("Collage Frames"),
    SHAPE("Shape"),
    COLLAGE("Collage"),
    PHOTOEDITOR("PhotoEditor"),
//    AI_ENHANCER("AI Enhancer"),
    AI_ENHANCER("AI Enhancer\uD83D\uDCAB"),
    GREETING("Greeting"),
    Effect("Effect"),
    BLEND("Blend"),
    TOP_PICK("TopPick"),
    DRAWING("Drawing"),
    LEARNING("learn_draw"),
    SKETCH("sketch"),
    IMPORT_GALLERY("import_gallery")
}

@Keep
enum class MainMenuBlendOptions(val title: String) {
    SEAMLESS("Seamless"),
    TAPE("Tape"),
    PAPER("Paper"),
    REWIND("Rewind"),
    FILM("Film"),
    PLASTIC("Plastic"),
    STORIES("Stories"),
    NEON("Neon Style"),
    PROFILE_PICTURE("Profile Pic"),
    BLEND("AI Blend"),
    EFFECTS("Effects"),
    DRIP_ART("Drip Art"),
    OVERLAY("Overlay Effects"),
    SPIRAL("Spiral"),
    DOUBLE_EXPOSURE("Double Exposure"),
    BG_ART("Bg Art"),
    COLLAGE("Collage"),
    DRAWING("Drawing"),
    LEARNING("learn_draw"),
    SKETCH("sketch"),
    IMPORT_GALLERY("import_gallery")
}