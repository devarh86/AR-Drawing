package com.example.questions_intro.ui.model

import androidx.annotation.Keep
import androidx.compose.ui.text.AnnotatedString
import com.example.questions_intro.R

@Keep
data class IntroScreenContent(
    var heading: String? = "heading",
    var icon: Int = R.drawable.ai_image_enhancer,
    var animation: Int = R.raw.click_anim,
    var subHeading:String = "subheading"
)
