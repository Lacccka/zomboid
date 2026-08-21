/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import org.apache.commons.lang3.NumberRange;

public final class DoubleRange
extends NumberRange<Double> {
    private static final long serialVersionUID = 1L;

    public static DoubleRange of(double fromInclusive, double toInclusive) {
        return DoubleRange.of((Double)fromInclusive, (Double)toInclusive);
    }

    public static DoubleRange of(Double fromInclusive, Double toInclusive) {
        return new DoubleRange(fromInclusive, toInclusive);
    }

    private DoubleRange(Double number1, Double number2) {
        super(number1, number2, null);
    }
}

