/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server.thread;

import org.javacord.api.event.channel.thread.ThreadMembersUpdateEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ServerThreadChannelAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface ServerThreadChannelMembersUpdateListener
extends ServerAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener,
ServerThreadChannelAttachableListener {
    public void onThreadMembersUpdate(ThreadMembersUpdateEvent var1);
}

