package com.project.common.utils.enums.serializers

import com.fahad.newtruelovebyfahad.type.FramesFilesKeyChoices
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class FramesFilesKeyChoicesDeserializer :
    JsonDeserializer<FramesFilesKeyChoices?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FramesFilesKeyChoices {
        val value = json.asString
        return FramesFilesKeyChoices.safeValueOf(value)
    }
}
