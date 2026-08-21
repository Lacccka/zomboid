/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.ImDrawList;
import imgui.ImFontGlyph;
import imgui.ImVec2;
import imgui.binding.ImGuiStructDestroyable;

public final class ImFont
extends ImGuiStructDestroyable {
    private final ImFontGlyph fallbackGlyph = new ImFontGlyph(0L);
    private final ImFontGlyph foundGlyph = new ImFontGlyph(0L);

    public ImFont() {
    }

    public ImFont(long ptr) {
        super(ptr);
    }

    @Override
    protected long create() {
        return this.nCreate();
    }

    private native long nCreate();

    public native float getFallbackAdvanceX();

    public native void setFallbackAdvanceX(float var1);

    public native float getFontSize();

    public ImFontGlyph getFallbackGlyph() {
        this.fallbackGlyph.ptr = this.nGetFallbackGlyphPtr();
        return this.fallbackGlyph;
    }

    private native long nGetFallbackGlyphPtr();

    public native short getConfigDataCount();

    public native short getFallbackChar();

    public native short getEllipsisChar();

    public native void setEllipsisChar(int var1);

    public native short getDotChar();

    public native void setDotChar(int var1);

    public native boolean getDirtyLookupTables();

    public native void setDirtyLookupTables(boolean var1);

    public native float getScale();

    public native void setScale(float var1);

    public native float getAscent();

    public native void setAscent(float var1);

    public native float getDescent();

    public native void setDescent(float var1);

    public native int getMetricsTotalSurface();

    public native void setMetricsTotalSurface(int var1);

    public ImFontGlyph findGlyph(int c) {
        this.foundGlyph.ptr = this.nFindGlyph(c);
        return this.foundGlyph;
    }

    private native long nFindGlyph(int var1);

    public ImFontGlyph findGlyphNoFallback(int c) {
        this.foundGlyph.ptr = this.nFindGlyphNoFallback(c);
        return this.foundGlyph;
    }

    private native long nFindGlyphNoFallback(int var1);

    public native float getCharAdvance(int var1);

    public native boolean isLoaded();

    public native String getDebugName();

    public ImVec2 calcTextSizeA(float size, float maxWidth, float wrapWidth, String text) {
        ImVec2 value = new ImVec2();
        this.calcTextSizeA(value, size, maxWidth, wrapWidth, text);
        return value;
    }

    public native void calcTextSizeA(ImVec2 var1, float var2, float var3, float var4, String var5);

    public native float calcTextSizeAX(float var1, float var2, float var3, String var4);

    public native float calcTextSizeAY(float var1, float var2, float var3, String var4);

    public native String calcWordWrapPositionA(float var1, String var2, String var3, float var4);

    public void renderChar(ImDrawList drawList, float size, float posX, float posY, int col, int c) {
        this.nRenderChar(drawList.ptr, size, posX, posY, col, c);
    }

    private native void nRenderChar(long var1, float var3, float var4, float var5, int var6, int var7);

    public void renderText(ImDrawList drawList, float size, float posX, float posY, int col, float clipRectX, float clipRectY, float clipRectW, float clipRectZ, String text, String textEnd) {
        this.nRenderText(drawList.ptr, size, posX, posY, col, clipRectX, clipRectY, clipRectW, clipRectZ, text, textEnd, 0.0f, false);
    }

    public void renderText(ImDrawList drawList, float size, float posX, float posY, int col, float clipRectX, float clipRectY, float clipRectW, float clipRectZ, String text, String textEnd, float wrapWidth) {
        this.nRenderText(drawList.ptr, size, posX, posY, col, clipRectX, clipRectY, clipRectW, clipRectZ, text, textEnd, wrapWidth, false);
    }

    public void renderText(ImDrawList drawList, float size, float posX, float posY, int col, float clipRectX, float clipRectY, float clipRectW, float clipRectZ, String text, String textEnd, float wrapWidth, boolean cpuFineClip) {
        this.nRenderText(drawList.ptr, size, posX, posY, col, clipRectX, clipRectY, clipRectW, clipRectZ, text, textEnd, wrapWidth, cpuFineClip);
    }

    private native void nRenderText(long var1, float var3, float var4, float var5, int var6, float var7, float var8, float var9, float var10, String var11, String var12, float var13, boolean var14);
}

