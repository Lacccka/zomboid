/*
 * Decompiled with CFR 0.152.
 */
package imgui.extension.implot;

import imgui.binding.ImGuiStructDestroyable;

public final class ImPlotRange
extends ImGuiStructDestroyable {
    public ImPlotRange(long ptr) {
        super(ptr);
    }

    public ImPlotRange() {
        this(0.0, 0.0);
    }

    public ImPlotRange(double min, double max) {
        this(0L);
        this.ptr = this.create(min, max);
    }

    @Override
    protected native long create();

    protected native long create(double var1, double var3);

    public native boolean contains(double var1);

    public native double size();

    public native double getMin();

    public native double getMax();

    public native void setMin(double var1);

    public native void nSetMax(double var1);
}

