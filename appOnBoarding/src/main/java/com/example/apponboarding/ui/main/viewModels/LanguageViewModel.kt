package com.example.apponboarding.ui.main.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.apponboarding.domain.Language
import com.example.apponboarding.domain.useCases.SelectLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val selectLanguageUseCase: SelectLanguageUseCase
) : ViewModel() {

    private val _languages = MutableLiveData<List<Language>>()
    val languages: LiveData<List<Language>> = _languages

    private val _selectedLanguage = MutableLiveData<Language?>()
    val selectedLanguage: LiveData<Language?> = _selectedLanguage

   /* private val _isLanguageSelected = MutableLiveData<Boolean>()
    val isLanguageSelected: LiveData<Boolean> = _isLanguageSelected*/

    private val _isLanguageSelected = MutableStateFlow(false)
    val isLanguageSelected: StateFlow<Boolean> = _isLanguageSelected

    init {
        loadLanguages()
    }

    fun loadLanguages() {
        val languageList = selectLanguageUseCase.getLanguages()
        _languages.value = languageList

        // Nullable value for selected language
        val selected = selectLanguageUseCase.getSelectedLanguage()
        _selectedLanguage.value = selected
        _isLanguageSelected.value = selected != null
    }

    fun selectLanguage(language: Language) {
        selectLanguageUseCase.selectLanguage(language)
        _selectedLanguage.value = language
        _isLanguageSelected.value = true
    }

    fun getSelectedLanguageCode(): String {
        return selectLanguageUseCase.getSelectedLanguage()?.languageCode ?: "en" // Default to English
    }
}