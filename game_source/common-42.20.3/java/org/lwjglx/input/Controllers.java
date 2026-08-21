/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;
import org.lwjglx.input.Controller;
import org.lwjglx.input.GamepadState;
import zombie.ZomboidFileSystem;
import zombie.core.Core;
import zombie.core.logger.ExceptionLogger;

public class Controllers {
    public static final int MAX_AXES = 6;
    public static final int MAX_BUTTONS = 15;
    public static final int MAX_CONTROLLERS = 16;
    private static final Controller[] controllers = new Controller[16];
    private static boolean isCreated;
    private static Consumer<Integer> controllerConnectedCallback;
    private static Consumer<Integer> controllerDisconnectedCallback;
    private static int debugToggleControllerPluggedIn;

    public static void create() {
        Controllers.readGameControllerDB();
        GLFW.glfwSetJoystickCallback(Controllers::updateControllersCount);
        for (int i = 0; i < 16; ++i) {
            if (!GLFW.glfwJoystickPresent(i)) continue;
            Controllers.controllers[i] = new Controller(i);
        }
        isCreated = true;
    }

    private static void readGameControllerDB() {
        String path;
        File file = new File("./media/gamecontrollerdb.txt").getAbsoluteFile();
        if (file.exists()) {
            Controllers.readGameControllerDB(file);
        }
        if ((file = new File(path = ZomboidFileSystem.instance.getCacheDirSub("joypads" + File.separator + "gamecontrollerdb.txt"))).exists()) {
            Controllers.readGameControllerDB(file);
        }
    }

    private static void readGameControllerDB(File file) {
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader);){
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
            }
            ByteBuffer pathBuf = MemoryUtil.memUTF8(stringBuilder.toString());
            if (GLFW.glfwUpdateGamepadMappings(pathBuf)) {
                // empty if block
            }
            MemoryUtil.memFree(pathBuf);
        }
        catch (Exception ex) {
            ExceptionLogger.logException(ex);
        }
    }

    public static void setControllerConnectedCallback(Consumer<Integer> controllerConnectedCallback) {
        Controllers.controllerConnectedCallback = controllerConnectedCallback;
    }

    public static void setControllerDisconnectedCallback(Consumer<Integer> controllerDisconnectedCallback) {
        Controllers.controllerDisconnectedCallback = controllerDisconnectedCallback;
    }

    public static int getControllerCount() {
        if (!Controllers.isCreated()) {
            throw new RuntimeException("Before calling 'getJoypadCount()' you should call 'create()' method");
        }
        return controllers.length;
    }

    public static Controller getController(int i) {
        if (!Controllers.isCreated()) {
            throw new RuntimeException("Before calling 'getJoypad(int)' you should call 'create()' method");
        }
        return controllers[i];
    }

    public static boolean isCreated() {
        return isCreated;
    }

    public static void poll(GamepadState[] gamepadStates) {
        if (!Controllers.isCreated()) {
            throw new RuntimeException("Before calling 'poll()' you should call 'create()' method");
        }
        if (Core.debug && debugToggleControllerPluggedIn >= 0 && debugToggleControllerPluggedIn < 16) {
            int index = debugToggleControllerPluggedIn;
            debugToggleControllerPluggedIn = -1;
            if (controllers[index] != null) {
                Controllers.updateControllersCount(index, 262146);
            } else if (GLFW.glfwJoystickIsGamepad(index)) {
                Controllers.updateControllersCount(index, 262145);
            }
        }
        for (int i = 0; i < controllers.length; ++i) {
            Controller controller = controllers[i];
            if (controller == null) continue;
            controller.poll(gamepadStates[i]);
        }
    }

    private static void updateControllersCount(int joystickID, int event) {
        if (event == 262145) {
            Controller pluggedController;
            Controllers.controllers[joystickID] = pluggedController = new Controller(joystickID);
            if (controllerConnectedCallback != null) {
                controllerConnectedCallback.accept(joystickID);
            }
        } else if (event == 262146) {
            Controllers.controllers[joystickID] = null;
            if (controllerDisconnectedCallback != null) {
                controllerDisconnectedCallback.accept(joystickID);
            }
        }
    }

    public static void setDebugToggleControllerPluggedIn(int index) {
        debugToggleControllerPluggedIn = index;
    }

    static {
        debugToggleControllerPluggedIn = -1;
    }
}

