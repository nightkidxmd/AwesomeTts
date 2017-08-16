package com.dreamland.awesometts;

/**
 * Created by XMD on 2017/4/15.
 */

public interface ITTS {
    void init();
    void play(String content);
    void stop();
    void destroy();
    void setRole(int role);
    int getRole();
    void setPitch(int pitch);
    int getPitch();
    void setSpeed(int speed);
    int getSpeed();
    void setVolume(int volume);
    int getVolume();
}
