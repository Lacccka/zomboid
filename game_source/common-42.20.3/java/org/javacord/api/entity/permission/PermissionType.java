/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.permission;

public enum PermissionType {
    CREATE_INSTANT_INVITE(1L),
    KICK_MEMBERS(2L),
    BAN_MEMBERS(4L),
    ADMINISTRATOR(8L),
    MANAGE_CHANNELS(16L),
    MANAGE_SERVER(32L),
    ADD_REACTIONS(64L),
    VIEW_AUDIT_LOG(128L),
    VIEW_SERVER_INSIGHTS(524288L),
    VIEW_CHANNEL(1024L),
    SEND_MESSAGES(2048L),
    SEND_TTS_MESSAGES(4096L),
    MANAGE_MESSAGES(8192L),
    EMBED_LINKS(16384L),
    ATTACH_FILE(32768L),
    READ_MESSAGE_HISTORY(65536L),
    MENTION_EVERYONE(131072L),
    USE_EXTERNAL_EMOJIS(262144L),
    USE_EXTERNAL_STICKERS(0x2000000000L),
    CONNECT(0x100000L),
    SPEAK(0x200000L),
    MUTE_MEMBERS(0x400000L),
    DEAFEN_MEMBERS(0x800000L),
    MOVE_MEMBERS(0x1000000L),
    USE_VOICE_ACTIVITY(0x2000000L),
    PRIORITY_SPEAKER(256L),
    STREAM(512L),
    REQUEST_TO_SPEAK(0x100000000L),
    START_EMBEDDED_ACTIVITIES(0x8000000000L),
    MANAGE_THREADS(0x400000000L),
    CREATE_PUBLIC_THREADS(0x800000000L),
    CREATE_PRIVATE_THREADS(0x1000000000L),
    SEND_MESSAGES_IN_THREADS(0x4000000000L),
    CHANGE_NICKNAME(0x4000000L),
    MANAGE_NICKNAMES(0x8000000L),
    MANAGE_ROLES(0x10000000L),
    MANAGE_WEBHOOKS(0x20000000L),
    MANAGE_EMOJIS(0x40000000L),
    USE_APPLICATION_COMMANDS(0x80000000L),
    MODERATE_MEMBERS(0x10000000000L);

    private final long value;

    private PermissionType(long value) {
        this.value = value;
    }

    public long getValue() {
        return this.value;
    }

    public boolean isSet(long l) {
        return (l & this.getValue()) != 0L;
    }

    public long set(long l, boolean set) {
        if (set && !this.isSet(l)) {
            return l + this.getValue();
        }
        if (!set && this.isSet(l)) {
            return l - this.getValue();
        }
        return l;
    }
}

