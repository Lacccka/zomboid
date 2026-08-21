/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.binding.ImGuiStructDestroyable;

public final class ImGuiTextFilter
extends ImGuiStructDestroyable {
    public ImGuiTextFilter() {
        this("");
    }

    public ImGuiTextFilter(String defaultFilter) {
        this.ptr = this.nCreate(defaultFilter);
    }

    ImGuiTextFilter(long ptr) {
        super(ptr);
    }

    @Override
    protected long create() {
        return this.nCreate("");
    }

    private native long nCreate(String var1);

    public boolean draw() {
        return this.draw("Filter (inc,-exc)");
    }

    public boolean draw(String label) {
        return this.draw(label, 0.0f);
    }

    public native boolean draw(String var1, float var2);

    public native boolean passFilter(String var1);

    public native void build();

    public native void clear();

    public native boolean isActive();

    public native String getInputBuffer();

    public native void setInputBuffer(String var1);
}

