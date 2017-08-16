package com.dreamland.awesometts;

import android.content.Context;

import com.dreamland.awesometts.flytek.AwesomeTTS;
import com.dreamland.awesometts.flytek.MagicTTS;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by XMD on 2017/4/15.
 */

public class TtsCore implements ITTS {
    private Map<TtsType,ITTS> ittsMap = new ConcurrentHashMap<>();
    private ITTS itts;
    private ITTSInnerListener ittsInnerListener;

    private WeakReference<Context> context;


    public TtsCore(Context context, TtsType ttsType, ITTSInnerListener ittsInnerListener){
        this.context = new WeakReference<>(context);
        this.ittsInnerListener = ittsInnerListener;
        switchTts(ttsType);
    }


    public void switchTts(TtsType ttsType){
        itts = ittsMap.get(ttsType);
        if(itts == null){
            if(TtsType.MAGIC_TTS.equals(ttsType)){
                itts = new MagicTTS(context.get(),ittsInnerListener);
//            }else if(TtsType.UNISOUND_TTS.equals(ttsType)){
//                itts = new UnisoundTTS(context.get(),ittsInnerListener);
            }else if(TtsType.AWESOME_TTS.equals(ttsType)){
                itts = new AwesomeTTS(context.get(),ittsInnerListener);
            }
        }
        init();
    }


    @Override
    public void init() {
        itts.init();
    }

    @Override
    public void play(String content) {
        itts.play(content);
    }

    @Override
    public void stop() {
        itts.stop();
    }

    @Override
    public void destroy() {
        Set<Map.Entry<TtsType,ITTS>> entries = ittsMap.entrySet();
        for(Map.Entry<TtsType,ITTS> entry:entries){
            entry.getValue().destroy();
        }
        ittsMap.clear();
    }

    @Override
    public void setRole(int role) {
        itts.setRole(role);
    }

    @Override
    public int getRole() {
        return itts.getRole();
    }

    @Override
    public void setPitch(int pitch) {
        itts.setPitch(pitch);
    }

    @Override
    public int getPitch() {
        return itts.getPitch();
    }

    @Override
    public void setSpeed(int speed) {
        itts.setPitch(speed);
    }

    @Override
    public int getSpeed() {
        return itts.getSpeed();
    }

    @Override
    public void setVolume(int volume) {
        itts.setVolume(volume);
    }

    @Override
    public int getVolume() {
        return itts.getVolume();
    }
}
