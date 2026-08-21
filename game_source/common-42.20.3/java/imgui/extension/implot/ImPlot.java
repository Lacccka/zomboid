/*
 * Decompiled with CFR 0.152.
 */
package imgui.extension.implot;

import imgui.ImDrawList;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.extension.implot.ImPlotContext;
import imgui.extension.implot.ImPlotLimits;
import imgui.extension.implot.ImPlotPoint;
import imgui.extension.implot.ImPlotStyle;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;

public final class ImPlot {
    private static final ImDrawList IM_DRAW_LIST = new ImDrawList(0L);
    private static final ImPlotContext IMPLOT_CONTEXT = new ImPlotContext(0L);
    private static final ImPlotPoint IMPLOT_POINT = new ImPlotPoint(0L);
    private static final ImPlotLimits IMPLOT_LIMITS = new ImPlotLimits(0L);
    private static final ImPlotStyle IMPLOT_STYLE = new ImPlotStyle(0L);

    private ImPlot() {
    }

    public static ImPlotContext createContext() {
        ImPlot.IMPLOT_CONTEXT.ptr = ImPlot.nCreateContext();
        return IMPLOT_CONTEXT;
    }

    private static native long nCreateContext();

    public static void destroyContext(ImPlotContext ctx) {
        ImPlot.nDestroyContext(ctx.ptr);
    }

    private static native void nDestroyContext(long var0);

    public static ImPlotContext getCurrentContext() {
        ImPlot.IMPLOT_CONTEXT.ptr = ImPlot.nGetCurrentContext();
        return IMPLOT_CONTEXT;
    }

    private static native long nGetCurrentContext();

    public static void setCurrentContext(ImPlotContext ctx) {
        ImPlot.nSetCurrentContext(ctx.ptr);
    }

    private static native void nSetCurrentContext(long var0);

    public static native boolean beginPlot(String var0);

    public static native boolean beginPlot(String var0, String var1, String var2);

    public static boolean beginPlot(String titleID, String xLabel, String yLabel, ImVec2 size) {
        return ImPlot.nBeginPlot(titleID, xLabel, yLabel, size.x, size.y);
    }

    private static native boolean nBeginPlot(String var0, String var1, String var2, float var3, float var4);

    public static boolean beginPlot(String titleID, String xLabel, String yLabel, ImVec2 size, int flags, int xFlags, int yFlags) {
        return ImPlot.nBeginPlot(titleID, xLabel, yLabel, size.x, size.y, flags, xFlags, yFlags);
    }

    private static native boolean nBeginPlot(String var0, String var1, String var2, float var3, float var4, int var5, int var6, int var7);

    public static boolean beginPlot(String titleID, String xLabel, String yLabel, ImVec2 size, int flags, int xFlags, int yFlags, int y2Flags, int y3Flags, String y2Label, String y3Label) {
        return ImPlot.nBeginPlot(titleID, xLabel, yLabel, size.x, size.y, flags, xFlags, yFlags, y2Flags, y3Flags, y2Label, y3Label);
    }

    private static native boolean nBeginPlot(String var0, String var1, String var2, float var3, float var4, int var5, int var6, int var7, int var8, int var9, String var10, String var11);

    public static native void endPlot();

    private static <T extends Number> void convertArrays(T[] xs, T[] ys, double[] xsOut, double[] ysOut) {
        if (xs.length != ys.length) {
            throw new IllegalArgumentException("Invalid length for arrays");
        }
        for (int i = 0; i < xs.length; ++i) {
            xsOut[i] = ((Number)xs[i]).doubleValue();
            ysOut[i] = ((Number)ys[i]).doubleValue();
        }
    }

    private static <T extends Number> void convertArrays(T[] xs, T[] ys1, T[] ys2, double[] xsOut, double[] ysOut1, double[] ysOut2) {
        if (xs.length != ys1.length || xs.length != ys2.length) {
            throw new IllegalArgumentException("Invalid length for arrays");
        }
        for (int i = 0; i < xs.length; ++i) {
            xsOut[i] = ((Number)xs[i]).doubleValue();
            ysOut1[i] = ((Number)ys1[i]).doubleValue();
            ysOut2[i] = ((Number)ys2[i]).doubleValue();
        }
    }

    public static <T extends Number> void plotLine(String labelID, T[] xs, T[] ys) {
        ImPlot.plotLine((String)labelID, xs, ys, (int)0);
    }

