/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server;

import java.util.Arrays;

public enum NsfwLevel {
    DEFAULT(0),
    EXPLICIT(1),
    SAFE(2),
    AGE_RESTRICTED(3),
    UNKNOWN(-1);

    private final int id;

    private NsfwLevel(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static NsfwLevel fromId(int id) {
        return Arrays.stream(NsfwLevel.values()).filter(nsfwLevel -> nsfwLevel.getId() == id).findFirst().orElse(UNKNOWN);
    }
}

