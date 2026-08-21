/*
 * Decompiled with CFR 0.152.
 */
package imgui.extension.implot;

import imgui.ImVec2;
import imgui.binding.ImGuiStructDestroyable;

public final class ImPlotPoint
extends ImGuiStructDestroyable {
    public ImPlotPoint(long ptr) {
        super(ptr);
    }

    public ImPlotPoint() {
        this(0.0, 0.0);
    }

    public ImPlotPoint(ImVec2 vec) {
        this(vec.x, vec.y);
    }

    public ImPlotPoint(double x, double y) {
        this(0L);
        this.ptr = this.create(x, y);
    }

    @Override
    protected native long create();

    protected native long create(double var1, double var3);

    public native double getX();

    public native double getY();

    public native void setX(double var1);

    public native void setY(double var1);
}

