/*
 * Decompiled with CFR 0.152.
 */
package imgui.internal;

import imgui.binding.ImGuiStruct;

public class ImGuiWindow
extends ImGuiStruct {
    public ImGuiWindow(long ptr) {
        super(ptr);
    }

    public native boolean isScrollbarX();

    public native boolean isScrollbarY();
}

