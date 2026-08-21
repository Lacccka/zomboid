/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server;

public enum MultiFactorAuthenticationLevel {
    NONE(0),
    ELEVATED(1),
    UNKNOWN(-1);

    private final int id;

    private MultiFactorAuthenticationLevel(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static MultiFactorAuthenticationLevel fromId(int id) {
        for (MultiFactorAuthenticationLevel multiFactorAuthenticationLevel : MultiFactorAuthenticationLevel.values()) {
            if (multiFactorAuthenticationLevel.getId() != id) continue;
            return multiFactorAuthenticationLevel;
        }
        return UNKNOWN;
    }
}

