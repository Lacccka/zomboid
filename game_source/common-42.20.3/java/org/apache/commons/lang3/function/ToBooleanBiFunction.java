/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.function;

@FunctionalInterface
public interface ToBooleanBiFunction<T, U> {
    public boolean applyAsBoolean(T var1, U var2);
}

