package com.example.apponboarding.domain

interface LanguageRepository {
    fun getLanguages(): List<Language>
    fun setSelectedLanguage(language: Language)
    fun getSelectedLanguage(): Language?
}
