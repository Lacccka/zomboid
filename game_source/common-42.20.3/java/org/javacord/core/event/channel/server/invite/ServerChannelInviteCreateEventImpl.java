/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.invite;

import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.event.channel.server.invite.ServerChannelInviteCreateEvent;
import org.javacord.core.event.channel.server.ServerChannelEventImpl;

public class ServerChannelInviteCreateEventImpl
extends ServerChannelEventImpl
implements ServerChannelInviteCreateEvent {
    private final Invite invite;

    public ServerChannelInviteCreateEventImpl(Invite invite, ServerChannel channel) {
        super(channel);
        this.invite = invite;
    }

    @Override
    public Invite getInvite() {
        return this.invite;
    }
}

