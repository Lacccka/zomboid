/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.function;

import java.util.function.Supplier;

public class Suppliers {
    private static Supplier NUL = () -> null;

    public static <T> T get(Supplier<T> supplier) {
        return supplier == null ? null : (T)supplier.get();
    }

    public static <T> Supplier<T> nul() {
        return NUL;
    }

    @Deprecated
    public Suppliers() {
    }
}

