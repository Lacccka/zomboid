/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server;

import org.javacord.api.event.channel.server.ServerChannelChangePositionEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface ServerChannelChangePositionListener
extends ServerAttachableListener,
ServerChannelAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerChannelChangePosition(ServerChannelChangePositionEvent var1);
}

