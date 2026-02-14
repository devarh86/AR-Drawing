package com.project.common.data_source

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import androidx.core.graphics.values
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.lang.reflect.Type


@Keep
class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        val listType: Type = object : TypeToken<List<String?>?>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromList(list: List<String?>?): String {
        val gson = Gson()
        return gson.toJson(list) ?: ""
    }

    @TypeConverter
    fun fromIntList(intList: List<Int>?): String? {
        if (intList == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.toJson(intList, type)
    }

    @TypeConverter
    fun toIntList(intListString: String?): List<Int>? {
        if (intListString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(intListString, type)
    }

    @TypeConverter
    fun fromFloatArray(floatArray: FloatArray): String {
        return floatArray.joinToString(",")
    }

    @TypeConverter
    fun toFloatArray(floatArrayString: String): FloatArray {
        return try {
            val stringArray = floatArrayString.split(",").toTypedArray()
            FloatArray(stringArray.size) { stringArray[it].toFloat() }
        } catch (ex: Exception) {
            val matrix = Matrix()
            matrix.reset()
            matrix.values()
        }
    }

    @TypeConverter
    fun fromDrawable(drawable: Drawable): ByteArray {
        val bitmapDrawable = drawable as BitmapDrawable
        val bitmap = bitmapDrawable.bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    @TypeConverter
    fun toDrawable(byteArray: ByteArray): Drawable {
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        return BitmapDrawable(null, bitmap)
    }

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?): ByteArray? {
        val stream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray?): Bitmap? {
        val bitmap = byteArray?.size?.let { BitmapFactory.decodeByteArray(byteArray, 0, it) }
        return bitmap
    }
}