/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server.invite;

public enum TargetUserType {
    STREAM(1),
    UNKNOWN(-1);

    private final int id;

    private TargetUserType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static TargetUserType fromId(int id) {
        for (TargetUserType targetUserType : TargetUserType.values()) {
            if (targetUserType.getId() != id) continue;
            return targetUserType;
        }
        return UNKNOWN;
    }
}

