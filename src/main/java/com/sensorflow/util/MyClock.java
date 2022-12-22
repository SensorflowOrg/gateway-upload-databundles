package com.sensorflow.util;

public interface MyClock {
    void register(Listener listener);
    void start();
    void stop();

    @FunctionalInterface
    interface Listener {
        void timeElapsed();
    }
}
