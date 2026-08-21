/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server.invite;

import org.javacord.api.event.channel.server.invite.ServerChannelInviteCreateEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface ServerChannelInviteCreateListener
extends ServerAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerChannelInviteCreate(ServerChannelInviteCreateEvent var1);
}

