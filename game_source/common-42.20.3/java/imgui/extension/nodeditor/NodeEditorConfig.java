/*
 * Decompiled with CFR 0.152.
 */
package imgui.extension.nodeditor;

import imgui.binding.ImGuiStructDestroyable;

public final class NodeEditorConfig
extends ImGuiStructDestroyable {
    public NodeEditorConfig() {
    }

    public NodeEditorConfig(long ptr) {
        super(ptr);
    }

    @Override
    protected long create() {
        return this.nCreate();
    }

    private native long nCreate();

    public native String getSettingsFile();

    public native void setSettingsFile(String var1);
}

