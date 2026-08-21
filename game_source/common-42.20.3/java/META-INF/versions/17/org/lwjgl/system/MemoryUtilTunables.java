/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.libc.LibCString;
import sun.misc.Unsafe;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
final class MemoryUtilTunables {
    private static final int FILL_PATTERN_32 = Integer.divideUnsigned(-1, 255);
    private static final long FILL_PATTERN_64 = Long.divideUnsigned(-1L, 255L);
    private static final long BASE_OFFSET_BYTE;
    private static final long BASE_OFFSET_SHORT;
    private static final long BASE_OFFSET_INT;
    private static final long BASE_OFFSET_LONG;
    private static final long BASE_OFFSET_FLOAT;
    private static final long BASE_OFFSET_DOUBLE;

    private MemoryUtilTunables() {
    }

    static void memset(long ptr, int value, long bytes) {
        if (bytes < 256L) {
            int p = (int)ptr;
            if (Pointer.BITS64) {
                if ((p & 7) == 0) {
                    MemoryUtilTunables.memset64(ptr, value, bytes);
                    return;
                }
            } else if ((p & 3) == 0) {
                MemoryUtilTunables.memset32(p, value, bytes);
                return;
            }
        }
        LibCString.nmemset(ptr, value, bytes);
    }

    private static void memset64(long ptr, int value, long bytes) {
        int offset;
        int limit = (int)bytes & 0xFF;
        long l = (long)(value & 0xFF) * FILL_PATTERN_64;
        int aligned = limit & 0xFFFFFFF8;
        for (offset = 0; offset < aligned; offset += 8) {
            MemoryUtil.UNSAFE.putLong(null, ptr + (long)offset, l);
        }
        if (offset < (limit & 0xFFFFFFFC)) {
            MemoryUtil.UNSAFE.putInt(null, ptr + (long)offset, (int)l);
            offset += 4;
        }
        if (offset < (limit & 0xFFFFFFFE)) {
            MemoryUtil.UNSAFE.putShort(null, ptr + (long)offset, (short)l);
            offset += 2;
        }
        if (offset < limit) {
            MemoryUtil.UNSAFE.putByte(null, ptr + (long)offset, (byte)l);
        }
    }

    private static void memset32(int ptr, int value, long bytes) {
        int offset;
        int limit = (int)bytes & 0xFF;
        int i = (value & 0xFF) * FILL_PATTERN_32;
        int aligned = limit & 0xFFFFFFFC;
        for (offset = 0; offset < aligned; offset += 4) {
            MemoryUtil.UNSAFE.putInt(null, (long)(ptr + offset) & 0xFFFFFFFFL, i);
        }
        if (offset < (limit & 0xFFFFFFFE)) {
            MemoryUtil.UNSAFE.putShort(null, ptr + offset, (short)i);
            offset += 2;
        }
        if (offset < limit) {
            MemoryUtil.UNSAFE.putByte(null, ptr + offset, (byte)i);
        }
    }

    static void memcpy(long src, long dst, long bytes) {
        if (bytes <= 0L) {
            return;
        }
        long lastByteIndex = bytes - 1L;
        MemoryUtil.UNSAFE.copyMemory(null, src, null, dst, lastByteIndex + (bytes & 1L));
        MemoryUtil.UNSAFE.putByte(null, dst + lastByteIndex, MemoryUtil.UNSAFE.getByte(null, src + lastByteIndex));
    }

    private static void memcpy(Object src, long dst, long srcOffset, long bytes) {
        if (bytes <= 0L) {
            return;
        }
        long lastByteIndex = bytes - 1L;
        MemoryUtil.UNSAFE.copyMemory(src, srcOffset, null, dst, lastByteIndex + (bytes & 1L));
        MemoryUtil.UNSAFE.putByte(null, dst + lastByteIndex, MemoryUtil.UNSAFE.getByte(src, srcOffset + lastByteIndex));
    }

