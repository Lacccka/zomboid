/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.stream;

import java.util.stream.IntStream;

public class IntStreams {
    public static IntStream range(int endExclusive) {
        return IntStream.range(0, endExclusive);
    }

    public static IntStream rangeClosed(int endInclusive) {
        return IntStream.rangeClosed(0, endInclusive);
    }

    @Deprecated
    public IntStreams() {
    }
}

