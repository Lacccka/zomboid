/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.thread;

import org.javacord.api.event.channel.server.thread.ServerThreadChannelEvent;

public interface ServerThreadChannelChangeAutoArchiveDurationEvent
extends ServerThreadChannelEvent {
    public int getNewAutoArchiveDuration();

    public int getOldAutoArchiveDuration();
}

