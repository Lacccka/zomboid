/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server;

import org.javacord.api.event.server.ServerBecomesUnavailableEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface ServerBecomesUnavailableListener
extends ServerAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerBecomesUnavailable(ServerBecomesUnavailableEvent var1);
}