    public static <T extends Number> void plotLine(String labelID, T[] xs, T[] ys, int offset) {
        double[] x = new double[xs.length];
        double[] y = new double[ys.length];
        ImPlot.convertArrays(xs, ys, (double[])x, (double[])y);
        ImPlot.nPlotLine(labelID, x, y, x.length, offset);
    }

    public static void plotLine(String labelID, double[] xs, double[] ys, int size, int offset) {
        ImPlot.nPlotLine(labelID, xs, ys, size, offset);
    }

    private static native void nPlotLine(String var0, double[] var1, double[] var2, int var3, int var4);

    public static <T extends Number> void plotScatter(String labelID, T[] xs, T[] ys) {
        ImPlot.plotScatter((String)labelID, xs, ys, (int)0);
    }

    public static <T extends Number> void plotScatter(String labelID, T[] xs, T[] ys, int offset) {
        double[] x = new double[xs.length];
        double[] y = new double[ys.length];
        ImPlot.convertArrays(xs, ys, (double[])x, (double[])y);
        ImPlot.nPlotScatter(labelID, x, y, x.length, offset);
    }

    public static void plotScatter(String labelID, double[] xs, double[] ys, int size, int offset) {
        ImPlot.nPlotScatter(labelID, xs, ys, size, offset);
    }

    private static native void nPlotScatter(String var0, double[] var1, double[] var2, int var3, int var4);

    public static <T extends Number> void plotStairs(String labelID, T[] xs, T[] ys) {
        ImPlot.plotStairs((String)labelID, xs, ys, (int)0);
    }

    public static <T extends Number> void plotStairs(String labelID, T[] xs, T[] ys, int offset) {
        double[] x = new double[xs.length];
        double[] y = new double[ys.length];
        ImPlot.convertArrays(xs, ys, (double[])x, (double[])y);
        ImPlot.nPlotStairs(labelID, x, y, x.length, offset);
    }

    public static void plotStairs(String labelID, double[] xs, double[] ys, int size, int offset) {
        ImPlot.nPlotStairs(labelID, xs, ys, size, offset);
    }

    private static native void nPlotStairs(String var0, double[] var1, double[] var2, int var3, int var4);

    public static <T extends Number> void plotShaded(String labelID, T[] xs, T[] ys, int yRef) {
        ImPlot.plotShaded((String)labelID, xs, ys, (int)yRef, (int)0);
    }

    public static <T extends Number> void plotShaded(String labelID, T[] xs, T[] ys1, T[] ys2) {
        ImPlot.plotShaded((String)labelID, xs, ys1, ys2, (int)0);
    }

    public static <T extends Number> void plotShaded(String labelID, T[] xs, T[] ys, int yRef, int offset) {
        double[] x = new double[xs.length];
        double[] y = new double[ys.length];
        ImPlot.convertArrays(xs, ys, (double[])x, (double[])y);
        ImPlot.nPlotShaded(labelID, x, y, x.length, yRef, offset);
    }

    public static <T extends Number> void plotShaded(String labelID, T[] xs, T[] ys1, T[] ys2, int offset) {
        double[] x = new double[xs.length];
        double[] y1 = new double[ys1.length];
        double[] y2 = new double[ys2.length];
        ImPlot.convertArrays(xs, ys1, ys2, (double[])x, (double[])y1, (double[])y2);
        ImPlot.nPlotShaded(labelID, x, y1, y2, x.length, offset);
    }

    public static void plotShaded(String labelID, double[] xs, double[] ys, int size, int yRef, int offset) {
        ImPlot.nPlotShaded(labelID, xs, ys, size, yRef, offset);
    }

    private static native void nPlotShaded(String var0, double[] var1, double[] var2, int var3, int var4, int var5);

    public static void plotShaded(String labelID, double[] xs, double[] ys1, double[] ys2, int size, int offset) {
        ImPlot.nPlotShaded(labelID, xs, ys1, ys2, size, offset);
    }

    private static native void nPlotShaded(String var0, double[] var1, double[] var2, double[] var3, int var4, int var5);

    public static <T extends Number> void plotBars(String labelID, T[] xs, T[] ys) {
        ImPlot.plotBars((String)labelID, xs, ys, (float)0.67f, (int)0);
    }

    public static <T extends Number> void plotBars(String labelID, T[] xs, T[] ys, float width) {
        ImPlot.plotBars((String)labelID, xs, ys, (float)width, (int)0);
    }

