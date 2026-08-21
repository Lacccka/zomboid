/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.thread;

import org.javacord.api.event.channel.server.thread.ServerThreadChannelEvent;

public interface ServerThreadChannelChangeArchivedEvent
extends ServerThreadChannelEvent {
    public boolean isArchived();

    public boolean wasArchived();
}

