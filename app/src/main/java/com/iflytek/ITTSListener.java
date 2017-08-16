package com.iflytek;

/**
 * Created by XMD on 2017/4/16.
 */

public interface ITTSListener {
    void onTTSPlayInterrupted();
    void onTTSPlayBegin();
    void onTTSProgressReturn(int pram1, int pram2);
    void onTTSPlayCompleted();
}
