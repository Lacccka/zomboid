/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.thread;

import org.javacord.api.entity.channel.ThreadMember;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelEvent;

public interface ServerPrivateThreadJoinEvent
extends ServerThreadChannelEvent {
    public ThreadMember getThreadMember();
}

