/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server;

import org.javacord.api.event.server.ServerBecomesAvailableEvent;
import org.javacord.api.listener.GloballyAttachableListener;

@FunctionalInterface
public interface ServerBecomesAvailableListener
extends GloballyAttachableListener {
    public void onServerBecomesAvailable(ServerBecomesAvailableEvent var1);
}

