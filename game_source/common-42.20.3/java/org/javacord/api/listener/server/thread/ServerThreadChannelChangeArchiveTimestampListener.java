/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.thread;

import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeArchiveTimestampEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ServerThreadChannelAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface ServerThreadChannelChangeArchiveTimestampListener
extends ServerThreadChannelAttachableListener,
ServerAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerThreadChannelChangeArchiveTimestamp(ServerThreadChannelChangeArchiveTimestampEvent var1);
}

