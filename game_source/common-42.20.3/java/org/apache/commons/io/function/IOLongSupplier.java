/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.util.function.LongSupplier;
import org.apache.commons.io.function.Uncheck;

@FunctionalInterface
public interface IOLongSupplier {
    default public LongSupplier asSupplier() {
        return () -> Uncheck.getAsLong(this);
    }

    public long getAsLong() throws IOException;
}

