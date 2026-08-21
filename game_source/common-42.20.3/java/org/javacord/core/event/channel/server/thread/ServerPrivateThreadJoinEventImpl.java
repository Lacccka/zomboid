/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.thread;

import org.javacord.api.entity.channel.ThreadMember;
import org.javacord.api.event.channel.server.thread.ServerPrivateThreadJoinEvent;
import org.javacord.core.entity.channel.ServerThreadChannelImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelEventImpl;

public class ServerPrivateThreadJoinEventImpl
extends ServerThreadChannelEventImpl
implements ServerPrivateThreadJoinEvent {
    private final ThreadMember threadMember;

    public ServerPrivateThreadJoinEventImpl(ServerThreadChannelImpl channel, ThreadMember threadMember) {
        super(channel);
        this.threadMember = threadMember;
    }

    @Override
    public ThreadMember getThreadMember() {
        return this.threadMember;
    }
}

