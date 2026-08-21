/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server.thread;

import org.javacord.api.event.channel.thread.ThreadDeleteEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ServerThreadChannelAttachableListener;

@FunctionalInterface
public interface ServerThreadChannelDeleteListener
extends ServerThreadChannelAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onThreadDelete(ThreadDeleteEvent var1);
}

