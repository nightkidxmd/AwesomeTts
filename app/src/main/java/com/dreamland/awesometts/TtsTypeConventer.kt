package com.dreamland.awesometts

import com.bluelinelabs.logansquare.typeconverters.TypeConverter
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser

/**
 * Created by @author XMD on 2017/12/20.
 */
class TtsTypeConventer : TypeConverter<TtsType> {
    override fun parse(jsonParser: JsonParser?) =
            when (jsonParser?.intValue) {
                0 -> TtsType.NONE_TTS
                1 -> TtsType.MAGIC_TTS
                2 -> TtsType.AWESOME_TTS
                else -> null
            }

    override fun serialize(`object`: TtsType?, fieldName: String?, writeFieldNameForObject: Boolean, jsonGenerator: JsonGenerator?) {
        `object`?.let {
            jsonGenerator?.writeFieldName(fieldName)
            jsonGenerator?.writeNumber(`object`.ordinal)
        }
    }
}