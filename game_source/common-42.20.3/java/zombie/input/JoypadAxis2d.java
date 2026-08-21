/*
 * Decompiled with CFR 0.152.
 */
package zombie.input;

import zombie.UsedFromLua;
import zombie.core.math.PZMath;
import zombie.input.AxisValueSupplier;
import zombie.input.JoypadManager;
import zombie.iso.Vector2;

@UsedFromLua
public enum JoypadAxis2d {
    LeftStick(JoypadManager::getMovementAxisX, JoypadManager::getMovementAxisY, JoypadManager::isMovementAxisBeingApplied),
    RightStick(JoypadManager::getAimingAxisX, JoypadManager::getAimingAxisY, JoypadManager::isAimingAxisBeingApplied);

    private final AxisValueSupplier axisSupplierX;
    private final AxisValueSupplier axisSupplierY;
    private final IsAxisAppliedSupplier isAppliedSupplier;
    private static final JoypadAxis2d[] values;

    private JoypadAxis2d(AxisValueSupplier axisSupplierX, AxisValueSupplier axisSupplierY, IsAxisAppliedSupplier isAppliedSupplier) {
        this.axisSupplierX = axisSupplierX;
        this.axisSupplierY = axisSupplierY;
        this.isAppliedSupplier = isAppliedSupplier;
    }

    @UsedFromLua
    public String getNameTranslationKey() {
        return "UI_optionscreen_gamepad_JoypadAxis2d_name_" + this.name();
    }

    public float getLength(int joypadBind) {
        return PZMath.getLength(this.getValueX(joypadBind), this.getValueY(joypadBind));
    }

    public Vector2 getValue(int joypadBind, Vector2 out) {
        return out.set(this.getValueX(joypadBind), this.getValueY(joypadBind));
    }

    public float getValueX(int joypadBind) {
        return this.axisSupplierX.getAxisValue(JoypadManager.instance, joypadBind);
    }

    public float getValueY(int joypadBind) {
        return this.axisSupplierY.getAxisValue(JoypadManager.instance, joypadBind);
    }

    public boolean isApplied(int joypadBind) {
        return this.isAppliedSupplier.isApplied(JoypadManager.instance, joypadBind);
    }

    @UsedFromLua
    public static JoypadAxis2d[] getAxes() {
        return values;
    }

    static {
        values = JoypadAxis2d.values();
    }

    public static interface IsAxisAppliedSupplier {
        public boolean isApplied(JoypadManager var1, int var2);
    }
}

