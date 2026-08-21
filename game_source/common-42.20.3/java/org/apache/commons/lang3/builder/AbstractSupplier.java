/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.builder;

import org.apache.commons.lang3.function.FailableSupplier;

public abstract class AbstractSupplier<T, B extends AbstractSupplier<T, B, E>, E extends Throwable>
implements FailableSupplier<T, E> {
    protected B asThis() {
        return (B)this;
    }
}

