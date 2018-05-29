package com.dreamland.awesometts;

import android.content.Context;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by XMD on 2017/4/15.
 */

public abstract class BaseTTS extends TtsStateMachine implements ITTS,ITTSInnerListener {
    private ITTSInnerListener listener;
    private WeakReference<Context> context;
    protected String resPath;
    protected int role = -1;
    protected BaseTTS(Context context,ITTSInnerListener listener,String name,String resPath) {
        super(name);
        this.context = new WeakReference<>(context);
        this.listener = listener;
        this.resPath = resPath;
    }

    @Override
    public void onTTSEnd() {
        listener.onTTSEnd();
    }

    @Override
    public void onTTSPlay() {
        listener.onTTSPlay();
    }

    protected Context getContext() {
        return context.get();
    }

    @Override
    final public void init() {
        sendMessage(MESSAGE_INIT);
    }

    @Override
    final public void init(int role) {
        this.role = role;
        sendMessage(MESSAGE_INIT);
    }

    @Override
    final public void play(String content) {
        sendMessage(MESSAGE_TTS_PLAY,content);
    }

    @Override
    final public void stop() {
        sendMessage(MESSAGE_TTS_STOP);
    }

    @Override
    final public void destroy() {
        sendMessage(MESSAGE_DESTROY);
    }

    @Override
    final public void setPitch(int pitch) {
        sendMessage(MESSAGE_TTS_SET_PITCH,pitch);
    }


    @Override
    final public void setSpeed(int speed) {
        sendMessage(MESSAGE_TTS_SET_SPEED,speed);
    }


    @Override
    final public void setVolume(int volume) {
        sendMessage(MESSAGE_TTS_SET_VOLUME,volume);
    }

    @Override
    final public void switchRole(int role) {
        this.role = role;
        sendMessage(MESSAGE_SWITCHING_ROLE);
    }

    @Override
    final protected void onHandleTts(Message msg) {
        switch (msg.what){
            case MESSAGE_TTS_PLAY:
                handleTtsPlay((String) msg.obj);
                break;
            case MESSAGE_TTS_STOP:
                handleTtsStop();
                break;
            case MESSAGE_TTS_SET_PITCH:
                handleSetPitch(msg.arg1);
                break;
            case MESSAGE_TTS_SET_SPEED:
                handleSetSpeed(msg.arg1);
                break;
            case MESSAGE_TTS_SET_VOLUME:
                handleSetVolume(msg.arg1);
                break;
        }
    }

    abstract protected void handleTtsPlay(String content);
    abstract protected void handleTtsStop();
    abstract protected void handleSetPitch(int pitch);
    abstract protected void handleSetVolume(int volume);
    abstract protected void handleSetSpeed(int speed);
}
