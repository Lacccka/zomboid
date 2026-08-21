/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.message;

import java.time.Instant;
import java.util.Optional;
import org.javacord.api.event.channel.TextChannelEvent;

public interface ChannelPinsUpdateEvent
extends TextChannelEvent {
    public Optional<Instant> getLastPinTimestamp();
}

