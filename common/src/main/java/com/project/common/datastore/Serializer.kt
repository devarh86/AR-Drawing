package com.project.common.datastore

import androidx.annotation.Keep
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.project.common.datastore.FramePreferencesOuterClass
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception

@Keep
object UserPreferencesSerializer : Serializer<FramePreferencesOuterClass.FramePreferences> {

    override val defaultValue: FramePreferencesOuterClass.FramePreferences =
        FramePreferencesOuterClass.FramePreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): FramePreferencesOuterClass.FramePreferences {
        try {
            return FramePreferencesOuterClass.FramePreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
//            throw CorruptionException("Cannot read proto.", exception)
            return defaultValue
        }
    }

    override suspend fun writeTo(
        t: FramePreferencesOuterClass.FramePreferences,
        output: OutputStream
    ) {
        try {
            t.writeTo(output)
        } catch (ex: Exception) {
        }
    }
}