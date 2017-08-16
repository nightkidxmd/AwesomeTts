package com.dreamland.awesometts;

import android.os.Message;

import com.dreamland.awesometts.utils.statemachine.State;
import com.dreamland.awesometts.utils.statemachine.StateMachine;


/**
 * Created by XMD on 2017/4/15.
 */

abstract public class TtsStateMachine extends StateMachine {
    private Default aDefault;
    private EngineInitializing engineInitializing;
    private EngineInitializingError engineInitializingError;
    private EngineReady engineReady;
    private EngineDestroying engineDestroying;
    private EngineDestroyed engineDestroyed;


    public static final int MESSAGE_INIT        =      0;
    public static final int MESSAGE_INIT_ERROR  =      1;
    public static final int MESSAGE_READY       =      2;
    public static final int MESSAGE_DESTROY     =      3;
    public static final int MESSAGE_DESTROYED   =      4;


    public static final int MESSAGE_TTS_PLAY            =    100;
    public static final int MESSAGE_TTS_STOP            =    101;
    public static final int MESSAGE_TTS_SET_VOLUME      =    102;
    public static final int MESSAGE_TTS_SET_PITCH       =    103;
    public static final int MESSAGE_TTS_SET_SPEED       =    104;
    public static final int MESSAGE_TTS_SET_ROLE        =    105;


    protected TtsStateMachine(String name) {
        super(name);
        init();
    }

    private void init(){
        addState(aDefault = new Default());
        addState(engineInitializing = new EngineInitializing(),aDefault);
        addState(engineInitializingError = new EngineInitializingError(),aDefault);
        addState(engineReady = new EngineReady(),engineInitializing);
        addState(engineDestroying = new EngineDestroying(),engineReady);
        addState(engineDestroyed = new EngineDestroyed(),engineDestroying);
        setInitialState(aDefault);
        start();
    }


    private class Default extends State {
        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_INIT:
                    transitionTo(engineInitializing);
                    return HANDLED;
                case MESSAGE_INIT_ERROR:
                    transitionTo(engineInitializingError);
                    return HANDLED;
                case MESSAGE_TTS_PLAY:
                    deferMessage(msg);
                    return HANDLED;
            }
            return super.processMessage(msg);
        }
}

    public class EngineInitializing extends State{
        @Override
        public void enter() {
            super.enter();
            onInitializing();
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_READY:
                    transitionTo(engineReady);
                case MESSAGE_INIT:
                    return HANDLED;
                default:
                    deferMessage(msg);
                    return HANDLED;
            }
        }
    }

    public class EngineInitializingError extends State{
        @Override
        public void enter() {
            super.enter();
            transitionTo(aDefault);
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_INIT_ERROR:
                    return HANDLED;
            }
            return super.processMessage(msg);
        }
    }

    public class EngineReady extends State{
        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_TTS_PLAY:
                case MESSAGE_TTS_STOP:
                case MESSAGE_TTS_SET_PITCH:
                case MESSAGE_TTS_SET_ROLE:
                case MESSAGE_TTS_SET_SPEED:
                case MESSAGE_TTS_SET_VOLUME:
                    onHandleTts(msg);
                case MESSAGE_READY:
                    return HANDLED;
                case MESSAGE_DESTROY:
                    transitionTo(engineDestroying);
                    return HANDLED;
            }
            return super.processMessage(msg);
        }
    }

    public class EngineDestroying extends State{
        @Override
        public void enter() {
            super.enter();
            onDestroy();
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_DESTROYED:
                    transitionTo(engineDestroyed);
                case MESSAGE_DESTROY:
                    return HANDLED;
            }
            return super.processMessage(msg);
        }
    }

    public class EngineDestroyed extends State{
        @Override
        public void enter() {
            super.enter();
            quit();
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_DESTROYED:
                    return HANDLED;
            }
            return super.processMessage(msg);
        }
    }


    abstract protected void onInitializing();
    abstract protected void onHandleTts(Message msg);
    abstract protected void onDestroy();
}
