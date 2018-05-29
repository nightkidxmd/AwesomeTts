package com.dreamland.awesometts.flytek;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;
import com.dreamland.awesometts.BaseTTS;
import com.dreamland.awesometts.ITTSInnerListener;
import com.dreamland.awesometts.TtsType;
import com.dreamland.awesometts.utils.FileUtil;
import com.iflytek.ITTSListener;
import com.iflytek.speech.tts.TtsPlayer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author XMD
 * @date 2017/4/16
 * 讯飞原生tts
 */

public class AwesomeTTS extends BaseTTS implements ITTSListener {
    public static final int TTS_ROLE_JIAJIA = 9;
    public static final int TTS_ROLE_CATHERINE = 20;
    public static final int TTS_ROLE_JOHN = 17;
    public static final int TTS_ROLE_NANNAN = 7;
    public static final int TTS_ROLE_XIAOFENG = 4;
    public static final int TTS_ROLE_XIAOKUN = 25;
    public static final int TTS_ROLE_XIAOLIN = 22;
    public static final int TTS_ROLE_XIAOMEI = 15;
    public static final int TTS_ROLE_XIAOMENG = 23;
    public static final int TTS_ROLE_XIAOQIAN = 11;
    public static final int TTS_ROLE_XIAOQIANG = 24;
    public static final int TTS_ROLE_XIAORONG = 14;
    public static final int TTS_ROLE_XIAOYAN = 3;
    public static final int TTS_ROLE_XIAOXUE = 99;
    private static final int ISS_TTS_PARAM_SPEAKER = 1280;
    private static final int ISS_TTS_PARAM_VOICE_SPEED = 1282;
    private static final int ISS_TTS_PARAM_VOICE_PITCH = 1283;
    private static final int ISS_TTS_PARAM_VOLUME = 1284;

    private static final String TAG = "AwesomeTTS";

    @SuppressLint("UseSparseArrays")
    private static final Map<Integer, String> ROLE_MAPS = new HashMap<>();

    static {
        ROLE_MAPS.put(TTS_ROLE_JIAJIA, "jiajia.irf");
        ROLE_MAPS.put(TTS_ROLE_CATHERINE, "catherine.irf");
        ROLE_MAPS.put(TTS_ROLE_JOHN, "john.irf");
        ROLE_MAPS.put(TTS_ROLE_NANNAN, "nannan.irf");
        ROLE_MAPS.put(TTS_ROLE_XIAOFENG, "xiaofeng.irf");
        ROLE_MAPS.put(TTS_ROLE_XIAOKUN, "xiaokun.irf");
        ROLE_MAPS.put(TTS_ROLE_XIAOLIN, "xiaolin.irf");
        ROLE_MAPS.put(TTS_ROLE_XIAOMEI, "xiaomei.irf");
        ROLE_MAPS.put(TTS_ROLE_XIAOMENG, "xiaomeng.irf");
        ROLE_MAPS.put(TTS_ROLE_XIAOQIAN, "xiaoqian.irf");
        ROLE_MAPS.put(TTS_ROLE_XIAOQIANG, "xiaoqiang.irf");
        ROLE_MAPS.put(TTS_ROLE_XIAORONG, "xiaorong.irf");
        ROLE_MAPS.put(TTS_ROLE_XIAOYAN, "xiaoyan.irf");
        ROLE_MAPS.put(TTS_ROLE_XIAOXUE, "xiaoxue.irf");
    }

    private TtsPlayer ttsPlayer;


    public AwesomeTTS(Context context, ITTSInnerListener listener, String resPath) {
        super(context, listener, "AwesomeTTS", resPath);
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
    protected void handleSetPitch(int pitch) {
        ttsPlayer.SetParam(ISS_TTS_PARAM_VOICE_PITCH, pitch);
    }

    @Override
    protected void handleSetVolume(int volume) {
        ttsPlayer.SetParam(ISS_TTS_PARAM_VOLUME, volume);
    }

    @Override
    protected void handleSetSpeed(int speed) {
        ttsPlayer.SetParam(ISS_TTS_PARAM_VOICE_SPEED, speed);
    }


    @Override
    public int getRole() {
        return role;
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
    public TtsType getTtsType() {
        return TtsType.AWESOME_TTS;
    }

    @Override
    protected void onInitializing() {
        if (TextUtils.isEmpty(resPath)) {
            resPath = "/sdcard/iflytek/res/tts";
        }
        File resDir = new File(resPath);
        if (!resDir.exists()) {
            if (!resDir.mkdirs()) {
                sendMessage(MESSAGE_INIT_ERROR, -1);
                return;
            }
        }
        prepareTtsResourceFile(getContext(), resPath);
        ttsPlayer = new TtsPlayer();
        ttsPlayer.setListener(this);
        int ret;
        if ((ret = ttsPlayer.Init(AudioManager.STREAM_NOTIFICATION, resPath)) != 0) {
            sendMessage(MESSAGE_INIT_ERROR, ret);
        } else {
            ttsPlayer.SetParam(ISS_TTS_PARAM_SPEAKER, role == -1 ? 9 : role);
            ttsPlayer.SetParam(ISS_TTS_PARAM_VOLUME, 32767);
            sendMessage(MESSAGE_READY);
        }
    }

    private void makeConfigFile(String destDir) {
        AwesomeTtsConfig awesomeTtsConfig = new AwesomeTtsConfig();
        awesomeTtsConfig.addTtsConfig(new TtsConfig("common.irf", 0));
        awesomeTtsConfig.addTtsConfig(new TtsConfig(ROLE_MAPS.get(role), 0));
        try {
            FileUtil.writeFile(destDir + File.separator + "isstts.cfg", LoganSquare.serialize(awesomeTtsConfig), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareTtsResourceFile(Context context, String destDir) {
        makeConfigFile(destDir);
        FileUtil.copyFromAssetsToSdcard(context, false, "tts/iflytek/original/common.irf", destDir + File.separator + "common.irf");
        String roleFile = ROLE_MAPS.get(role);
        FileUtil.copyFromAssetsToSdcard(context, false, "tts/iflytek/original/" + roleFile, destDir + File.separator + roleFile);
    }

    @Override
    protected void onDestroy() {
        ttsPlayer.Release();
        ttsPlayer = null;
        sendMessage(MESSAGE_DESTROYED);
    }

    @Override
    protected void onSwitchingRole() {
        ttsPlayer.Release();
        ttsPlayer = null;
        ttsPlayer = new TtsPlayer();
        prepareTtsResourceFile(getContext(), resPath);
        ttsPlayer = new TtsPlayer();
        ttsPlayer.setListener(this);
        int ret;
        if ((ret = ttsPlayer.Init(AudioManager.STREAM_NOTIFICATION, resPath)) != 0) {
            sendMessage(MESSAGE_INIT_ERROR, ret);
        } else {
            ttsPlayer.SetParam(ISS_TTS_PARAM_SPEAKER, role == -1 ? 9 : role);
            ttsPlayer.SetParam(ISS_TTS_PARAM_VOLUME, 32767);
            sendMessage(MESSAGE_READY);
        }
    }

    @Override
    public void onTTSPlayInterrupted() {
        Log.i(TAG,"onTTSPlayInterrupted");
        onTTSEnd();
    }

    @Override
    public void onTTSPlayBegin() {
        Log.i(TAG,"onTTSPlayBegin");
        onTTSPlay();
    }

    @Override
    public void onTTSProgressReturn(int pram1, int pram2) {

    }

    @Override
    public void onTTSPlayCompleted() {
        Log.i(TAG,"onTTSEnd");
        onTTSEnd();
    }
}
