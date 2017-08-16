package com.dreamland.awesometts.utils.statemachine.base;

import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.dreamland.awesometts.utils.statemachine.State;


/**
 * Created by XMD on 2017/7/26.
 */

public class BaseState extends State {
    @NonNull
    protected BaseStateMachine baseStateMachine;
    @NonNull
    private Message message;
    public BaseState(@NonNull BaseStateMachine baseStateMachine){
        this.baseStateMachine = baseStateMachine;
    }

    public void setMessage(@NonNull Message message) {
        this.message = message;
    }
    @NonNull
    public Message getMessage() {
        return message;
    }

    @CallSuper
    @Override
    public void enter() {
        super.enter();
        baseStateMachine.onStateEnter(this);
    }

    @CallSuper
    @Override
    public void exit() {
        super.exit();
        baseStateMachine.onStateExit(this);
    }
}
