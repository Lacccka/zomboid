/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.invite;

import org.javacord.api.event.channel.server.ServerChannelEvent;

public interface ServerChannelInviteDeleteEvent
extends ServerChannelEvent {
    public String getCode();
}

