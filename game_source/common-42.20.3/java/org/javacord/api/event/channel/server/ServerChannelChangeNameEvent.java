/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server;

import org.javacord.api.event.channel.server.ServerChannelEvent;

public interface ServerChannelChangeNameEvent
extends ServerChannelEvent {
    public String getNewName();

    public String getOldName();
}

