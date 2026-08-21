/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm.mapping;

import java.lang.foreign.MemoryLayout;

final class Mappings {
    private Mappings() {
    }

    static String nameConst(MemoryLayout layout) {
        return layout.name().orElseThrow() + " const";
    }

    static void check(MemoryLayout layout) {
        if (layout.name().isEmpty()) {
            throw new IllegalArgumentException("Layout must be named");
        }
    }
}

