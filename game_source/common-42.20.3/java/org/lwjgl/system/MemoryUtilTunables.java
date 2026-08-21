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
    private static final long BASE_OFFSET_BYTE = Integer.toUnsignedLong(Unsafe.ARRAY_BYTE_BASE_OFFSET);
    private static final long BASE_OFFSET_SHORT = Integer.toUnsignedLong(Unsafe.ARRAY_SHORT_BASE_OFFSET);
    private static final long BASE_OFFSET_INT = Integer.toUnsignedLong(Unsafe.ARRAY_INT_BASE_OFFSET);
    private static final long BASE_OFFSET_LONG = Integer.toUnsignedLong(Unsafe.ARRAY_LONG_BASE_OFFSET);
    private static final long BASE_OFFSET_FLOAT = Integer.toUnsignedLong(Unsafe.ARRAY_FLOAT_BASE_OFFSET);
    private static final long BASE_OFFSET_DOUBLE = Integer.toUnsignedLong(Unsafe.ARRAY_DOUBLE_BASE_OFFSET);

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
            MemoryUtil.UNSAFE.putInt(null, Integer.toUnsignedLong(ptr + offset), i);
        }
        if (offset < (limit & 0xFFFFFFFE)) {
            MemoryUtil.UNSAFE.putShort(null, Integer.toUnsignedLong(ptr + offset), (short)i);
            offset += 2;
        }
        if (offset < limit) {
            MemoryUtil.UNSAFE.putByte(null, Integer.toUnsignedLong(ptr + offset), (byte)i);
        }
    }

    static void memcpy(long src, long dst, long bytes) {
        if (Pointer.BITS64 && bytes <= 160L && ((src | dst) & 7L) == 0L) {
            MemoryUtilTunables.memcpyAligned64(src, dst, bytes);
            return;
        }
        LibCString.nmemcpy(dst, src, bytes);
    }

    private static void memcpyAligned64(long src, long dst, long bytes) {
        int offset;
        int limit = (int)bytes & 0xFF;
        int aligned = limit & 0xFFFFFFF8;
        for (offset = 0; offset < aligned; offset += 8) {
            MemoryUtil.UNSAFE.putLong(null, dst + (long)offset, MemoryUtil.UNSAFE.getLong(null, src + (long)offset));
        }
        if (offset < (limit & 0xFFFFFFFC)) {
            MemoryUtil.UNSAFE.putInt(null, dst + (long)offset, MemoryUtil.UNSAFE.getInt(null, src + (long)offset));
            offset += 4;
        }
        if (offset < (limit & 0xFFFFFFFE)) {
            MemoryUtil.UNSAFE.putShort(null, dst + (long)offset, MemoryUtil.UNSAFE.getShort(null, src + (long)offset));
            offset += 2;
        }
        if (offset < limit) {
            MemoryUtil.UNSAFE.putByte(null, dst + (long)offset, MemoryUtil.UNSAFE.getByte(null, src + (long)offset));
        }
    }

    static void memcpy(byte[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtil.UNSAFE.copyMemory(src, BASE_OFFSET_BYTE + (long)offset, null, dst, size);
    }

    static void memcpy(short[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtil.UNSAFE.copyMemory(src, BASE_OFFSET_SHORT + APIUtil.apiGetBytes(offset, 1), null, dst, APIUtil.apiGetBytes(size, 1));
    }

    static void memcpy(int[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtil.UNSAFE.copyMemory(src, BASE_OFFSET_INT + APIUtil.apiGetBytes(offset, 2), null, dst, APIUtil.apiGetBytes(size, 2));
    }

    static void memcpy(long[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtil.UNSAFE.copyMemory(src, BASE_OFFSET_LONG + APIUtil.apiGetBytes(offset, 3), null, dst, APIUtil.apiGetBytes(size, 3));
    }

    static void memcpy(float[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtil.UNSAFE.copyMemory(src, BASE_OFFSET_FLOAT + APIUtil.apiGetBytes(offset, 2), null, dst, APIUtil.apiGetBytes(size, 2));
    }

    static void memcpy(double[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtil.UNSAFE.copyMemory(src, BASE_OFFSET_DOUBLE + APIUtil.apiGetBytes(offset, 3), null, dst, APIUtil.apiGetBytes(size, 3));
    }

    static void memcpy(long src, byte[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtil.UNSAFE.copyMemory(null, src, dst, BASE_OFFSET_BYTE + (long)offset, size);
    }

    static void memcpy(long src, short[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtil.UNSAFE.copyMemory(null, src, dst, BASE_OFFSET_SHORT + APIUtil.apiGetBytes(offset, 1), APIUtil.apiGetBytes(size, 1));
    }

    static void memcpy(long src, int[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtil.UNSAFE.copyMemory(null, src, dst, BASE_OFFSET_INT + APIUtil.apiGetBytes(offset, 2), APIUtil.apiGetBytes(size, 2));
    }

    static void memcpy(long src, long[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtil.UNSAFE.copyMemory(null, src, dst, BASE_OFFSET_LONG + APIUtil.apiGetBytes(offset, 3), APIUtil.apiGetBytes(size, 3));
    }

    static void memcpy(long src, float[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtil.UNSAFE.copyMemory(null, src, dst, BASE_OFFSET_FLOAT + APIUtil.apiGetBytes(offset, 2), APIUtil.apiGetBytes(size, 2));
    }

    static void memcpy(long src, double[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtil.UNSAFE.copyMemory(null, src, dst, BASE_OFFSET_DOUBLE + APIUtil.apiGetBytes(offset, 3), APIUtil.apiGetBytes(size, 3));
    }
}

