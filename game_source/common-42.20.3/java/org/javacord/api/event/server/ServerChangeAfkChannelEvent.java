/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import java.util.Optional;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeAfkChannelEvent
extends ServerEvent {
    public Optional<ServerVoiceChannel> getOldAfkChannel();

    public Optional<ServerVoiceChannel> getNewAfkChannel();
}

