package org.delfino.utils;

import static org.lwjgl.glfw.GLFW.*;

public class Timer {
    double start_ticks;
    double paused_ticks;
    boolean paused;
    boolean started;

    public void start() {
        this.started = true;
        this.paused  = false;

        this.start_ticks  = glfwGetTime();
        this.paused_ticks = 0;
    }

    public void stop() {
        this.started = false;
        this.paused  = false;

        this.start_ticks  = 0;
        this.paused_ticks = 0;
    }

    public double get_ticks() {
        double time = 0;
        if (this.started) {
            if (this.paused) {
                time = this.paused_ticks;
            } else {
                time = glfwGetTime() - this.start_ticks;
            }
        }
        return time;
    }
}
