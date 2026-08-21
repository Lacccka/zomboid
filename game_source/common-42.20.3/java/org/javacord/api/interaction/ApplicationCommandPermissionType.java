/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

public enum ApplicationCommandPermissionType {
    ROLE(1),
    USER(2),
    CHANNEL(3),
    UNKNOWN(-1);

    private final int value;

    private ApplicationCommandPermissionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static ApplicationCommandPermissionType fromValue(int value) {
        for (ApplicationCommandPermissionType type : ApplicationCommandPermissionType.values()) {
            if (type.getValue() != value) continue;
            return type;
        }
        return UNKNOWN;
    }
}

