/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server;

public enum VerificationLevel {
    NONE(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    VERY_HIGH(4),
    UNKNOWN(-1);

    private final int id;

    private VerificationLevel(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static VerificationLevel fromId(int id) {
        for (VerificationLevel verificationLevel : VerificationLevel.values()) {
            if (verificationLevel.getId() != id) continue;
            return verificationLevel;
        }
        return UNKNOWN;
    }
}

