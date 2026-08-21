/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.thread;

import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeArchivedEvent;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelEventImpl;

public class ServerThreadChannelChangeArchivedEventImpl
extends ServerThreadChannelEventImpl
implements ServerThreadChannelChangeArchivedEvent {
    private final boolean isArchived;
    private final boolean wasArchived;

    public ServerThreadChannelChangeArchivedEventImpl(ServerThreadChannel channel, boolean isArchived, boolean wasArchived) {
        super(channel);
        this.isArchived = isArchived;
        this.wasArchived = wasArchived;
    }

    @Override
    public boolean isArchived() {
        return this.isArchived;
    }

    @Override
    public boolean wasArchived() {
        return this.wasArchived;
    }
}

