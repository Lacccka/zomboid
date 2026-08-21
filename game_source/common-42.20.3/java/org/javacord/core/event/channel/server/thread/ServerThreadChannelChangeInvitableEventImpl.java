/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.thread;

import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeInvitableEvent;
import org.javacord.core.entity.channel.ServerThreadChannelImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelEventImpl;

public class ServerThreadChannelChangeInvitableEventImpl
extends ServerThreadChannelEventImpl
implements ServerThreadChannelChangeInvitableEvent {
    private final boolean newInvitable;
    private final boolean oldInvitable;

    public ServerThreadChannelChangeInvitableEventImpl(ServerThreadChannelImpl channel, boolean newInvitable, boolean oldInvitable) {
        super(channel);
        this.newInvitable = newInvitable;
        this.oldInvitable = oldInvitable;
    }

    @Override
    public boolean wasInvitable() {
        return this.oldInvitable;
    }

    @Override
    public boolean isInvitable() {
        return this.newInvitable;
    }
}

