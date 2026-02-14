package com.project.common.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.project.common.repo.datastore.AppDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DataStoreViewModel @Inject constructor(
    private val appDataStore: AppDataStore
) : ViewModel() {

    fun initFun(){}

    fun initViewModel() {}

    val introCounter = appDataStore.readIntroCounter().asLiveData()
    fun updateIntroCounter(screen: Int) = viewModelScope.launch(IO) {
        appDataStore.writeIntroCounter(screen)
    }

    val appSessions = appDataStore.readSessionCounter().asLiveData()
    fun incrementAppSession() = viewModelScope.launch {
        appDataStore.incrementSessionCounter()
    }

    val surveyComplete = appDataStore.readSurveyComplete().asLiveData()
    fun updateSurveyComplete() = viewModelScope.launch(IO) {
        appDataStore.writeSurveyComplete()
    }

    val valentinePopShown= appDataStore.readValentinePopShown().asLiveData()
    fun updateValentinePopShown() = viewModelScope.launch(IO) {
        appDataStore.writeValentinePopShown()
    }


    val blendPopShown= appDataStore.readBlendPopUpShown().asLiveData()
    fun updateBlendPopShown() = viewModelScope.launch(IO) {
        appDataStore.writeBlendPopUpShown()
    }
    val multiFitPopShown= appDataStore.readBlendPopUpShown().asLiveData()
    fun updateMultiFitPopShown() = viewModelScope.launch(IO) {
        appDataStore.writeBlendPopUpShown()
    }
    val framePopShown= appDataStore.readFramePopUpShown().asLiveData()
    fun updateFramePopShown() = viewModelScope.launch(IO) {
        appDataStore.writeFramePopUpShown()
    }
    val collagePopShown= appDataStore.readCollagePopUpShown().asLiveData()
    fun updateCollagePopShown() = viewModelScope.launch(IO) {
        appDataStore.writeCollagePopUpShown()
    }
    val enhancerPopShown= appDataStore.readEnhancerPopUpShown().asLiveData()
    fun updateEnhancerPopShown() = viewModelScope.launch(IO) {
        appDataStore.writeEnhancerPopUpShown()
    }
    val photoPopShown= appDataStore.readPhotoPopUpShown().asLiveData()
    fun updatePhotoPopShown() = viewModelScope.launch(IO) {
        appDataStore.writePhotoPopUpShown()
    }

    val readIsRatingShownAfterFirstSave = appDataStore.readRatingAfterFirstSave().asLiveData()
    fun updateShowRatingAfterFirstSave() = viewModelScope.launch(IO) {
        appDataStore.writeRatingAfterFirstSave()
    }

    val introComplete = appDataStore.readIntroComplete().asLiveData()
    fun updateIntroComplete() = viewModelScope.launch(IO) {
        appDataStore.writeIntroComplete()
    }

    val saveSessions = appDataStore.readSaveCounter().asLiveData()

    fun incrementSaveSession() = viewModelScope.launch {
        appDataStore.incrementSaveCounter()
    }

    fun writeCurrentTime() = viewModelScope.launch(IO) {
        appDataStore.writeCurrentTime(System.currentTimeMillis())
    }

    val blendObGuideCompleted = appDataStore.readBlendOnBoardComplete().asLiveData()

    suspend fun readCurrentTime():Boolean{
        return withContext(IO){
            appDataStore.readCurrentTIme()
        }
    }

    val questionScreenCompleted = appDataStore.readQuestionsCompleted().asLiveData()


}
