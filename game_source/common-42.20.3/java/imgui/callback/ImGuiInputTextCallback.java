/*
 * Decompiled with CFR 0.152.
 */
package imgui.callback;

import imgui.ImGuiInputTextCallbackData;
import java.util.function.Consumer;

public abstract class ImGuiInputTextCallback
implements Consumer<ImGuiInputTextCallbackData> {
    @Override
    public final void accept(long ptr) {
        this.accept(new ImGuiInputTextCallbackData(ptr));
    }
}

