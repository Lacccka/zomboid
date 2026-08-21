/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util;

import io.vavr.collection.Set;
import org.javacord.core.util.VavrSetView;

public class ImmutableToJavaMapper {
    private ImmutableToJavaMapper() {
        throw new UnsupportedOperationException();
    }

    public static <J extends V, V> java.util.Set<J> mapToJava(Set<V> set) {
        return new VavrSetView<V>(set, true);
    }
}

