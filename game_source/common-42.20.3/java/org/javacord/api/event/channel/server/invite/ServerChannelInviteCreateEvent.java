/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.invite;

import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.event.channel.server.ServerChannelEvent;

public interface ServerChannelInviteCreateEvent
extends ServerChannelEvent {
    public Invite getInvite();
}

