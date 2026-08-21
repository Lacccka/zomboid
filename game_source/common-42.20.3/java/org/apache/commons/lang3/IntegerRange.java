/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import org.apache.commons.lang3.NumberRange;

public final class IntegerRange
extends NumberRange<Integer> {
    private static final long serialVersionUID = 1L;

    public static IntegerRange of(int fromInclusive, int toInclusive) {
        return IntegerRange.of((Integer)fromInclusive, (Integer)toInclusive);
    }

    public static IntegerRange of(Integer fromInclusive, Integer toInclusive) {
        return new IntegerRange(fromInclusive, toInclusive);
    }

    private IntegerRange(Integer number1, Integer number2) {
        super(number1, number2, null);
    }
}

