package com.dreamland.awesometts.flytek;

import android.content.Context;
import android.media.AudioManager;
import android.os.Environment;

import com.iflytek.ITTSListener;
import com.iflytek.speech.tts.TtsPlayer;
import com.dreamland.awesometts.BaseTTS;
import com.dreamland.awesometts.ITTSInnerListener;
import com.dreamland.awesometts.utils.FileUtil;

import java.io.File;

/**
 * Created by XMD on 2017/4/16.
 * 讯飞原生tts
 */

public class AwesomeTTS extends BaseTTS implements ITTSListener {
    private static final int ISS_TTS_PARAM_SPEAKER = 1280;
    private static final int ISS_TTS_PARAM_VOICE_SPEED = 1282;
    private static final int ISS_TTS_PARAM_VOICE_PITCH = 1283;
    private static final int ISS_TTS_PARAM_VOLUME = 1284;
    private TtsPlayer ttsPlayer;
    public AwesomeTTS(Context context, ITTSInnerListener listener) {
        super(context, listener, "AwesomeTTS");
    }

    @Override
    protected void handleTtsPlay(String content) {
        ttsPlayer.Start(content);
    }

    @Override
    protected void handleTtsStop() {
        ttsPlayer.Stop();
    }

    @Override
    protected void handleSetRole(int role) {
        ttsPlayer.SetParam(ISS_TTS_PARAM_SPEAKER,role);
    }

    @Override
    protected void handleSetPitch(int pitch) {
        ttsPlayer.SetParam(ISS_TTS_PARAM_VOICE_PITCH,pitch);
    }

    @Override
    protected void handleSetVolume(int volume) {
        ttsPlayer.SetParam(ISS_TTS_PARAM_VOLUME,volume);
    }

    @Override
    protected void handleSetSpeed(int speed) {
        ttsPlayer.SetParam(ISS_TTS_PARAM_VOICE_SPEED, speed);
    }

    @Override
    public int getRole() {
        return 0;
    }

    @Override
    public int getPitch() {
        return 0;
    }

    @Override
    public int getSpeed() {
        return 0;
    }

    @Override
    public int getVolume() {
        return 0;
    }

    @Override
    protected void onInitializing() {
        File resDir = new File("/sdcard/iflytek/res/tts");
        if(!resDir.exists()){
            if(!resDir.mkdirs()){
                sendMessage(MESSAGE_INIT_ERROR,-1);
                return;
            }
        }
        FileUtil.copyFromAssetsDirToSdcard(getContext(), "tts/iflytek/original", Environment.getExternalStorageDirectory().getAbsoluteFile()+"/iflytek/res/tts");

        ttsPlayer = new TtsPlayer();
        ttsPlayer.setListener(this);
        int ret;
        if((ret = ttsPlayer.Init(AudioManager.STREAM_NOTIFICATION,Environment.getExternalStorageDirectory().getAbsoluteFile()+"/iflytek/res/tts")) != 0){
            sendMessage(MESSAGE_INIT_ERROR,ret);
        }else {
            ttsPlayer.SetParam(ISS_TTS_PARAM_SPEAKER, 9);
            ttsPlayer.SetParam(ISS_TTS_PARAM_VOLUME, 32767);
            sendMessage(MESSAGE_READY);
        }
    }

    @Override
    protected void onDestroy() {
        ttsPlayer.Release();
        sendMessage(MESSAGE_DESTROYED);
    }

    @Override
    public void onTTSPlayInterrupted() {
        onTTSEnd();
    }

    @Override
    public void onTTSPlayBegin() {
        onTTSPlay();
    }

    @Override
    public void onTTSProgressReturn(int pram1, int pram2) {

    }

    @Override
    public void onTTSPlayCompleted() {
        onTTSEnd();
    }
}
