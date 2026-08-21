/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryUtil;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
final class MemoryUtilTunables {
    private static final String PROPERTY_PATH = "java.lang.foreign.native.threshold.power.";
    private static final long NATIVE_THRESHOLD_FILL = MemoryUtilTunables.powerOfPropertyOr("fill", 5);
    private static final long VECTOR_THRESHOLD_FILL = 16384L;
    private static final long VECTOR_THRESHOLD_BATCH = 16383L;
    private static final long NATIVE_THRESHOLD_COPY = MemoryUtilTunables.powerOfPropertyOr("copy", 6);
    private static final long VECTOR_THRESHOLD_COPY = 16384L;
    private static final long FILL_PATTERN_64 = Long.divideUnsigned(-1L, 255L);

    private MemoryUtilTunables() {
    }

    private static long powerOfPropertyOr(String name, int defaultPower) {
        int power = Integer.getInteger(PROPERTY_PATH + name, defaultPower);
        return 1L << Math.clamp((long)power, 0, 30);
    }

    static void memset(long ptr, int value, long bytes) {
        byte b = (byte)(value & 0xFF);
        if (bytes < NATIVE_THRESHOLD_FILL) {
            MemorySegment.ofAddress(ptr).reinterpret(bytes).fill(b);
        } else {
            long lastByteIndex = bytes - 1L;
            MemorySegment.ofAddress(ptr).reinterpret(lastByteIndex + (bytes & 1L)).fill(b);
            MemorySegment.ofAddress(ptr + lastByteIndex).reinterpret(1L).set(ValueLayout.JAVA_BYTE, 0L, b);
        }
    }

    private static void memsetMid(long ptr, byte b, long bytes) {
        int offset;
        int limit = (int)(bytes & 0x3FFFL);
        long longValue = FILL_PATTERN_64 * (long)b;
        int aligned = limit & 0xFFFFFFF8;
        for (offset = 0; offset < aligned; offset += 8) {
            MemoryUtil.memPutLong(ptr + (long)offset, longValue);
        }
        if (offset < (limit & 0xFFFFFFFC)) {
            MemoryUtil.memPutInt(ptr + (long)offset, (int)longValue);
            offset += 4;
        }
        if (offset < (limit & 0xFFFFFFFE)) {
            MemoryUtil.memPutShort(ptr + (long)offset, (short)longValue);
            offset += 2;
        }
        if (offset < limit) {
            MemoryUtil.memPutByte(ptr + (long)offset, b);
        }
    }

    private static void memsetHigh(long ptr, byte b, long bytes) {
        long longValue = FILL_PATTERN_64 * (long)b;
        int offset = 0;
        long aligned = bytes & 0xFFFFFFFFFFFFFFF8L;
        while ((long)offset < aligned) {
            MemoryUtil.memPutLong(ptr + (long)offset, longValue);
            offset += 8;
        }
        if ((long)offset < (bytes & 0xFFFFFFFFFFFFFFFCL)) {
            MemoryUtil.memPutInt(ptr + (long)offset, (int)longValue);
            offset += 4;
        }
        if ((long)offset < (bytes & 0xFFFFFFFFFFFFFFFEL)) {
            MemoryUtil.memPutShort(ptr + (long)offset, (short)longValue);
            offset += 2;
        }
        if ((long)offset < bytes) {
            MemoryUtil.memPutByte(ptr + (long)offset, b);
        }
    }

    static void memcpy(long src, long dst, long bytes) {
        if (bytes < NATIVE_THRESHOLD_COPY) {
            MemorySegment S = MemorySegment.ofAddress(src).reinterpret(bytes);
            MemorySegment D = MemorySegment.ofAddress(dst).reinterpret(bytes);
            D.copyFrom(S);
        } else {
            long lastByteIndex = bytes - 1L;
            long copyBytes = lastByteIndex + (bytes & 1L);
            MemorySegment S = MemorySegment.ofAddress(src).reinterpret(copyBytes);
            MemorySegment D = MemorySegment.ofAddress(dst).reinterpret(copyBytes);
            D.copyFrom(S);
            MemoryUtil.memPutByte(dst + lastByteIndex, MemoryUtil.memGetByte(src + lastByteIndex));
        }
    }

    private static void memcpyMid(long src, long dst, long bytes) {
        int offset;
        int limit = (int)(bytes & 0x3FFFL);
        int aligned = limit & 0xFFFFFFF8;
        for (offset = 0; offset < aligned; offset += 8) {
            MemoryUtil.memPutLong(dst + (long)offset, MemoryUtil.memGetLong(src + (long)offset));
        }
        if (offset < (limit & 0xFFFFFFFC)) {
            MemoryUtil.memPutInt(dst + (long)offset, MemoryUtil.memGetInt(src + (long)offset));
            offset += 4;
        }
        if (offset < (limit & 0xFFFFFFFE)) {
            MemoryUtil.memPutShort(dst + (long)offset, MemoryUtil.memGetShort(src + (long)offset));
            offset += 2;
        }
        if (offset < limit) {
            MemoryUtil.memPutByte(dst + (long)offset, MemoryUtil.memGetByte(src + (long)offset));
        }
    }

