package com.example.questions_intro.ui.view_model

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.questions_intro.R
import com.example.questions_intro.ui.model.IntroScreenContent
import com.project.common.utils.setString
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor(@ApplicationContext private val context: Context) :
    ViewModel() {

    fun initializing() {}

    fun screenOneContent(context: Context): IntroScreenContent {
        return IntroScreenContent(
            heading = context.setString(com.project.common.R.string.intro_one_heading),
            subHeading = context.setString(com.project.common.R.string.intro_one_sub_heading),
            icon = R.drawable.ai_image_enhancer
        )
    }

    fun screenTwoContent(context: Context): IntroScreenContent {
            return IntroScreenContent(
                heading = context.setString(com.project.common.R.string.intro_two_heading),
                subHeading = context.setString(com.project.common.R.string.intro_two_sub_heading),
                icon = R.drawable.new_collage_intro
            )
    }

    fun screenThreeContent(context: Context): IntroScreenContent {
        return IntroScreenContent(
            heading = context.setString(com.project.common.R.string.intro_three_heading),
            subHeading = context.setString(com.project.common.R.string.intro_three_sub_heading),
            icon = R.drawable.photo_editor_intro
        )
    }

    private fun applyDynamicColors(
        firstText: String,
        selectedText: String,
        remainingText: String,
    ): AnnotatedString? {
        try {
            return buildAnnotatedString {
                append(if (firstText.isBlank()) "" else "$firstText ")
                withStyle(
                    style = SpanStyle(
                        color = Color(
                            ContextCompat.getColor(
                                context,
                                com.project.common.R.color.selected_color
                            )
                        ),
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("\"$selectedText\"")
                }
                append(" $remainingText")
            }
        } catch (ex: Exception) {
//            AnnotatedString(if(firstText.isBlank())"" else "$firstText " + selectedText + " " + remainingText)
            return null
        }
    }
}