/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server;

import org.javacord.api.event.server.ServerChangeNsfwLevelEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

public interface ServerChangeNsfwLevelListener
extends ServerAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerChangeNsfwLevel(ServerChangeNsfwLevelEvent var1);
}

