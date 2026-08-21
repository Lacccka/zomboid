/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.connection;

import org.javacord.api.event.connection.ReconnectEvent;
import org.javacord.api.listener.GloballyAttachableListener;

@FunctionalInterface
public interface ReconnectListener
extends GloballyAttachableListener {
    public void onReconnect(ReconnectEvent var1);
}

