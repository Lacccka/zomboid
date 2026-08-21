/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.util.Comparator;
import org.apache.commons.io.function.Uncheck;

@FunctionalInterface
public interface IOComparator<T> {
    default public Comparator<T> asComparator() {
        return (t, u) -> Uncheck.compare(this, t, u);
    }

    public int compare(T var1, T var2) throws IOException;
}

