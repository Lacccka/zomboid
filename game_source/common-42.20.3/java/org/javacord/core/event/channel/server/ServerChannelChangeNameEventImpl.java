/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server;

import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.event.channel.server.ServerChannelChangeNameEvent;
import org.javacord.core.event.channel.server.ServerChannelEventImpl;

public class ServerChannelChangeNameEventImpl
extends ServerChannelEventImpl
implements ServerChannelChangeNameEvent {
    private final String newName;
    private final String oldName;

    public ServerChannelChangeNameEventImpl(ServerChannel channel, String newName, String oldName) {
        super(channel);
        this.newName = newName;
        this.oldName = oldName;
    }

    @Override
    public String getNewName() {
        return this.newName;
    }

    @Override
    public String getOldName() {
        return this.oldName;
    }
}

