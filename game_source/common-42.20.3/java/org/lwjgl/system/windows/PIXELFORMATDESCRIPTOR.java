/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.windows;

import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

public class PIXELFORMATDESCRIPTOR
extends Struct<PIXELFORMATDESCRIPTOR>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int NSIZE;
    public static final int NVERSION;
    public static final int DWFLAGS;
    public static final int IPIXELTYPE;
    public static final int CCOLORBITS;
    public static final int CREDBITS;
    public static final int CREDSHIFT;
    public static final int CGREENBITS;
    public static final int CGREENSHIFT;
    public static final int CBLUEBITS;
    public static final int CBLUESHIFT;
    public static final int CALPHABITS;
    public static final int CALPHASHIFT;
    public static final int CACCUMBITS;
    public static final int CACCUMREDBITS;
    public static final int CACCUMGREENBITS;
    public static final int CACCUMBLUEBITS;
    public static final int CACCUMALPHABITS;
    public static final int CDEPTHBITS;
    public static final int CSTENCILBITS;
    public static final int CAUXBUFFERS;
    public static final int ILAYERTYPE;
    public static final int BRESERVED;
    public static final int DWLAYERMASK;
    public static final int DWVISIBLEMASK;
    public static final int DWDAMAGEMASK;

    protected PIXELFORMATDESCRIPTOR(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected PIXELFORMATDESCRIPTOR create(long address, @Nullable ByteBuffer container) {
        return new PIXELFORMATDESCRIPTOR(address, container);
    }

    public PIXELFORMATDESCRIPTOR(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), PIXELFORMATDESCRIPTOR.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="WORD")
    public short nSize() {
        return PIXELFORMATDESCRIPTOR.nnSize(this.address());
    }

    @NativeType(value="WORD")
    public short nVersion() {
        return PIXELFORMATDESCRIPTOR.nnVersion(this.address());
    }

    @NativeType(value="DWORD")
    public int dwFlags() {
        return PIXELFORMATDESCRIPTOR.ndwFlags(this.address());
    }

    @NativeType(value="BYTE")
    public byte iPixelType() {
        return PIXELFORMATDESCRIPTOR.niPixelType(this.address());
    }

    @NativeType(value="BYTE")
    public byte cColorBits() {
        return PIXELFORMATDESCRIPTOR.ncColorBits(this.address());
    }

    @NativeType(value="BYTE")
    public byte cRedBits() {
        return PIXELFORMATDESCRIPTOR.ncRedBits(this.address());
    }

    @NativeType(value="BYTE")
    public byte cRedShift() {
        return PIXELFORMATDESCRIPTOR.ncRedShift(this.address());
    }

    @NativeType(value="BYTE")
    public byte cGreenBits() {
        return PIXELFORMATDESCRIPTOR.ncGreenBits(this.address());
    }

    @NativeType(value="BYTE")
    public byte cGreenShift() {
        return PIXELFORMATDESCRIPTOR.ncGreenShift(this.address());
    }

    @NativeType(value="BYTE")
    public byte cBlueBits() {
        return PIXELFORMATDESCRIPTOR.ncBlueBits(this.address());
    }

    @NativeType(value="BYTE")
    public byte cBlueShift() {
        return PIXELFORMATDESCRIPTOR.ncBlueShift(this.address());
    }

    @NativeType(value="BYTE")
    public byte cAlphaBits() {
        return PIXELFORMATDESCRIPTOR.ncAlphaBits(this.address());
    }

    @NativeType(value="BYTE")
    public byte cAlphaShift() {
        return PIXELFORMATDESCRIPTOR.ncAlphaShift(this.address());
    }

    @NativeType(value="BYTE")
    public byte cAccumBits() {
        return PIXELFORMATDESCRIPTOR.ncAccumBits(this.address());
    }

    @NativeType(value="BYTE")
    public byte cAccumRedBits() {
        return PIXELFORMATDESCRIPTOR.ncAccumRedBits(this.address());
    }

    @NativeType(value="BYTE")
    public byte cAccumGreenBits() {
        return PIXELFORMATDESCRIPTOR.ncAccumGreenBits(this.address());
    }

    @NativeType(value="BYTE")
    public byte cAccumBlueBits() {
        return PIXELFORMATDESCRIPTOR.ncAccumBlueBits(this.address());
    }

    @NativeType(value="BYTE")
    public byte cAccumAlphaBits() {
        return PIXELFORMATDESCRIPTOR.ncAccumAlphaBits(this.address());
    }

    @NativeType(value="BYTE")
    public byte cDepthBits() {
        return PIXELFORMATDESCRIPTOR.ncDepthBits(this.address());
    }

    @NativeType(value="BYTE")
    public byte cStencilBits() {
        return PIXELFORMATDESCRIPTOR.ncStencilBits(this.address());
    }

    @NativeType(value="BYTE")
    public byte cAuxBuffers() {
        return PIXELFORMATDESCRIPTOR.ncAuxBuffers(this.address());
    }

    @NativeType(value="BYTE")
    public byte iLayerType() {
        return PIXELFORMATDESCRIPTOR.niLayerType(this.address());
    }

    @NativeType(value="BYTE")
    public byte bReserved() {
        return PIXELFORMATDESCRIPTOR.nbReserved(this.address());
    }

    @NativeType(value="DWORD")
    public int dwLayerMask() {
        return PIXELFORMATDESCRIPTOR.ndwLayerMask(this.address());
    }

    @NativeType(value="DWORD")
    public int dwVisibleMask() {
        return PIXELFORMATDESCRIPTOR.ndwVisibleMask(this.address());
    }

    @NativeType(value="DWORD")
    public int dwDamageMask() {
        return PIXELFORMATDESCRIPTOR.ndwDamageMask(this.address());
    }

    public PIXELFORMATDESCRIPTOR nSize(@NativeType(value="WORD") short value) {
        PIXELFORMATDESCRIPTOR.nnSize(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR nVersion(@NativeType(value="WORD") short value) {
        PIXELFORMATDESCRIPTOR.nnVersion(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR dwFlags(@NativeType(value="DWORD") int value) {
        PIXELFORMATDESCRIPTOR.ndwFlags(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR iPixelType(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.niPixelType(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cColorBits(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncColorBits(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cRedBits(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncRedBits(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cRedShift(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncRedShift(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cGreenBits(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncGreenBits(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cGreenShift(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncGreenShift(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cBlueBits(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncBlueBits(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cBlueShift(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncBlueShift(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cAlphaBits(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncAlphaBits(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cAlphaShift(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncAlphaShift(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cAccumBits(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncAccumBits(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cAccumRedBits(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncAccumRedBits(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cAccumGreenBits(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncAccumGreenBits(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cAccumBlueBits(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncAccumBlueBits(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cAccumAlphaBits(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncAccumAlphaBits(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cDepthBits(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncDepthBits(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cStencilBits(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncStencilBits(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR cAuxBuffers(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.ncAuxBuffers(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR iLayerType(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.niLayerType(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR bReserved(@NativeType(value="BYTE") byte value) {
        PIXELFORMATDESCRIPTOR.nbReserved(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR dwLayerMask(@NativeType(value="DWORD") int value) {
        PIXELFORMATDESCRIPTOR.ndwLayerMask(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR dwVisibleMask(@NativeType(value="DWORD") int value) {
        PIXELFORMATDESCRIPTOR.ndwVisibleMask(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR dwDamageMask(@NativeType(value="DWORD") int value) {
        PIXELFORMATDESCRIPTOR.ndwDamageMask(this.address(), value);
        return this;
    }

    public PIXELFORMATDESCRIPTOR set(short nSize, short nVersion, int dwFlags, byte iPixelType, byte cColorBits, byte cRedBits, byte cRedShift, byte cGreenBits, byte cGreenShift, byte cBlueBits, byte cBlueShift, byte cAlphaBits, byte cAlphaShift, byte cAccumBits, byte cAccumRedBits, byte cAccumGreenBits, byte cAccumBlueBits, byte cAccumAlphaBits, byte cDepthBits, byte cStencilBits, byte cAuxBuffers, byte iLayerType, byte bReserved, int dwLayerMask, int dwVisibleMask, int dwDamageMask) {
        this.nSize(nSize);
        this.nVersion(nVersion);
        this.dwFlags(dwFlags);
        this.iPixelType(iPixelType);
        this.cColorBits(cColorBits);
        this.cRedBits(cRedBits);
        this.cRedShift(cRedShift);
        this.cGreenBits(cGreenBits);
        this.cGreenShift(cGreenShift);
        this.cBlueBits(cBlueBits);
        this.cBlueShift(cBlueShift);
        this.cAlphaBits(cAlphaBits);
        this.cAlphaShift(cAlphaShift);
        this.cAccumBits(cAccumBits);
        this.cAccumRedBits(cAccumRedBits);
        this.cAccumGreenBits(cAccumGreenBits);
        this.cAccumBlueBits(cAccumBlueBits);
        this.cAccumAlphaBits(cAccumAlphaBits);
        this.cDepthBits(cDepthBits);
        this.cStencilBits(cStencilBits);
        this.cAuxBuffers(cAuxBuffers);
        this.iLayerType(iLayerType);
        this.bReserved(bReserved);
        this.dwLayerMask(dwLayerMask);
        this.dwVisibleMask(dwVisibleMask);
        this.dwDamageMask(dwDamageMask);
        return this;
    }

    public PIXELFORMATDESCRIPTOR set(PIXELFORMATDESCRIPTOR src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static PIXELFORMATDESCRIPTOR malloc() {
        return new PIXELFORMATDESCRIPTOR(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static PIXELFORMATDESCRIPTOR calloc() {
        return new PIXELFORMATDESCRIPTOR(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static PIXELFORMATDESCRIPTOR create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new PIXELFORMATDESCRIPTOR(MemoryUtil.memAddress(container), container);
    }

    public static PIXELFORMATDESCRIPTOR create(long address) {
        return new PIXELFORMATDESCRIPTOR(address, null);
    }

    public static @Nullable PIXELFORMATDESCRIPTOR createSafe(long address) {
        return address == 0L ? null : new PIXELFORMATDESCRIPTOR(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(PIXELFORMATDESCRIPTOR.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = PIXELFORMATDESCRIPTOR.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static PIXELFORMATDESCRIPTOR malloc(MemoryStack stack) {
        return new PIXELFORMATDESCRIPTOR(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static PIXELFORMATDESCRIPTOR calloc(MemoryStack stack) {
        return new PIXELFORMATDESCRIPTOR(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static short nnSize(long struct) {
        return MemoryUtil.memGetShort(struct + (long)NSIZE);
    }

    public static short nnVersion(long struct) {
        return MemoryUtil.memGetShort(struct + (long)NVERSION);
    }

    public static int ndwFlags(long struct) {
        return MemoryUtil.memGetInt(struct + (long)DWFLAGS);
    }

    public static byte niPixelType(long struct) {
        return MemoryUtil.memGetByte(struct + (long)IPIXELTYPE);
    }

    public static byte ncColorBits(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CCOLORBITS);
    }

    public static byte ncRedBits(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CREDBITS);
    }

    public static byte ncRedShift(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CREDSHIFT);
    }

    public static byte ncGreenBits(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CGREENBITS);
    }

    public static byte ncGreenShift(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CGREENSHIFT);
    }

    public static byte ncBlueBits(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CBLUEBITS);
    }

    public static byte ncBlueShift(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CBLUESHIFT);
    }

    public static byte ncAlphaBits(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CALPHABITS);
    }

    public static byte ncAlphaShift(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CALPHASHIFT);
    }

    public static byte ncAccumBits(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CACCUMBITS);
    }

    public static byte ncAccumRedBits(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CACCUMREDBITS);
    }

    public static byte ncAccumGreenBits(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CACCUMGREENBITS);
    }

    public static byte ncAccumBlueBits(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CACCUMBLUEBITS);
    }

    public static byte ncAccumAlphaBits(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CACCUMALPHABITS);
    }

    public static byte ncDepthBits(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CDEPTHBITS);
    }

    public static byte ncStencilBits(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CSTENCILBITS);
    }

    public static byte ncAuxBuffers(long struct) {
        return MemoryUtil.memGetByte(struct + (long)CAUXBUFFERS);
    }

    public static byte niLayerType(long struct) {
        return MemoryUtil.memGetByte(struct + (long)ILAYERTYPE);
    }

    public static byte nbReserved(long struct) {
        return MemoryUtil.memGetByte(struct + (long)BRESERVED);
    }

    public static int ndwLayerMask(long struct) {
        return MemoryUtil.memGetInt(struct + (long)DWLAYERMASK);
    }

    public static int ndwVisibleMask(long struct) {
        return MemoryUtil.memGetInt(struct + (long)DWVISIBLEMASK);
    }

    public static int ndwDamageMask(long struct) {
        return MemoryUtil.memGetInt(struct + (long)DWDAMAGEMASK);
    }

    public static void nnSize(long struct, short value) {
        MemoryUtil.memPutShort(struct + (long)NSIZE, value);
    }

    public static void nnVersion(long struct, short value) {
        MemoryUtil.memPutShort(struct + (long)NVERSION, value);
    }

    public static void ndwFlags(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)DWFLAGS, value);
    }

    public static void niPixelType(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)IPIXELTYPE, value);
    }

    public static void ncColorBits(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CCOLORBITS, value);
    }

    public static void ncRedBits(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CREDBITS, value);
    }

    public static void ncRedShift(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CREDSHIFT, value);
    }

    public static void ncGreenBits(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CGREENBITS, value);
    }

    public static void ncGreenShift(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CGREENSHIFT, value);
    }

    public static void ncBlueBits(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CBLUEBITS, value);
    }

    public static void ncBlueShift(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CBLUESHIFT, value);
    }

    public static void ncAlphaBits(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CALPHABITS, value);
    }

    public static void ncAlphaShift(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CALPHASHIFT, value);
    }

    public static void ncAccumBits(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CACCUMBITS, value);
    }

    public static void ncAccumRedBits(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CACCUMREDBITS, value);
    }

    public static void ncAccumGreenBits(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CACCUMGREENBITS, value);
    }

    public static void ncAccumBlueBits(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CACCUMBLUEBITS, value);
    }

    public static void ncAccumAlphaBits(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CACCUMALPHABITS, value);
    }

    public static void ncDepthBits(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CDEPTHBITS, value);
    }

    public static void ncStencilBits(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CSTENCILBITS, value);
    }

    public static void ncAuxBuffers(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)CAUXBUFFERS, value);
    }

    public static void niLayerType(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)ILAYERTYPE, value);
    }

    public static void nbReserved(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)BRESERVED, value);
    }

    public static void ndwLayerMask(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)DWLAYERMASK, value);
    }

    public static void ndwVisibleMask(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)DWVISIBLEMASK, value);
    }

    public static void ndwDamageMask(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)DWDAMAGEMASK, value);
    }

    static {
        Struct.Layout layout = PIXELFORMATDESCRIPTOR.__struct(PIXELFORMATDESCRIPTOR.__member(2), PIXELFORMATDESCRIPTOR.__member(2), PIXELFORMATDESCRIPTOR.__member(4), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(1), PIXELFORMATDESCRIPTOR.__member(4), PIXELFORMATDESCRIPTOR.__member(4), PIXELFORMATDESCRIPTOR.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        NSIZE = layout.offsetof(0);
        NVERSION = layout.offsetof(1);
        DWFLAGS = layout.offsetof(2);
        IPIXELTYPE = layout.offsetof(3);
        CCOLORBITS = layout.offsetof(4);
        CREDBITS = layout.offsetof(5);
        CREDSHIFT = layout.offsetof(6);
        CGREENBITS = layout.offsetof(7);
        CGREENSHIFT = layout.offsetof(8);
        CBLUEBITS = layout.offsetof(9);
        CBLUESHIFT = layout.offsetof(10);
        CALPHABITS = layout.offsetof(11);
        CALPHASHIFT = layout.offsetof(12);
        CACCUMBITS = layout.offsetof(13);
        CACCUMREDBITS = layout.offsetof(14);
        CACCUMGREENBITS = layout.offsetof(15);
        CACCUMBLUEBITS = layout.offsetof(16);
        CACCUMALPHABITS = layout.offsetof(17);
        CDEPTHBITS = layout.offsetof(18);
        CSTENCILBITS = layout.offsetof(19);
        CAUXBUFFERS = layout.offsetof(20);
        ILAYERTYPE = layout.offsetof(21);
        BRESERVED = layout.offsetof(22);
        DWLAYERMASK = layout.offsetof(23);
        DWVISIBLEMASK = layout.offsetof(24);
        DWDAMAGEMASK = layout.offsetof(25);
    }

    public static class Buffer
    extends StructBuffer<PIXELFORMATDESCRIPTOR, Buffer>
    implements NativeResource {
        private static final PIXELFORMATDESCRIPTOR ELEMENT_FACTORY = PIXELFORMATDESCRIPTOR.create(-1L);

        public Buffer(ByteBuffer container) {
            super(container, container.remaining() / SIZEOF);
        }

        public Buffer(long address, int cap) {
            super(address, null, -1, 0, cap, cap);
        }

        Buffer(long address, @Nullable ByteBuffer container, int mark, int pos, int lim, int cap) {
            super(address, container, mark, pos, lim, cap);
        }

        @Override
        protected Buffer self() {
            return this;
        }

        @Override
        protected Buffer create(long address, @Nullable ByteBuffer container, int mark, int position, int limit, int capacity) {
            return new Buffer(address, container, mark, position, limit, capacity);
        }

        @Override
        protected PIXELFORMATDESCRIPTOR getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="WORD")
        public short nSize() {
            return PIXELFORMATDESCRIPTOR.nnSize(this.address());
        }

        @NativeType(value="WORD")
        public short nVersion() {
            return PIXELFORMATDESCRIPTOR.nnVersion(this.address());
        }

        @NativeType(value="DWORD")
        public int dwFlags() {
            return PIXELFORMATDESCRIPTOR.ndwFlags(this.address());
        }

        @NativeType(value="BYTE")
        public byte iPixelType() {
            return PIXELFORMATDESCRIPTOR.niPixelType(this.address());
        }

        @NativeType(value="BYTE")
        public byte cColorBits() {
            return PIXELFORMATDESCRIPTOR.ncColorBits(this.address());
        }

        @NativeType(value="BYTE")
        public byte cRedBits() {
            return PIXELFORMATDESCRIPTOR.ncRedBits(this.address());
        }

        @NativeType(value="BYTE")
        public byte cRedShift() {
            return PIXELFORMATDESCRIPTOR.ncRedShift(this.address());
        }

        @NativeType(value="BYTE")
        public byte cGreenBits() {
            return PIXELFORMATDESCRIPTOR.ncGreenBits(this.address());
        }

        @NativeType(value="BYTE")
        public byte cGreenShift() {
            return PIXELFORMATDESCRIPTOR.ncGreenShift(this.address());
        }

        @NativeType(value="BYTE")
        public byte cBlueBits() {
            return PIXELFORMATDESCRIPTOR.ncBlueBits(this.address());
        }

        @NativeType(value="BYTE")
        public byte cBlueShift() {
            return PIXELFORMATDESCRIPTOR.ncBlueShift(this.address());
        }

        @NativeType(value="BYTE")
        public byte cAlphaBits() {
            return PIXELFORMATDESCRIPTOR.ncAlphaBits(this.address());
        }

        @NativeType(value="BYTE")
        public byte cAlphaShift() {
            return PIXELFORMATDESCRIPTOR.ncAlphaShift(this.address());
        }

        @NativeType(value="BYTE")
        public byte cAccumBits() {
            return PIXELFORMATDESCRIPTOR.ncAccumBits(this.address());
        }

        @NativeType(value="BYTE")
        public byte cAccumRedBits() {
            return PIXELFORMATDESCRIPTOR.ncAccumRedBits(this.address());
        }

        @NativeType(value="BYTE")
        public byte cAccumGreenBits() {
            return PIXELFORMATDESCRIPTOR.ncAccumGreenBits(this.address());
        }

        @NativeType(value="BYTE")
        public byte cAccumBlueBits() {
            return PIXELFORMATDESCRIPTOR.ncAccumBlueBits(this.address());
        }

        @NativeType(value="BYTE")
        public byte cAccumAlphaBits() {
            return PIXELFORMATDESCRIPTOR.ncAccumAlphaBits(this.address());
        }

        @NativeType(value="BYTE")
        public byte cDepthBits() {
            return PIXELFORMATDESCRIPTOR.ncDepthBits(this.address());
        }

        @NativeType(value="BYTE")
        public byte cStencilBits() {
            return PIXELFORMATDESCRIPTOR.ncStencilBits(this.address());
        }

        @NativeType(value="BYTE")
        public byte cAuxBuffers() {
            return PIXELFORMATDESCRIPTOR.ncAuxBuffers(this.address());
        }

        @NativeType(value="BYTE")
        public byte iLayerType() {
            return PIXELFORMATDESCRIPTOR.niLayerType(this.address());
        }

        @NativeType(value="BYTE")
        public byte bReserved() {
            return PIXELFORMATDESCRIPTOR.nbReserved(this.address());
        }

        @NativeType(value="DWORD")
        public int dwLayerMask() {
            return PIXELFORMATDESCRIPTOR.ndwLayerMask(this.address());
        }

        @NativeType(value="DWORD")
        public int dwVisibleMask() {
            return PIXELFORMATDESCRIPTOR.ndwVisibleMask(this.address());
        }

        @NativeType(value="DWORD")
        public int dwDamageMask() {
            return PIXELFORMATDESCRIPTOR.ndwDamageMask(this.address());
        }

        public Buffer nSize(@NativeType(value="WORD") short value) {
            PIXELFORMATDESCRIPTOR.nnSize(this.address(), value);
            return this;
        }

        public Buffer nVersion(@NativeType(value="WORD") short value) {
            PIXELFORMATDESCRIPTOR.nnVersion(this.address(), value);
            return this;
        }

        public Buffer dwFlags(@NativeType(value="DWORD") int value) {
            PIXELFORMATDESCRIPTOR.ndwFlags(this.address(), value);
            return this;
        }

        public Buffer iPixelType(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.niPixelType(this.address(), value);
            return this;
        }

        public Buffer cColorBits(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncColorBits(this.address(), value);
            return this;
        }

        public Buffer cRedBits(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncRedBits(this.address(), value);
            return this;
        }

        public Buffer cRedShift(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncRedShift(this.address(), value);
            return this;
        }

        public Buffer cGreenBits(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncGreenBits(this.address(), value);
            return this;
        }

        public Buffer cGreenShift(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncGreenShift(this.address(), value);
            return this;
        }

        public Buffer cBlueBits(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncBlueBits(this.address(), value);
            return this;
        }

        public Buffer cBlueShift(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncBlueShift(this.address(), value);
            return this;
        }

        public Buffer cAlphaBits(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncAlphaBits(this.address(), value);
            return this;
        }

        public Buffer cAlphaShift(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncAlphaShift(this.address(), value);
            return this;
        }

        public Buffer cAccumBits(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncAccumBits(this.address(), value);
            return this;
        }

        public Buffer cAccumRedBits(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncAccumRedBits(this.address(), value);
            return this;
        }

        public Buffer cAccumGreenBits(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncAccumGreenBits(this.address(), value);
            return this;
        }

        public Buffer cAccumBlueBits(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncAccumBlueBits(this.address(), value);
            return this;
        }

        public Buffer cAccumAlphaBits(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncAccumAlphaBits(this.address(), value);
            return this;
        }

        public Buffer cDepthBits(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncDepthBits(this.address(), value);
            return this;
        }

        public Buffer cStencilBits(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncStencilBits(this.address(), value);
            return this;
        }

        public Buffer cAuxBuffers(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.ncAuxBuffers(this.address(), value);
            return this;
        }

        public Buffer iLayerType(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.niLayerType(this.address(), value);
            return this;
        }

        public Buffer bReserved(@NativeType(value="BYTE") byte value) {
            PIXELFORMATDESCRIPTOR.nbReserved(this.address(), value);
            return this;
        }

        public Buffer dwLayerMask(@NativeType(value="DWORD") int value) {
            PIXELFORMATDESCRIPTOR.ndwLayerMask(this.address(), value);
            return this;
        }

        public Buffer dwVisibleMask(@NativeType(value="DWORD") int value) {
            PIXELFORMATDESCRIPTOR.ndwVisibleMask(this.address(), value);
            return this;
        }

        public Buffer dwDamageMask(@NativeType(value="DWORD") int value) {
            PIXELFORMATDESCRIPTOR.ndwDamageMask(this.address(), value);
            return this;
        }
    }
}

