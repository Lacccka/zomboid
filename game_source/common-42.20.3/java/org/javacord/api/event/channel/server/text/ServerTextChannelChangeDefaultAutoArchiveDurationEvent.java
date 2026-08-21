/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.text;

import org.javacord.api.event.channel.server.text.ServerTextChannelEvent;

public interface ServerTextChannelChangeDefaultAutoArchiveDurationEvent
extends ServerTextChannelEvent {
    public int getOldDefaultAutoArchiveDuration();

    public int getAutoArchiveDuration();
}

