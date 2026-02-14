package com.example.inapp.repo.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingDataStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "in_app_purchase")

    private object AppPreferencesKeys {
        val IS_PRO = booleanPreferencesKey("IS_PRO")
        val HINT = booleanPreferencesKey("HINT")
        val IMAGE_HINT = booleanPreferencesKey("IMAGE_HINT")
        val HINT_SWAP = booleanPreferencesKey("HINT_SWAP")
        val HINT_CROP_BLEND = booleanPreferencesKey("HINT_CROP_BLEND")
        val HINT_GALLERY_BLEND = booleanPreferencesKey("HINT_GALLERY_BLEND")
        val SAVE_QUALITY = stringPreferencesKey("SAVE_QUALITY")
        val WATER_MARK = longPreferencesKey("WATER_MARK")
        val RESOLUTION = longPreferencesKey("RESOLUTION")
    }


    fun readIsPro() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.IS_PRO] ?: false }

    suspend fun writeIsPro(check: Boolean) {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.IS_PRO] = check
        }
    }

    suspend fun writeIsAlreadyShown(check: Boolean) {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.HINT] = check
        }
    }

    fun readIsAlreadyShownHintSwap() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.HINT_SWAP] ?: false }

    suspend fun writeIsAlreadyShownHintSwap(check: Boolean) {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.HINT_SWAP] = check
        }
    }

    fun readIsAlreadyShownCropBlend() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.HINT_CROP_BLEND] ?: false }

    suspend fun writeIsAlreadyShownCropBlend(check: Boolean) {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.HINT_CROP_BLEND] = check
        }
    }

    fun readIsAlreadyShownGalleryBlend() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.HINT_GALLERY_BLEND] ?: false }

    suspend fun writeIsAlreadyShownGalleryBlend(check: Boolean) {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.HINT_GALLERY_BLEND] = check
        }
    }

    fun readSaveQuality() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.SAVE_QUALITY] ?: "MEDIUM" }

    suspend fun writeSaveQuality(value: String) {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.SAVE_QUALITY] = value
        }
    }

    fun readIsAlreadyShown() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.HINT] ?: false }


    suspend fun writeIsAlreadyShownGalleryHint(check: Boolean) {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.IMAGE_HINT] = check
        }
    }

    fun readIsAlreadyShownGalleryHint() = context.applicationContext.dataStore.data
        .map { preferences -> preferences[AppPreferencesKeys.IMAGE_HINT] ?: false }


    suspend fun writeWaterMark(value: Long) {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.WATER_MARK] = value
        }
    }

    suspend fun readAndShowWaterMark(): Boolean {
        context.applicationContext.dataStore.data
            .map { preferences -> preferences[AppPreferencesKeys.WATER_MARK] ?: -1L }.firstOrNull()
            ?.let {
                if (it != -1L) {
                    return calculateHourDifference(it) >= 24
                } else {
                    return true
                }
            } ?: return true
    }

    private fun calculateHourDifference(oldDateTimeInMillis: Long): Long {
        val currentTimeInMillis = System.currentTimeMillis()
        val timeDifferenceInMillis = currentTimeInMillis - oldDateTimeInMillis
        return timeDifferenceInMillis / (1000 * 60 * 60)
    }

    suspend fun writeResolutions(value: Long) {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[AppPreferencesKeys.RESOLUTION] = value
        }
    }

    suspend fun readAndShowResolutions(): Boolean {
        context.applicationContext.dataStore.data
            .map { preferences -> preferences[AppPreferencesKeys.RESOLUTION] ?: -1L }.firstOrNull()
            ?.let {
                if (it != -1L) {
                    return calculateHourDifference(it) >= 24
                } else {
                    return true
                }
            } ?: return true
    }
}