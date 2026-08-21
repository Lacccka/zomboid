/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.invite;

import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.event.channel.server.invite.ServerChannelInviteDeleteEvent;
import org.javacord.core.event.channel.server.ServerChannelEventImpl;

public class ServerChannelInviteDeleteEventImpl
extends ServerChannelEventImpl
implements ServerChannelInviteDeleteEvent {
    private final String code;

    public ServerChannelInviteDeleteEventImpl(String code, ServerChannel channel) {
        super(channel);
        this.code = code;
    }

    @Override
    public String getCode() {
        return this.code;
    }
}

