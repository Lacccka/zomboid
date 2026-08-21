/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx;

import org.lwjgl.glfw.GLFW;

public class Sys {
    public static long getTimerResolution() {
        return 1000L;
    }

    public static long getTime() {
        return (long)(GLFW.glfwGetTime() * 1000.0);
    }

    public static long getNanoTime() {
        return (long)(GLFW.glfwGetTime() * 1.0E9);
    }
}

