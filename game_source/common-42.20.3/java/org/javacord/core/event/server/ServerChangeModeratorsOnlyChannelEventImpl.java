/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import java.util.Optional;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.event.server.ServerChangeModeratorsOnlyChannelEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeModeratorsOnlyChannelEventImpl
extends ServerEventImpl
implements ServerChangeModeratorsOnlyChannelEvent {
    private final ServerTextChannel newModeratorsOnlyChannel;
    private final ServerTextChannel oldModeratorsOnlyChannel;

    public ServerChangeModeratorsOnlyChannelEventImpl(ServerImpl server, ServerTextChannel newModeratorsOnlyChannel, ServerTextChannel oldModeratorsOnlyChannel) {
        super(server);
        this.newModeratorsOnlyChannel = newModeratorsOnlyChannel;
        this.oldModeratorsOnlyChannel = oldModeratorsOnlyChannel;
    }

    @Override
    public Optional<ServerTextChannel> getOldModeratorsOnlyChannel() {
        return Optional.ofNullable(this.oldModeratorsOnlyChannel);
    }

    @Override
    public Optional<ServerTextChannel> getNewModeratorsOnlyChannel() {
        return Optional.ofNullable(this.newModeratorsOnlyChannel);
    }
}

