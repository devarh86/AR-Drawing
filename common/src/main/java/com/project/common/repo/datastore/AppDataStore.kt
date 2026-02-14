package com.project.common.repo.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "framme_app")

    private object AppPreferencesKeys {
        val CYCLE_COUNTER = intPreferencesKey("CYCLE_COUNTER")
        val SESSION_COUNTER = intPreferencesKey("SESSION_COUNTER")
        val SAVE_COUNTER = intPreferencesKey("SAVE_COUNTER")
        val INTRO_COMPLETE = booleanPreferencesKey("INTRO_COMPLETE")
        val SHOW_RATING = booleanPreferencesKey("SHOW_RATING")
        val QUESTION_COMPLETE = booleanPreferencesKey("QUESTION_COMPLETE")
        val SURVEY_COMPLETE = booleanPreferencesKey("SURVEY_COMPLETE")
        val VALENTINE_POP_UP_SHOWN = booleanPreferencesKey("VALENTINE_POP_UP_SHOWN")
        val CURRENT_TIME = longPreferencesKey("CURRENT_TIME")
        val INTRO_COUNTER = intPreferencesKey("INTRO_COUNTER")
        val BLEND_OB_COMPLETE = booleanPreferencesKey("BLEND_OB_COMPLETE")
        val AUTH_KEY = stringPreferencesKey("AUTH_KEY")


        val BLEND_POP_UP_SHOWN = booleanPreferencesKey("BLEND_POP_UP_SHOWN")
        val FRAME_POP_UP_SHOWN = booleanPreferencesKey("FRAME_POP_UP_SHOWN")
        val MULTI_FIT_POP_UP_SHOWN = booleanPreferencesKey("MULTI_FIT_POP_UP_SHOWN")
        val COLLAGE_POP_UP_SHOWN = booleanPreferencesKey("COLLAGE_POP_UP_SHOWN")
        val ENHANCER_POP_UP_SHOWN = booleanPreferencesKey("ENHANCER_POP_UP_SHOWN")
        val PHOTO_POP_UP_SHOWN = booleanPreferencesKey("PHOTO_POP_UP_SHOWN")

        val AUTH_KEY_SKETCH = stringPreferencesKey("AUTH_KEY_SKETCH")

    }

    suspend fun writeAuthKeySketch(key: String) {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.AUTH_KEY_SKETCH] = key
        }
    }

    fun readAuthKeySketchCompleteRunBlock(context: Context): String {
        return runBlocking(Dispatchers.IO) {
            context.applicationContext.dataStore.data.first()[AppPreferencesKeys.AUTH_KEY_SKETCH] ?: ""
        }
    }

    fun readBlendOnBoardComplete() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.BLEND_OB_COMPLETE] ?: false }

    suspend fun writeBlendOnBoardComplete() {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.BLEND_OB_COMPLETE] = true
        }
    }

    suspend fun writeAuthKey(key: String) {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.AUTH_KEY] = key
        }
    }

    fun readAuthKeyComplete() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.AUTH_KEY] ?: "" }

    fun readAuthKeyCompleteRunBlock(context: Context): String {
        return runBlocking(Dispatchers.IO) {
            context.applicationContext.dataStore.data.first()[AppPreferencesKeys.AUTH_KEY] ?: ""
        }
    }

    suspend fun writeSurveyComplete() {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.SURVEY_COMPLETE] = true
        }
    }

    fun readSurveyComplete() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.SURVEY_COMPLETE] ?: false }


    fun readValentinePopShown() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.VALENTINE_POP_UP_SHOWN] ?: false }

    suspend fun writeValentinePopShown() {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.VALENTINE_POP_UP_SHOWN] = true

        }
    }

    suspend fun writeIntroCounter(value: Int) {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.INTRO_COUNTER] = value
        }
    }

    fun readIntroCounter() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.INTRO_COUNTER] ?: 0 }

    fun readBlendPopUpShown() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.BLEND_POP_UP_SHOWN] ?: false }

    suspend fun writeBlendPopUpShown() {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.BLEND_POP_UP_SHOWN] = true

        }
    }
  suspend fun writeMultiFitPopUpShown(){
        context.applicationContext.dataStore.edit { preferences->
            preferences[AppPreferencesKeys.MULTI_FIT_POP_UP_SHOWN]=true

        }
    }


    fun readFramePopUpShown() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.FRAME_POP_UP_SHOWN] ?: false }


    fun readMultiFitPopUpShown() = context.applicationContext.dataStore.data
        .map { preferences->preferences[AppPreferencesKeys.MULTI_FIT_POP_UP_SHOWN]?:false }

    suspend fun writeFramePopUpShown(){
        context.applicationContext.dataStore.edit { preferences->
            preferences[AppPreferencesKeys.FRAME_POP_UP_SHOWN]=true

        }
    }

    fun readCollagePopUpShown() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.COLLAGE_POP_UP_SHOWN] ?: false }

    suspend fun writeCollagePopUpShown() {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.COLLAGE_POP_UP_SHOWN] = true

        }
    }

    fun readEnhancerPopUpShown() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.ENHANCER_POP_UP_SHOWN] ?: false }

    suspend fun writeEnhancerPopUpShown() {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.ENHANCER_POP_UP_SHOWN] = true

        }
    }

    fun readPhotoPopUpShown() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.PHOTO_POP_UP_SHOWN] ?: false }

    suspend fun writePhotoPopUpShown() {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.PHOTO_POP_UP_SHOWN] = true

        }
    }


    fun readSessionCounter() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.SESSION_COUNTER] ?: 1 }

    suspend fun incrementSessionCounter() {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.SESSION_COUNTER] = readSessionCounter().first() + 1
        }
    }

    fun readRatingAfterFirstSave() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.SHOW_RATING] ?: false }

    suspend fun writeRatingAfterFirstSave() {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.SHOW_RATING] = true
        }
    }

    fun readIntroComplete() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.INTRO_COMPLETE] ?: false }

    suspend fun writeIntroComplete() {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.INTRO_COMPLETE] = true
        }
    }

    fun readSaveCounter() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.SAVE_COUNTER] ?: 1 }

    suspend fun incrementSaveCounter() {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.SAVE_COUNTER] = readSaveCounter().first() + 1
        }
    }

    fun readCycleCounter() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.CYCLE_COUNTER] ?: 1 }

    suspend fun incrementCycleCounter() {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.CYCLE_COUNTER] = readCycleCounter().first() + 1
        }
    }

    suspend fun writeCurrentTime(value: Long) {
        kotlin.runCatching {
            context.applicationContext.dataStore.data
                .map { preferences -> preferences[AppPreferencesKeys.CURRENT_TIME] ?: -1L }
                .firstOrNull()
                ?.let {
                    if (it == -1L) {
                        context.applicationContext.dataStore.edit { preferences ->
                            preferences[AppPreferencesKeys.CURRENT_TIME] = value

                        }
                    }
                }
        }
    }

    suspend fun readCurrentTIme(): Boolean {
        context.applicationContext.dataStore.data
            .map { preferences -> preferences[AppPreferencesKeys.CURRENT_TIME] ?: -1L }
            .firstOrNull()
            ?.let {
                if (it != -1L) {
                    return calculateHourDifference(it) >= 24
                } else {
                    return false
                }
            } ?: return false
    }

    private fun calculateHourDifference(oldDateTimeInMillis: Long): Long {
        val currentTimeInMillis = System.currentTimeMillis()
        val timeDifferenceInMillis = currentTimeInMillis - oldDateTimeInMillis
        return timeDifferenceInMillis / (1000 * 60 * 60)
    }

    fun readQuestionsCompleted() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.QUESTION_COMPLETE] ?: false }

    suspend fun writeQuestionComplete() {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.QUESTION_COMPLETE] = true
        }
    }
}