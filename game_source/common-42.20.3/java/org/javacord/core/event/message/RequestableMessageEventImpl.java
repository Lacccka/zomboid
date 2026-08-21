/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.RequestableMessageEvent;
import org.javacord.core.event.message.OptionalMessageEventImpl;

public abstract class RequestableMessageEventImpl
extends OptionalMessageEventImpl
implements RequestableMessageEvent {
    public RequestableMessageEventImpl(DiscordApi api, long messageId, TextChannel channel) {
        super(api, messageId, channel);
    }

    @Override
    public CompletableFuture<Message> requestMessage() {
        return this.getMessage().map(CompletableFuture::completedFuture).orElseGet(() -> this.getChannel().getMessageById(this.getMessageId()));
    }
}

