/*
 * Decompiled with CFR 0.152.
 */
package zombie.core.input;

import java.util.ArrayList;
import org.lwjglx.input.Controller;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import zombie.Lua.LuaEventManager;
import zombie.Lua.LuaManager;
import zombie.core.Core;
import zombie.core.Translator;
import zombie.input.ControllerStateCache;
import zombie.input.GameKeyboard;
import zombie.input.JoypadManager;

public final class Input {
    public static final int ANY_CONTROLLER = -1;
    private final Controller[] controllers = new Controller[16];
    private final ArrayList<Controller> newlyConnected = new ArrayList();
    private final ArrayList<Controller> newlyDisconnected = new ArrayList();
    private final boolean[][] controllerPressed = new boolean[16][15];
    private final boolean[][] controllerWasPressed = new boolean[16][15];
    private final float[][] controllerPov = new float[16][2];
    private final ControllerStateCache controllerStateCache = new ControllerStateCache();

    public static String getKeyName(int code) {
        if (code >= 10000) {
            return Translator.getText("IGUI_mouse_btn_" + (code - 10000), new Object[0]);
        }
        String keyName = Keyboard.getKeyName(code);
        String translatedKeyName = Translator.getTextOrNull("IGUI_Key_" + keyName, new Object[0]);
        if (translatedKeyName != null) {
            return translatedKeyName;
        }
        return keyName;
    }

    public static int getKeyCode(String keyName) {
        if (System.getProperty("os.name").contains("OS X")) {
            if ("Left ALT".equals(keyName)) {
                return 219;
            }
            if ("Right ALT".equals(keyName)) {
                return 220;
            }
        }
        if ("Right SHIFT".equals(keyName)) {
            return 54;
        }
        if ("Left SHIFT".equals(keyName)) {
            return 42;
        }
        if ("Left ALT".equals(keyName)) {
            return 56;
        }
        if ("Right ALT".equals(keyName)) {
            return 184;
        }
        return Keyboard.getKeyIndex(keyName);
    }

    public int getControllerCount() {
        return this.controllers.length;
    }

    public int getAxisCount(int index) {
        Controller controller = this.getController(index);
        if (controller == null) {
            return 0;
        }
        return controller.getAxisCount();
    }

    public float getAxisValue(int index, int axis) {
        Controller controller = this.getController(index);
        if (controller == null) {
            return 0.0f;
        }
        return controller.getAxisValue(axis);
    }

    public String getAxisName(int index, int axis) {
        Controller controller = this.getController(index);
        if (controller == null) {
            return null;
        }
        return controller.getAxisName(axis);
    }

    public boolean isControllerLeftD(int index) {
        if (index == -1) {
            for (int i = 0; i < this.controllers.length; ++i) {
                if (!this.isControllerLeftD(i)) continue;
                return true;
            }
            return false;
        }
        Controller controller = this.getController(index);
        if (controller == null) {
            return false;
        }
        return controller.getPovX() < -0.5f;
    }

    public boolean isControllerRightD(int index) {
        if (index == -1) {
            for (int i = 0; i < this.controllers.length; ++i) {
                if (!this.isControllerRightD(i)) continue;
                return true;
            }
            return false;
        }
        Controller controller = this.getController(index);
        if (controller == null) {
            return false;
        }
        return controller.getPovX() > 0.5f;
    }

    public boolean isControllerUpD(int index) {
        if (index == -1) {
            for (int i = 0; i < this.controllers.length; ++i) {
                if (!this.isControllerUpD(i)) continue;
                return true;
            }
            return false;
        }
        Controller controller = this.getController(index);
        if (controller == null) {
            return false;
        }
        return controller.getPovY() < -0.5f;
    }

    public boolean isControllerDownD(int index) {
        if (index == -1) {
            for (int i = 0; i < this.controllers.length; ++i) {
                if (!this.isControllerDownD(i)) continue;
                return true;
            }
            return false;
        }
        Controller controller = this.getController(index);
        if (controller == null) {
            return false;
        }
        return controller.getPovY() > 0.5f;
    }

    private Controller checkControllerButton(int index, int button) {
        Controller controller = this.getController(index);
        if (controller == null) {
            return null;
        }
        if (button < 0 || button >= controller.getButtonCount()) {
            return null;
        }
        return controller;
    }

    public boolean isButtonPressedD(int button, int index) {
        if (index == -1) {
            for (int i = 0; i < this.controllers.length; ++i) {
                if (!this.isButtonPressedD(button, i)) continue;
                return true;
            }
            return false;
        }
        Controller controller = this.checkControllerButton(index, button);
        if (controller == null) {
            return false;
        }
        return this.controllerPressed[index][button];
    }

    public boolean wasButtonPressed(int index, int button) {
        Controller controller = this.checkControllerButton(index, button);
        if (controller == null) {
            return false;
        }
        return this.controllerWasPressed[index][button];
    }

