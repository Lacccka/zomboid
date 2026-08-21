/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.function;

@FunctionalInterface
public interface FailableShortSupplier<E extends Throwable> {
    public short getAsShort() throws E;
}

