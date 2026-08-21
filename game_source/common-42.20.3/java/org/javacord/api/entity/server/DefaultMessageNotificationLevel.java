/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server;

public enum DefaultMessageNotificationLevel {
    ALL_MESSAGES(0),
    ONLY_MENTIONS(1),
    UNKNOWN(-1);

    private final int id;

    private DefaultMessageNotificationLevel(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static DefaultMessageNotificationLevel fromId(int id) {
        for (DefaultMessageNotificationLevel verificationLevel : DefaultMessageNotificationLevel.values()) {
            if (verificationLevel.getId() != id) continue;
            return verificationLevel;
        }
        return UNKNOWN;
    }
}

