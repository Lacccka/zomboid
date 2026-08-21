/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.voice;

import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelChangeNsfwEvent;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelEventImpl;

public class ServerVoiceChannelChangeNsfwEventImpl
extends ServerVoiceChannelEventImpl
implements ServerVoiceChannelChangeNsfwEvent {
    private final boolean newNsfw;
    private final boolean oldNsfw;

    public ServerVoiceChannelChangeNsfwEventImpl(ServerVoiceChannel channel, boolean newNsfw, boolean oldNsfw) {
        super(channel);
        this.newNsfw = newNsfw;
        this.oldNsfw = oldNsfw;
    }

    @Override
    public boolean isNsfw() {
        return this.newNsfw;
    }

    @Override
    public boolean wasNsfw() {
        return this.oldNsfw;
    }
}