    public static <T extends Number> void plotBars(String labelID, T[] xs, T[] ys, float width, int offset) {
        double[] x = new double[xs.length];
        double[] y = new double[ys.length];
        ImPlot.convertArrays(xs, ys, (double[])x, (double[])y);
        ImPlot.nPlotBars(labelID, x, y, x.length, width, offset);
    }

    public static void plotBars(String labelID, double[] xs, double[] ys, int size, float width, int offset) {
        ImPlot.nPlotBars(labelID, xs, ys, size, width, offset);
    }

    private static native void nPlotBars(String var0, double[] var1, double[] var2, int var3, float var4, int var5);

    public static <T extends Number> void plotBarsH(String labelID, T[] xs, T[] ys) {
        ImPlot.plotBarsH((String)labelID, xs, ys, (float)0.67f, (int)0);
    }

    public static <T extends Number> void plotBarsH(String labelID, T[] xs, T[] ys, float height) {
        ImPlot.plotBarsH((String)labelID, xs, ys, (float)height, (int)0);
    }

    public static <T extends Number> void plotBarsH(String labelID, T[] xs, T[] ys, float height, int offset) {
        double[] x = new double[xs.length];
        double[] y = new double[ys.length];
        ImPlot.convertArrays(xs, ys, (double[])x, (double[])y);
        ImPlot.nPlotBarsH(labelID, x, y, x.length, height, offset);
    }

    public static void plotBarsH(String labelID, double[] xs, double[] ys, int size, float height, int offset) {
        ImPlot.nPlotBarsH(labelID, xs, ys, size, height, offset);
    }

    private static native void nPlotBarsH(String var0, double[] var1, double[] var2, int var3, float var4, int var5);

    public static <T extends Number> void plotErrorBars(String labelID, T[] xs, T[] ys, T[] err) {
        ImPlot.plotErrorBars((String)labelID, xs, ys, err, (int)0);
    }

    public static <T extends Number> void plotErrorBars(String labelID, T[] xs, T[] ys, T[] err, int offset) {
        double[] x = new double[xs.length];
        double[] y = new double[ys.length];
        double[] errOut = new double[err.length];
        ImPlot.convertArrays(xs, ys, (double[])x, (double[])y);
        ImPlot.convertArrays(xs, err, (double[])x, (double[])errOut);
        ImPlot.nPlotErrorBars(labelID, x, y, errOut, x.length, offset);
    }

    public static void plotErrorBars(String labelID, double[] xs, double[] ys, double[] err, int size, int offset) {
        ImPlot.nPlotErrorBars(labelID, xs, ys, err, size, offset);
    }

    private static native void nPlotErrorBars(String var0, double[] var1, double[] var2, double[] var3, int var4, int var5);

    public static <T extends Number> void plotErrorBarsH(String labelID, T[] xs, T[] ys, T[] err) {
        ImPlot.plotErrorBarsH((String)labelID, xs, ys, err, (int)0);
    }

    public static <T extends Number> void plotErrorBarsH(String labelID, T[] xs, T[] ys, T[] err, int offset) {
        double[] x = new double[xs.length];
        double[] y = new double[ys.length];
        double[] errOut = new double[err.length];
        ImPlot.convertArrays(xs, ys, (double[])x, (double[])y);
        ImPlot.convertArrays(xs, err, (double[])x, (double[])errOut);
        ImPlot.nPlotErrorBarsH(labelID, x, y, errOut, x.length, offset);
    }

    public static void plotErrorBarsH(String labelID, double[] xs, double[] ys, double[] err, int size, int offset) {
        ImPlot.nPlotErrorBarsH(labelID, xs, ys, err, size, offset);
    }

    private static native void nPlotErrorBarsH(String var0, double[] var1, double[] var2, double[] var3, int var4, int var5);

    public static <T extends Number> void plotStems(String labelID, T[] values2, int yRef) {
        ImPlot.plotStems((String)labelID, values2, (int)yRef, (int)0);
    }

    public static <T extends Number> void plotStems(String labelID, T[] values2, int yRef, int offset) {
        double[] v = new double[values2.length];
        for (int i = 0; i < values2.length; ++i) {
            v[i] = ((Number)values2[i]).doubleValue();
        }
        ImPlot.nPlotStems(labelID, v, v.length, yRef, offset);
    }