    public boolean isButtonStartPress(int index, int button) {
        return !this.wasButtonPressed(index, button) && this.isButtonPressedD(button, index);
    }

    public boolean isButtonReleasePress(int index, int button) {
        return this.wasButtonPressed(index, button) && !this.isButtonPressedD(button, index);
    }

    public void initControllers() {
        this.updateGameThread();
        this.updateGameThread();
    }

    private void onControllerConnected(Controller controller) {
        JoypadManager.instance.onControllerConnected(controller);
        if (LuaManager.env == null) {
            return;
        }
        LuaEventManager.triggerEvent("OnGamepadConnect", controller.getID());
    }

    private void onControllerDisconnected(Controller controller) {
        JoypadManager.instance.onControllerDisconnected(controller);
        if (LuaManager.env == null) {
            return;
        }
        LuaEventManager.triggerEvent("OnGamepadDisconnect", controller.getID());
    }

    public void poll() {
        if (!Core.getInstance().isDoingTextEntry()) {
            while (GameKeyboard.getEventQueuePolling().next()) {
            }
        }
        while (Mouse.next()) {
        }
        this.controllerStateCache.poll();
    }

    public Controller getController(int index) {
        if (index < 0 || index >= this.controllers.length) {
            return null;
        }
        return this.controllers[index];
    }

    public int getButtonCount(int index) {
        Controller controller = this.getController(index);
        return controller == null ? null : Integer.valueOf(controller.getButtonCount());
    }

    public String getButtonName(int index, int button) {
        Controller controller = this.getController(index);
        return controller == null ? null : controller.getButtonName(button);
    }

    public void updateGameThread() {
        Controller controller;
        int i;
        if (!this.controllerStateCache.getState().isCreated()) {
            this.controllerStateCache.swap();
            return;
        }
        if (this.checkConnectDisconnect(this.controllerStateCache)) {
            for (i = 0; i < this.newlyDisconnected.size(); ++i) {
                controller = this.newlyDisconnected.get(i);
                this.onControllerDisconnected(controller);
            }
            for (i = 0; i < this.newlyConnected.size(); ++i) {
                controller = this.newlyConnected.get(i);
                this.onControllerConnected(controller);
            }
        }
        for (i = 0; i < this.getControllerCount(); ++i) {
            controller = this.getController(i);
            if (controller == null) continue;
            int buttonCount = controller.getButtonCount();
            for (int buttonIdx = 0; buttonIdx < buttonCount; ++buttonIdx) {
                this.controllerWasPressed[i][buttonIdx] = this.controllerPressed[i][buttonIdx];
                if (this.controllerPressed[i][buttonIdx] && !controller.isButtonPressed(buttonIdx)) {
                    this.controllerPressed[i][buttonIdx] = false;
                    continue;
                }
                if (this.controllerPressed[i][buttonIdx] || !controller.isButtonPressed(buttonIdx)) continue;
                this.controllerPressed[i][buttonIdx] = true;
                JoypadManager.instance.onPressed(i, buttonIdx);
            }
            int axisCount = controller.getAxisCount();
            for (int axisIdx = 0; axisIdx < axisCount; ++axisIdx) {
                float axisValue = controller.getAxisValue(axisIdx);
                if (controller.isGamepad() && axisIdx == 4 || axisIdx == 5) {
                    if (!(axisValue > 0.0f)) continue;
                    JoypadManager.instance.onPressedTrigger(i, axisIdx);
                    continue;
                }
                if (axisValue < -0.5f) {
                    JoypadManager.instance.onPressedAxisNeg(i, axisIdx);
                }
                if (!(axisValue > 0.5f)) continue;
                JoypadManager.instance.onPressedAxis(i, axisIdx);
            }
            float povX = controller.getPovX();
            float povY = controller.getPovY();
            if (povX == this.controllerPov[i][0] && povY == this.controllerPov[i][1]) continue;
            this.controllerPov[i][0] = povX;
            this.controllerPov[i][1] = povY;
            JoypadManager.instance.onPressedPov(i);
        }
        this.controllerStateCache.swap();
    }

    private boolean checkConnectDisconnect(ControllerStateCache controllerState) {
        boolean bChanged = false;
        this.newlyConnected.clear();
        this.newlyDisconnected.clear();
        for (int i = 0; i < 16; ++i) {
            Controller controller = controllerState.getController(i);
            if (controller == this.controllers[i]) continue;
            bChanged = true;
            if (controller == null || !controller.isGamepad()) {
                if (this.controllers[i] != null) {
                    this.newlyDisconnected.add(this.controllers[i]);
                }
                controller = null;
            } else {
                this.newlyConnected.add(controller);
            }
            this.controllers[i] = controller;
        }
        return bChanged;
    }

    public void quit() {
        this.controllerStateCache.quit();
    }
}

