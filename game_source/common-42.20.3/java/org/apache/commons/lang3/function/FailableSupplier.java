/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.function;

@FunctionalInterface
public interface FailableSupplier<T, E extends Throwable> {
    public static final FailableSupplier NUL = () -> null;

    public static <T, E extends Exception> FailableSupplier<T, E> nul() {
        return NUL;
    }

    public T get() throws E;
}