    public static void plotStems(String labelID, double[] values2, int size, int yRef, int offset) {
        ImPlot.nPlotStems(labelID, values2, size, yRef, offset);
    }

    private static native void nPlotStems(String var0, double[] var1, int var2, int var3, int var4);

    public static <T extends Number> void plotVLines(String labelID, T[] values2) {
        ImPlot.plotVLines((String)labelID, values2, (int)0);
    }

    public static <T extends Number> void plotVLines(String labelID, T[] values2, int offset) {
        double[] v = new double[values2.length];
        for (int i = 0; i < values2.length; ++i) {
            v[i] = ((Number)values2[i]).doubleValue();
        }
        ImPlot.nPlotVLines(labelID, v, v.length, offset);
    }

    public static void plotVLines(String labelID, double[] values2, int size, int offset) {
        ImPlot.nPlotVLines(labelID, values2, size, offset);
    }

    private static native void nPlotVLines(String var0, double[] var1, int var2, int var3);

    public static <T extends Number> void plotHLines(String labelID, T[] values2) {
        ImPlot.plotHLines((String)labelID, values2, (int)0);
    }

    public static <T extends Number> void plotHLines(String labelID, T[] values2, int offset) {
        double[] v = new double[values2.length];
        for (int i = 0; i < values2.length; ++i) {
            v[i] = ((Number)values2[i]).doubleValue();
        }
        ImPlot.nPlotHLines(labelID, v, v.length, offset);
    }

    public static void plotHLines(String labelID, double[] values2, int size, int offset) {
        ImPlot.nPlotHLines(labelID, values2, size, offset);
    }

    private static native void nPlotHLines(String var0, double[] var1, int var2, int var3);

    public static <T extends Number> void plotPieChart(String[] labelIDs, T[] values2, double x, double y, double radius) {
        double[] v = new double[values2.length];
        for (int i = 0; i < values2.length; ++i) {
            v[i] = ((Number)values2[i]).doubleValue();
        }
        String labelIDsSs = "";
        boolean first = true;
        int maxSize = 0;
        for (String s : labelIDs) {
            if (first) {
                first = false;
                continue;
            }
            labelIDsSs = labelIDsSs + "\n";
            if (s.length() > maxSize) {
                maxSize = s.length();
            }
            labelIDsSs = labelIDsSs + s.replace("\n", "");
        }
        ImPlot.nPlotPieChart(labelIDsSs, maxSize, v, v.length, x, y, radius);
    }

    public static void plotPieChart(String labelIDsSs, int strLen, double[] values2, int size, double x, double y, double radius) {
        ImPlot.nPlotPieChart(labelIDsSs, strLen, values2, size, x, y, radius);
    }

    private static native void nPlotPieChart(String var0, int var1, double[] var2, int var3, double var4, double var6, double var8);

    public static <T extends Number> void plotHeatmap(String labelID, T[][] values2, int offset) {
        double[] v = new double[values2.length * values2[0].length];
        int pos = 0;
        T[][] TArray = values2;
        int n = TArray.length;
        for (int i = 0; i < n; ++i) {
            T[] a;
            for (T p : a = TArray[i]) {
                v[pos++] = ((Number)p).doubleValue();
            }
        }
        ImPlot.nPlotHeatmap(labelID, v, values2[0].length, values2.length);
    }

    public static void plotHeatmap(String labelID, double[] values2, int rows, int cols) {
        ImPlot.nPlotHeatmap(labelID, values2, rows, cols);
    }

    private static native void nPlotHeatmap(String var0, double[] var1, int var2, int var3);

    public static <T extends Number> double plotHistogram(String labelID, T[] values2) {
        double[] v = new double[values2.length];
        for (int i = 0; i < values2.length; ++i) {
            v[i] = ((Number)values2[i]).doubleValue();
        }
        return ImPlot.nPlotHistogram(labelID, v, v.length);
    }

    public static double plotHistogram(String labelID, double[] values2, int size) {
        return ImPlot.nPlotHistogram(labelID, values2, size);
    }

    private static native double nPlotHistogram(String var0, double[] var1, int var2);

    public static <T extends Number> double plotHistogram2D(String labelID, T[] xs, T[] ys) {
        double[] x = new double[xs.length];
        double[] y = new double[ys.length];
        ImPlot.convertArrays(xs, ys, (double[])x, (double[])y);
        return ImPlot.nPlotHistogram2D(labelID, x, y, x.length);
    }

