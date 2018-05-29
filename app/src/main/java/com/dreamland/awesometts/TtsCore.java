package com.dreamland.awesometts;

import android.content.Context;

import com.dreamland.awesometts.flytek.AwesomeTTS;
import com.dreamland.awesometts.flytek.MagicTTS;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author XMD
 * @date 2017/4/15
 */

public class TtsCore implements ITTS {
    private final ReentrantLock lock = new ReentrantLock(true);
    private ITTS itts;
    private ITTSInnerListener ittsInnerListener;

    private WeakReference<Context> context;
    private String resPath;

    public TtsCore(Context context, ITTSInnerListener ittsInnerListener, String resPath, TtsRoleConfig ttsRoleConfig) {
        this.context = new WeakReference<>(context);
        this.ittsInnerListener = ittsInnerListener;
        this.resPath = resPath;
        switchTts(ttsRoleConfig);
    }


    public void switchTts(TtsRoleConfig ttsRoleConfig) {
        lock.lock();
        try{
            if (itts == null) {
                initTtsLk(ttsRoleConfig);
            } else if (itts.getTtsType() != ttsRoleConfig.ttsType) {
                itts.destroy();
                itts = null;
                initTtsLk(ttsRoleConfig);
            } else {
                itts.switchRole(ttsRoleConfig.ttsRole);
            }
        }finally {
            lock.unlock();
        }

    }

    private void initTtsLk(TtsRoleConfig ttsRoleConfig){
        if (TtsType.MAGIC_TTS.equals(ttsRoleConfig.ttsType)) {
            itts = new MagicTTS(context.get(), ittsInnerListener);
        } else if (TtsType.AWESOME_TTS.equals(ttsRoleConfig.ttsType)) {
            itts = new AwesomeTTS(context.get(), ittsInnerListener, resPath);
        }
        itts.init(ttsRoleConfig.ttsRole);
    }

    @Override
    public void init() {
        lock.lock();
        try{
            itts.init();
        }finally {
            lock.unlock();
        }

    }

    @Override
    public void init(int role) {
        lock.lock();
        try {
            itts.init(role);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void switchRole(int role) {
        lock.lock();
        try {
            itts.switchRole(role);
        }finally {
            lock.unlock();
        }

    }

    @Override
    public void play(String content) {
        lock.lock();
        try {
            itts.play(content);
        }finally {
            lock.unlock();
        }

    }

    @Override
    public void stop() {
        lock.lock();
        try {
            itts.stop();
        }finally {
            lock.unlock();
        }

    }

    @Override
    public void destroy() {
        lock.lock();
        try {
            itts.destroy();
            itts = null;
        }finally {
            lock.unlock();
        }

    }

    @Override
    public int getRole() {
        return itts.getRole();
    }

    @Override
    public int getPitch() {
        return itts.getPitch();
    }

    @Override
    public void setPitch(int pitch) {
        itts.setPitch(pitch);
    }

    @Override
    public int getSpeed() {
        return itts.getSpeed();
    }

    @Override
    public void setSpeed(int speed) {
        itts.setPitch(speed);
    }

    @Override
    public int getVolume() {
        return itts.getVolume();
    }

    @Override
    public void setVolume(int volume) {
        itts.setVolume(volume);
    }

    @Override
    public TtsType getTtsType() {
        lock.lock();
        try {
            return itts == null? TtsType.NONE_TTS : itts.getTtsType();
        }finally {
            lock.unlock();
        }

    }
}
