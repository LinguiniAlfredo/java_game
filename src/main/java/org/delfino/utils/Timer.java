package org.delfino.utils;

import static org.lwjgl.glfw.GLFW.*;

public class Timer {
    double  startTicks;
    double  pausedTicks;
    boolean paused;
    boolean started;

    public void start() {
        this.started = true;
        this.paused  = false;

        this.startTicks = glfwGetTime();
        this.pausedTicks = 0;
    }

    public void stop() {
        this.started = false;
        this.paused  = false;

        this.startTicks = 0;
        this.pausedTicks = 0;
    }

    public double get_ticks() {
        double time = 0;
        if (this.started) {
            if (this.paused) {
                time = this.pausedTicks;
            } else {
                time = glfwGetTime() - this.startTicks;
            }
        }
        return time;
    }
}
