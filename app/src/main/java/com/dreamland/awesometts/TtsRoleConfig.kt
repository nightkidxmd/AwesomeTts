package com.dreamland.awesometts

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

/**
 * Created by @author XMD on 2017/12/11.
 */
@JsonObject
data class TtsRoleConfig (
        @JvmField
        @JsonField
        var ttsType: TtsType = TtsType.AWESOME_TTS,

        @JvmField
        @JsonField
        var ttsRole: Int = -1
)