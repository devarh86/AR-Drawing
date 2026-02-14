package com.example.questions_intro.ui.view_model

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.example.questions_intro.R
import com.example.questions_intro.ui.model.Questions
import com.example.questions_intro.ui.model.QuestionsChoices
import com.project.common.utils.setString

class QuestionsViewModel() : ViewModel() {

    val fragmentStateMaps: MutableList<MutableMap<Int, Boolean>> = mutableListOf()

    // Initialize a new state map for each fragment (if needed)
    fun initializeStateForFragment(fragmentIndex: Int, index: Int, value: Boolean) {
        kotlin.runCatching {
            if (fragmentIndex < fragmentStateMaps.size && fragmentIndex >= 0) {
                fragmentStateMaps[fragmentIndex][index] = value
            } else {
                val stateMap = mutableStateMapOf<Int, Boolean>()
                stateMap[index] = value
                fragmentStateMaps.add(stateMap)
            }
        }
    }

    fun getMapForCurrentFragment(fragmentIndex: Int): MutableMap<Int, Boolean>? {
        try {
            return if (fragmentIndex < fragmentStateMaps.size && fragmentIndex >= 0) {
                fragmentStateMaps[fragmentIndex]
            } else {
                null
            }
        } catch (ex: Exception) {
            return null
        }
    }

    fun initializing() {}

    fun fetchQuestions(screen: Int, context: Context): Questions {
        return when (screen) {
            1 -> {
                Questions(
                    context.setString(com.project.common.R.string.pick_your_journey),
                    context.setString(com.project.common.R.string.help_us_bring_experiences)
                )

            }

            2 -> {
                Questions(
                    context.setString(com.project.common.R.string.where_publish_content),
                    context.setString(com.project.common.R.string.help_us_bring_experiences)
                )

            }

            3 -> {
                Questions(
                    context.setString(com.project.common.R.string.define_your_role ),
                    context.setString(com.project.common.R.string.help_us_bring_experiences)
                )
            }

            else -> {
                Questions(
                    context.setString(com.project.common.R.string.how_did_you_find_us),
                    context.setString(com.project.common.R.string.help_us_bring_experiences)
                )
            }
        }
    }

    fun screenOneContent(context: Context): List<QuestionsChoices> {
        return listOf(
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.personal_user),
                icon = R.drawable.personal_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.student),
                icon = R.drawable.student_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.content_creator),
                icon = R.drawable.content_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.photo_editor),
                icon = R.drawable.editor_photo_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.design_teacher),
                icon = R.drawable.teacher_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.business_owner),
                icon = R.drawable.small_business_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.employee),
                icon = R.drawable.employee_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.event_planner),
                icon = R.drawable.event_planner_icon
            )
        )
    }

    fun screenTwoContent(context: Context): List<QuestionsChoices> {
        return listOf(
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.collage),
                icon = R.drawable.collage_img_new
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.photo_edit),
                icon = R.drawable.photo_editor_new
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.ai_enhance),
                icon = R.drawable.ai_enhancer_new
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.templates),
                icon = R.drawable.frame_img_new
            ),
//            QuestionsChoices(
//                title = context.getString(com.project.common.R.string.bg_art),
//                icon = R.drawable.personal_icon
//            ),
//            QuestionsChoices(
//                title = context.getString(com.project.common.R.string.profile_pic),
//                icon = R.drawable.personal_icon
//            ),
//            QuestionsChoices(
//                title = context.getString(com.project.common.R.string.pip_template),
//                icon = R.drawable.personal_icon
//            ),
//            QuestionsChoices(
//                title = context.getString(com.project.common.R.string.other),
//                icon = R.drawable.personal_icon
//            )
        )
    }

    fun screenThreeContent(context: Context): List<QuestionsChoices> {
        return listOf(
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.facebook),
                icon = R.drawable.fb_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.instagram),
                icon = R.drawable.insta_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.tiktok),
                icon = R.drawable.tiktok_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.pinterest),
                icon = R.drawable.pintrest_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.linkedin),
                icon = R.drawable.linked_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.reddit),
                icon = R.drawable.reddit_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.dribbble),
                icon = R.drawable.dribble_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.behance),
                icon = R.drawable.behance_icon
            )
        )
    }

    fun screenFourContent(context: Context): List<QuestionsChoices> {
        return listOf(
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.facebook),
                icon = R.drawable.fb_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.tiktok),
                icon = R.drawable.tiktok_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.youtube),
                icon = R.drawable.yout_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.instagram),
                icon = R.drawable.insta_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.discord),
                icon = R.drawable.discord_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.reddit),
                icon = R.drawable.reddit_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.linkedin),
                icon = R.drawable.linked_icon
            ),
            QuestionsChoices(
                title = context.getString(com.project.common.R.string.twitter),
                icon = R.drawable.twitter_icon
            )
        )
    }
}