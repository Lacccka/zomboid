/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.input;

import java.nio.ByteBuffer;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.system.MemoryUtil;

public final class GamepadState {
    public boolean polled;
    public final GLFWGamepadState axesButtons = GLFWGamepadState.malloc();
    public final ByteBuffer hats = MemoryUtil.memAlloc(8);
    public int hatState = 0;

    public void set(GamepadState rhs) {
        this.polled = rhs.polled;
        this.axesButtons.set(rhs.axesButtons);
        this.hats.clear();
        rhs.hats.position(0);
        this.hats.put(rhs.hats);
        this.hatState = rhs.hatState;
    }

    public void quit() {
        this.axesButtons.free();
        MemoryUtil.memFree(this.hats);
    }
}

