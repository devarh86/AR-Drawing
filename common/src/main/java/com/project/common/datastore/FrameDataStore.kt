package com.project.common.datastore

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.project.common.datastore.FramePreferencesOuterClass
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Keep
@Singleton
class FrameDataStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val Context.dataStore: DataStore<FramePreferencesOuterClass.FramePreferences> by dataStore(
        fileName = "frames",
        serializer = UserPreferencesSerializer
    )

    fun readIsUnlock(id: Int): Flow<Boolean> =
        context.applicationContext.dataStore.data.map { preferences ->
            if (preferences.unlockedList != null && preferences.unlockedList.isNotEmpty()) {
                preferences.unlockedList.contains(id)
            } else {
                false
            }
        }

    fun readAll(): Flow<List<Int>>? {
        try {
            return context.applicationContext.dataStore.data.map { preferences ->
                preferences.unlockedList ?: emptyList()
            }
        } catch (ex: Exception) {
            Log.e("error", "readAll: ", ex)
        }
        return null
    }

    suspend fun writeUnlockedId(id: Int) {
        runCatching {
            context.applicationContext.dataStore.updateData { currentPreferences ->
                val updatedPreferences = currentPreferences.toBuilder()
                    .addUnlocked(id)
                    .build()
                updatedPreferences
            }
        }
    }
}

