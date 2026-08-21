/*
 * Decompiled with CFR 0.152.
 */
package imgui.extension.imguiknobs;

import imgui.type.ImFloat;
import imgui.type.ImInt;

public final class ImGuiKnobs {
    private ImGuiKnobs() {
        throw new UnsupportedOperationException();
    }

    public static boolean knobFloat(String label, ImFloat pValue, float minValue, float maxValue, float speed, String format, int variant, float size, int flags, int steps) {
        return ImGuiKnobs.nKnob(label, pValue.getData(), minValue, maxValue, speed, format, variant, size, flags, steps);
    }

    private static native boolean nKnob(String var0, float[] var1, float var2, float var3, float var4, String var5, int var6, float var7, int var8, int var9);

    public static boolean knobInt(String label, ImInt pValue, int minValue, int maxValue, float speed, String format, int variant, float size, int flags, int steps) {
        return ImGuiKnobs.nKnobInt(label, pValue.getData(), minValue, maxValue, speed, format, variant, size, flags, steps);
    }

    private static native boolean nKnobInt(String var0, int[] var1, int var2, int var3, float var4, String var5, int var6, float var7, int var8, int var9);
}

