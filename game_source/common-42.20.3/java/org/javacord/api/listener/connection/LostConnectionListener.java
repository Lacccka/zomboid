/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.connection;

import org.javacord.api.event.connection.LostConnectionEvent;
import org.javacord.api.listener.GloballyAttachableListener;

@FunctionalInterface
public interface LostConnectionListener
extends GloballyAttachableListener {
    public void onLostConnection(LostConnectionEvent var1);
}

