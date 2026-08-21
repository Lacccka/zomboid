/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

public enum MessageFlag {
    CROSSPOSTED(1),
    IS_CROSSPOST(2),
    SUPPRESS_EMBEDS(4),
    SOURCE_MESSAGE_DELETED(8),
    URGENT(16),
    HAS_THREAD(32),
    EPHEMERAL(64),
    LOADING(128),
    FAILED_TO_MENTION_SOME_ROLES_IN_THREAD(256),
    SUPPRESS_NOTIFICATIONS(4096),
    UNKNOWN(-1);

    private final int id;

    private MessageFlag(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static MessageFlag getFlagTypeById(int id) {
        for (MessageFlag value : MessageFlag.values()) {
            if (value.getId() != id) continue;
            return value;
        }
        return UNKNOWN;
    }
}