    public static double plotHistogram2D(String labelID, double[] xs, double[] ys, int size) {
        return ImPlot.nPlotHistogram2D(labelID, xs, ys, size);
    }

    private static native double nPlotHistogram2D(String var0, double[] var1, double[] var2, int var3);

    public static <T extends Number> void plotDigital(String labelID, T[] xs, T[] ys) {
        double[] x = new double[xs.length];
        double[] y = new double[ys.length];
        ImPlot.convertArrays(xs, ys, (double[])x, (double[])y);
        ImPlot.nPlotDigital(labelID, x, y, x.length);
    }

    public static void plotDigital(String labelID, double[] xs, double[] ys, int size) {
        ImPlot.nPlotDigital(labelID, xs, ys, size);
    }

    private static native void nPlotDigital(String var0, double[] var1, double[] var2, int var3);

    public static void plotText(String text, double x, double y) {
        ImPlot.plotText(text, x, y, false);
    }

    public static native void plotText(String var0, double var1, double var3, boolean var5);

    public static native void plotDummy(String var0);

    public static native void setNextPlotLimits(double var0, double var2, double var4, double var6, int var8);

    public static native void setNextPlotLimitsX(double var0, double var2, int var4);

    public static native void setNextPlotLimitsY(double var0, double var2, int var4);

    public static void linkNextPlotLimits(ImDouble xmin, ImDouble xmax, ImDouble ymin, ImDouble ymax) {
        ImPlot.linkNextPlotLimits(xmin, xmax, ymin, ymax, null, null, null, null);
    }

    public static void linkNextPlotLimits(ImDouble xmin, ImDouble xmax, ImDouble ymin, ImDouble ymax, ImDouble ymin2, ImDouble ymax2) {
        ImPlot.linkNextPlotLimits(xmin, xmax, ymin, ymax, ymin2, ymax2, null, null);
    }

    public static void linkNextPlotLimits(ImDouble xmin, ImDouble xmax, ImDouble ymin, ImDouble ymax, ImDouble ymin2, ImDouble ymax2, ImDouble ymin3, ImDouble ymax3) {
        ImPlot.nLinkNextPlotLimits(xmin.getData(), xmax.getData(), ymin.getData(), ymax.getData(), ymin2.getData(), ymax2.getData(), ymin3.getData(), ymax3.getData());
    }

    private static native void nLinkNextPlotLimits(double[] var0, double[] var1, double[] var2, double[] var3, double[] var4, double[] var5, double[] var6, double[] var7);

    public static void fitNextPlotAxes() {
        ImPlot.fitNextPlotAxes(true, true, true, true);
    }

    public static void fitNextPlotAxes(boolean x, boolean y) {
        ImPlot.fitNextPlotAxes(x, y, true, true);
    }

    public static native void fitNextPlotAxes(boolean var0, boolean var1, boolean var2, boolean var3);

    public static void setNextPlotTicksX(double xMin, double xMax, int nTicks) {
        ImPlot.setNextPlotTicksX(xMin, xMax, nTicks, null, false);
    }

    public static void setNextPlotTicksX(double xMin, double xMax, int nTicks, String[] labels, boolean keepDefault) {
        String[] labelStrings = new String[4];
        for (int i = 0; i < 4; ++i) {
            labelStrings[i] = labels != null && i < labels.length ? labels[i] : null;
        }
        ImPlot.nSetNextPlotTicksX(xMin, xMax, nTicks, labelStrings[0], labelStrings[1], labelStrings[2], labelStrings[3], keepDefault);
    }

    public static void setNextPlotTicksX(double xMin, double xMax, int nTicks, String s1, String s2, String s3, String s4, boolean keepDefault) {
        ImPlot.nSetNextPlotTicksX(xMin, xMax, nTicks, s1, s2, s3, s4, keepDefault);
    }

    private static native void nSetNextPlotTicksX(double var0, double var2, int var4, String var5, String var6, String var7, String var8, boolean var9);

    public static void setNextPlotTicksY(double xMin, double xMax, int nTicks) {
        ImPlot.setNextPlotTicksY(xMin, xMax, nTicks, null, false, 0);
    }

    public static void setNextPlotTicksY(double xMin, double xMax, int nTicks, String[] labels, boolean keepDefault) {
        ImPlot.setNextPlotTicksY(xMin, xMax, nTicks, labels, keepDefault, 0);
    }

