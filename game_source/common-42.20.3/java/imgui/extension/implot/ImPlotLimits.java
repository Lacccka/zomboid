/*
 * Decompiled with CFR 0.152.
 */
package imgui.extension.implot;

import imgui.ImVec2;
import imgui.binding.ImGuiStructDestroyable;
import imgui.extension.implot.ImPlotPoint;
import imgui.extension.implot.ImPlotRange;

public final class ImPlotLimits
extends ImGuiStructDestroyable {
    public ImPlotLimits(long ptr) {
        super(ptr);
    }

    public ImPlotLimits() {
        this(0.0, 0.0, 0.0, 0.0);
    }

    public ImPlotLimits(double xMin, double xMax, double yMin, double yMax) {
        this(0L);
        this.ptr = this.create(xMin, xMax, yMin, yMax);
    }

    @Override
    protected native long create();

    protected native long create(double var1, double var3, double var5, double var7);

    public boolean contains(ImPlotPoint plotPoint) {
        return this.contains(plotPoint.getX(), plotPoint.getY());
    }

    public native boolean contains(double var1, double var3);

    public ImVec2 minVec() {
        ImPlotPoint val = this.min();
        ImVec2 vec = new ImVec2((float)val.getX(), (float)val.getY());
        val.destroy();
        return vec;
    }

    public ImPlotPoint min() {
        return new ImPlotPoint(this.nMin());
    }

    private native long nMin();

    public ImPlotPoint max() {
        return new ImPlotPoint(this.nMax());
    }

    public ImVec2 maxVec() {
        ImPlotPoint val = this.max();
        ImVec2 vec = new ImVec2((float)val.getX(), (float)val.getY());
        val.destroy();
        return vec;
    }

    private native long nMax();

    public ImPlotRange getX() {
        return new ImPlotRange(this.nGetX(this.ptr));
    }

    private native long nGetX(long var1);

    public ImPlotRange getY() {
        return new ImPlotRange(this.nGetY(this.ptr));
    }

    private native long nGetY(long var1);

    public void setX(ImPlotRange value) {
        this.nSetX(value.ptr);
    }

    private native void nSetX(long var1);

    public void setY(ImPlotRange value) {
        this.nSetY(value.ptr);
    }

    private native void nSetY(long var1);
}

