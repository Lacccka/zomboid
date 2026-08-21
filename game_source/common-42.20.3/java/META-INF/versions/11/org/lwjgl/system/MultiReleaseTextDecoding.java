/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import java.nio.charset.StandardCharsets;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.MemoryUtil;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
final class MultiReleaseTextDecoding {
    private MultiReleaseTextDecoding() {
    }

    static String decodeUTF8(long source, int length) {
        if (length <= 0) {
            return "";
        }
        byte[] bytes = length <= MemoryUtil.ARRAY_TLC_SIZE ? MemoryUtil.ARRAY_TLC_BYTE.get() : new byte[length];
        MemoryUtil.memByteBuffer(source, length).get(bytes, 0, length);
        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }

    static {
        APIUtil.apiLog("Java 11 text decoding enabled");
    }
}

