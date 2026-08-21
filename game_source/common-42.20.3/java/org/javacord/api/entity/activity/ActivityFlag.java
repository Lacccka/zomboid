/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.activity;

public enum ActivityFlag {
    INSTANCE(1),
    JOIN(2),
    SPECTATE(4),
    JOIN_REQUEST(8),
    SYNC(16),
    PLAY(32),
    PARTY_PRIVACY_FRIENDS(64),
    PARTY_PRIVACY_VOICE_CHANNEL(128),
    EMBEDDED(256),
    UNKNOWN(-1);

    private final int flag;

    private ActivityFlag(int flag) {
        this.flag = flag;
    }

    public static ActivityFlag getActivityFlagById(int flags) {
        for (ActivityFlag activityFlag : ActivityFlag.values()) {
            if (activityFlag.flag != flags) continue;
            return activityFlag;
        }
        return UNKNOWN;
    }

    public int asInt() {
        return this.flag;
    }
}