    public static void setNextPlotTicksY(double xMin, double xMax, int nTicks, String[] labels, boolean keepDefault, int yAxis) {
        String[] labelStrings = new String[4];
        for (int i = 0; i < 4; ++i) {
            labelStrings[i] = labels != null && i < labels.length ? labels[i] : null;
        }
        ImPlot.nSetNextPlotTicksY(xMin, xMax, nTicks, labelStrings[0], labelStrings[1], labelStrings[2], labelStrings[3], keepDefault, yAxis);
    }

    public static void setNextPlotTicksY(double xMin, double xMax, int nTicks, String s1, String s2, String s3, String s4, boolean keepDefault, int yAxis) {
        ImPlot.nSetNextPlotTicksY(xMin, xMax, nTicks, s1, s2, s3, s4, keepDefault, yAxis);
    }

    private static native void nSetNextPlotTicksY(double var0, double var2, int var4, String var5, String var6, String var7, String var8, boolean var9, int var10);

    public static native void setNextPlotFormatX(String var0);

    public static void setNextPlotFormatY(String fmt) {
        ImPlot.setNextPlotFormatY(fmt, 0);
    }

    public static native void setNextPlotFormatY(String var0, int var1);

    public static native void setPlotYAxis(int var0);

    public static void hideNextItem() {
        ImPlot.hideNextItem(true, 2);
    }

    public static native void hideNextItem(boolean var0, int var1);

    public static ImPlotPoint pixelsToPlot(ImVec2 pix, int yAxis) {
        ImPlot.IMPLOT_POINT.ptr = ImPlot.nPixelsToPlot(pix.x, pix.y, yAxis);
        return IMPLOT_POINT;
    }

    public static ImPlotPoint pixelsToPlot(float x, float y, int yAxis) {
        ImPlot.IMPLOT_POINT.ptr = ImPlot.nPixelsToPlot(x, y, yAxis);
        return IMPLOT_POINT;
    }

    private static native long nPixelsToPlot(float var0, float var1, int var2);

    public static ImVec2 plotToPixels(ImPlotPoint plt, int yAxis) {
        return ImPlot.plotToPixels(plt.getX(), plt.getY(), yAxis);
    }

    public static ImVec2 plotToPixels(double x, double y, int yAxis) {
        ImVec2 value = new ImVec2();
        ImPlot.nPlotToPixels(x, y, yAxis, value);
        return value;
    }

    private static native void nPlotToPixels(double var0, double var2, int var4, ImVec2 var5);

    public static ImVec2 getPlotPos() {
        ImVec2 value = new ImVec2();
        ImPlot.getPlotPos(value);
        return value;
    }

    public static native void getPlotPos(ImVec2 var0);

    public static ImVec2 getPlotSize() {
        ImVec2 value = new ImVec2();
        ImPlot.getPlotSize(value);
        return value;
    }

    public static native void getPlotSize(ImVec2 var0);

    public static native boolean isPlotHovered();

    public static native boolean isPlotXAxisHovered();

    public static native boolean isPlotYAxisHovered(int var0);

    public static ImPlotPoint getPlotMousePos(int yAxis) {
        ImPlot.IMPLOT_POINT.ptr = ImPlot.nGetPlotMousePos(yAxis);
        return IMPLOT_POINT;
    }

    private static native long nGetPlotMousePos(int var0);

    public static ImPlotLimits getPlotLimits(int yAxis) {
        ImPlot.IMPLOT_LIMITS.ptr = ImPlot.nGetPlotLimits(yAxis);
        return IMPLOT_LIMITS;
    }

    private static native long nGetPlotLimits(int var0);

    public static native boolean isPlotSelected();

    public static ImPlotLimits getPlotSelection(int yAxis) {
        ImPlot.IMPLOT_LIMITS.ptr = ImPlot.nGetPlotSelection(yAxis);
        return IMPLOT_LIMITS;
    }

    private static native long nGetPlotSelection(int var0);

    public static native boolean isPlotQueried();

    public static ImPlotLimits getPlotQuery(int yAxis) {
        ImPlot.IMPLOT_LIMITS.ptr = ImPlot.nGetPlotQuery(yAxis);
        return IMPLOT_LIMITS;
    }

    private static native long nGetPlotQuery(int var0);

