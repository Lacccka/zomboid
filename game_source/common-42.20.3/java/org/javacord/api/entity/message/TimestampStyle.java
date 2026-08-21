/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message;

import java.time.Instant;

public enum TimestampStyle {
    SHORT_TIME("t"),
    LONG_TIME("T"),
    SHORT_DATE("d"),
    LONG_DATE("D"),
    SHORT_DATE_TIME("f"),
    LONG_DATE_TIME("F"),
    RELATIVE_TIME("R"),
    UNKNOWN("");

    private final String timestampStyle;

    private TimestampStyle(String timestampStyle) {
        this.timestampStyle = timestampStyle;
    }

    public static TimestampStyle byStyle(String timestampStyle) {
        for (TimestampStyle value : TimestampStyle.values()) {
            if (!value.timestampStyle.equals(timestampStyle)) continue;
            return value;
        }
        return UNKNOWN;
    }

    public String getTimestampStyle() {
        return this.timestampStyle;
    }

    public String getTimestampTag(long epochSeconds) {
        return "<t:" + epochSeconds + ":" + this.timestampStyle + ">";
    }

    public String getTimestampTag(Instant instant) {
        return this.getTimestampTag(instant.getEpochSecond());
    }
}

