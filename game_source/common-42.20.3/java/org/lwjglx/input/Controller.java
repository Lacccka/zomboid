/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.input;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjglx.input.GamepadState;

public final class Controller {
    private final String joystickName;
    private final String gamepadName;
    private final int buttonsCount;
    private final int axisCount;
    private final int hatCount;
    private final int id;
    private final boolean isGamepad;
    private final String guid;
    private final float[] deadZone;
    public GamepadState gamepadState;
    private static final String[] axisNames = new String[]{"left stick X", "left stick Y", "right stick X", "right stick Y", "left trigger", "right trigger"};
    private static final String[] buttonNames = new String[]{"A", "B", "X", "Y", "left bumper", "right bumper", "back", "start", "guide", "left stick", "right stick", "d-pad up", "d-pad right", "d-pad down", "d-pad left"};

    public Controller(int i) {
        this.id = i;
        Object joystickName = GLFW.glfwGetJoystickName(i);
        if (joystickName == null) {
            joystickName = "ControllerName" + i;
        }
        this.joystickName = joystickName;
        Object gamepadName = GLFW.glfwGetGamepadName(i);
        if (gamepadName == null) {
            gamepadName = "GamepadName" + i;
        }
        this.gamepadName = gamepadName;
        this.isGamepad = GLFW.glfwJoystickIsGamepad(i);
        if (this.isGamepad) {
            this.axisCount = 6;
            this.buttonsCount = 15;
        } else {
            FloatBuffer axis = GLFW.glfwGetJoystickAxes(i);
            this.axisCount = axis == null ? 0 : axis.remaining();
            ByteBuffer buttons = GLFW.glfwGetJoystickButtons(i);
            this.buttonsCount = buttons == null ? 0 : buttons.remaining();
        }
        ByteBuffer hats = GLFW.glfwGetJoystickHats(i);
        this.hatCount = hats == null ? 0 : hats.remaining();
        this.guid = GLFW.glfwGetJoystickGUID(i);
        this.deadZone = new float[this.axisCount];
        Arrays.fill(this.deadZone, 0.2f);
    }

    public int getID() {
        return this.id;
    }

    public String getGUID() {
        return this.guid;
    }

    public boolean isGamepad() {
        return this.isGamepad;
    }

    public String getJoystickName() {
        return this.joystickName;
    }

    public String getGamepadName() {
        return this.gamepadName;
    }

    public int getAxisCount() {
        return this.axisCount;
    }

    public float getAxisValue(int axis) {
        if (this.gamepadState == null || !this.gamepadState.polled) {
            return 0.0f;
        }
        if (axis < 0 || axis >= 15) {
            return 0.0f;
        }
        return this.gamepadState.axesButtons.axes(axis);
    }

    public int getButtonCount() {
        return this.buttonsCount;
    }

    public int getHatCount() {
        return this.hatCount;
    }

    public int getHatState() {
        if (this.gamepadState == null || !this.gamepadState.polled) {
            return 0;
        }
        return this.gamepadState.hatState;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ByteBuffer getJoystickHats(int jid, ByteBuffer hats) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        IntBuffer count = stack.callocInt(1);
        try {
            long __result = GLFW.nglfwGetJoystickHats(jid, MemoryUtil.memAddress(count));
            hats.clear();
            hats.limit(count.get(0));
            if (__result != 0L) {
                MemoryUtil.memCopy(__result, MemoryUtil.memAddress(hats), count.get(0));
            }
            ByteBuffer byteBuffer = hats;
            return byteBuffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public String getAxisName(int axis) {
        return axisNames[axis];
    }

    public float getXAxisValue() {
        return this.getAxisValue(0);
    }

    public float getYAxisValue() {
        return this.getAxisValue(1);
    }

    public float getDeadZone(int axis) {
        return this.deadZone[axis];
    }

    public void setDeadZone(int axis, float val) {
        this.deadZone[axis] = val;
    }

    public float getPovX() {
        if (this.gamepadState == null || !this.gamepadState.polled) {
            return 0.0f;
        }
        if ((this.gamepadState.hatState & 8) != 0) {
            return -1.0f;
        }
        if ((this.gamepadState.hatState & 2) != 0) {
            return 1.0f;
        }
        return 0.0f;
    }

    public float getPovY() {
        if (this.gamepadState == null || !this.gamepadState.polled) {
            return 0.0f;
        }
        if ((this.gamepadState.hatState & 1) != 0) {
            return -1.0f;
        }
        if ((this.gamepadState.hatState & 4) != 0) {
            return 1.0f;
        }
        return 0.0f;
    }

    public boolean isButtonPressed(int index) {
        if (this.gamepadState == null || !this.gamepadState.polled) {
            return false;
        }
        if (index < 0 || index >= 15) {
            return false;
        }
        return this.gamepadState.axesButtons.buttons(index) == 1;
    }

    public boolean isButtonRelease(int index) {
        if (this.gamepadState == null || !this.gamepadState.polled) {
            return false;
        }
        if (index < 0 || index >= 15) {
            return false;
        }
        return this.gamepadState.axesButtons.buttons(index) == 0;
    }

    public String getButtonName(int n) {
        if (n >= buttonNames.length) {
            return "Extra button " + (n - buttonNames.length + 1);
        }
        return buttonNames[n];
    }

    public void poll(GamepadState gamepadState) {
        if (GLFW.glfwGetGamepadState(this.id, gamepadState.axesButtons)) {
            gamepadState.polled = true;
            ByteBuffer hats = this.getJoystickHats(this.id, gamepadState.hats);
            gamepadState.hatState = hats.remaining() == 0 ? 0 : (int)hats.get(0);
        } else {
            gamepadState.polled = false;
        }
    }
}

