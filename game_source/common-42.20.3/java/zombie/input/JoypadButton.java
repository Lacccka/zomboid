/*
 * Decompiled with CFR 0.152.
 */
package zombie.input;

import zombie.UsedFromLua;
import zombie.input.IsButtonDownSupplier;
import zombie.input.JoypadManager;

@UsedFromLua
public enum JoypadButton {
    A(JoypadManager::isAPressed),
    B(JoypadManager::isBPressed),
    X(JoypadManager::isXPressed),
    Y(JoypadManager::isYPressed),
    LeftStick(JoypadManager::isL3Pressed),
    RightStick(JoypadManager::isR3Pressed),
    LeftBump(JoypadManager::isLBPressed),
    RightBump(JoypadManager::isRBPressed),
    Back(JoypadManager::isBackPressed),
    Start(JoypadManager::isStartPressed),
    Guide(JoypadManager::isGuidePressed),
    DPadLeft(JoypadManager::isLeftPressed),
    DPadRight(JoypadManager::isRightPressed),
    DPadUp(JoypadManager::isUpPressed),
    DPadDown(JoypadManager::isDownPressed);

    private final IsButtonDownSupplier buttonDownSupplier;
    private static final JoypadButton[] values;

    private JoypadButton(IsButtonDownSupplier buttonDownSupplier) {
        this.buttonDownSupplier = buttonDownSupplier;
    }

    @UsedFromLua
    public boolean isDown(int joypadBind) {
        return this.buttonDownSupplier.isDown(JoypadManager.instance, joypadBind);
    }

    @UsedFromLua
    public String getNameTranslationKey() {
        return "UI_optionscreen_gamepad_JoypadButton_name_" + this.name();
    }

    public static int getButtonCount() {
        return values.length;
    }

    @UsedFromLua
    public static boolean isButtonDown(int joypadBind, int buttonIdx) {
        return buttonIdx >= 0 && buttonIdx < JoypadButton.getButtonCount() && JoypadButton.fromIndex(buttonIdx).isDown(joypadBind);
    }

    @UsedFromLua
    public static JoypadButton[] getButtons() {
        return values;
    }

    public static JoypadButton fromIndex(int buttonIdx) {
        if (buttonIdx < 0 || buttonIdx >= JoypadButton.getButtonCount()) {
            throw new ArrayIndexOutOfBoundsException(String.format("Index out of range: %d. Expected 0 - %d", buttonIdx, JoypadButton.getButtonCount()));
        }
        return values[buttonIdx];
    }

    static {
        values = JoypadButton.values();
    }
}

