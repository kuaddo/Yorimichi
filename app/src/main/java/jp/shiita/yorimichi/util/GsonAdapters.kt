package jp.shiita.yorimichi.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

internal class BooleanAdapter : JsonDeserializer<Boolean> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Boolean =
        when (json?.asInt) {
            0 -> false
            else -> true
        }
}