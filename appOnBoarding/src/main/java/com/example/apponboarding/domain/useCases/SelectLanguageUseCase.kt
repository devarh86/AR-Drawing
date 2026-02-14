package com.example.apponboarding.domain.useCases

import com.example.apponboarding.domain.Language
import com.example.apponboarding.domain.LanguageRepository
import javax.inject.Inject

class SelectLanguageUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) {

    // Retrieve all available languages
    fun getLanguages(): List<Language> = languageRepository.getLanguages()

    // Set the selected language
    fun selectLanguage(language: Language) = languageRepository.setSelectedLanguage(language)

    // Get the currently selected language
    fun getSelectedLanguage(): Language? = languageRepository.getSelectedLanguage()
}

