/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import imgui.ImFont;
import imgui.ImFontConfig;
import imgui.binding.ImGuiStructDestroyable;
import imgui.type.ImInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImFontAtlas
extends ImGuiStructDestroyable {
    private ByteBuffer alpha8pixels = null;
    private ByteBuffer rgba32pixels = null;

    public ImFontAtlas() {
    }

    public ImFontAtlas(long ptr) {
        super(ptr);
    }

    static native void nInit();

    @Override
    protected long create() {
        return this.nCreate();
    }

    private native long nCreate();

    public ImFont addFont(ImFontConfig imFontConfig) {
        return new ImFont(this.nAddFont(imFontConfig.ptr));
    }

    private native long nAddFont(long var1);

    public ImFont addFontDefault() {
        return new ImFont(this.nAddFontDefault());
    }

    private native long nAddFontDefault();

    public ImFont addFontDefault(ImFontConfig imFontConfig) {
        return new ImFont(this.nAddFontDefault(imFontConfig.ptr));
    }

    private native long nAddFontDefault(long var1);

    public ImFont addFontFromFileTTF(String filename, float sizePixels) {
        return new ImFont(this.nAddFontFromFileTTF(filename, sizePixels));
    }

    private native long nAddFontFromFileTTF(String var1, float var2);

    public ImFont addFontFromFileTTF(String filename, float sizePixels, ImFontConfig imFontConfig) {
        return new ImFont(this.nAddFontFromFileTTF(filename, sizePixels, imFontConfig.ptr));
    }

    private native long nAddFontFromFileTTF(String var1, float var2, long var3);

    public ImFont addFontFromFileTTF(String filename, float sizePixels, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromFileTTF(filename, sizePixels, glyphRanges));
    }

    private native long nAddFontFromFileTTF(String var1, float var2, short[] var3);

    public ImFont addFontFromFileTTF(String filename, float sizePixels, ImFontConfig imFontConfig, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromFileTTF(filename, sizePixels, imFontConfig.ptr, glyphRanges));
    }

    private native long nAddFontFromFileTTF(String var1, float var2, long var3, short[] var5);

    public ImFont addFontFromMemoryTTF(byte[] fontData, float sizePixels) {
        return new ImFont(this.nAddFontFromMemoryTTF(fontData, fontData.length, sizePixels));
    }

    private native long nAddFontFromMemoryTTF(byte[] var1, int var2, float var3);

    public ImFont addFontFromMemoryTTF(byte[] fontData, float sizePixels, ImFontConfig imFontConfig) {
        return new ImFont(this.nAddFontFromMemoryTTF(fontData, fontData.length, sizePixels, imFontConfig.ptr));
    }

    private native long nAddFontFromMemoryTTF(byte[] var1, int var2, float var3, long var4);

    public ImFont addFontFromMemoryTTF(byte[] fontData, float sizePixels, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryTTF(fontData, fontData.length, sizePixels, glyphRanges));
    }

    private native long nAddFontFromMemoryTTF(byte[] var1, int var2, float var3, short[] var4);

    public ImFont addFontFromMemoryTTF(byte[] fontData, float sizePixels, ImFontConfig imFontConfig, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryTTF(fontData, fontData.length, sizePixels, imFontConfig.ptr, glyphRanges));
    }

    private native long nAddFontFromMemoryTTF(byte[] var1, int var2, float var3, long var4, short[] var6);

    public ImFont addFontFromMemoryCompressedTTF(byte[] compressedFontData, float sizePixels) {
        return new ImFont(this.nAddFontFromMemoryCompressedTTF(compressedFontData, compressedFontData.length, sizePixels));
    }

    private native long nAddFontFromMemoryCompressedTTF(byte[] var1, int var2, float var3);

    public ImFont addFontFromMemoryCompressedTTF(byte[] compressedFontData, float sizePixels, ImFontConfig imFontConfig) {
        return new ImFont(this.nAddFontFromMemoryCompressedTTF(compressedFontData, compressedFontData.length, sizePixels, imFontConfig.ptr));
    }

    private native long nAddFontFromMemoryCompressedTTF(byte[] var1, int var2, float var3, long var4);

    public ImFont addFontFromMemoryCompressedTTF(byte[] compressedFontData, float sizePixels, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryCompressedTTF(compressedFontData, compressedFontData.length, sizePixels, glyphRanges));
    }

    private native long nAddFontFromMemoryCompressedTTF(byte[] var1, int var2, float var3, short[] var4);

    public ImFont addFontFromMemoryCompressedTTF(byte[] compressedFontData, float sizePixels, ImFontConfig imFontConfig, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryCompressedTTF(compressedFontData, compressedFontData.length, sizePixels, imFontConfig.ptr, glyphRanges));
    }

    private native long nAddFontFromMemoryCompressedTTF(byte[] var1, int var2, float var3, long var4, short[] var6);

    public ImFont addFontFromMemoryCompressedBase85TTF(String compressedFontDataBase85, float sizePixels) {
        return new ImFont(this.nAddFontFromMemoryCompressedBase85TTF(compressedFontDataBase85, sizePixels));
    }

    private native long nAddFontFromMemoryCompressedBase85TTF(String var1, float var2);

    public ImFont addFontFromMemoryCompressedBase85TTF(String compressedFontDataBase85, float sizePixels, ImFontConfig imFontConfig) {
        return new ImFont(this.nAddFontFromMemoryCompressedBase85TTF(compressedFontDataBase85, sizePixels, imFontConfig.ptr));
    }

    private native long nAddFontFromMemoryCompressedBase85TTF(String var1, float var2, long var3);

    public ImFont addFontFromMemoryCompressedBase85TTF(String compressedFontDataBase85, float sizePixels, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryCompressedBase85TTF(compressedFontDataBase85, sizePixels, glyphRanges));
    }

    private native long nAddFontFromMemoryCompressedBase85TTF(String var1, float var2, short[] var3);

    public ImFont addFontFromMemoryCompressedBase85TTF(String compressedFontDataBase85, float sizePixels, ImFontConfig imFontConfig, short[] glyphRanges) {
        return new ImFont(this.nAddFontFromMemoryCompressedBase85TTF(compressedFontDataBase85, sizePixels, imFontConfig.ptr, glyphRanges));
    }

    private native long nAddFontFromMemoryCompressedBase85TTF(String var1, float var2, long var3, short[] var5);

    public native void clearInputData();

    public native void clearTexData();

    public native void clearFonts();

    public native void clear();

    public native boolean build();

    public ByteBuffer getTexDataAsAlpha8(ImInt outWidth, ImInt outHeight) {
        return this.getTexDataAsAlpha8(outWidth, outHeight, new ImInt());
    }

    public ByteBuffer getTexDataAsAlpha8(ImInt outWidth, ImInt outHeight, ImInt outBytesPerPixel) {
        this.getTexDataAsAlpha8(outWidth.getData(), outHeight.getData(), outBytesPerPixel.getData());
        return this.alpha8pixels;
    }

    private ByteBuffer createAlpha8Pixels(int size) {
        if (this.alpha8pixels == null || this.alpha8pixels.limit() != size) {
            this.alpha8pixels = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        } else {
            this.alpha8pixels.clear();
        }
        return this.alpha8pixels;
    }

    private native void getTexDataAsAlpha8(int[] var1, int[] var2, int[] var3);

    public ByteBuffer getTexDataAsRGBA32(ImInt outWidth, ImInt outHeight) {
        return this.getTexDataAsRGBA32(outWidth, outHeight, new ImInt());
    }

    public ByteBuffer getTexDataAsRGBA32(ImInt outWidth, ImInt outHeight, ImInt outBytesPerPixel) {
        this.nGetTexDataAsRGBA32(outWidth.getData(), outHeight.getData(), outBytesPerPixel.getData());
        return this.rgba32pixels;
    }

    private ByteBuffer createRgba32Pixels(int size) {
        if (this.rgba32pixels == null || this.rgba32pixels.limit() != size) {
            this.rgba32pixels = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        } else {
            this.rgba32pixels.clear();
        }
        return this.rgba32pixels;
    }

    private native void nGetTexDataAsRGBA32(int[] var1, int[] var2, int[] var3);

    public native boolean isBuilt();

    public native void setTexID(int var1);

    public native short[] getGlyphRangesDefault();

    public native short[] getGlyphRangesKorean();

    public native short[] getGlyphRangesJapanese();

    public native short[] getGlyphRangesChineseFull();

    public native short[] getGlyphRangesChineseSimplifiedCommon();

    public native short[] getGlyphRangesCyrillic();

    public native short[] getGlyphRangesThai();

    public native short[] getGlyphRangesVietnamese();

    public native int addCustomRectRegular(int var1, int var2);

    public int addCustomRectFontGlyph(ImFont imFont, short id, int width, int height, float advanceX) {
        return this.nAddCustomRectFontGlyph(imFont.ptr, id, width, height, advanceX);
    }

    private native int nAddCustomRectFontGlyph(long var1, short var3, int var4, int var5, float var6);

    public int addCustomRectFontGlyph(ImFont imFont, short id, int width, int height, float advanceX, float offsetX, float offsetY) {
        return this.nAddCustomRectFontGlyph(imFont.ptr, id, width, height, advanceX, offsetX, offsetY);
    }

    private native int nAddCustomRectFontGlyph(long var1, short var3, int var4, int var5, float var6, float var7, float var8);

    public native boolean getLocked();

    public native void setLocked(boolean var1);

    public native int getFlags();

    public native void setFlags(int var1);

    public void addFlags(int flags) {
        this.setFlags(this.getFlags() | flags);
    }

    public void removeFlags(int flags) {
        this.setFlags(this.getFlags() & ~flags);
    }

    public boolean hasFlags(int flags) {
        return (this.getFlags() & flags) != 0;
    }

    public native int getTexID();

    public native int getTexDesiredWidth();

    public native void setTexDesiredWidth(int var1);

    public native int getTexGlyphPadding();

    public native void setTexGlyphPadding(int var1);
}

