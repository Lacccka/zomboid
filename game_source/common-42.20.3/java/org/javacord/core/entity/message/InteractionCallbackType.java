/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message;

public enum InteractionCallbackType {
    PONG(1),
    CHANNEL_MESSAGE_WITH_SOURCE(4),
    DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE(5),
    DEFERRED_UPDATE_MESSAGE(6),
    UPDATE_MESSAGE(7),
    APPLICATION_COMMAND_AUTOCOMPLETE_RESULT(8),
    MODAL(9),
    UNKNOWN(-1);

    private final int id;

    private InteractionCallbackType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static InteractionCallbackType getInteractionCallbackTypeById(int id) {
        for (InteractionCallbackType value : InteractionCallbackType.values()) {
            if (value.id != id) continue;
            return value;
        }
        return UNKNOWN;
    }
}

