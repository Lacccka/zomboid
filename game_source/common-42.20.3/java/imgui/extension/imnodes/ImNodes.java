/*
 * Decompiled with CFR 0.152.
 */
package imgui.extension.imnodes;

import imgui.ImVec2;
import imgui.extension.imnodes.ImNodesContext;
import imgui.extension.imnodes.ImNodesStyle;
import imgui.type.ImBoolean;
import imgui.type.ImInt;

public final class ImNodes {
    private static final ImNodesStyle STYLE = new ImNodesStyle(0L);

    private ImNodes() {
    }

    public static ImNodesContext editorContextCreate() {
        return new ImNodesContext();
    }

    public static void editorContextFree(ImNodesContext context) {
        context.destroy();
    }

    public static void editorContextSet(ImNodesContext context) {
        ImNodes.nEditorContextSet(context.ptr);
    }

    private static native void nEditorContextSet(long var0);

    public static native void createContext();

    public static native void destroyContext();

    public static ImNodesStyle getStyle() {
        ImNodes.STYLE.ptr = ImNodes.nGetStyle();
        return STYLE;
    }

    private static native long nGetStyle();

    public static native void styleColorsDark();

    public static native void styleColorsClassic();

    public static native void styleColorsLight();

    public static native void pushColorStyle(int var0, int var1);

    public static native void popColorStyle();

    public static native void pushStyleVar(int var0, float var1);

    public static native void pushStyleVar(int var0, float var1, float var2);

    public static native void popStyleVar();

    public static native void beginNodeEditor();

    public static native void endNodeEditor();

    public static native void beginNode(int var0);

    public static native void endNode();

    public static native void link(int var0, int var1, int var2);

    public static native void beginNodeTitleBar();

    public static native void endNodeTitleBar();

    public static native void beginStaticAttribute(int var0);

    public static native void endStaticAttribute();

    public static void beginInputAttribute(int id) {
        ImNodes.beginInputAttribute(id, 1);
    }

    public static native void beginInputAttribute(int var0, int var1);

    public static native void endInputAttribute();

    public static void beginOutputAttribute(int id) {
        ImNodes.beginOutputAttribute(id, 1);
    }

    public static native void beginOutputAttribute(int var0, int var1);

    public static native void pushAttributeFlag(int var0);

    public static native void endOutputAttribute();

    public static native boolean isEditorHovered();

    public static native int getHoveredNode();

    public static native int getHoveredLink();

    public static native int getHoveredPin();

    public static native int getActiveAttribute();

    public static native boolean isAttributeActive();

    public static boolean isLinkStarted(ImInt startAtAttributeId) {
        return ImNodes.nIsLinkStarted(startAtAttributeId.getData());
    }

    private static native boolean nIsLinkStarted(int[] var0);

    public static boolean isLinkDropped(ImInt startedAtAttributeId, boolean includingDetachedLinks) {
        return ImNodes.nIsLinkDropped(startedAtAttributeId.getData(), includingDetachedLinks);
    }

    private static native boolean nIsLinkDropped(int[] var0, boolean var1);

    public static boolean isLinkCreated(ImInt startedAtAttributeId, ImInt endedAtAttributeId) {
        return ImNodes.nIsLinkCreated(startedAtAttributeId.getData(), endedAtAttributeId.getData());
    }

    private static native boolean nIsLinkCreated(int[] var0, int[] var1);

    public static boolean isLinkCreated(ImInt startedAtNodeId, ImInt startedAtAttributeId, ImInt endedAtNodeId, ImInt endedAtAttributeId, ImBoolean createdFromSnap) {
        return ImNodes.nIsLinkCreated(startedAtNodeId.getData(), startedAtAttributeId.getData(), endedAtNodeId.getData(), endedAtAttributeId.getData(), createdFromSnap.getData());
    }

    private static native boolean nIsLinkCreated(int[] var0, int[] var1, int[] var2, int[] var3, boolean[] var4);

    public static boolean isLinkDestroyed(ImInt linkId) {
        return ImNodes.nIsLinkDestroyed(linkId.getData());
    }

    private static native boolean nIsLinkDestroyed(int[] var0);

    public static native int numSelectedNodes();

    public static native int numSelectedLinks();

    public static native void getSelectedNodes(int[] var0);

    public static native void getSelectedLinks(int[] var0);

    public static native void clearNodeSelection();

    public static native void clearNodeSelection(int var0);

    public static native void clearLinkSelection();

    public static native void clearLinkSelection(int var0);

    public static native void selectNode(int var0);

    public static native void selectLink(int var0);

    public static native boolean isNodeSelected(int var0);

    public static native boolean isLinkSelected(int var0);

    public static native void setNodeDraggable(int var0, boolean var1);

    public static native void getNodeDimensions(int var0, ImVec2 var1);

    public static native float getNodeDimensionsX(int var0);

    public static native float getNodeDimensionsY(int var0);

    public static native void setNodeScreenSpacePos(int var0, float var1, float var2);

    public static native void setNodeEditorSpacePos(int var0, float var1, float var2);

    public static native void setNodeGridSpacePos(int var0, float var1, float var2);

    public static native void getNodeScreenSpacePos(int var0, ImVec2 var1);

    public static native float getNodeScreenSpacePosX(int var0);

    public static native float getNodeScreenSpacePosY(int var0);

    public static native void getNodeEditorSpacePos(int var0, ImVec2 var1);

    public static native float getNodeEditorSpacePosX(int var0);

    public static native float getNodeEditorSpacePosY(int var0);

    public static native void getNodeGridSpacePos(int var0, ImVec2 var1);

    public static native float getNodeGridSpacePosX(int var0);

    public static native float getNodeGridSpacePosY(int var0);

    public static native void editorResetPanning(float var0, float var1);

    public static native void editorContextGetPanning(ImVec2 var0);

    public static native void editorMoveToNode(int var0);

    public static native String saveCurrentEditorStateToIniString();

    public static String saveEditorStateToIniString(ImNodesContext context) {
        return ImNodes.nSaveEditorStateToIniString(context.ptr);
    }

    private static native String nSaveEditorStateToIniString(long var0);

    public static native void loadCurrentEditorStateFromIniString(String var0, int var1);

    public static void loadEditorStateFromIniString(ImNodesContext context, String data, int dataSize) {
        ImNodes.nLoadEditorStateFromIniString(context.ptr, data, dataSize);
    }

    private static native void nLoadEditorStateFromIniString(long var0, String var2, int var3);

    public static native void saveCurrentEditorStateToIniFile(String var0);

    public static void saveEditorStateToIniFile(ImNodesContext context, String fileName) {
        ImNodes.nSaveEditorStateToIniFile(context.ptr, fileName);
    }

    private static native void nSaveEditorStateToIniFile(long var0, String var2);

    public static native void loadCurrentEditorStateFromIniFile(String var0);

    public static void loadEditorStateFromIniFile(ImNodesContext context, String fileName) {
        ImNodes.nLoadEditorStateFromIniFile(context.ptr, fileName);
    }

    private static native void nLoadEditorStateFromIniFile(long var0, String var2);

    public static native void miniMap(float var0, int var1);
}

