/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import java.util.Optional;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.event.server.ServerChangeRulesChannelEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeRulesChannelEventImpl
extends ServerEventImpl
implements ServerChangeRulesChannelEvent {
    private final ServerTextChannel oldRulesChannel;
    private final ServerTextChannel newRulesChannel;

    public ServerChangeRulesChannelEventImpl(ServerImpl server, ServerTextChannel newRulesChannel, ServerTextChannel oldRulesChannel) {
        super(server);
        this.newRulesChannel = newRulesChannel;
        this.oldRulesChannel = oldRulesChannel;
    }

    @Override
    public Optional<ServerTextChannel> getOldRulesChannel() {
        return Optional.ofNullable(this.oldRulesChannel);
    }

    @Override
    public Optional<ServerTextChannel> getNewRulesChannel() {
        return Optional.ofNullable(this.newRulesChannel);
    }
}

