/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.Arrays;

public enum ChannelType {
    SERVER_TEXT_CHANNEL(0, true, false, true, true),
    PRIVATE_CHANNEL(1, true, true, false, false),
    SERVER_VOICE_CHANNEL(2, false, true, true, true),
    GROUP_CHANNEL(3, true, true, false, false),
    CHANNEL_CATEGORY(4, false, false, true, true),
    SERVER_NEWS_CHANNEL(5, true, false, true, true),
    SERVER_STORE_CHANNEL(6, true, false, true, true),
    SERVER_NEWS_THREAD(10, true, false, true, false),
    SERVER_PUBLIC_THREAD(11, true, false, true, false),
    SERVER_PRIVATE_THREAD(12, true, false, true, false),
    SERVER_STAGE_VOICE_CHANNEL(13, false, true, true, true),
    SERVER_DIRECTORY_CHANNEL(14, false, false, true, true),
    SERVER_FORUM_CHANNEL(15, false, false, true, true),
    UNKNOWN(-1, false, false, false, false);

    private static final ChannelType[] textChannelTypes;
    private static final ChannelType[] voiceChannelTypes;
    private static final ChannelType[] serverChannelTypes;
    private static final ChannelType[] regularServerChannelTypes;
    private final int id;
    private final boolean textChannelType;
    private final boolean voiceChannelType;
    private final boolean serverChannelType;
    private final boolean regularServerChannelType;

    private ChannelType(int id, boolean textChannelType, boolean voiceChannelType, boolean serverChannelType, boolean regularServerChannelType) {
        this.id = id;
        this.textChannelType = textChannelType;
        this.voiceChannelType = voiceChannelType;
        this.serverChannelType = serverChannelType;
        this.regularServerChannelType = regularServerChannelType;
    }

    public int getId() {
        return this.id;
    }

    public boolean isTextChannelType() {
        return this.textChannelType;
    }

    public boolean isVoiceChannelType() {
        return this.voiceChannelType;
    }

    public boolean isServerChannelType() {
        return this.serverChannelType;
    }

    public boolean isRegularServerChannelType() {
        return this.regularServerChannelType;
    }

    public static ChannelType fromId(int id) {
        for (ChannelType type : ChannelType.values()) {
            if (type.getId() != id) continue;
            return type;
        }
        return UNKNOWN;
    }

    public static ChannelType[] getTextChannelTypes() {
        return textChannelTypes;
    }

    public static ChannelType[] getVoiceChannelTypes() {
        return voiceChannelTypes;
    }

    public static ChannelType[] getServerChannelTypes() {
        return serverChannelTypes;
    }

    public static ChannelType[] getRegularServerChannelTypes() {
        return regularServerChannelTypes;
    }

    static {
        textChannelTypes = (ChannelType[])Arrays.stream(ChannelType.values()).filter(ChannelType::isTextChannelType).toArray(ChannelType[]::new);
        voiceChannelTypes = (ChannelType[])Arrays.stream(ChannelType.values()).filter(ChannelType::isVoiceChannelType).toArray(ChannelType[]::new);
        serverChannelTypes = (ChannelType[])Arrays.stream(ChannelType.values()).filter(ChannelType::isServerChannelType).toArray(ChannelType[]::new);
        regularServerChannelTypes = (ChannelType[])Arrays.stream(ChannelType.values()).filter(ChannelType::isRegularServerChannelType).toArray(ChannelType[]::new);
    }
}

