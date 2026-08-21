/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message;

import java.util.Optional;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.OptionalMessageEvent;
import org.javacord.core.event.message.MessageEventImpl;

public abstract class OptionalMessageEventImpl
extends MessageEventImpl
implements OptionalMessageEvent {
    private final Message message;

    public OptionalMessageEventImpl(DiscordApi api, long messageId, TextChannel channel) {
        super(api, messageId, channel);
        this.message = api.getCachedMessageById(messageId).orElse(null);
    }

    @Override
    public Optional<Message> getMessage() {
        return Optional.ofNullable(this.message);
    }
}

