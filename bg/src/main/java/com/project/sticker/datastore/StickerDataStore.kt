package com.project.sticker.datastore

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.preferencesDataStore
import com.project.sticker.datastore.StickerPreferencesOuterClass.StickerPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Keep
@Singleton
class StickerDataStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val Context.dataStore: DataStore<StickerPreferences> by dataStore(
        fileName = "stickers",
        serializer = UserPreferencesSerializer
    )

    fun readIsUnlock(id: Int): Flow<Boolean>? {
        return try {
            context.applicationContext.dataStore.data.map { preferences ->
                if (preferences.unlockedList != null && preferences.unlockedList.isNotEmpty()) {
                    preferences.unlockedList.contains(id)
                } else {
                    false
                }
            }
        } catch (ex: Exception) {
            null
        }
    }

    suspend fun writeUnlockedId(id: Int) {
        try {
            context.applicationContext.dataStore.updateData { currentPreferences ->
                val updatedPreferences = currentPreferences.toBuilder()
                    .addUnlocked(id)
                    .build()
                updatedPreferences
            }
        }catch (ex: Exception){
            Log.e("error", "writeUnlockedId: $ex")
        }
    }
}