    private static void memcpyHigh(long src, long dst, long bytes) {
        long offset;
        long limit = bytes & 0xFFFFFFFFFFFFFFF8L;
        for (offset = 0L; offset < limit; offset += 8L) {
            MemoryUtil.memPutLong(dst + offset, MemoryUtil.memGetLong(src + offset));
        }
        if (offset < (bytes & 0xFFFFFFFFFFFFFFFCL)) {
            MemoryUtil.memPutInt(dst + offset, MemoryUtil.memGetInt(src + offset));
            offset += 4L;
        }
        if (offset < (bytes & 0xFFFFFFFFFFFFFFFEL)) {
            MemoryUtil.memPutShort(dst + offset, MemoryUtil.memGetShort(src + offset));
            offset += 2L;
        }
        if (offset < bytes) {
            MemoryUtil.memPutByte(dst + offset, MemoryUtil.memGetByte(src + offset));
        }
    }

    private static void memcpy(MemorySegment src, long dst, long offset, long bytes) {
        if (bytes < NATIVE_THRESHOLD_COPY) {
            MemorySegment S = src.asSlice(offset, bytes);
            MemorySegment D = MemorySegment.ofAddress(dst).reinterpret(bytes);
            D.copyFrom(S);
        } else {
            long lastByteIndex = bytes - 1L;
            long copyBytes = lastByteIndex + (bytes & 1L);
            MemorySegment S = src.asSlice(offset, copyBytes);
            MemorySegment D = MemorySegment.ofAddress(dst).reinterpret(copyBytes);
            D.copyFrom(S);
            MemoryUtil.memPutByte(dst + lastByteIndex, src.get(ValueLayout.JAVA_BYTE, offset + lastByteIndex));
        }
    }

    static void memcpy(byte[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtilTunables.memcpy(MemorySegment.ofArray(src), dst, (long)offset, (long)size);
    }

    static void memcpy(short[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtilTunables.memcpy(MemorySegment.ofArray(src), dst, APIUtil.apiGetBytes(offset, 1), APIUtil.apiGetBytes(size, 1));
    }

    static void memcpy(int[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtilTunables.memcpy(MemorySegment.ofArray(src), dst, APIUtil.apiGetBytes(offset, 2), APIUtil.apiGetBytes(size, 2));
    }

    static void memcpy(long[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtilTunables.memcpy(MemorySegment.ofArray(src), dst, APIUtil.apiGetBytes(offset, 3), APIUtil.apiGetBytes(size, 3));
    }

    static void memcpy(float[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtilTunables.memcpy(MemorySegment.ofArray(src), dst, APIUtil.apiGetBytes(offset, 2), APIUtil.apiGetBytes(size, 2));
    }

    static void memcpy(double[] src, long dst, int offset, int size) {
        Checks.checkMemcpy(dst, offset, size, src.length);
        MemoryUtilTunables.memcpy(MemorySegment.ofArray(src), dst, APIUtil.apiGetBytes(offset, 3), APIUtil.apiGetBytes(size, 3));
    }

    private static void memcpy(long src, MemorySegment dst, long offset, long bytes) {
        if (bytes < NATIVE_THRESHOLD_COPY) {
            MemorySegment S = MemorySegment.ofAddress(src).reinterpret(bytes);
            MemorySegment D = dst.asSlice(offset, bytes);
            D.copyFrom(S);
        } else {
            long lastByteIndex = bytes - 1L;
            long copyBytes = lastByteIndex + (bytes & 1L);
            MemorySegment S = MemorySegment.ofAddress(src).reinterpret(copyBytes);
            MemorySegment D = dst.asSlice(offset, copyBytes);
            D.copyFrom(S);
            dst.set(ValueLayout.JAVA_BYTE, offset + lastByteIndex, MemoryUtil.memGetByte(src + lastByteIndex));
        }
    }

    static void memcpy(long src, byte[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtilTunables.memcpy(src, MemorySegment.ofArray(dst), (long)offset, (long)size);
    }

    static void memcpy(long src, short[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtilTunables.memcpy(src, MemorySegment.ofArray(dst), APIUtil.apiGetBytes(offset, 1), APIUtil.apiGetBytes(size, 1));
    }

    static void memcpy(long src, int[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtilTunables.memcpy(src, MemorySegment.ofArray(dst), APIUtil.apiGetBytes(offset, 2), APIUtil.apiGetBytes(size, 2));
    }

    static void memcpy(long src, long[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtilTunables.memcpy(src, MemorySegment.ofArray(dst), APIUtil.apiGetBytes(offset, 3), APIUtil.apiGetBytes(size, 3));
    }

    static void memcpy(long src, float[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtilTunables.memcpy(src, MemorySegment.ofArray(dst), APIUtil.apiGetBytes(offset, 2), APIUtil.apiGetBytes(size, 2));
    }

    static void memcpy(long src, double[] dst, int offset, int size) {
        Checks.checkMemcpy(src, offset, size, dst.length);
        MemoryUtilTunables.memcpy(src, MemorySegment.ofArray(dst), APIUtil.apiGetBytes(offset, 3), APIUtil.apiGetBytes(size, 3));
    }

    static {
        APIUtil.apiLog("Java 25 memset/memcpy enabled");
    }
}

