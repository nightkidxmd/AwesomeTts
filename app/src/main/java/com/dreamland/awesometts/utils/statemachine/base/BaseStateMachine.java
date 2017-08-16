package com.dreamland.awesometts.utils.statemachine.base;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.dreamland.awesometts.utils.statemachine.StateMachine;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by XMD on 2017/7/25.
 */

abstract public class BaseStateMachine extends StateMachine {
    private StateListener stateListener;
    @SuppressLint("UseSparseArrays")
    private Map<Integer,BaseState> transitionMap = new HashMap<>();

    protected BaseStateMachine(@NonNull String name, @NonNull StateListener stateListener) {
        super(name);
        initAndStart(stateListener);
    }

    protected BaseStateMachine(@NonNull String name, @NonNull Looper looper, @NonNull StateListener stateListener) {
        super(name, looper);
        initAndStart(stateListener);
    }

    protected BaseStateMachine(@NonNull String name, @NonNull Handler handler, @NonNull StateListener stateListener) {
        super(name, handler);
        initAndStart(stateListener);
    }




    abstract protected void onInitTransitionMap();

    /**
     * 初始化状态树
     */
    abstract protected void onInitStateTree();


    abstract protected @NonNull
    BaseState getInitialState();

    protected  @NonNull
    IllegalArgumentException getDuplicatedTransitionExeception(int what, @NonNull BaseState state){
        return new IllegalArgumentException("<"+what+","+state+"> already added");
    }

    protected void addTransition(int what,BaseState state){
        if(transitionMap.containsKey(what)){
            throw getDuplicatedTransitionExeception(what,state);
        }
        transitionMap.put(what,state);
    }




    private void initAndStart(@NonNull StateListener stateListener){
        this.stateListener = stateListener;
        onInitStateTree();
        onInitTransitionMap();
        setInitialState(getInitialState());
        start();
    }

    public void transitionTo(@NonNull Message message){
        BaseState destState = transitionMap.get(message.what);
        if(destState == null){
            throw new IllegalArgumentException("message:"+message.what+" wasn't mapped into transition map!!");
        }
        destState.setMessage(message);
        transitionTo(destState);
    }

    public interface StateListener{
        void onStateEnter(@NonNull BaseState baseState);
        void onStateExit(@NonNull BaseState baseState);
    }



    void onStateEnter(BaseState baseState){
        stateListener.onStateEnter(baseState);
    }

    void onStateExit(BaseState baseState){
        stateListener.onStateExit(baseState);
    }
}
