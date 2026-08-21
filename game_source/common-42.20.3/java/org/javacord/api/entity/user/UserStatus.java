/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.user;

public enum UserStatus {
    ONLINE("online"),
    IDLE("idle"),
    DO_NOT_DISTURB("dnd"),
    INVISIBLE("invisible"),
    OFFLINE("offline");

    private final String statusString;

    private UserStatus(String statusString) {
        this.statusString = statusString;
    }

    public String getStatusString() {
        return this.statusString;
    }

    public static UserStatus fromString(String str) {
        for (UserStatus status : UserStatus.values()) {
            if (!status.statusString.equals(str)) continue;
            return status;
        }
        return OFFLINE;
    }
}