    public static void setPlotQuery(ImPlotLimits query, int yAxis) {
        ImPlot.nSetPlotQuery(query.ptr, yAxis);
    }

    private static native void nSetPlotQuery(long var0, int var2);

    public static void annotate(double x, double y, ImVec2 pixOffset, String ... fmt) {
        ImPlot.annotate(x, y, pixOffset, new ImVec4(0.0f, 0.0f, 0.0f, 0.0f), fmt);
    }

    public static void annotate(double x, double y, ImVec2 pixOffset, ImVec4 color, String ... fmt) {
        ImPlot.nAnnotate(x, y, pixOffset.x, pixOffset.y, color.w, color.x, color.y, color.z, fmt.length > 0 ? fmt[0] : null, fmt.length > 1 ? fmt[1] : null, fmt.length > 2 ? fmt[2] : null, fmt.length > 3 ? fmt[3] : null, fmt.length > 4 ? fmt[4] : null);
    }

    private static native void nAnnotate(double var0, double var2, float var4, float var5, float var6, float var7, float var8, float var9, String var10, String var11, String var12, String var13, String var14);

    public static void annotateClamped(double x, double y, ImVec2 pixOffset, String ... fmt) {
        ImPlot.annotateClamped(x, y, pixOffset, new ImVec4(0.0f, 0.0f, 0.0f, 0.0f), fmt);
    }

    public static void annotateClamped(double x, double y, ImVec2 pixOffset, ImVec4 color, String ... fmt) {
        ImPlot.nAnnotateClamped(x, y, pixOffset.x, pixOffset.y, color.w, color.x, color.y, color.z, fmt.length > 0 ? fmt[0] : null, fmt.length > 1 ? fmt[1] : null, fmt.length > 2 ? fmt[2] : null, fmt.length > 3 ? fmt[3] : null, fmt.length > 4 ? fmt[4] : null);
    }

    private static native void nAnnotateClamped(double var0, double var2, float var4, float var5, float var6, float var7, float var8, float var9, String var10, String var11, String var12, String var13, String var14);

    public static boolean dragLineX(String id, double xValue, boolean showLabel, ImVec4 color, float thickness) {
        return ImPlot.nDragLineX(id, xValue, showLabel, color.w, color.x, color.y, color.z, thickness);
    }

    private static native boolean nDragLineX(String var0, double var1, boolean var3, float var4, float var5, float var6, float var7, float var8);

    public static boolean dragLineY(String id, double yValue, boolean showLabel, ImVec4 color, float thickness) {
        return ImPlot.nDragLineY(id, yValue, showLabel, color.w, color.x, color.y, color.z, thickness);
    }

    private static native boolean nDragLineY(String var0, double var1, boolean var3, float var4, float var5, float var6, float var7, float var8);

    public static boolean dragPoint(String id, double x, double y, boolean showLabel, ImVec4 color, float radius) {
        return ImPlot.nDragPoint(id, x, y, showLabel, color.w, color.x, color.y, color.z, radius);
    }

    private static native boolean nDragPoint(String var0, double var1, double var3, boolean var5, float var6, float var7, float var8, float var9, float var10);

    public static native void setLegendLocation(int var0, int var1, boolean var2);

    public static native void setMousePosLocation(int var0);

    public static native boolean isLegendEntryHovered(String var0);

    public static boolean beginLegendPopup(String labelID) {
        return ImPlot.beginLegendPopup(labelID, 1);
    }

    public static native boolean beginLegendPopup(String var0, int var1);

    public static native void endLegendPopup();

    public static native boolean beginDragDropTarget();

    public static native boolean beginDragDropTargetX();

    public static native boolean beginDragDropTargetY(int var0);

    public static native boolean beginDragDropTargetLegend();

    public static native void endDragDropTarget();

    public static native boolean beginDragDropSource(int var0, int var1);

    public static native boolean beginDragDropSourceX(int var0, int var1);

    public static native boolean beginDragDropSourceY(int var0, int var1, int var2);

    public static native boolean beginDragDropSourceItem(String var0, int var1);

    public static native void endDragDropSource();

    public static ImPlotStyle getStyle() {
        ImPlot.IMPLOT_STYLE.ptr = ImPlot.nGetStyle();
        return IMPLOT_STYLE;
    }

    private static native long nGetStyle();

    public static native void styleColorsAuto();

    public static native void styleColorsClassic();

    public static native void styleColorsDark();

