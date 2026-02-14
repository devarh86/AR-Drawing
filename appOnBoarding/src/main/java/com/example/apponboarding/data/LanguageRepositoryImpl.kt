package com.example.apponboarding.data

import android.content.Context
import android.content.SharedPreferences
import com.example.apponboarding.R
import com.example.apponboarding.domain.Language
import com.example.apponboarding.domain.LanguageRepository
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val context: Context
) : LanguageRepository {

    private val languages = listOf(
        Language(
            name = context.getString(com.project.common.R.string.language_indo),
            flagResId = R.drawable.indonesian_icon,
            "in"
        ),
        Language(
            name = context.getString(com.project.common.R.string.language_english),
            flagResId = com.project.common.R.drawable.english_icon,
            "en"
        ),
        Language(
            name = context.getString(com.project.common.R.string.language_bengali),
            flagResId = R.drawable.bengali_icon,
            "bn"
        ),
        Language(
            name = context.getString(com.project.common.R.string.language_german),
            flagResId = R.drawable.german_icon,
            "de"
        ),
        Language(
            name = context.getString(com.project.common.R.string.language_portuguese),
            flagResId = R.drawable.portuguese_icon,
            "pt"
        ),
        Language(
            name = context.getString(com.project.common.R.string.language_arabic),
            flagResId = R.drawable.arabic_icon,
            "ar"
        ),
        Language(
            name = context.getString(com.project.common.R.string.language_spanish),
            flagResId = R.drawable.spanish_icon,
            "es"
        ),
        Language(
            name = context.getString(com.project.common.R.string.language_mandarin),
            flagResId = R.drawable.mandarin_icon,
            "zh"
        ),
        Language(
            name = context.getString(com.project.common.R.string.language_russian),
            flagResId = R.drawable.russian_icon,
            "ru"
        ),
        Language(
            name = context.getString(com.project.common.R.string.language_urdu),
            flagResId = R.drawable.urdu_icon,
            "ur"
        ),
        Language(
            name = context.getString(com.project.common.R.string.language_french),
            flagResId = R.drawable.french_icon,
            "fr"
        ),
        Language(
            name = context.getString(com.project.common.R.string.language_hindi),
            flagResId = R.drawable.hindi_icon,
            "hi"
        ),
        Language(
            name = context.getString(com.project.common.R.string.language_japanese),
            flagResId = R.drawable.japanese_icon,
            "ja"
        )

    )

    override fun getLanguages(): List<Language> {
        val selectedLanguage = getSelectedLanguage() 
        return languages.map { it.copy(isSelected = it.name == selectedLanguage.name) }
    }

    override fun setSelectedLanguage(language: Language) {
        sharedPreferences.edit()
            .putString("selected_language_code", language.languageCode)
            .apply()
    }

    override fun getSelectedLanguage(): Language {
        val currentLanguageCode = sharedPreferences.getString("selected_language_code", "en")
        return languages.find { it.languageCode == currentLanguageCode }
            ?: Language(
                name = context.getString(com.project.common.R.string.language_english),
                flagResId = com.project.common.R.drawable.english_icon,
                "en"
            )
    }
}
