package com.fahad.newtruelovebyfahad.ui.activities.pro.slider

import android.content.Context
import androidx.annotation.Keep
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.utils.enums.MainMenuOptions
import com.fahad.newtruelovebyfahad.utils.setString
import com.project.common.utils.enums.MainMenuBlendOptions

@Keep
object SliderList {

    fun getImageList(context: Context): ArrayList<SliderItem> {
        return arrayListOf(
            SliderItem(
                R.drawable.s_ai_enhance,
                MainMenuOptions.AI_ENHANCE.title,
                MainMenuOptions.AI_ENHANCE.title,
                context.setString(com.project.common.R.string.enhance_t_s),
                context.setString(com.project.common.R.string.enhance_d_s)
            ),
            SliderItem(
                R.drawable.s_bg_art,
                MainMenuOptions.BLEND.title,
                MainMenuBlendOptions.BG_ART.title,
                context.setString(com.project.common.R.string.bg_art_t_s),
                context.setString(com.project.common.R.string.bg_art_d_s)
            ),
            SliderItem(
                R.drawable.s_photo_colag,
                MainMenuOptions.COLLAGEFRAME.title,
                MainMenuOptions.COLLAGEFRAME.title,
                context.setString(com.project.common.R.string.collage_t_s),
                context.setString(com.project.common.R.string.collage_d_s)

            ),
            SliderItem(
                R.drawable.s_edit_img,
                com.project.common.utils.enums.MainMenuOptions.PHOTOEDITOR.title,
                com.project.common.utils.enums.MainMenuOptions.PHOTOEDITOR.title,
                context.setString(com.project.common.R.string.edit_t_s),
                context.setString(com.project.common.R.string.edit_d_s)

            ),
            SliderItem(
                R.drawable.s_blend_img,
                MainMenuOptions.BLEND.title,
                MainMenuBlendOptions.EFFECTS.title,
                context.setString(com.project.common.R.string.blend_t_s),
                context.setString(com.project.common.R.string.blend_d_s)
            ),
        )
    }

    fun getImageListHome(context: Context): ArrayList<SliderItem> {
        return arrayListOf(
            SliderItem(
                R.drawable.s_image_1,
                MainMenuOptions.DRAWING.title,
                context.setString(com.project.common.R.string.s_home_h1_sub),
                context.setString(com.project.common.R.string.s_home_h1),
                context.setString(com.project.common.R.string.s_home_h1_detail)
            ),
            SliderItem(
                R.drawable.s_image_2,
                MainMenuOptions.LEARNING.title,
                context.setString(com.project.common.R.string.s_home_h2_sub),
                context.setString(com.project.common.R.string.s_home_h2),
                context.setString(com.project.common.R.string.s_home_h1_detail)
            ),
            SliderItem(
                R.drawable.s_image_3,
                MainMenuOptions.SKETCH.title,
                context.setString(com.project.common.R.string.s_home_h3_sub),
                context.setString(com.project.common.R.string.s_home_h3),
                context.setString(com.project.common.R.string.s_home_h3_detail)

            ),
            SliderItem(
                R.drawable.s_image_4,
                MainMenuOptions.IMPORT_GALLERY.title,
                context.setString(com.project.common.R.string.s_home_h4_sub),
                context.setString(com.project.common.R.string.s_home_h4),
                context.setString(com.project.common.R.string.s_home_h4_detail)

            )
        )
    }

}





