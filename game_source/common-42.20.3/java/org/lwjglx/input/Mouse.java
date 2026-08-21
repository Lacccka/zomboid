/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.input;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.LWJGLException;
import org.lwjglx.LWJGLUtil;
import org.lwjglx.Sys;
import org.lwjglx.input.Cursor;
import org.lwjglx.input.EventQueue;
import org.lwjglx.opengl.Display;
import zombie.core.Core;

public class Mouse {
    private static boolean grabbed;
    private static int lastX;
    private static int lastY;
    private static int latestX;
    private static int latestY;
    private static int x;
    private static int y;
    private static final EventQueue queue;
    private static final int[] buttonEvents;
    private static final boolean[] buttonEventStates;
    private static final int[] xEvents;
    private static final int[] yEvents;
    private static final int[] lastxEvents;
    private static final int[] lastyEvents;
    private static final long[] nanoTimeEvents;
    private static boolean clipPostionToDisplay;
    static double scrollxpos;
    static double scrollypos;

    public static void addMoveEvent(double mouseX, double mouseY) {
        latestX = (int)mouseX;
        latestY = Display.getHeight() - (int)mouseY;
        Mouse.lastxEvents[Mouse.queue.getNextPos()] = xEvents[queue.getNextPos()];
        Mouse.lastyEvents[Mouse.queue.getNextPos()] = yEvents[queue.getNextPos()];
        Mouse.xEvents[Mouse.queue.getNextPos()] = latestX;
        Mouse.yEvents[Mouse.queue.getNextPos()] = latestY;
        Mouse.buttonEvents[Mouse.queue.getNextPos()] = -1;
        Mouse.buttonEventStates[Mouse.queue.getNextPos()] = false;
        Mouse.nanoTimeEvents[Mouse.queue.getNextPos()] = Sys.getNanoTime();
        queue.add();
    }

    public static void addButtonEvent(int button, boolean pressed) {
        Mouse.lastxEvents[Mouse.queue.getNextPos()] = xEvents[queue.getNextPos()];
        Mouse.lastyEvents[Mouse.queue.getNextPos()] = yEvents[queue.getNextPos()];
        Mouse.xEvents[Mouse.queue.getNextPos()] = latestX;
        Mouse.yEvents[Mouse.queue.getNextPos()] = latestY;
        Mouse.buttonEvents[Mouse.queue.getNextPos()] = button;
        Mouse.buttonEventStates[Mouse.queue.getNextPos()] = pressed;
        Mouse.nanoTimeEvents[Mouse.queue.getNextPos()] = Sys.getNanoTime();
        queue.add();
    }

    public static void poll() {
        if (!grabbed) {
            // empty if block
        }
        lastX = x;
        lastY = y;
        if (!grabbed && clipPostionToDisplay) {
            if (latestX < 0) {
                latestX = 0;
            }
            if (latestY < 0) {
                latestY = 0;
            }
            if (latestX > Display.getWidth() - 1) {
                latestX = Display.getWidth() - 1;
            }
            if (latestY > Display.getHeight() - 1) {
                latestY = Display.getHeight() - 1;
            }
        }
        x = latestX;
        y = latestY;
    }

    public static void create() throws LWJGLException {
    }

    public static boolean isCreated() {
        return Display.isCreated();
    }

    public static void setGrabbed(boolean grab) {
        GLFW.glfwSetInputMode(Display.getWindow(), 208897, grab ? 212995 : 212993);
        grabbed = grab;
    }

    public static boolean isGrabbed() {
        return grabbed;
    }

    public static boolean isButtonDown(int button) {
        return GLFW.glfwGetMouseButton(Display.getWindow(), button) == 1;
    }

    public static boolean next() {
        return queue.next();
    }

    public static int getEventX() {
        return xEvents[queue.getCurrentPos()];
    }

    public static int getEventY() {
        return yEvents[queue.getCurrentPos()];
    }

    public static int getEventDX() {
        return xEvents[queue.getCurrentPos()] - lastxEvents[queue.getCurrentPos()];
    }

    public static int getEventDY() {
        return yEvents[queue.getCurrentPos()] - lastyEvents[queue.getCurrentPos()];
    }

    public static long getEventNanoseconds() {
        return nanoTimeEvents[queue.getCurrentPos()];
    }

    public static int getEventButton() {
        return buttonEvents[queue.getCurrentPos()];
    }

    public static boolean getEventButtonState() {
        return buttonEventStates[queue.getCurrentPos()];
    }

    public static int getEventDWheel() {
        return 0;
    }

    public static int getX() {
        return x;
    }

    public static int getY() {
        return y;
    }

    public static int getDX() {
        return x - lastX;
    }

    public static int getDY() {
        return y - lastY;
    }

    public static int getDWheel() {
        int wheel = (int)scrollypos;
        scrollypos = 0.0;
        return wheel;
    }

    public static int getButtonCount() {
        return 8;
    }

    public static void setClipMouseCoordinatesToWindow(boolean clip) {
        clipPostionToDisplay = clip;
    }

    public static void setCursorPosition(int new_x, int new_y) {
        GLFW.glfwSetCursorPos(Display.getWindow(), new_x, new_y);
    }

    public static Cursor setNativeCursor(Cursor cursor) throws LWJGLException {
        GLFW.glfwSetCursor(Display.getWindow(), cursor.getHandle());
        return null;
    }

    public static void destroy() {
    }

    public static void updateCursor() {
    }

    public static void setDWheel(double xpos, double ypos) {
        if (LWJGLUtil.getPlatform() == 2) {
            if (Core.getInstance().getOptionMacOSIgnoreMouseWheelAcceleration()) {
                if (ypos != 0.0) {
                    double d = ypos = ypos > 0.0 ? 1.0 : -1.0;
                }
                if (xpos != 0.0) {
                    double d = xpos = xpos > 0.0 ? 1.0 : -1.0;
                }
            }
            if (Core.getInstance().getOptionMacOSMapHorizontalMouseWheelToVertical()) {
                if (ypos == 0.0) {
                    ypos = xpos;
                }
                xpos = 0.0;
            }
        }
        scrollypos += ypos;
        scrollxpos += xpos;
    }

    static {
        queue = new EventQueue(32);
        buttonEvents = new int[queue.getMaxEvents()];
        buttonEventStates = new boolean[queue.getMaxEvents()];
        xEvents = new int[queue.getMaxEvents()];
        yEvents = new int[queue.getMaxEvents()];
        lastxEvents = new int[queue.getMaxEvents()];
        lastyEvents = new int[queue.getMaxEvents()];
        nanoTimeEvents = new long[queue.getMaxEvents()];
        clipPostionToDisplay = true;
    }
}

