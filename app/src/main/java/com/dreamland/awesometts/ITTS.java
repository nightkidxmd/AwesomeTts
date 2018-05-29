package com.dreamland.awesometts;

/**
 *
 * @author XMD
 * @date 2017/4/15
 */

public interface ITTS {
    void init();
    void init(int role);
    void switchRole(int role);
    void play(String content);
    void stop();
    void destroy();
    int getRole();
    void setPitch(int pitch);
    int getPitch();
    void setSpeed(int speed);
    int getSpeed();
    void setVolume(int volume);
    int getVolume();
    TtsType getTtsType();
}