    static void memcpy(byte[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtilTunables.memcpy((Object)src, dst, BASE_OFFSET_BYTE + (long)offset, (long)size);
    }

    static void memcpy(short[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtilTunables.memcpy((Object)src, dst, BASE_OFFSET_SHORT + APIUtil.apiGetBytes(offset, 1), APIUtil.apiGetBytes(size, 1));
    }

    static void memcpy(int[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtilTunables.memcpy((Object)src, dst, BASE_OFFSET_INT + APIUtil.apiGetBytes(offset, 2), APIUtil.apiGetBytes(size, 2));
    }

    static void memcpy(long[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtilTunables.memcpy((Object)src, dst, BASE_OFFSET_LONG + APIUtil.apiGetBytes(offset, 3), APIUtil.apiGetBytes(size, 3));
    }

    static void memcpy(float[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtilTunables.memcpy((Object)src, dst, BASE_OFFSET_FLOAT + APIUtil.apiGetBytes(offset, 2), APIUtil.apiGetBytes(size, 2));
    }

    static void memcpy(double[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtilTunables.memcpy((Object)src, dst, BASE_OFFSET_DOUBLE + APIUtil.apiGetBytes(offset, 3), APIUtil.apiGetBytes(size, 3));
    }

    private static void memcpy(long src, Object dst, long dstOffset, long bytes) {
        if (bytes <= 0L) {
            return;
        }
        long lastByteIndex = bytes - 1L;
        MemoryUtil.UNSAFE.copyMemory(null, src, dst, dstOffset, lastByteIndex + (bytes & 1L));
        MemoryUtil.UNSAFE.putByte(dst, dstOffset + lastByteIndex, MemoryUtil.UNSAFE.getByte(null, src + lastByteIndex));
    }

    static void memcpy(long src, byte[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtilTunables.memcpy(src, (Object)dst, BASE_OFFSET_BYTE + (long)offset, (long)size);
    }

    static void memcpy(long src, short[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtilTunables.memcpy(src, (Object)dst, BASE_OFFSET_SHORT + APIUtil.apiGetBytes(offset, 1), APIUtil.apiGetBytes(size, 1));
    }

    static void memcpy(long src, int[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtilTunables.memcpy(src, (Object)dst, BASE_OFFSET_INT + APIUtil.apiGetBytes(offset, 2), APIUtil.apiGetBytes(size, 2));
    }

    static void memcpy(long src, long[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtilTunables.memcpy(src, (Object)dst, BASE_OFFSET_LONG + APIUtil.apiGetBytes(offset, 3), APIUtil.apiGetBytes(size, 3));
    }

    static void memcpy(long src, float[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtilTunables.memcpy(src, (Object)dst, BASE_OFFSET_FLOAT + APIUtil.apiGetBytes(offset, 2), APIUtil.apiGetBytes(size, 2));
    }

    static void memcpy(long src, double[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtilTunables.memcpy(src, (Object)dst, BASE_OFFSET_DOUBLE + APIUtil.apiGetBytes(offset, 3), APIUtil.apiGetBytes(size, 3));
    }

    static {
        APIUtil.apiLog("Java 17 memcpy enabled");
        BASE_OFFSET_BYTE = Integer.toUnsignedLong(Unsafe.ARRAY_BYTE_BASE_OFFSET);
        BASE_OFFSET_SHORT = Integer.toUnsignedLong(Unsafe.ARRAY_SHORT_BASE_OFFSET);
        BASE_OFFSET_INT = Integer.toUnsignedLong(Unsafe.ARRAY_INT_BASE_OFFSET);
        BASE_OFFSET_LONG = Integer.toUnsignedLong(Unsafe.ARRAY_LONG_BASE_OFFSET);
        BASE_OFFSET_FLOAT = Integer.toUnsignedLong(Unsafe.ARRAY_FLOAT_BASE_OFFSET);
        BASE_OFFSET_DOUBLE = Integer.toUnsignedLong(Unsafe.ARRAY_DOUBLE_BASE_OFFSET);
    }
}

