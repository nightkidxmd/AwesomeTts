package com.dreamland.awesometts;

public interface ITTSListener<T extends AbsTtsInfo> {
    void onTTSEnd(T info);
    void onTTSPlay(T info);
}
