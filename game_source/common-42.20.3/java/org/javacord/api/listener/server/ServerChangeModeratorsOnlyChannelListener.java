/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server;

import org.javacord.api.event.server.ServerChangeModeratorsOnlyChannelEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface ServerChangeModeratorsOnlyChannelListener
extends ServerAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerChangeModeratorsOnlyChannel(ServerChangeModeratorsOnlyChannelEvent var1);
}

