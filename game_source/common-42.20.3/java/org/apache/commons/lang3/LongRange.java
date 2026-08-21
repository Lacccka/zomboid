/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import org.apache.commons.lang3.NumberRange;

public final class LongRange
extends NumberRange<Long> {
    private static final long serialVersionUID = 1L;

    public static LongRange of(long fromInclusive, long toInclusive) {
        return LongRange.of((Long)fromInclusive, (Long)toInclusive);
    }

    public static LongRange of(Long fromInclusive, Long toInclusive) {
        return new LongRange(fromInclusive, toInclusive);
    }

    private LongRange(Long number1, Long number2) {
        super(number1, number2, null);
    }
}

