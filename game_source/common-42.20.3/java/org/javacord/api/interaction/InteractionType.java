/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

public enum InteractionType {
    PING(1),
    APPLICATION_COMMAND(2),
    MESSAGE_COMPONENT(3),
    APPLICATION_COMMAND_AUTOCOMPLETE(4),
    MODAL_SUBMIT(5),
    UNKNOWN(-1);

    private final int value;

    private InteractionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static InteractionType fromValue(int value) {
        for (InteractionType type : InteractionType.values()) {
            if (type.getValue() != value) continue;
            return type;
        }
        return UNKNOWN;
    }
}

