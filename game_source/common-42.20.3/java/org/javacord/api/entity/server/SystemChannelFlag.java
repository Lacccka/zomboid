/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server;

public enum SystemChannelFlag {
    SUPPRESS_JOIN_NOTIFICATIONS(1),
    SUPPRESS_PREMIUM_SUBSCRIPTIONS(2),
    SUPPRESS_GUILD_REMINDER_NOTIFICATIONS(4),
    SUPPRESS_JOIN_NOTIFICATION_REPLIES(8);

    private final int flag;

    private SystemChannelFlag(int flag) {
        this.flag = flag;
    }

    public int asInt() {
        return this.flag;
    }
}

