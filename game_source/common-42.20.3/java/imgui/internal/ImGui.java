/*
 * Decompiled with CFR 0.152.
 */
package imgui.internal;

import imgui.ImVec2;
import imgui.internal.ImGuiDockNode;
import imgui.internal.ImGuiWindow;
import imgui.internal.ImRect;
import imgui.type.ImFloat;
import imgui.type.ImInt;

public final class ImGui
extends imgui.ImGui {
    private static final ImGuiDockNode DOCK_NODE = new ImGuiDockNode(0L);
    private static final ImGuiWindow IMGUI_CURRENT_WINDOW = new ImGuiWindow(0L);

    public static ImVec2 calcItemSize(float sizeX, float sizeY, float defaultW, float defaultH) {
        ImVec2 value = new ImVec2();
        ImGui.calcItemSize(sizeX, sizeY, defaultW, defaultH, value);
        return value;
    }

    public static native void calcItemSize(float var0, float var1, float var2, float var3, ImVec2 var4);

    public static native float calcItemSizeX(float var0, float var1, float var2, float var3);

    public static native float calcItemSizeY(float var0, float var1, float var2, float var3);

    public static native void pushItemFlag(int var0, boolean var1);

    public static native void popItemFlag();

    public static native void dockBuilderDockWindow(String var0, int var1);

    public static ImGuiDockNode dockBuilderGetNode(int nodeId) {
        ImGui.DOCK_NODE.ptr = ImGui.nDockBuilderGetNode(nodeId);
        return DOCK_NODE;
    }

    private static native long nDockBuilderGetNode(int var0);

    public static ImGuiDockNode dockBuilderGetCentralNode(int nodeId) {
        ImGui.DOCK_NODE.ptr = ImGui.nDockBuilderGetCentralNode(nodeId);
        return DOCK_NODE;
    }

    private static native long nDockBuilderGetCentralNode(int var0);

    public static native int dockBuilderAddNode();

    public static native int dockBuilderAddNode(int var0);

    public static native int dockBuilderAddNode(int var0, int var1);

    public static native void dockBuilderRemoveNode(int var0);

    public static native void dockBuilderRemoveNodeDockedWindows(int var0);

    public static native void dockBuilderRemoveNodeDockedWindows(int var0, boolean var1);

    public static native void dockBuilderRemoveNodeChildNodes(int var0);

    public static native void dockBuilderSetNodePos(int var0, float var1, float var2);

    public static native void dockBuilderSetNodeSize(int var0, float var1, float var2);

    public static int dockBuilderSplitNode(int nodeId, int splitDir, float sizeRatioForNodeAtDir, ImInt outIdAtDir, ImInt outIdAtOppositeDir) {
        if (outIdAtDir == null && outIdAtOppositeDir != null) {
            return ImGui.nDockBuilderSplitNode(nodeId, splitDir, sizeRatioForNodeAtDir, 0, outIdAtOppositeDir.getData());
        }
        if (outIdAtDir != null && outIdAtOppositeDir == null) {
            return ImGui.nDockBuilderSplitNode(nodeId, splitDir, sizeRatioForNodeAtDir, outIdAtDir.getData());
        }
        if (outIdAtDir != null) {
            return ImGui.nDockBuilderSplitNode(nodeId, splitDir, sizeRatioForNodeAtDir, outIdAtDir.getData(), outIdAtOppositeDir.getData());
        }
        return ImGui.nDockBuilderSplitNode(nodeId, splitDir, sizeRatioForNodeAtDir);
    }

    private static native int nDockBuilderSplitNode(int var0, int var1, float var2, int[] var3, int[] var4);

    private static native int nDockBuilderSplitNode(int var0, int var1, float var2);

    private static native int nDockBuilderSplitNode(int var0, int var1, float var2, int[] var3);

    private static native int nDockBuilderSplitNode(int var0, int var1, float var2, int var3, int[] var4);

    public static native void dockBuilderCopyWindowSettings(String var0, String var1);

    public static native void dockBuilderFinish(int var0);

    public static boolean splitterBehavior(float bbMinX, float bbMinY, float bbMaxX, float bbMaxY, int id, int imGuiAxis, ImFloat size1, ImFloat size2, float minSize1, float minSize2) {
        return ImGui.splitterBehavior(bbMinX, bbMinY, bbMaxX, bbMaxY, id, imGuiAxis, size1, size2, minSize1, minSize2, 0.0f, 0.0f, 0);
    }

    public static boolean splitterBehavior(float bbMinX, float bbMinY, float bbMaxX, float bbMaxY, int id, int imGuiAxis, ImFloat size1, ImFloat size2, float minSize1, float minSize2, float hoverExtend) {
        return ImGui.splitterBehavior(bbMinX, bbMinY, bbMaxX, bbMaxY, id, imGuiAxis, size1, size2, minSize1, minSize2, hoverExtend, 0.0f, 0);
    }

    public static boolean splitterBehavior(float bbMinX, float bbMinY, float bbMaxX, float bbMaxY, int id, int imGuiAxis, ImFloat size1, ImFloat size2, float minSize1, float minSize2, float hoverExtend, float hoverVisibilityDelay, int bgCol) {
        return ImGui.nSplitterBehaviour(bbMinX, bbMinY, bbMaxX, bbMaxY, id, imGuiAxis, size1.getData(), size2.getData(), minSize1, minSize2, hoverExtend, hoverVisibilityDelay, bgCol);
    }

    private static native boolean nSplitterBehaviour(float var0, float var1, float var2, float var3, int var4, int var5, float[] var6, float[] var7, float var8, float var9, float var10, float var11, int var12);

    public static ImGuiWindow getCurrentWindow() {
        ImGui.IMGUI_CURRENT_WINDOW.ptr = ImGui.nGetCurrentWindow();
        return IMGUI_CURRENT_WINDOW;
    }

    private static native long nGetCurrentWindow();

    public static ImRect getWindowScrollbarRect(ImGuiWindow imGuiWindow, int axis) {
        ImRect imRect = new ImRect();
        ImGui.nGetWindowScrollbarRect(imGuiWindow.ptr, axis, imRect.min, imRect.max);
        return imRect;
    }

    private static native void nGetWindowScrollbarRect(long var0, int var2, ImVec2 var3, ImVec2 var4);
}

