/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.thread;

import org.javacord.api.event.channel.server.thread.ServerThreadChannelEvent;

public interface ServerThreadChannelChangeTotalMessageSentEvent
extends ServerThreadChannelEvent {
    public int getNewTotalMessageSent();

    public int getOldTotalMessageSent();
}

