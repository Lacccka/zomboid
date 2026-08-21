/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.utils;

public class ExactMath {
    public static int add(int x, long y) {
        try {
            return Math.addExact(x, Math.toIntExact(y));
        }
        catch (ArithmeticException exp) {
            throw new IllegalArgumentException("Argument too large or result overflows", exp);
        }
    }

    private ExactMath() {
    }
}