    public static native void styleColorsLight();

    public static native void pushStyleColor(int var0, long var1);

    public static void pushStyleColor(int idx, ImVec4 col) {
        ImPlot.nPushStyleColor(idx, col.w, col.x, col.y, col.z);
    }

    private static native void nPushStyleColor(int var0, float var1, float var2, float var3, float var4);

    public static void popStyleColor() {
        ImPlot.popStyleColor(1);
    }

    public static native void popStyleColor(int var0);

    public static native void pushStyleVar(int var0, float var1);

    public static native void pushStyleVar(int var0, int var1);

    public static void pushStyleVar(int idx, ImVec2 val) {
        ImPlot.nPushStyleVar(idx, val.x, val.y);
    }

    private static native void nPushStyleVar(int var0, float var1, float var2);

    public static void popStyleVar() {
        ImPlot.popStyleVar(1);
    }

    public static native void popStyleVar(int var0);

    public static ImVec4 getLastItemColor() {
        return new ImVec4(ImPlot.nGetLastItemColorS(0), ImPlot.nGetLastItemColorS(1), ImPlot.nGetLastItemColorS(2), ImPlot.nGetLastItemColorS(3));
    }

    private static native float nGetLastItemColorS(int var0);

    public static native String getStyleColorName(int var0);

    public static native String getMarkerName(int var0);

    public static int addColormap(String name, ImVec4[] cols) {
        float[] w = new float[cols.length];
        float[] x = new float[cols.length];
        float[] y = new float[cols.length];
        float[] z = new float[cols.length];
        for (int i = 0; i < cols.length; ++i) {
            w[i] = cols[i].w;
            x[i] = cols[i].x;
            y[i] = cols[i].y;
            z[i] = cols[i].z;
        }
        return ImPlot.nAddColormap(name, w, x, y, z, cols.length);
    }

    private static native int nAddColormap(String var0, float[] var1, float[] var2, float[] var3, float[] var4, int var5);

    public static native int getColormapCount();

    public static native String getColormapName(int var0);

    public static native int getColormapIndex(String var0);

    public static native void pushColormap(int var0);

    public static native void pushColormap(String var0);

    public static void popColormap() {
        ImPlot.popColormap(1);
    }

    public static native void popColormap(int var0);

    public static ImVec4 nextColormapColor() {
        ImVec4 vec = new ImVec4();
        ImPlot.nNextColormapColor(vec);
        return vec;
    }

    private static native void nNextColormapColor(ImVec4 var0);

    public static native int getColormapSize(int var0);

    public static ImVec4 getColormapColor(int idx, int cmap) {
        ImVec4 vec = new ImVec4();
        ImPlot.nGetColormapColor(idx, cmap, vec);
        return vec;
    }

    private static native void nGetColormapColor(int var0, int var1, ImVec4 var2);

    public static ImVec4 sampleColormap(float t, int cmap) {
        ImVec4 vec = new ImVec4();
        ImPlot.nSampleColormap(t, cmap, vec);
        return vec;
    }

    private static native void nSampleColormap(float var0, int var1, ImVec4 var2);

    public static native void colormapScale(String var0, double var1, double var3, int var5);

    public static native boolean colormapSlider(String var0, float var1, int var2);

    public static native boolean colormapButton(String var0, int var1);

    public static void bustColorCache() {
        ImPlot.bustColorCache(null);
    }

    public static native void bustColorCache(String var0);

    public static void itemIcon(ImVec4 col) {
        ImPlot.nItemIcon(col.w, col.x, col.y, col.z);
    }

    private static native void nItemIcon(double var0, double var2, double var4, double var6);

    public static native void colormapIcon(int var0);

    public static ImDrawList getPlotDrawList() {
        ImPlot.IM_DRAW_LIST.ptr = ImPlot.nGetPlotDrawList();
        return IM_DRAW_LIST;
    }

    private static native long nGetPlotDrawList();

    public static native void pushPlotClipRect(float var0);

    public static native void popPlotClipRect();

    public static native boolean showStyleSelector(String var0);

    public static native boolean showColormapSelector(String var0);

    public static native void showStyleEditor();

    public static native void showUserGuide();

    public static native void showMetricsWindow();

    public static void showDemoWindow(ImBoolean pOpen) {
        ImPlot.nShowDemoWindow(pOpen.getData());
    }

    private static native void nShowDemoWindow(boolean[] var0);
}

