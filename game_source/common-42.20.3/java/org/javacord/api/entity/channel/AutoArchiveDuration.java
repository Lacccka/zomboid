/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

public enum AutoArchiveDuration {
    ONE_HOUR(60),
    ONE_DAY(1440),
    THREE_DAYS(4320),
    ONE_WEEK(10080);

    private final int minutes;

    private AutoArchiveDuration(int minutes) {
        this.minutes = minutes;
    }

    public int asInt() {
        return this.minutes;
    }
}

