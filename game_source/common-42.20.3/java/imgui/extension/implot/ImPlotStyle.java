/*
 * Decompiled with CFR 0.152.
 */
package imgui.extension.implot;

import imgui.ImVec2;
import imgui.ImVec4;
import imgui.binding.ImGuiStruct;

public final class ImPlotStyle
extends ImGuiStruct {
    public ImPlotStyle(long ptr) {
        super(ptr);
    }

    public native float getLineWeight();

    public native void setLineWeight(float var1);

    public native int getMarker();

    public native void setMarker(float var1);

    public native float getMarkerSize();

    public native void setMarkerSize(float var1);

    public native float getMarkerWeight();

    public native void setMarkerWeight(float var1);

    public native float getFillAlpha();

    public native void setFillAlpha(float var1);

    public native float getErrorBarSize();

    public native void setErrorBarSize(float var1);

    public native float getErrorBarWeight();

    public native void setErrorBarWeight(float var1);

    public native float getDigitalBitHeight();

    public native void setDigitalBitHeight(float var1);

    public native float getDigitalBitGap();

    public native void setDigitalBitGap(float var1);

    public native float getPlotBorderSize();

    public native void setPlotBorderSize(float var1);

    public native float getMinorAlpha();

    public native void setMinorAlpha(float var1);

    public ImVec2 getMajorTickLen() {
        ImVec2 vec = new ImVec2();
        this.nGetMajorTickLen(vec);
        return vec;
    }

    private native void nGetMajorTickLen(ImVec2 var1);

    public void setMajorTickLen(ImVec2 majorTickLen) {
        this.nSetMajorTickLen(majorTickLen.x, majorTickLen.y);
    }

    private native void nSetMajorTickLen(float var1, float var2);

    public ImVec2 getMinorTickLen() {
        ImVec2 vec = new ImVec2();
        this.nGetMinorTickLen(vec);
        return vec;
    }

    private native void nGetMinorTickLen(ImVec2 var1);

    public void setMinorTickLen(ImVec2 minorTickLen) {
        this.nSetMinorTickLen(minorTickLen.x, minorTickLen.y);
    }

    private native void nSetMinorTickLen(float var1, float var2);

    public ImVec2 getMajorTickSize() {
        ImVec2 vec = new ImVec2();
        this.nGetMajorTickSize(vec);
        return vec;
    }

    private native void nGetMajorTickSize(ImVec2 var1);

    public void setMajorTickSize(ImVec2 majorTickSize) {
        this.nSetMajorTickSize(majorTickSize.x, majorTickSize.y);
    }

    private native void nSetMajorTickSize(float var1, float var2);

    public ImVec2 getMinorTickSize() {
        ImVec2 vec = new ImVec2();
        this.nGetMinorTickSize(vec);
        return vec;
    }

    private native void nGetMinorTickSize(ImVec2 var1);

    public void setMinorTickSize(ImVec2 minorTickSize) {
        this.nSetMinorTickSize(minorTickSize.x, minorTickSize.y);
    }

    private native void nSetMinorTickSize(float var1, float var2);

    public ImVec2 getMajorGridSize() {
        ImVec2 vec = new ImVec2();
        this.nGetMajorGridSize(vec);
        return vec;
    }

    private native void nGetMajorGridSize(ImVec2 var1);

    public void setMajorGridSize(ImVec2 majorGridSize) {
        this.nSetMajorGridSize(majorGridSize.x, majorGridSize.y);
    }

    private native void nSetMajorGridSize(float var1, float var2);

    public ImVec2 getMinorGridSize() {
        ImVec2 vec = new ImVec2();
        this.nGetMinorGridSize(vec);
        return vec;
    }

    private native void nGetMinorGridSize(ImVec2 var1);

    public void setMinorGridSize(ImVec2 minorGridSize) {
        this.nSetMinorGridSize(minorGridSize.x, minorGridSize.y);
    }

    private native void nSetMinorGridSize(float var1, float var2);

    public ImVec2 getPlotPadding() {
        ImVec2 vec = new ImVec2();
        this.nGetPlotPadding(vec);
        return vec;
    }

    private native void nGetPlotPadding(ImVec2 var1);

    public void setPlotPadding(ImVec2 plotPadding) {
        this.nSetPlotPadding(plotPadding.x, plotPadding.y);
    }

    private native void nSetPlotPadding(float var1, float var2);

    public ImVec2 getLabelPadding() {
        ImVec2 vec = new ImVec2();
        this.nGetLabelPadding(vec);
        return vec;
    }

    private native void nGetLabelPadding(ImVec2 var1);

    public void setLabelPadding(ImVec2 labelPadding) {
        this.nSetLabelPadding(labelPadding.x, labelPadding.y);
    }

    private native void nSetLabelPadding(float var1, float var2);

    public ImVec2 getLegendPadding() {
        ImVec2 vec = new ImVec2();
        this.nGetLegendPadding(vec);
        return vec;
    }

    private native void nGetLegendPadding(ImVec2 var1);

    public void setLegendPadding(ImVec2 legendPadding) {
        this.nSetLegendPadding(legendPadding.x, legendPadding.y);
    }

    private native void nSetLegendPadding(float var1, float var2);

    public ImVec2 getLegendInnerPadding() {
        ImVec2 vec = new ImVec2();
        this.nGetLegendInnerPadding(vec);
        return vec;
    }

    private native void nGetLegendInnerPadding(ImVec2 var1);

    public void setLegendInnerPadding(ImVec2 legendInnerPadding) {
        this.nSetLegendInnerPadding(legendInnerPadding.x, legendInnerPadding.y);
    }

    private native void nSetLegendInnerPadding(float var1, float var2);

    public ImVec2 getLegendSpacing() {
        ImVec2 vec = new ImVec2();
        this.nGetLegendSpacing(vec);
        return vec;
    }

    private native void nGetLegendSpacing(ImVec2 var1);

    public void setLegendSpacing(ImVec2 legendSpacing) {
        this.nSetLegendSpacing(legendSpacing.x, legendSpacing.y);
    }

    private native void nSetLegendSpacing(float var1, float var2);

    public ImVec2 getMousePosPadding() {
        ImVec2 vec = new ImVec2();
        this.nGetMousePosPadding(vec);
        return vec;
    }

    private native void nGetMousePosPadding(ImVec2 var1);

    public void setMousePosPadding(ImVec2 mousePosPadding) {
        this.nSetMousePosPadding(mousePosPadding.x, mousePosPadding.y);
    }

    private native void nSetMousePosPadding(float var1, float var2);

    public ImVec2 getAnnotationPadding() {
        ImVec2 vec = new ImVec2();
        this.nGetAnnotationPadding(vec);
        return vec;
    }

    private native void nGetAnnotationPadding(ImVec2 var1);

    public void setAnnotationPadding(ImVec2 annotationPadding) {
        this.nSetAnnotationPadding(annotationPadding.x, annotationPadding.y);
    }

    private native void nSetAnnotationPadding(float var1, float var2);

    public ImVec2 getFitPadding() {
        ImVec2 vec = new ImVec2();
        this.nGetFitPadding(vec);
        return vec;
    }

    private native void nGetFitPadding(ImVec2 var1);

    public void setFitPadding(ImVec2 fitPadding) {
        this.nSetFitPadding(fitPadding.x, fitPadding.y);
    }

    private native void nSetFitPadding(float var1, float var2);

    public ImVec2 getPlotDefaultSize() {
        ImVec2 vec = new ImVec2();
        this.nGetPlotDefaultSize(vec);
        return vec;
    }

    private native void nGetPlotDefaultSize(ImVec2 var1);

    public void setPlotDefaultSize(ImVec2 plotDefaultSize) {
        this.nSetPlotDefaultSize(plotDefaultSize.x, plotDefaultSize.y);
    }

    private native void nSetPlotDefaultSize(float var1, float var2);

    public ImVec2 getPlotMinSize() {
        ImVec2 vec = new ImVec2();
        this.nGetPlotMinSize(vec);
        return vec;
    }

    private native void nGetPlotMinSize(ImVec2 var1);

    public void setPlotMinSize(ImVec2 plotMinSize) {
        this.nSetPlotMinSize(plotMinSize.x, plotMinSize.y);
    }

    private native void nSetPlotMinSize(float var1, float var2);

    public ImVec4[] getColors() {
        float[] w = new float[25];
        float[] x = new float[25];
        float[] y = new float[25];
        float[] z = new float[25];
        this.nGetColors(w, x, y, z, 25);
        ImVec4[] colors = new ImVec4[25];
        for (int i = 0; i < 25; ++i) {
            colors[i] = new ImVec4(w[i], x[i], y[i], z[i]);
        }
        return colors;
    }

    private native void nGetColors(float[] var1, float[] var2, float[] var3, float[] var4, int var5);

    public void setColors(ImVec4[] colors) {
        float[] w = new float[25];
        float[] x = new float[25];
        float[] y = new float[25];
        float[] z = new float[25];
        for (int i = 0; i < 25; ++i) {
            w[i] = colors[i].w;
            x[i] = colors[i].x;
            y[i] = colors[i].y;
            z[i] = colors[i].z;
        }
        this.nSetColors(w, x, y, z, 25);
    }

    private native void nSetColors(float[] var1, float[] var2, float[] var3, float[] var4, int var5);

    public native int getColormap();

    public native void setColormap(int var1);

    public native boolean isAntiAliasedLines();

    public native void setAntiAliasedLines(boolean var1);

    public native boolean isUseLocalTime();

    public native void setUseLocalTime(boolean var1);

    public native boolean isUseISO8601();

    public native void setUseISO8601(boolean var1);

    public native boolean isUse24HourClock();

    public native void setUse24HourClock(boolean var1);
}

