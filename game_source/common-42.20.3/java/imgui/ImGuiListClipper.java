/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.callback.ImListClipperCallback;

public final class ImGuiListClipper {
    private ImGuiListClipper() {
    }

    public static void forEach(int itemsCount, ImListClipperCallback callback) {
        ImGuiListClipper.forEach(itemsCount, -1, callback);
    }

    public static native void forEach(int var0, int var1, ImListClipperCallback var2);
}

