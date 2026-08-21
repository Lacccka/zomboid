/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server;

import org.javacord.api.event.channel.server.ServerChannelDeleteEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface ServerChannelDeleteListener
extends ServerAttachableListener,
ServerChannelAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerChannelDelete(ServerChannelDeleteEvent var1);
}

