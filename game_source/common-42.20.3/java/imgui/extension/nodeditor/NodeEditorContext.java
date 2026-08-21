/*
 * Decompiled with CFR 0.152.
 */
package imgui.extension.nodeditor;

import imgui.binding.ImGuiStructDestroyable;
import imgui.extension.nodeditor.NodeEditorConfig;

public final class NodeEditorContext
extends ImGuiStructDestroyable {
    public NodeEditorContext() {
    }

    public NodeEditorContext(NodeEditorConfig config) {
        this(NodeEditorContext.nCreate(config.ptr));
    }

    public NodeEditorContext(long ptr) {
        super(ptr);
    }

    @Override
    protected long create() {
        return this.nCreate();
    }

    @Override
    public void destroy() {
        this.nDestroyEditorContext();
    }

    private native long nCreate();

    private static native long nCreate(long var0);

    private native void nDestroyEditorContext();
}

