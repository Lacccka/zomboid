/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.util.function.IntSupplier;
import org.apache.commons.io.function.Uncheck;

@FunctionalInterface
public interface IOIntSupplier {
    default public IntSupplier asIntSupplier() {
        return () -> Uncheck.getAsInt(this);
    }

    public int getAsInt() throws IOException;
}

