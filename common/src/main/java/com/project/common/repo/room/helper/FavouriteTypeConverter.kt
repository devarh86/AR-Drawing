package com.project.common.repo.room.helper

import androidx.annotation.Keep
import com.fahad.newtruelovebyfahad.GetFeatureScreenQuery
import com.fahad.newtruelovebyfahad.type.FramesFilesKeyChoices
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.project.common.utils.enums.serializers.FramesFilesKeyChoicesDeserializer


@Keep
object FavouriteTypeConverter {
    fun fromJson(value: String?): GetFeatureScreenQuery.Frame? {
        if (value == null) {
            return null
        }
        val gson = Gson()
        return try {
            gson.fromJson(value, object : TypeToken<GetFeatureScreenQuery.Frame?>() {}.type)
        } catch (ex: Exception) {
            null
        }
    }

    fun toJson(frame: Any?): String? {
        if (frame == null) {
            return null
        }
        val gson = GsonBuilder()
            .registerTypeAdapter(
                FramesFilesKeyChoices::class.java,
                FramesFilesKeyChoicesDeserializer()
            )
            .create()
        return try {
            gson.toJson(frame)
        } catch (ex: Exception) {
            null
        }
    }
}