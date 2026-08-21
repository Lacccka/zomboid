/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.ImGuiViewport;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.binding.ImGuiStruct;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImDrawData
extends ImGuiStruct {
    public static final int SIZEOF_IM_DRAW_IDX = 2;
    public static final int SIZEOF_IM_DRAW_VERT = 20;
    private static final int RESIZE_FACTOR = 5000;
    private static ByteBuffer dataBuffer = ByteBuffer.allocateDirect(25000).order(ByteOrder.nativeOrder());
    private static final ImGuiViewport OWNER_VIEWPORT = new ImGuiViewport(0L);

    public ImDrawData(long ptr) {
        super(ptr);
    }

    public native int getCmdListCmdBufferSize(int var1);

    public native int getCmdListCmdBufferElemCount(int var1, int var2);

    public ImVec4 getCmdListCmdBufferClipRect(int cmdListIdx, int cmdBufferIdx) {
        ImVec4 value = new ImVec4();
        this.getCmdListCmdBufferClipRect(cmdListIdx, cmdBufferIdx, value);
        return value;
    }

    public native void getCmdListCmdBufferClipRect(int var1, int var2, ImVec4 var3);

    public native int getCmdListCmdBufferTextureId(int var1, int var2);

    public native int getCmdListCmdBufferVtxOffset(int var1, int var2);

    public native int getCmdListCmdBufferIdxOffset(int var1, int var2);

    public native int getCmdListIdxBufferSize(int var1);

    public ByteBuffer getCmdListIdxBufferData(int cmdListIdx) {
        int idxBufferCapacity = this.getCmdListIdxBufferSize(cmdListIdx) * ImDrawData.sizeOfImDrawIdx();
        if (dataBuffer.capacity() < idxBufferCapacity) {
            dataBuffer.clear();
            dataBuffer = ByteBuffer.allocateDirect(idxBufferCapacity + 5000).order(ByteOrder.nativeOrder());
        }
        this.nGetCmdListIdxBufferData(cmdListIdx, dataBuffer, idxBufferCapacity);
        dataBuffer.position(0);
        dataBuffer.limit(idxBufferCapacity);
        return dataBuffer;
    }

    private native void nGetCmdListIdxBufferData(int var1, ByteBuffer var2, int var3);

    public native int getCmdListVtxBufferSize(int var1);

    public ByteBuffer getCmdListVtxBufferData(int cmdListIdx) {
        int vtxBufferCapacity = this.getCmdListVtxBufferSize(cmdListIdx) * ImDrawData.sizeOfImDrawVert();
        if (dataBuffer.capacity() < vtxBufferCapacity) {
            dataBuffer.clear();
            dataBuffer = ByteBuffer.allocateDirect(vtxBufferCapacity + 5000).order(ByteOrder.nativeOrder());
        }
        this.nGetCmdListVtxBufferData(cmdListIdx, dataBuffer, vtxBufferCapacity);
        dataBuffer.position(0);
        dataBuffer.limit(vtxBufferCapacity);
        return dataBuffer;
    }

    private native void nGetCmdListVtxBufferData(int var1, ByteBuffer var2, int var3);

    public static native int sizeOfImDrawVert();

    public static native int sizeOfImDrawIdx();

    public native boolean getValid();

    public native int getCmdListsCount();

    public native int getTotalIdxCount();

    public native int getTotalVtxCount();

    public ImVec2 getDisplayPos() {
        ImVec2 value = new ImVec2();
        this.getDisplayPos(value);
        return value;
    }

    public native void getDisplayPos(ImVec2 var1);

    public native float getDisplayPosX();

    public native float getDisplayPosY();

    public ImVec2 getDisplaySize() {
        ImVec2 value = new ImVec2();
        this.getDisplaySize(value);
        return value;
    }

    public native void getDisplaySize(ImVec2 var1);

    public native float getDisplaySizeX();

    public native float getDisplaySizeY();

    public ImVec2 getFramebufferScale() {
        ImVec2 value = new ImVec2();
        this.getFramebufferScale(value);
        return value;
    }

    public native void getFramebufferScale(ImVec2 var1);

    public native float getFramebufferScaleX();

    public native float getFramebufferScaleY();

    public ImGuiViewport getOwnerViewport() {
        ImDrawData.OWNER_VIEWPORT.ptr = this.nGetOwnerViewport();
        return OWNER_VIEWPORT;
    }

    private native long nGetOwnerViewport();

    public native void deIndexAllBuffers();

    public native void scaleClipRects(float var1, float var2);
}

