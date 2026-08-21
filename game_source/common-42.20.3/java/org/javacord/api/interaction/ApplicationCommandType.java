/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

public enum ApplicationCommandType {
    APPLICATION_COMMAND(0),
    SLASH(1),
    USER(2),
    MESSAGE(3),
    UNKNOWN(-1);

    private final int type;

    private ApplicationCommandType(int type) {
        this.type = type;
    }

    public int getValue() {
        return this.type;
    }

    public static ApplicationCommandType fromValue(int value) {
        for (ApplicationCommandType type : ApplicationCommandType.values()) {
            if (type.getValue() != value) continue;
            return type;
        }
        return UNKNOWN;
    }
}

