/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message;

import java.time.Instant;
import java.util.Optional;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.ChannelPinsUpdateEvent;
import org.javacord.core.event.EventImpl;

public class ChannelPinsUpdateEventImpl
extends EventImpl
implements ChannelPinsUpdateEvent {
    private final TextChannel channel;
    private final Instant lastPinTimestamp;

    public ChannelPinsUpdateEventImpl(TextChannel channel, Instant lastPinTimestamp) {
        super(channel.getApi());
        this.channel = channel;
        this.lastPinTimestamp = lastPinTimestamp;
    }

    @Override
    public TextChannel getChannel() {
        return this.channel;
    }

    @Override
    public Optional<Instant> getLastPinTimestamp() {
        return Optional.ofNullable(this.lastPinTimestamp);
    }
}

