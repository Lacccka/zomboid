/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server;

public enum BoostLevel {
    NONE(0),
    TIER_1(1),
    TIER_2(2),
    TIER_3(3),
    UNKNOWN(-1);

    private final int id;

    private BoostLevel(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static BoostLevel fromId(int id) {
        for (BoostLevel boostLevel : BoostLevel.values()) {
            if (boostLevel.getId() != id) continue;
            return boostLevel;
        }
        return UNKNOWN;
    }
}

