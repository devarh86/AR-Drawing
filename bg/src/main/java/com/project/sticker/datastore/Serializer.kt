package com.project.sticker.datastore

import androidx.annotation.Keep
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.project.sticker.datastore.StickerPreferencesOuterClass.StickerPreferences
import java.io.InputStream
import java.io.OutputStream

@Keep
object UserPreferencesSerializer : Serializer<StickerPreferences> {
    override val defaultValue: StickerPreferences = StickerPreferences.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): StickerPreferences {
        try {
            return StickerPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
//            throw CorruptionException("Cannot read proto.", exception)
            return defaultValue
        }
    }

    override suspend fun writeTo(t: StickerPreferences, output: OutputStream) = t.writeTo(output)
}