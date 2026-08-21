/*
 * Decompiled with CFR 0.152.
 */
package imgui.extension.memedit;

import imgui.binding.ImGuiStructDestroyable;
import imgui.extension.memedit.MemoryEditorSizes;

public final class MemoryEditor
extends ImGuiStructDestroyable {
    public MemoryEditor() {
    }

    public MemoryEditor(long ptr) {
        super(ptr);
    }

    @Override
    protected long create() {
        return this.nCreate();
    }

    private native long nCreate();

    public native void gotoAddrAndHighlight(long var1, long var3);

    public void calcSizes(MemoryEditorSizes s, long memSize, long baseDisplayAddr) {
        this.nCalcSizes(s.ptr, memSize, baseDisplayAddr);
    }

    public native void nCalcSizes(long var1, long var3, long var5);

    public void drawWindow(String title, long memData, long memSize) {
        this.drawWindow(title, memData, memSize, 0L);
    }

    public native void drawWindow(String var1, long var2, long var4, long var6);

    public void drawContents(long memData, long memSize) {
        this.drawContents(memData, memSize, 0L);
    }

    public native void drawContents(long var1, long var3, long var5);

    public void drawOptionsLine(MemoryEditorSizes s, long memData, long memSize, long baseDisplayAddr) {
        this.nDrawOptionsLine(s.ptr, memData, memSize, baseDisplayAddr);
    }

    public native void nDrawOptionsLine(long var1, long var3, long var5, long var7);

    public void drawPreviewLine(MemoryEditorSizes s, long memDataVoid, long memSize, long baseDisplayAddr) {
        this.nDrawPreviewLine(s.ptr, memDataVoid, memSize, baseDisplayAddr);
    }

    public native void nDrawPreviewLine(long var1, long var3, long var5, long var7);
}

