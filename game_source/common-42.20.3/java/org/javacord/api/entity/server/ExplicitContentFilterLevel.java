/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server;

public enum ExplicitContentFilterLevel {
    DISABLED(0),
    MEMBERS_WITHOUT_ROLES(1),
    ALL_MEMBERS(2),
    UNKNOWN(-1);

    private final int id;

    private ExplicitContentFilterLevel(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static ExplicitContentFilterLevel fromId(int id) {
        for (ExplicitContentFilterLevel verificationLevel : ExplicitContentFilterLevel.values()) {
            if (verificationLevel.getId() != id) continue;
            return verificationLevel;
        }
        return UNKNOWN;
    }
}

