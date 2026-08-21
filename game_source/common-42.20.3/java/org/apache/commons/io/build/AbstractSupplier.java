/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.build;

import org.apache.commons.io.function.IOSupplier;

public abstract class AbstractSupplier<T, B extends AbstractSupplier<T, B>>
implements IOSupplier<T> {
    protected B asThis() {
        return (B)this;
    }
}

