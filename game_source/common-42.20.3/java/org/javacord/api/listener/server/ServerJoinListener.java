/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server;

import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.api.listener.GloballyAttachableListener;

@FunctionalInterface
public interface ServerJoinListener
extends GloballyAttachableListener {
    public void onServerJoin(ServerJoinEvent var1);
}

