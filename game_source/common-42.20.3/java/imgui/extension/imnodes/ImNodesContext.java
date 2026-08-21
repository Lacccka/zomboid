/*
 * Decompiled with CFR 0.152.
 */
package imgui.extension.imnodes;

import imgui.binding.ImGuiStructDestroyable;

public final class ImNodesContext
extends ImGuiStructDestroyable {
    public ImNodesContext() {
    }

    public ImNodesContext(long ptr) {
        super(ptr);
    }

    @Override
    protected long create() {
        return this.nCreate();
    }

    @Override
    public void destroy() {
        this.nDestroy();
    }

    private native long nCreate();

    private native void nDestroy();
}

