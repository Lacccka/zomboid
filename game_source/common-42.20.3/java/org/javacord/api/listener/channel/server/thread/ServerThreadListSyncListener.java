/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server.thread;

import org.javacord.api.event.channel.thread.ThreadListSyncEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface ServerThreadListSyncListener
extends ServerAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onThreadListSync(ThreadListSyncEvent var1);
}

