package com.dreamland.awesometts.flytek

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.util.*

/**
 * Created by @author XMD on 2017/12/11.
 */
@JsonObject
data class AwesomeTtsConfig (
        @JvmField @JsonField var resource: ArrayList<TtsConfig> = ArrayList()
){
    fun addTtsConfig(ttsConfig: TtsConfig){
        resource.add(ttsConfig)
    }
}

@JsonObject
data class TtsConfig(
        @JvmField @JsonField var name:String? = null,
        @JvmField @JsonField var loadtype:Int = 0
)