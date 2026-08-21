/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import java.util.Optional;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerChangeAfkChannelEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeAfkChannelEventImpl
extends ServerEventImpl
implements ServerChangeAfkChannelEvent {
    private final ServerVoiceChannel newAfkChannel;
    private final ServerVoiceChannel oldAfkChannel;

    public ServerChangeAfkChannelEventImpl(Server server, ServerVoiceChannel newAfkChannel, ServerVoiceChannel oldAfkChannel) {
        super(server);
        this.newAfkChannel = newAfkChannel;
        this.oldAfkChannel = oldAfkChannel;
    }

    @Override
    public Optional<ServerVoiceChannel> getOldAfkChannel() {
        return Optional.ofNullable(this.oldAfkChannel);
    }

    @Override
    public Optional<ServerVoiceChannel> getNewAfkChannel() {
        return Optional.ofNullable(this.newAfkChannel);
    }
}

