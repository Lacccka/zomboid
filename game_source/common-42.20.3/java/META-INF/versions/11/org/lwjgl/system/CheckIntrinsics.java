/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import java.util.Objects;
import org.lwjgl.system.APIUtil;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public final class CheckIntrinsics {
    private CheckIntrinsics() {
    }

    public static int checkIndex(int index, int length) {
        return Objects.checkIndex(index, length);
    }

    public static int checkFromToIndex(int fromIndex, int toIndex, int length) {
        return Objects.checkFromToIndex(fromIndex, toIndex, length);
    }

    public static int checkFromIndexSize(int fromIndex, int size, int length) {
        return Objects.checkFromIndexSize(fromIndex, size, length);
    }

    static {
        APIUtil.apiLog("Java 11 check intrinsics enabled");
    }
}

