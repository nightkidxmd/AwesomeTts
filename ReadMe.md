## DISCLAIMER
`ONLY FOR STUDY, NOT FOR COMMERCIAL USE `

### 1. assets copy
> **Note:**DO NOT CHANGE ASSETS PATH
#### 1.1 TtsType.MAGIC_TTS
>iflytek tts for autonavi --- Zhiling Lin

`/assets/tts/iflytek/autonavi`

You can download more voice res from AutoNavi App
#### 1.2 TtsType.AWESOME_TTS
>iflytek original tts

`/assets/tts/original`

#### 2. usage
```java
class TtsService: BaseSevice(), ITTSListener<TtsInfo> {
        private val ttsEngine by lazy { TTSEngine<TtsInfo>(applicationContext,TtsType.AWESOME_TTS) }
        override fun onCreate() {
            super.onCreate()
            ttsEngine.registerITTSListener(this)
            ttsEngine.init()
        }
        override fun onDestroy() {
             super.onDestroy()
             ttsEngine.destroy()
        }
    
        override fun handleMessage(p0: Message?) {
            if (p0?.obj is Intent) {
                (p0.obj as Intent).also {
                    val data = it.getBundleExtra(ActionConst.EXTRA_DATA)
                    when (it.action) {
                        ActionConst.ACTION_TTS_REQUEST -> {
                            //Play tts
                            ttsEngine.play(data.getSerializable(ActionConst.EXTRA_DATA) as TtsInfo)
                        }
                    }
                }
            }
        }
    
    
        override fun onTTSEnd(p0: TtsInfo?) {
             //TODO tts play end
        }
        
        override fun onTTSPlay(p0: TtsInfo?) {
             //TODO tts play start
        }
}
```