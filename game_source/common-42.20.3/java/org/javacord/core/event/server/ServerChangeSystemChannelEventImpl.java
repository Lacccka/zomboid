/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import java.util.Optional;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerChangeSystemChannelEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeSystemChannelEventImpl
extends ServerEventImpl
implements ServerChangeSystemChannelEvent {
    private final ServerTextChannel newSystemChannel;
    private final ServerTextChannel oldSystemChannel;

    public ServerChangeSystemChannelEventImpl(Server server, ServerTextChannel newSystemChannel, ServerTextChannel oldSystemChannel) {
        super(server);
        this.newSystemChannel = newSystemChannel;
        this.oldSystemChannel = oldSystemChannel;
    }

    @Override
    public Optional<ServerTextChannel> getOldSystemChannel() {
        return Optional.ofNullable(this.oldSystemChannel);
    }

    @Override
    public Optional<ServerTextChannel> getNewSystemChannel() {
        return Optional.ofNullable(this.newSystemChannel);
    }
}

